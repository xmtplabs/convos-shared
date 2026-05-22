# Convos Messaging Codecs

This directory is the wire-format reference for every **Convos-specific** XMTP
content codec that ships in the iOS client. It is the spec port-implementers
(Android, server agents, integrations, devtools) should read from.

Two flavours of "Convos-specific" protocol are documented here:

1. **XMTP-registered codecs under the `convos.org` authority** — full
   `ContentCodec` implementations with their own `ContentTypeID`. There are
   14 of these (see [Codec index](#codec-index)).
2. **Attachment encodings layered over standard XMTP codecs** — conventions
   on top of XMTP's `RemoteAttachmentCodec` (and friends) that pick mimeTypes,
   filename patterns, file formats, and which metadata fields the sender
   populates. The wire format is XMTP-standard; what's Convos-specific is the
   contract around how the bytes are produced and what extra metadata lives
   only on the sender's device. There are 4 of these (see
   [Attachment encodings](#attachment-encodings)).

The generic XMTP codecs themselves (`Text`, `Reply`, `Reaction`, `ReactionV2`,
`Attachment`, `RemoteAttachment`, `MultiRemoteAttachment`, `GroupUpdated`,
`ReadReceipt`) are **out of scope** — those are defined and documented by
the XMTP project and are imported as-is from `XMTPiOS`.

## Shared concepts

### Content type IDs

Every XMTP content codec advertises a `ContentTypeID` made up of:

```
ContentTypeID(
    authorityID:  "convos.org",        // the namespace the codec belongs to
    typeID:       "<typeID>",           // unique per codec within the namespace
    versionMajor: 1,
    versionMinor: 0
)
```

All Convos codecs use `authorityID = "convos.org"` and version `1.0`. Per-codec
docs write the full triple as `convos.org/<typeID>:1.0`.

### Registration

iOS registers every codec on the XMTP client at session boot:

- `ConvosCore/Sources/ConvosCore/Inboxes/SessionStateMachine.swift` —
  `ClientOptions.codecs: [...]` (lines 963–983)
- `ConvosCore/Sources/ConvosConnectionsXMTP/Bootstrap/ConvosConnectionsXMTP.swift` —
  `ConvosConnectionsXMTP.codecs()` appends the three `ConnectionInvocation` /
  `ConnectionInvocationResult` / `ConnectionPayload` codecs.

If a codec is not in that list, incoming messages of its type will fail to
decode and fall back to the codec's `fallback(content:)` text (or, if that is
`nil`, be silently dropped from the conversation transcript).

### `ContentCodec` contract

Every codec conforms to `XMTPiOS.ContentCodec` and supplies four operations:

| Operation | What it does | When it runs |
|---|---|---|
| `encode(content:) -> EncodedContent` | Serializes the Swift value into bytes (`content` + `type` ContentTypeID) | At send time |
| `decode(content:) -> T` | Deserializes received bytes back to the Swift value | At receive time |
| `fallback(content:) -> String?` | Plaintext rendering for clients that don't know the codec | Read by other clients / notification surfaces; per-codec, may be `nil` |
| `shouldPush(content:) -> Bool` | Whether incoming messages with this type should generate a push notification | Push routing |

### Encoding families

Convos codecs use one of two wire encodings for the `EncodedContent.content`
byte blob:

- **JSON via Swift `Codable`** — used by 12 of 14 codecs. Field names match the
  Swift property names verbatim (no `snake_case` remap on the wrapper structs);
  values inside use whatever `Codable` default applies, with a few codecs
  setting `dateEncodingStrategy = .iso8601`.
- **Binary Protocol Buffers** (via SwiftProtobuf) — used only by
  `ProfileUpdateCodec` and `ProfileSnapshotCodec`. The schema lives at
  `ConvosCore/Sources/ConvosCore/Profiles/Proto/profile_messages.proto`.

### Versioning

Codec identity carries a major/minor version (currently `1.0` for every codec).
Several payload structs additionally carry an in-band `version: Int` field and
reject decode when the received version exceeds `Self.supportedVersion`:

- `CapabilityRequest`
- `CapabilityRequestResult`
- `CloudConnectionGrantRequest`
- `ConnectionEvent`

(`ConnectionInvocation`, `ConnectionInvocationResult`, and `ConnectionPayload`
carry a `schemaVersion` field on the envelope, but currently do not gate decode
on it — they instead use forward-compatible `unknown` cases for the body.)

This in-band version is a finer-grained gate than the codec's
`ContentTypeID.versionMajor`; it lets us add fields under the same content type
ID without requiring a registration handshake.

### Sender verification

A handful of codecs are sensitive to who sent the message. Those checks live in
`ConvosCore/Sources/ConvosCore/Storage/Writers/IncomingMessageWriter.swift`,
**not** inside the codec — the codec only handles bytes ↔ struct. The
per-codec docs call out which content types are filtered.

## Codec index

Codecs are numbered by **implementation age in iOS** — `#1` is the oldest
codec in the repo, `#14` is the newest. The number is the file's first-commit
date on `main` (ties broken by request/reply pairing and dependency). The
same number appears on each per-codec doc.

| # | First added | Type ID | Doc | Purpose |
|---|---|---|---|---|
| 1 | 2025‑08‑20 | `convos.org/explode_settings:1.0` | [codec-001-explode-settings.md](./codec-001-explode-settings.md) | Conversation expiration / "disappearing" group setting |
| 2 | 2026‑02‑26 | `convos.org/invite_join_error:1.0` | [codec-002-invite-join-error.md](./codec-002-invite-join-error.md) | Host ➜ joiner failure reply for an invite acceptance |
| 3 | 2026‑03‑05 | `convos.org/profile_update:1.0` | [codec-003-profile-update.md](./codec-003-profile-update.md) | Self-published member profile change (name / avatar / metadata) |
| 4 | 2026‑03‑05 | `convos.org/profile_snapshot:1.0` | [codec-004-profile-snapshot.md](./codec-004-profile-snapshot.md) | Bulk member-profile catch-up sent when adding new members |
| 5 | 2026‑03‑05 | `convos.org/join_request:1.0` | [codec-005-join-request.md](./codec-005-join-request.md) | Joiner ➜ host DM announcing acceptance of an invite |
| 6 | 2026‑03‑11 | `convos.org/assistant_join_request:1.0` | [codec-006-assistant-join-request.md](./codec-006-assistant-join-request.md) | Lifecycle event for an assistant joining a conversation |
| 7 | 2026‑04‑03 | `convos.org/typing_indicator:1.0` | [codec-007-typing-indicator.md](./codec-007-typing-indicator.md) | Ephemeral "user is typing" presence |
| 8 | 2026‑04‑27 | `convos.org/connection_grant_request:1.0` | [codec-008-cloud-connection-grant-request.md](./codec-008-cloud-connection-grant-request.md) | Agent asks the user to connect an external service (Composio cloud connection) |
| 9 | 2026‑05‑09 | `convos.org/capability_request:1.0` | [codec-009-capability-request.md](./codec-009-capability-request.md) | Agent ➜ user request for a capability/subject grant |
| 10 | 2026‑05‑09 | `convos.org/capability_request_result:1.0` | [codec-010-capability-request-result.md](./codec-010-capability-request-result.md) | User ➜ agent reply (approve / deny / cancel) carrying available actions |
| 11 | 2026‑05‑09 | `convos.org/connection_event:1.0` | [codec-011-connection-event.md](./codec-011-connection-event.md) | Side-effect notification: grant or revoke of a capability/provider |
| 12 | 2026‑05‑09 | `convos.org/connection_payload:1.0` | [codec-012-connection-payload.md](./codec-012-connection-payload.md) | Device ➜ agent telemetry envelope (Health, Calendar, Contacts, etc.) |
| 13 | 2026‑05‑09 | `convos.org/connection_invocation:1.0` | [codec-013-connection-invocation.md](./codec-013-connection-invocation.md) | Agent ➜ device action invocation (write request against a `DataSink`) |
| 14 | 2026‑05‑09 | `convos.org/connection_invocation_result:1.0` | [codec-014-connection-invocation-result.md](./codec-014-connection-invocation-result.md) | Device ➜ agent reply to an invocation |

## Attachment encodings

These are *not* XMTP-registered codecs — they ride on the XMTP-standard
`RemoteAttachmentCodec` (`xmtp.org/remoteStaticAttachment:1.0`). The
"protocol" lives in the conventions around mimeType, filename, file
format, and which `StoredRemoteAttachment` fields the sender populates
(most of those fields do **not** travel over the wire; the receiver
re-derives a stripped `StoredRemoteAttachment` from the wire
`RemoteAttachment` plus a mimeType inferred from the filename extension).

Numbered separately from the XMTP-registered codecs above. Same
oldest-first rule: `#1` is the oldest send path in the iOS repo.

| # | First added | MIME type | Filename pattern | Doc | Purpose |
|---|---|---|---|---|---|
| 1 | 2026‑02‑14 | `image/jpeg` | `photo_<ts>_<uuid>.jpg` | [attachment-001-image.md](./attachment-001-image.md) | Still images (JPEG) |
| 2 | 2026‑04‑03 | `video/mp4` | `video_<ts>_<uuid>.mp4` | [attachment-002-video.md](./attachment-002-video.md) | H.264 mp4 video clips |
| 3 | 2026‑04‑06 | `audio/m4a` | `voice_memo_<ts>_<uuid>.m4a` | [attachment-003-voice-memo.md](./attachment-003-voice-memo.md) | AAC voice recordings (with locally-computed waveform) |
| 4 | 2026‑05‑03 | *caller-supplied* | *caller-supplied* | [attachment-004-file.md](./attachment-004-file.md) | Arbitrary files (PDF, Markdown, etc.) |

### What travels vs. what's local-only

A short reference applicable to **all** attachment encodings:

| Field | On the wire? | Notes |
|---|---|---|
| `url`, `contentDigest`, `secret`, `salt`, `nonce` | yes | XMTP-standard `RemoteAttachment` fields |
| `filename` | yes | Sender chooses; receiver discriminates by its extension |
| `mimeType` | no — **inferred by receiver** from filename extension | `UTType(filenameExtension:)` in `DecodedMessage+DBRepresentation.handleRemoteAttachmentContent` |
| `mediaWidth`, `mediaHeight`, `mediaDuration`, `thumbnailDataBase64` | **no** — populated only on the sender's local DB | Receivers see `nil` until they download and inspect the payload themselves |
| `waveformLevels` (voice memos) | **no** — local-only sidecar in `AttachmentLocalState` | Sender records live from `AVAudioRecorder`; receiver re-computes via `VoiceMemoWaveformAnalyzer` after download |

If you're porting Convos to another platform, the wire contract is just the
XMTP `RemoteAttachment` + a filename with the conventional extension. The
sender-only fields are quality-of-life metadata for the device that authored
the message; they are not part of the protocol contract.

## How to use this reference

- The **Identity** table on each page is the wire contract: `Authority ID`,
  `Type ID`, `Version`, encoding, fallback, push policy. Other clients must
  match these exactly to interoperate.
- The **Payload schema** table is the field-by-field source of truth. Where the
  Swift source applies validation (length caps, version gates, default
  fallbacks) it is listed in the "Notes" column.
- The **Example encoding** shows a canonical wire payload (JSON object or
  protobuf textproto). Field ordering is not significant for JSON; for
  Protobuf, the field numbers in `profile_messages.proto` are.
- The **Producers and consumers** section lists iOS-side file paths so port
  authors can grep for behavioural reference, and so reviewers can locate the
  dispatch site.
