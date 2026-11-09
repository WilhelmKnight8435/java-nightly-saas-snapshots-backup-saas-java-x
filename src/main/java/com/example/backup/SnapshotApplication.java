package com.example.backup;

import java.time.LocalDate;

public final class SnapshotApplication {
  public static void main(String[] args) throws Exception {
    String bucket = System.getenv().getOrDefault("SNAPSHOT_BUCKET", "snapshot-demo-" + java.util.UUID.randomUUID());
    InfraiClient client = new InfraiClient();
    client.createBucket(bucket);
    LocalDate date = LocalDate.now();
    try {
      TenantSnapshotService service = new TenantSnapshotService(client, bucket);
      service.onboard("tenant-acme", "acct-1042", "admin@acme.test");
      service.onboard("tenant-orbit", "acct-2088", "ops@orbit.test");
      System.out.println("snapshot object: " + service.nightlySnapshot(date));
    } finally {
      // Also attempt deletion when an upload response is lost after the write.
      try {
        client.deleteObject(bucket, "snapshots/" + date + ".json");
      } finally {
        client.deleteBucket(bucket);
      }
    }
  }
}
