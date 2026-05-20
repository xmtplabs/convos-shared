# Connection Invocation (`convos.org/connection_invocation:1.0`)

> **Codec #13** — first added 2026‑05‑09 (paired with [Connection Invocation Result](./codec-014-connection-invocation-result.md)).

## Purpose

Agent ➜ device action invocation: "please run *action* on this device's
*DataSink* with these arguments." Used for writes (create calendar event,
update contact, etc.) and for any device-bound effect described by an
`ActionSchema`. The device replies with a
[`connection_invocation_result`](./codec-014-connection-invocation-result.md) carrying
the same `invocationId` so the agent can correlate the response.

The wire format is transport-agnostic; this codec is the XMTP-specific
wrapper.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `connection_invocation` |
| Version | `1.0` |
| Encoding | JSON |
| Fallback text | `"Action requested: <action.name>"` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosConnectionsXMTP/Codecs/ConnectionInvocationCodec.swift` |
| Payload struct | `ConvosConnections/Sources/ConvosConnections/Core/ConnectionInvocation.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | UUID (string) | yes | Envelope-unique id. |
| `schemaVersion` | int | yes | Currently `1` (`ConnectionInvocation.currentSchemaVersion`). |
| `invocationId` | string | yes | Correlation id echoed back in the result. Agents pick this. |
| `kind` | string enum (`ConnectionKind`) | yes | Which device source is being invoked. See [connection-payload.md](./codec-012-connection-payload.md#connectionkind-values). |
| `action` | object (`ConnectionAction`) | yes | `{ "name": string, "arguments": { <name>: ArgumentValue, ... } }`. The `name` must match an `ActionSchema.actionName` for the given `kind`. |
| `issuedAt` | date (default `Codable` — epoch seconds) | yes | When the agent issued the invocation. |

### `ConnectionAction`

| Field | Type | Notes |
|---|---|---|
| `name` | string | Action name from the `ActionSchema` (`<kind>.<actionName>` matches `ActionSchema.id`). |
| `arguments` | map<string, ArgumentValue> | Keys match the schema's `inputs[].name`. |

### `ArgumentValue` tagged encoding

Every argument value is wrapped in a tagged object:

```json
{ "type": "<tag>", "value": <value> }
```

| Tag | `value` type | Notes |
|---|---|---|
| `string` | string | |
| `bool` | bool | |
| `int` | int | |
| `double` | number | |
| `date` | date (`Codable` default) | Numeric epoch by default. |
| `iso8601` | string | Pre-formatted ISO‑8601 string. Use when callers want lexical control. |
| `enum` | string | Raw value from an `ActionSchema` enum parameter. |
| `array` | array of `ArgumentValue` | Nested values are themselves tagged objects. |
| `null` | `null` | Used to clear an optional argument. |

(The same tag set is used by `ConnectionInvocationResult.result`.)

## Example encoding

```json
{
  "id": "C2A1F4D2-...",
  "schemaVersion": 1,
  "invocationId": "inv_5b1e7a2c",
  "kind": "calendar",
  "action": {
    "name": "createEvent",
    "arguments": {
      "title":    { "type": "string",  "value": "Kickoff" },
      "startsAt": { "type": "iso8601", "value": "2026-06-01T17:00:00Z" },
      "allDay":   { "type": "bool",    "value": false }
    }
  },
  "issuedAt": 769420800
}
```

## Producers and consumers

- **Sent by** — agent runtime / server-side agent processes.
- **Received by**
  - Codec decodes into `ConnectionInvocation`.
  - `ConvosCore/Sources/ConvosConnectionsXMTP/Listener/XMTPInvocationListener.swift`
    routes the invocation into `ConnectionsManager`, which dispatches it to
    the matching `DataSink`.
  - Dispatched in
    `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
    (`case ContentTypeConnectionInvocation:`).

## Notes / gotchas

- The set of valid `action.name` values is discovered out-of-band via the
  `availableActions` returned in a
  [`capability_request_result`](./codec-010-capability-request-result.md#availableaction-shape).
  Don't hard-code action names — read them from the result.
- `schemaVersion` is not gated on decode; future forward-compat should be
  layered via new optional fields, not version bumps, or by introducing a
  new content type.
- `arguments` is a `map`, so the on-the-wire ordering of keys is not
  significant. Receivers must not rely on key order.
