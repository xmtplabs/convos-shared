# Cloud Connection Grant Request (`convos.org/connection_grant_request:1.0`)

> **Codec #8** — first added 2026‑04‑27.

## Purpose

Agent ➜ user ask to connect an external cloud service (e.g. Google Calendar,
GitHub) via Composio. The card-style UI rendered from this message walks the
user through the OAuth handoff; the resulting grant becomes a
`ProviderID` like `composio.googlecalendar` that subsequent
[`CapabilityRequest`](./codec-009-capability-request.md) messages can target.

Distinct from `CapabilityRequest`: this one solicits the *connection*
(OAuth + service binding); a `CapabilityRequest` solicits the *capability*
(read/write verb against an already-connected subject).

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `connection_grant_request` |
| Version | `1.0` |
| Encoding | JSON (versioned in-band) |
| Fallback text | `"The assistant asked to connect <service>"` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Custom Content Types/CloudConnectionGrantRequestCodec.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `version` | int | yes | In-band schema version. Decoder rejects when `version > 1` (`CloudConnectionGrantRequest.supportedVersion`). |
| `service` | string | yes | Composio toolkit slug or display token for the service being requested (e.g. `googlecalendar`). |
| `requestedByInboxId` | string | yes | Inbox ID of the agent making the request. |
| `targetInboxId` | string | yes | Inbox ID of the user being asked to grant. Must match the receiving inbox; mismatches are filtered out. |
| `reason` | string | yes | Free-text rationale shown on the connection card. **Truncated to 500 chars on decode** (`maxReasonLength`). |

## Example encoding

```json
{
  "version": 1,
  "service": "googlecalendar",
  "requestedByInboxId": "0xagent...",
  "targetInboxId": "0xuser...",
  "reason": "I need calendar access to schedule the kickoff."
}
```

## Producers and consumers

- **Sent by** — agent-side code.
- **Received by** — dispatched in
  `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
  (`case ContentTypeCloudConnectionGrantRequest:`). The Cloud Connections
  manager (`ConvosCore/Sources/ConvosCore/CloudConnections/CloudConnectionManager.swift`)
  drives the UI.

## Notes / gotchas

- **Sender verification**:
  `ConvosCore/Sources/ConvosCore/Storage/Writers/IncomingMessageWriter.swift`
  *silently drops* `connection_grant_request` messages from senders that are
  not recognized assistants. The codec itself does no sender check — that's
  by design (codecs handle bytes, not policy). Port implementations must
  layer an equivalent guard at the write site.
- `reason` is truncated **silently** on decode; senders should pre-clip rather
  than rely on this.
- Successful OAuth completion is signalled separately via a
  [`connection_event`](./codec-011-connection-event.md) with `action = "granted"`.
