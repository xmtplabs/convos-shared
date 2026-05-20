# Invite Join Error (`convos.org/invite_join_error:1.0`)

> **Codec #2** — first added 2026‑02‑26.

## Purpose

Host ➜ joiner failure reply for a [`join_request`](./codec-005-join-request.md). Tells
the joiner why their invite acceptance couldn't be completed, in a form the
joiner client can render directly via `userFacingMessage`. Always keyed by
the same `inviteSlug` (here named `inviteTag`) so the joiner can correlate.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `invite_join_error` |
| Version | `1.0` |
| Encoding | JSON (`JSONEncoder` with `dateEncodingStrategy = .iso8601`) |
| Fallback text | `userFacingMessage` |
| `shouldPush` | `true` — the joiner is waiting and should be told the attempt failed |
| Defined in | `ConvosInvites/Sources/ConvosInvites/ContentTypes/InviteJoinErrorCodec.swift` |
| Payload struct | `ConvosInvites/Sources/ConvosInvites/Models.swift` (struct `InviteJoinError`, enum `InviteJoinErrorType`) |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `errorType` | string enum | yes | One of `conversation_expired`, `conversation_not_found`, `consent_not_allowed`, `generic_failure`. Forward-compat: any unknown rawValue decodes to `conversation_expired` so older joiners keep the existing UX. |
| `inviteTag` | string | yes | The 10-character invite slug from the originating `join_request.inviteSlug`. |
| `timestamp` | string (ISO‑8601 date-time) | yes | When the host produced the error. |

### `errorType` values

| Wire value | Swift case | Meaning |
|---|---|---|
| `conversation_expired` | `.conversationExpired` | The signed invite's `conversationExpiresAt` has passed. **Also the fallback for any unknown rawValue** — see notes. |
| `conversation_not_found` | `.conversationNotFound` | `findConversation` returned `nil`; libxmtp doesn't have the group locally. |
| `consent_not_allowed` | `.consentNotAllowed` | The conversation exists locally but its consent state is not `.allowed`. |
| `generic_failure` | `.genericFailure` | Catch-all for non-expiry failures. |

### `userFacingMessage` (derived, not on the wire)

The Swift struct exposes a computed `userFacingMessage` based on `errorType`:

| `errorType` | `userFacingMessage` |
|---|---|
| `conversation_expired`, `conversation_not_found`, `consent_not_allowed` | `"This conversation is no longer available"` |
| `generic_failure` | `"Failed to join conversation"` |

This value isn't part of the wire payload; it's reconstructed by the decoder.
Port implementations should mirror the string-mapping above so user-visible
behaviour stays consistent.

## Example encoding

```json
{
  "errorType": "conversation_expired",
  "inviteTag": "h2kfn39ax8",
  "timestamp": "2026-05-15T22:14:08Z"
}
```

## Producers and consumers

- **Sent by** — invites host pipeline when join handling fails (see
  `InviteJoinFailureReason` and the join-handler code in
  `ConvosInvites/Sources/ConvosInvites/`).
- **Received by** — joiner's invite subsystem; surfaces `userFacingMessage`
  as a toast / inline error in the invite acceptance flow.

## Notes / gotchas

- **Wire-format forward compatibility**: `InviteJoinErrorType.init(rawValue:)`
  collapses unknown raw values to `.conversationExpired` rather than failing
  decode. This means a newer host adding `errorType: "rate_limited"` won't
  break older joiners — they'll see "This conversation is no longer
  available" instead of nothing. Implementers must preserve this default.
- `timestamp` uses ISO‑8601 (along with [`explode_settings`](./codec-001-explode-settings.md)
  — the rest of the codecs use Swift's default numeric `Date` encoding).
