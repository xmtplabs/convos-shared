# Capability Request Result (`convos.org/capability_request_result:1.0`)

> **Codec #10** — first added 2026‑05‑09 (paired reply for [Capability Request](./codec-009-capability-request.md)).

## Purpose

Device ➜ agent reply to a [`CapabilityRequest`](./codec-009-capability-request.md).
Always emitted — including on `cancelled` and `denied` — so the agent can
correlate by `requestId` and stop waiting.

On approval, the reply carries (a) the providers the resolver actually
persisted, and (b) the action schemas the agent can now invoke. Agents should
treat the returned `providers` set as ground truth — a `preferredProviders`
hint on the request is advisory.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `capability_request_result` |
| Version | `1.0` |
| Encoding | JSON (versioned in-band) |
| Fallback text | `"Approved <subject> access"` / `"Declined <subject> access"` / `"Cancelled <subject> access request"` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Custom Content Types/CapabilityRequestResultCodec.swift` |
| Payload struct | `ConvosCore/Sources/ConvosCore/CapabilityResolution/CapabilityRequestResult.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `version` | int | yes | In-band schema version. Decoder rejects when `version > 1` (`CapabilityRequestResult.supportedVersion`). |
| `requestId` | string | yes | Echo of the originating `CapabilityRequest.requestId`. |
| `status` | string enum | yes | `approved`, `denied`, or `cancelled`. |
| `subject` | string enum | yes | Mirror of the request's `subject`. See `CapabilitySubject` in [capability-request.md](./codec-009-capability-request.md#capabilitysubject-values). |
| `capability` | string enum | yes | Mirror of the request's `capability`. See `ConnectionCapability` in [capability-request.md](./codec-009-capability-request.md#connectioncapability-values). |
| `providers` | array of `ProviderID` | optional (defaults to `[]`) | Empty for `denied` / `cancelled`. For `approved`: 1 entry for writes and for non-federating subjects; ≥1 for federating-read subjects (currently only `fitness`). **Truncated to first 16 on decode** (`maxProviders`). |
| `availableActions` | array of `AvailableAction` | optional (defaults to `[]`) | Action schemas the agent can now invoke, filtered to the approved providers and capability. Empty for `denied` / `cancelled`. **Truncated to first 64 on decode** (`maxAvailableActions`). |

### `AvailableAction` shape

```ts
{
  providerId: string,            // ProviderID, e.g. "composio.googlecalendar"
  kind: ConnectionKind,          // string enum, see connection-payload.md
  actionName: string,            // matches an ActionSchema.actionName
  summary: string,
  inputs: Parameter[],
  outputs: Parameter[]
}
```

### `Parameter` shape (used inside `AvailableAction`)

| Field | Type | Notes |
|---|---|---|
| `name` | string | Parameter name as referenced by `ConnectionInvocation.action.arguments`. |
| `type` | string | Type tag (e.g. `string`, `bool`, `iso8601`, `enum`, `array`); see the `ArgumentValue` tag set in [connection-invocation.md](./codec-013-connection-invocation.md#argumentvalue-tagged-encoding). |
| `description` | string | Human-readable hint. |
| `isRequired` | bool | Whether the parameter must be supplied on invoke. |

## Example encoding

```json
{
  "version": 1,
  "requestId": "cap_req_7c2e9a",
  "status": "approved",
  "subject": "calendar",
  "capability": "write_create",
  "providers": ["composio.googlecalendar"],
  "availableActions": [
    {
      "providerId": "composio.googlecalendar",
      "kind": "calendar",
      "actionName": "createEvent",
      "summary": "Add an event to the user's calendar",
      "inputs": [
        { "name": "title",   "type": "string",  "description": "Event title",                "isRequired": true  },
        { "name": "startsAt","type": "iso8601", "description": "ISO‑8601 start instant",     "isRequired": true  },
        { "name": "endsAt",  "type": "iso8601", "description": "ISO‑8601 end instant",       "isRequired": false }
      ],
      "outputs": [
        { "name": "eventId", "type": "string",  "description": "Provider event id",          "isRequired": true  }
      ]
    }
  ]
}
```

## Producers and consumers

- **Sent by**
  `ConvosCore/Sources/ConvosCore/CapabilityResolution/CapabilityRequestResultWriter.swift`
  once the user resolves the picker.
- **Received by** — dispatched in
  `ConvosCore/Sources/ConvosCore/Storage/XMTP DB Representations/DecodedMessage+DBRepresentation.swift`
  (`case ContentTypeCapabilityRequestResult:`). The agent runtime consumes
  these to discover the action manifest it just gained access to.

## Notes / gotchas

- The result *must* be sent for every terminal user action, including
  cancellation (closed picker, app backgrounded past timeout, etc.) — agents
  rely on it to release pending state.
- `availableActions[].inputs[]/outputs[]` use the simplified `Parameter` shape
  shown above, not the richer `ActionParameter` shape used in-process by the
  device — port implementations should not bring across `ParameterType` (with
  `allowed` / `element`) into the wire schema.
- `providers` and `availableActions` are silently truncated on decode.
