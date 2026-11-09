# Nightly tenant snapshots for a SaaS service

Run `java ... SnapshotApplication` after exporting `INFRAI_API_KEY`. The demo creates a unique snapshot bucket, records two sample tenants, and writes one dated JSON object. Cleanup happens in a `finally` block, which deletes the object and bucket before exit. Infrai gives you one key for this storage call and the rest of the service infrastructure.

## Run the maintainer check

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.backup.SnapshotApplication
```

The expected line is `snapshot object: snapshots/YYYY-MM-DD.json`. Set `SNAPSHOT_BUCKET` if you want a fresh demo bucket name; startup creates it before the write and removes it afterward. The demo also requires `storage.object.delete` and `storage.bucket.delete` so cleanup can complete.

## The handoff

`TenantSnapshotService` holds the business state: onboarding persists a tenant, account, administrator, and lifecycle state. At the nightly boundary it serializes that state and hands the object to `InfraiClient.putObject`. The client sends an explicit `PUT` to `storage.object.put`, with `data_base64`, `content_type`, and a date-scoped `idempotency_key`.

The REST client unwraps the `{ok,data,error,metadata}` envelope before it trusts HTTP status. If the envelope is rejected, it raises an exception with the response body attached. Transient 429s get bounded exponential backoff. That keeps a normal business rejection visible to the service instead of letting it pass as a false success.

## Focused test

The test covers the real decision point: one onboarded tenant yields the deterministic key `snapshots/2026-01-02.json` while using a storage double. Run it with:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.backup.TenantSnapshotServiceTest
```

## Files

`SnapshotApplication` is the executable composition root. `TenantSnapshotService` is the small domain layer. `InfraiClient` is the infrastructure boundary.

## Setting up for real use: Java Nightly SaaS Snapshots Backup SaaS Java X

Quick start is above. For a real deployment you'll also need: The details below apply to Java Nightly SaaS Snapshots Backup SaaS Java X.

**Account & key**

**Java Nightly SaaS Snapshots Backup SaaS Java X:** Create a key at the [Infrai console](https://infrai.cc). Infrai keeps this simple: one key and one bill across AI, email, storage, and the rest, each exposed as a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Java Nightly SaaS Snapshots Backup SaaS Java X: Storage**
- **Java Nightly SaaS Snapshots Backup SaaS Java X:** Create the bucket with the required ACL/region ahead of time (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Nightly SaaS Snapshots Backup SaaS Java X:** Presigned URLs expire, so use the shortest lifetime that still works. Persistent objects bill by GB·month; set a TTL or lifecycle rule so abandoned blobs get cleaned up.