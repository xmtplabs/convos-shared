# Connection Payload (`convos.org/connection_payload:1.0`)

> **Codec #12** — first added 2026‑05‑09 (Connections / Capabilities commit).

## Purpose

Device ➜ agent telemetry envelope carrying captured data from a native iOS
`DataSource` (Health, Calendar, Contacts, Location, Photos, Music, Motion,
HomeKit, ScreenTime). Used both for one-shot pushes and for streamed deltas
from background observers.

The envelope is intentionally `Codable` (JSON) at this layer. The body
payload (`HealthPayload`, `CalendarPayload`, etc.) has its own
`schemaVersion` so each data source can iterate independently of the
envelope's own version.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `connection_payload` |
| Version | `1.0` |
| Encoding | JSON |
| Fallback text | `ConnectionPayload.summary` — a short human-readable description derived from the body (e.g. *"3 calendar events"*). For `unknown` bodies: `"Unknown payload (<rawType>)"`. |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosConnectionsXMTP/Codecs/ConnectionPayloadCodec.swift` |
| Payload struct | `ConvosConnections/Sources/ConvosConnections/Payloads/ConnectionPayload.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | UUID (string) | yes | Envelope-unique id. |
| `schemaVersion` | int | yes | Currently `1` (`ConnectionPayload.currentSchemaVersion`). Not gated on decode — forward compatibility comes via the `unknown` body case. |
| `source` | string enum (`ConnectionKind`) | yes | Origin data source. See [ConnectionKind values](#connectionkind-values). |
| `capturedAt` | date (Swift `Codable` default — typically a numeric epoch) | yes | When the data was captured on-device. |
| `body` | tagged object | yes | Source-specific body. See below. |

### `body` shape

```json
{ "type": "<bodyType>", "data": { ... source-specific fields ... } }
```

Wire `type` discriminator values (mapping to the `BodyType` enum in
`ConnectionPayload.swift`):

| Wire `type` | Swift case | Body struct |
|---|---|---|
| `health` | `.health` | `HealthPayload` |
| `calendar` | `.calendar` | `CalendarPayload` |
| `location` | `.location` | `LocationPayload` |
| `contacts` | `.contacts` | `ContactsPayload` |
| `photos` | `.photos` | `PhotosPayload` |
| `music` | `.music` | `MusicPayload` |
| `motion` | `.motion` | `MotionPayload` |
| `home_kit` | `.homeKit` | `HomePayload` |
| `screen_time` | `.screenTime` | `ScreenTimePayload` |
| *any other string* | `.unknown(rawType:, data:)` | Round-tripped through `JSONValue` so older receivers can forward unfamiliar payloads without dropping data. |

Each body type carries its own `schemaVersion`; the per-source structs are
out of scope for this codec doc — see `ConvosConnections/Sources/ConvosConnections/Payloads/*Payload.swift`.

### `ConnectionKind` values

`health`, `calendar`, `contacts`, `location`, `photos`, `music`, `home_kit`,
`screen_time`, `motion`. The raw strings are persisted in `EnablementStore`
and used as the `body.type` discriminator above — **changing a raw value is a
breaking change**.

## Example encoding

```json
{
  "id": "8E0F4D2C-3F4D-4E0C-9F3A-2A1A1B1C1D1E",
  "schemaVersion": 1,
  "source": "calendar",
  "capturedAt": 769420800,
  "body": {
    "type": "calendar",
    "data": {
      "schemaVersion": 1,
      "events": [
        { "id": "evt-1", "title": "Kickoff", "startsAt": "2026-06-01T17:00:00Z" }
      ]
    }
  }
}
```

## Producers and consumers

- **Sent by**
  - Per-source `DataSource` modules under
    `ConvosConnections/Sources/ConvosConnections/DataSources/<Source>/`.
  - Health background observers stream deltas via
    `HealthBackgroundDeliveryGateway` ➜
    `ConvosConnectionsXMTP` ➜ this codec.
- **Received by**
  - Codec decodes the envelope. Agents inspect the body type and read whichever
    `*Payload` struct they care about.
- **Stored as** — not persisted to the user-facing message DB; payloads are
  consumed by agents.

## Notes / gotchas

- `ConnectionPayloadBody.unknown` preserves forward compatibility: an older
  client decoding a newer body type keeps the raw `type` string and the JSON
  blob, so they round-trip on re-send. Implementations should preserve this
  behaviour.
- `capturedAt` uses Swift's default `Codable` `Date` encoding (an epoch-seconds
  number) rather than ISO‑8601 — this is consistent across all
  `ConnectionPayload` traffic; only `ExplodeSettings` and `InviteJoinError`
  switch to ISO‑8601.
- The fallback text (`ConnectionPayload.summary`) is the only user-visible
  surface; the payload itself is agent-facing.
