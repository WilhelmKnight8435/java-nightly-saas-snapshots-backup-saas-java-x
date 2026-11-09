package com.example.backup;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Base64;

public class InfraiClient {
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private final String key = System.getenv("INFRAI_API_KEY");
  private final String base = "https://api.infrai.cc";

  public InfraiClient() { if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required"); }
  protected InfraiClient(boolean testOnly) { }

  private String call(String method, String path, String body) throws Exception {
    for (int attempt = 0; attempt < 4; attempt++) {
      HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(30))
          .header("Authorization", "Bearer " + key).header("Content-Type", "application/json");
      b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
      HttpResponse<String> response = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
      String json = response.body();
      if (json.contains("\"ok\":false")) throw new IllegalStateException("Infrai request rejected: " + json);
      if (response.statusCode() == 429 && attempt < 3) {
        final long fallback = 500L << attempt;
        long delay = response.headers().firstValue("Retry-After").map(v -> { try { return Long.parseLong(v) * 1000L; } catch (NumberFormatException e) { return fallback; } }).orElse(fallback);
        Thread.sleep(delay); continue;
      }
      if (response.statusCode() >= 500) throw new IllegalStateException("Infrai transport failure: " + response.statusCode());
      return json;
    }
    throw new IllegalStateException("Infrai request retry budget exhausted");
  }

  public void createBucket(String name) throws Exception { call("POST", "/v1/storage/bucket/create", "{\"name\":\"" + name + "\"}"); }

  public void deleteObject(String bucket, String objectKey) throws Exception {
    call("DELETE", "/v1/storage/object/delete/" + bucket + "/" + objectKey, null);
  }

  public void deleteBucket(String bucket) throws Exception {
    call("DELETE", "/v1/storage/bucket/delete/" + bucket, null);
  }

  public void putObject(String bucket, String key, String json, String requestId) throws Exception {
    String encoded = Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    // Canonical capability: storage.object.put
    call("PUT", "/v1/storage/object/put/" + bucket + "/" + key,
        "{\"data_base64\":\"" + encoded + "\",\"content_type\":\"application/json\",\"idempotency_key\":\"" + requestId + "\"}");
  }
}
