package com.example.backup;

import java.time.*;
import java.util.*;

public final class TenantSnapshotService {
  private final InfraiClient storage;
  private final String bucket;
  private final Map<String, Tenant> tenants = new LinkedHashMap<>();

  public TenantSnapshotService(InfraiClient storage, String bucket) { this.storage = storage; this.bucket = bucket; }
  public void onboard(String tenantId, String accountId, String admin) {
    tenants.put(tenantId, new Tenant(tenantId, accountId, admin, "active"));
  }
  public String nightlySnapshot(LocalDate date) throws Exception {
    StringBuilder out = new StringBuilder("{\"snapshot_date\":\"").append(date).append("\",\"tenants\":[");
    boolean first = true;
    for (Tenant t : tenants.values()) { if (!first) out.append(','); first = false; out.append(t.json()); }
    out.append("]}");
    String objectKey = "snapshots/" + date + ".json";
    storage.putObject(bucket, objectKey, out.toString(), "snapshot-" + date);
    return objectKey;
  }
  public int tenantCount() { return tenants.size(); }
  private record Tenant(String id, String account, String admin, String state) {
    String json() { return "{\"tenant_id\":\""+id+"\",\"account_id\":\""+account+"\",\"admin\":\""+admin+"\",\"state\":\""+state+"\"}"; }
  }
}
