package bq.provider;

import bx.util.Json;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public abstract class BitcoinClient {

  public abstract JsonNode invokeRaw(JsonNode n);

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
    return invoke("getblockhash", block).asString("");
  }

  public JsonNode getBlock(String hash, int verbosity) {
    return invoke("getblock", hash, verbosity);
  }

  private JsonNode extractResult(JsonNode response) {
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

    return request;
  }
}
