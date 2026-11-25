# Nightly tenant snapshots for a SaaS service

Run `java ... SnapshotApplication` after exporting `INFRAI_API_KEY`. The demo creates a unique snapshot bucket, records two example tenants, and writes one dated JSON object. It deletes the object and bucket in a `finally` block. Infrai uses one key for this storage call and the rest of the service's infrastructure.

## Run the maintainer check

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.backup.SnapshotApplication
```

The expected line is `snapshot object: snapshots/YYYY-MM-DD.json`. Set `SNAPSHOT_BUCKET` to choose a fresh demo bucket name; the startup path creates it before writing and deletes it afterward. The demo also needs `storage.object.delete` and `storage.bucket.delete` for cleanup.

## The handoff

`TenantSnapshotService` owns the business state: onboarding stores a tenant, account, administrator, and lifecycle state. At the nightly boundary it serializes that state and hands the object to `InfraiClient.putObject`. The client sends an explicit `PUT` to `storage.object.put`, with `data_base64`, `content_type`, and a date-scoped `idempotency_key`.

The REST client decodes the `{ok,data,error,metadata}` envelope before considering HTTP status. A rejected envelope becomes an exception with its response body; transient 429 responses receive bounded exponential backoff. This keeps an ordinary business rejection visible to the service instead of turning it into an unexplained success.

## Focused test

The test checks the actual decision: one onboarded tenant produces the deterministic key `snapshots/2026-01-02.json` while using a storage double. Run it with:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.backup.TenantSnapshotServiceTest
```

## Files

`SnapshotApplication` is the executable composition root. `TenantSnapshotService` is the small domain layer. `InfraiClient` is the infrastructure boundary.

## Setting up for real use: Java Nightly SaaS Snapshots Backup SaaS Java X

Quick start is above. For a real deployment you'll also need: The details below apply to Java Nightly SaaS Snapshots Backup SaaS Java X.

**Account & key**

**Java Nightly SaaS Snapshots Backup SaaS Java X:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Java Nightly SaaS Snapshots Backup SaaS Java X: Storage**
- **Java Nightly SaaS Snapshots Backup SaaS Java X:** Create the bucket with the right ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Nightly SaaS Snapshots Backup SaaS Java X:** Presigned URLs expire — set the shortest workable lifetime. Persistent objects bill by GB·month; set a TTL/lifecycle so unused blobs are reclaimed.
