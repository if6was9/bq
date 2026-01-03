package bq.provider.bitcoin;

import bx.util.Json;
import bx.util.Slogger;
import com.google.common.base.Stopwatch;
import com.google.common.base.Suppliers;
import java.net.URI;
import java.util.function.Supplier;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.RequestBodyEntity;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4FamilyHttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4aHttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.RegionSet;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import tools.jackson.databind.JsonNode;

public class AmazonBitcoinClient extends BitcoinClient {

  static Logger logger = Slogger.forEnclosingClass();

  public static final String DEFAULT_ENDPOINT =
      "https://mainnet.bitcoin.managedblockchain.us-east-1.amazonaws.com";
  public static final String DEFAULT_REGION = "us-east-1";
  public static final String SERVICE_SIGNING_NAME = "managedblockchain";

  static Supplier<AwsCredentialsProvider> credentialsSupplier =
      Suppliers.memoize(
          () -> {
            return DefaultCredentialsProvider.builder().build();
          });

  private AmazonBitcoinClient() {}

  public static BitcoinClient create() {
    return new AmazonBitcoinClient();
  }

  RequestBodyEntity injectHeaders(RequestBodyEntity rbe) {
    AwsV4aHttpSigner signer = AwsV4aHttpSigner.create();

    // mainnet.bitcoin.managedblockchain.us-east-1.amazonaws.com
    // 2. Build the request representation

    String contentType = rbe.getHeaders().getFirst("Content-type");

    SdkHttpRequest request =
        SdkHttpRequest.builder()
            .uri(rbe.getUrl())
            .method(SdkHttpMethod.POST)
            .putHeader("Content-Type", contentType)
            .build();

    byte[] val = (byte[]) rbe.getBody().get().uniPart().getValue();

    AwsCredentialsIdentity id = credentialsSupplier.get().resolveIdentity().join();

    // 3. Execute the signing process
    var signedResult =
        signer
            .sign(
                r ->
                    r.identity(id)
                        .request(request)
                        .payload(ContentStreamProvider.fromByteArray(val))
                        .putProperty(AwsV4aHttpSigner.REGION_SET, RegionSet.create("us-east-1"))
                        .putProperty(
                            AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME, SERVICE_SIGNING_NAME)
                        .putProperty(AwsV4HttpSigner.REGION_NAME, DEFAULT_REGION))
            .request();

    signedResult
        .headers()
        .forEach(
            (k, v) -> {
              if (!k.equalsIgnoreCase("host")) {
                v.forEach(
                    headerValue -> {
                      rbe.headerReplace(k, headerValue);
                    });
              }
            });
    return rbe;
  }

  public tools.jackson.databind.JsonNode invokeRaw(JsonNode request) {

    URI uri = URI.create(DEFAULT_ENDPOINT);

    byte[] body = request.toString().getBytes();

    RequestBodyEntity rbe = Unirest.post(uri.toString()).contentType("application/json").body(body);

    RequestBodyEntity rbe2 = injectHeaders(rbe);

    Stopwatch sw = Stopwatch.createStarted();

    String method = request.path("method").asString(null);
    HttpResponse<byte[]> response = rbe2.asBytes();

    logger.atInfo().log(
        "method={} rc={} time={}ms len={}",
        method,
        response.getStatus(),
        sw.elapsed().toMillis(),
        response.getBody().length);

    var j = Json.readTree(response.getBody());

    if (!response.isSuccess()) {

      String message =
          String.format(
              "code=%s message=%s id=%s",
              j.path("error").path("code").asInt(-1),
              j.path("error").path("message").asString(null),
              j.path("id").asString(null));
      throw new BitcoinClientException(response.getStatus(), message);
    }
    return j;
  }
}
