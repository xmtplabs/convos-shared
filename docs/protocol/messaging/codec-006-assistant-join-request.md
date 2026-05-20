# Assistant Join Request (`convos.org/assistant_join_request:1.0`)

> **Codec #6** — first added 2026‑03‑11.

## Purpose

Signals the lifecycle of an assistant (agent) being invited into a
conversation. The user requests an assistant; the request is published into the
group as this content type with `status = pending`; the resolver later
transitions it to `noAgentsAvailable` or `failed` (success is implicit when
the agent actually joins the MLS group). Clients render an inline status row
in the transcript driven by these messages.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `assistant_join_request` |
| Version | `1.0` |
| Encoding | JSON (default `JSONEncoder` / `JSONDecoder`) |
| Fallback text | `"Assistant join requested"` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Custom Content Types/AssistantJoinRequestCodec.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `status` | string enum | yes | One of `pending`, `no_agents_available`, `failed`. Backed by `AssistantJoinStatus` in `ConvosCore/Sources/ConvosCore/Storage/Models/AssistantJoinStatus.swift`. |
| `requestedByInboxId` | string | yes | XMTP inbox ID of the member who initiated the request. |
| `requestId` | string | yes | Opaque correlation id chosen by the requester so later status transitions can be linked to the original request. |

### `status` enum values

| Wire value | Swift case | Meaning |
|---|---|---|
| `pending` | `.pending` | The request was published; the resolver hasn't placed an agent yet. |
| `no_agents_available` | `.noAgentsAvailable` | The resolver has nothing to offer (e.g. capacity, region). Terminal. |
| `failed` | `.failed` | The resolver attempted placement and failed. Terminal. |

## Example encoding

```json
{
  "status": "pending",
  "requestedByInboxId": "0xa1b2c3...",
  "requestId": "ajr_8f4c2e7d"
}
```

## Producers and consumers

- **Sent by** — assistant placement flow (the requesting client publishes
  `pending`; the resolver publishes the terminal status).
- **Received by** — dispatched in
  `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
  (`case ContentTypeAssistantJoinRequest:`) and surfaced as a status row in
  the conversation transcript.

## Notes / gotchas

- `AssistantJoinStatus.displayDuration` defines how long the row should remain
  visible: 35 s for `pending`, 3 s for the terminal states. Implementers on
  other platforms should mirror these durations to keep UX consistent.
- Successful placement is signalled by the agent actually joining the group
  (`GroupUpdated`); there is intentionally no `succeeded` enum value here.
- Background docs: `docs/plans/assistant-status.md` in the iOS repo.
