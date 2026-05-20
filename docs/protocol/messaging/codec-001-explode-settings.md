# Explode Settings (`convos.org/explode_settings:1.0`)

> **Codec #1** — first added 2025‑08‑20 (oldest Convos-specific codec in iOS).

## Purpose

Sets the conversation-wide expiration ("disappearing message") policy. A single
field carries the absolute timestamp at which the conversation and its
messages should auto-delete on every member's device. The message is sent as a
regular MLS group message so it is durable, ordered with the rest of the
transcript, and reaches every member exactly once.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `explode_settings` |
| Version | `1.0` |
| Encoding | JSON (`JSONEncoder` with `dateEncodingStrategy = .iso8601`) |
| Fallback text | `"Conversation expires at <expiresAt>"` |
| `shouldPush` | `true` — the message is delivered to every device so offline ones still receive the policy; the iOS NSE silently drops the notification so no banner is shown |
| Defined in | `ConvosCore/Sources/ConvosCore/Custom Content Types/ExplodeSettingsCodec.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `expiresAt` | string (ISO‑8601 date-time) | yes | Absolute UTC instant when the conversation should be considered exploded. Encoded with the default Swift `Codable` ISO‑8601 representation. |

## Example encoding

```json
{
  "expiresAt": "2026-06-01T17:30:00Z"
}
```

## Producers and consumers

- **Sent by**
  - `ConvosCore/Sources/ConvosCore/Storage/Workers/ScheduledExplosionManager.swift`
  - `ConvosCore/Sources/ConvosCore/Storage/Writers/ConversationExplosionWriter.swift`
- **Received by**
  - Dispatched in
    `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
    (`case ContentTypeExplodeSettings:`), stored as a message of type
    `update` in the local DB. Downstream, the scheduler arms a timer for
    `expiresAt`.

## Notes / gotchas

- `shouldPush = true` is deliberate even though no notification is shown to the
  user. The intent is delivery, not a banner: APNs is the cheapest way to wake
  every device to the new policy so the scheduled deletion fires consistently.
- The codec does no clock skew tolerance — `expiresAt` is taken at face value.
- An empty / unparseable payload raises `ExplodeSettingsCodecError.emptyContent`
  or `.invalidJSONFormat`; the message will then dispatch as
  `DecodedMessageDBRepresentationError.unsupportedContentType`.
