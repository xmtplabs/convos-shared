# Connection Invocation Result (`convos.org/connection_invocation_result:1.0`)

> **Codec #14** — first added 2026‑05‑09 (paired reply for [Connection Invocation](./codec-013-connection-invocation.md); newest Convos-specific codec in iOS).

## Purpose

Device ➜ agent reply to a
[`connection_invocation`](./codec-013-connection-invocation.md). Always emitted —
including on permission-revoked, denied, or executor-failure paths — so the
agent can correlate by `invocationId` and stop waiting. The `status` field
discriminates success from each kind of failure; success replies additionally
carry the schema's declared `outputs`.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `connection_invocation_result` |
| Version | `1.0` |
| Encoding | JSON |
| Fallback text | `"<actionName>: <status>"` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosConnectionsXMTP/Codecs/ConnectionInvocationResultCodec.swift` |
| Payload struct | `ConvosConnections/Sources/ConvosConnections/Core/ConnectionInvocation.swift` (struct `ConnectionInvocationResult`) |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | UUID (string) | yes | Envelope-unique id. |
| `schemaVersion` | int | yes | Currently `1`. |
| `invocationId` | string | yes | Echo of the originating `ConnectionInvocation.invocationId`. |
| `kind` | string enum (`ConnectionKind`) | yes | Mirror of the invocation's `kind`. See [connection-payload.md](./codec-012-connection-payload.md#connectionkind-values). |
| `actionName` | string | yes | The action name that was invoked. |
| `status` | string enum | yes | See [status values](#status-values). |
| `result` | map<string, ArgumentValue> | yes (may be empty) | Populated only when `status = success`. Keys match the corresponding `ActionSchema.outputs[].name`. Uses the same tagged [`ArgumentValue`](./codec-013-connection-invocation.md#argumentvalue-tagged-encoding) encoding as `ConnectionInvocation.arguments`. |
| `errorMessage` | string | optional | Human-readable failure description. Populated for non-success statuses where the underlying framework surfaced a message. |
| `completedAt` | date (default `Codable` — epoch seconds) | yes | When the device finished processing. |

### `status` values

| Wire value | Swift case | Meaning |
|---|---|---|
| `success` | `.success` | Action completed; `result` carries declared outputs. |
| `capability_not_enabled` | `.capabilityNotEnabled` | User has not granted the matching capability. |
| `capability_revoked` | `.capabilityRevoked` | Capability was previously granted but has since been revoked. |
| `requires_confirmation` | `.requiresConfirmation` | The action requires user confirmation that hasn't been given (e.g. a `ConfirmationRequest` is pending). |
| `authorization_denied` | `.authorizationDenied` | User actively denied this invocation. |
| `execution_failed` | `.executionFailed` | The DataSink reported a non-permission failure (e.g. network error). `errorMessage` typically populated. |
| `unknown_action` | `.unknownAction` | The device has no handler for the supplied `actionName`. |

## Example encoding

Success:

```json
{
  "id": "D7B2E0F1-...",
  "schemaVersion": 1,
  "invocationId": "inv_5b1e7a2c",
  "kind": "calendar",
  "actionName": "createEvent",
  "status": "success",
  "result": {
    "eventId": { "type": "string", "value": "evt_2b4f7" }
  },
  "completedAt": 769420805
}
```

Permission failure:

```json
{
  "id": "...",
  "schemaVersion": 1,
  "invocationId": "inv_5b1e7a2c",
  "kind": "calendar",
  "actionName": "createEvent",
  "status": "capability_revoked",
  "result": {},
  "errorMessage": "Calendar write access was revoked at 2026-05-12T11:02:33Z",
  "completedAt": 769420810
}
```

## Producers and consumers

- **Sent by** — `ConnectionsManager` on the device, after the invocation has
  been dispatched to its `DataSink` and resolved.
- **Received by**
  - Codec decodes into `ConnectionInvocationResult`.
  - Dispatched in
    `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
    (`case ContentTypeConnectionInvocationResult:`).
  - Agent runtime pairs by `invocationId` to release pending state.

## Notes / gotchas

- A result **must** be emitted even on error; agents block on it.
- `result` is required on the wire (the field is non-optional) but is `{}`
  for any non-success status.
- Failure semantics are policy on the device side: a denied confirmation
  produces `authorization_denied`, while a pending one that hasn't been
  resolved produces `requires_confirmation`.
