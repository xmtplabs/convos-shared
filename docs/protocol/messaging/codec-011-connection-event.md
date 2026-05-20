# Connection Event (`convos.org/connection_event:1.0`)

> **Codec #11** — first added 2026‑05‑09 (Connections / Capabilities commit).

## Purpose

Side-effect notification announcing a change in a member's connection-grant
state — typically that a capability/provider has just been granted or revoked.
Other members and agents observe these events to keep their local view of
"who can do what" in sync without needing to poll an out-of-band API.

Examples:
- After the user completes OAuth from a
  [`connection_grant_request`](./codec-008-cloud-connection-grant-request.md): publish a
  `connection_event` with `action = granted`.
- When the user revokes a specific agent's access from settings: publish a
  `connection_event` with `action = revoked` and the agent's inbox ID in
  `grantedToInboxId`.
- When the user disconnects the underlying OAuth entirely (multi-agent
  revoke): publish `action = revoked` and omit `grantedToInboxId`.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `connection_event` |
| Version | `1.0` |
| Encoding | JSON (versioned in-band) |
| Fallback text | `nil` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Connections/ConnectionEventCodec.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `version` | int | yes | In-band schema version. `ConnectionEvent.supportedVersion = 1`. |
| `providerId` | string | yes | `ProviderID` (dotted form, e.g. `composio.googlecalendar` or `device.calendar`). |
| `action` | string enum | yes | `granted` or `revoked`. |
| `capability` | string enum | optional | The verb the event applies to: `read`, `write_create`, `write_update`, `write_delete`. Optional so older writers that didn't tag events with a capability still decode cleanly; the formatter falls back to a generic phrase when omitted. |
| `grantedToInboxId` | string | optional | Inbox ID of the agent the event concerns. For `granted`, the agent gaining access; for `revoked`, the agent losing access. Optional on the wire so app-level / multi-agent revoke events (where no single agent is meaningful — e.g. the user disconnected the underlying OAuth) can omit it. |

### `action` values

| Wire value | Swift case |
|---|---|
| `granted` | `.granted` |
| `revoked` | `.revoked` |

## Example encoding

Granted to a specific agent:

```json
{
  "version": 1,
  "providerId": "composio.googlecalendar",
  "action": "granted",
  "capability": "write_create",
  "grantedToInboxId": "0xagent..."
}
```

Multi-agent revoke (user disconnected OAuth entirely):

```json
{
  "version": 1,
  "providerId": "composio.googlecalendar",
  "action": "revoked"
}
```

## Producers and consumers

- **Sent by** — the connections subsystem after a state transition (OAuth
  completion, settings-toggle revoke, etc.).
- **Received by** — dispatched in
  `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
  (`case ContentTypeConnectionEvent:`). Agents and the on-device resolver
  read it to invalidate cached capability state.

## Notes / gotchas

- Treat the absence of `capability` as "applies to all capabilities for this
  provider" — that's the legacy semantics older writers carry.
- Treat the absence of `grantedToInboxId` similarly as "applies to all agents
  with a grant on this provider".
- `version: 2` payloads will be rejected by `JSONDecoder` validation — the
  codec is intolerant to forward-version drift.
