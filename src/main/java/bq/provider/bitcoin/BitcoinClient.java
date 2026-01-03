package bq.provider.bitcoin;

import bx.util.Json;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public abstract class BitcoinClient {

  protected abstract JsonNode invokeRaw(JsonNode n);

  public JsonNode invoke(String method, ArrayNode params) {
    ObjectNode n = createRequest(method, params);

    return extractResult(invokeRaw(n));
  }

  public JsonNode invoke(String method) {

    return invoke(method, Json.createArrayNode());
  }

  public JsonNode invoke(String method, int arg) {
    ArrayNode params = Json.createArrayNode();
    params.add(arg);

    return invoke(method, params);
  }

  public JsonNode invoke(String method, String arg) {
    ArrayNode params = Json.createArrayNode();
    params.add(arg);

    return invoke(method, params);
  }

  public JsonNode invoke(String method, String arg1, int arg2) {
    ArrayNode params = Json.createArrayNode();
    params.add(arg1);
    params.add(arg2);

    return invoke(method, params);
  }

  public JsonNode getBlockChainInfo() {
    return invoke("getblockchaininfo");
  }

  public String getBlockHash(int block) {
    return invoke("getblockhash", block).asString(null);
  }

  public JsonNode getRawTransaction(String hash) {

    ArrayNode an = Json.createArrayNode();
    an.add(hash);
    an.add(true);
    return invoke("getrawtransaction", an);
  }

  public JsonNode getBlock(String hash, int verbosity) {
    return invoke("getblock", hash, verbosity);
  }

  protected JsonNode extractResult(JsonNode response) {

    JsonNode errorNode = response.path("error");
    if (errorNode.isObject()) {

      int code = errorNode.path("code").asInt(0);
      String message = errorNode.path("message").asString("");

      throw new BitcoinClientException(200, message, code);
    }

    return response.path("result");
  }

  private ObjectNode createRequest(String n, ArrayNode params) {
    ObjectNode request = Json.createObjectNode();
    request.put("jsonrpc", "2.0");
    request.put("method", n);

    if (params == null) {
      params = Json.createArrayNode();
    }

    request.set("params", params);
    request.put("id", UUID.randomUUID().toString());
    return request;
  }
}
