# Capability Request (`convos.org/capability_request:1.0`)

> **Codec #9** — first added 2026‑05‑09 (one of the six codecs introduced in the same Connections / Capabilities commit; paired with [Capability Request Result](./codec-010-capability-request-result.md)).

## Purpose

Agent ➜ device message: "I would like to access your *subject* with the
following *capability*". The device routes the request into the capability
resolver, which surfaces a picker / confirmation card; once the user approves
or denies, the device replies with a
[`CapabilityRequestResult`](./codec-010-capability-request-result.md) that carries the
same `requestId`.

Subjects are user-facing nouns (`calendar`, `contacts`, `mail`, …) and are
deliberately decoupled from `ConnectionKind` so subjects without a device
counterpart (`tasks`, `mail`) can still be requested.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `capability_request` |
| Version | `1.0` |
| Encoding | JSON (versioned in-band) |
| Fallback text | `"The assistant is requesting access to your <subject lowercased>"` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Custom Content Types/CapabilityRequestCodec.swift` |
| Payload struct | `ConvosCore/Sources/ConvosCore/CapabilityResolution/CapabilityRequest.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `version` | int | yes | In-band schema version. Decoder rejects when `version > 1` (`CapabilityRequest.supportedVersion`). |
| `requestId` | string | yes | Opaque correlation id. Replies echo it via `CapabilityRequestResult.requestId`. |
| `askerInboxId` | string | yes | Inbox ID of the agent making the request. Bound on the wire so the persisted grant, the `connection_event` credit, and the resolver lookup all key off the same value. |
| `subject` | string enum | yes | One of `calendar`, `contacts`, `tasks`, `mail`, `photos`, `fitness`, `music`, `location`, `home`, `screen_time`. See [`CapabilitySubject`](#capabilitysubject-values). |
| `capability` | string enum | yes | One of `read`, `write_create`, `write_update`, `write_delete`. See [`ConnectionCapability`](#connectioncapability-values). |
| `rationale` | string | yes | Free-text reason shown on the picker card. **Truncated to 500 chars on decode** (`maxRationaleLength`) so a hostile sender can't bloat the UI. |
| `preferredProviders` | array of string | optional | Provider hints (e.g. `["composio.googlecalendar"]`). **Truncated to first 16 items on decode** (`maxPreferredProviders`). Each entry is a `ProviderID` (dotted `<source>.<service>` string). |

### `CapabilitySubject` values

`calendar`, `contacts`, `tasks`, `mail`, `photos`, `fitness`, `music`,
`location`, `home`, `screen_time`. Of these, only `fitness` permits
read-federation across multiple providers; the rest resolve to a single
provider per grant.

### `ConnectionCapability` values

| Wire value | Swift case | `isWrite` |
|---|---|---|
| `read` | `.read` | `false` |
| `write_create` | `.writeCreate` | `true` |
| `write_update` | `.writeUpdate` | `true` |
| `write_delete` | `.writeDelete` | `true` |

The raw values are persisted in the `EnablementStore` — changing them is a
breaking change.

## Example encoding

```json
{
  "version": 1,
  "requestId": "cap_req_7c2e9a",
  "askerInboxId": "0xagent...",
  "subject": "calendar",
  "capability": "write_create",
  "rationale": "I'll add the confirmed meeting time to your work calendar.",
  "preferredProviders": ["composio.googlecalendar"]
}
```

## Producers and consumers

- **Sent by** — agent-side code (agent SDK / runtime). The iOS app rarely
  produces this; it's primarily a receiver.
- **Received by** — dispatched in
  `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
  (`case ContentTypeCapabilityRequest:`). The capability resolver
  (`ConvosCore/Sources/ConvosCore/CapabilityResolution/`) materializes a
  picker, and on user action emits a `CapabilityRequestResult`.

## Notes / gotchas

- Always pair with a `CapabilityRequestResult` keyed by the same `requestId`,
  even on cancel/deny — otherwise the agent will wait forever.
- `rationale` and `preferredProviders` are truncated **silently** on decode.
  Senders should keep them within bounds rather than rely on truncation.
- A future-version sender (`version: 2`) will be rejected at decode time;
  field-level forward compatibility must come via additive fields under
  `version: 1`.
