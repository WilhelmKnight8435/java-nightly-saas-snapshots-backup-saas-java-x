package com.example.backup;

import java.time.LocalDate;

public final class TenantSnapshotServiceTest {
  public static void main(String[] args) throws Exception {
    TenantSnapshotService service = new TenantSnapshotService(new InfraiClient(true) { public void putObject(String b,String k,String j,String r) {} }, "test-bucket");
    service.onboard("t-1", "a-1", "admin@test");
    if (service.tenantCount() != 1) throw new AssertionError("onboarding must add one active tenant");
    String key = service.nightlySnapshot(LocalDate.of(2026, 1, 2));
    if (!key.equals("snapshots/2026-01-02.json")) throw new AssertionError(key);
    System.out.println("business decision test passed");
  }
}
