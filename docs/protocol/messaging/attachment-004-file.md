# File Attachment

> **Attachment encoding #4** — first added 2026‑05‑03.

This is **not** a Convos-specific XMTP codec — it's an *encoding convention*
layered on top of the XMTP-standard `RemoteAttachmentCodec`. The wire format
is XMTP; what's documented here is the Convos contract for generic files
(PDFs, text, Markdown, HTML, arbitrary documents) that don't fall into the
image / video / voice-memo specializations.

## Purpose

Send an arbitrary file as a chat attachment. The mimeType is user/system
supplied (not constrained to a specific value), and the bubble renders a
generic file card with the filename and a type label derived from the
extension or mimeType.

## Identity

| Field | Value |
|---|---|
| Underlying XMTP codec | `xmtp.org/remoteStaticAttachment:1.0` (`RemoteAttachmentCodec`) |
| MIME type | Caller-supplied (e.g. `application/pdf`, `text/markdown`, `text/html`, `application/zip`) |
| Filename pattern | Caller-supplied; the **on-disk cache filename** uses the convention `<unique-prefix>_<filename>` where `<unique-prefix>` contains no underscores (a single `_` separates prefix from filename) |
| File format | Arbitrary |
| Receiver discriminator | Anything not matching `image/*`, `video/*`, `audio/*` ⇒ `MediaType.file` |
| Send entry point | `OutgoingMessageWriter.sendFile(at:filename:mimeType:replyToMessageId:)` |

## Wire shape

```
RemoteAttachment {
    url:            String     // HTTPS URL to encrypted ciphertext
    contentDigest:  String
    secret:         bytes
    salt:           bytes
    nonce:          bytes
    scheme:         "https"
    contentLength:  null
    filename:       "Quarterly_Report.pdf"   // caller-supplied
}
```

Encrypted payload is a standard XMTP `Attachment`:

```
Attachment {
    filename: "Quarterly_Report.pdf"
    mimeType: "application/pdf"
    data:     <file bytes>
}
```

## Sender-only metadata (`StoredRemoteAttachment`, local DB only)

| Field | Sender populates | Receiver gets |
|---|---|---|
| `url`, `contentDigest`, `secret`, `salt`, `nonce` | yes | yes |
| `filename` | caller-supplied | yes |
| `mimeType` | caller-supplied | **re-inferred from filename extension** (may differ from sender's value if extension is ambiguous) |
| `mediaWidth` / `mediaHeight` / `mediaDuration` / `thumbnailDataBase64` | — | — |

No media metadata is populated. The receiver's view of mimeType comes from
`UTType(filenameExtension:)` against the wire filename, not from the
sender's `mimeType` field (which never travels).

### Filename caching convention

The sender's `OutgoingMessageWriter.sendFile` builds a cache filename of the
form:

```
<unix-seconds>-<uuid-prefix-8>_<user-filename>
```

The first segment (`<unix-seconds>-<uuid-prefix-8>`) must contain **no
underscores** — the hydration logic in `MessagesRepository` strips
everything before the first `_` when deriving a display filename from a
local `file://` key. This is purely local-cache hygiene; the wire filename
sent to other devices is the user's original filename without the prefix.

## Receiver detection

```swift
guard let mimeType else { return .image }
if mimeType.hasPrefix("image/") { return .image }
if mimeType.hasPrefix("video/") { return .video }
if mimeType.hasPrefix("audio/") { return .audio }
return .file
```

Anything that doesn't match an image/video/audio prefix falls into `.file`.
A nil mimeType (e.g. a filename with no extension) currently defaults to
`.image` — that's a known quirk of `HydratedAttachment.mediaType` and not
ideal for arbitrary files; senders should ensure the filename always
carries an extension.

### Special-case rendering hints

`HydratedAttachment` exposes derived flags the bubble UI uses to pick a
renderer:

| Property | True when |
|---|---|
| `isMarkdownFile` | `filename` ends in `.md`/`.markdown`, **or** mimeType is `text/markdown` / `text/x-markdown` |
| `isHTMLFile` | `filename` ends in `.html`/`.htm`, **or** mimeType is `text/html` |
| `fileTypeLabel` | Uppercased extension (e.g. `"PDF"`), falling back to the second mimeType component if no extension |

These are receiver-side conveniences derived from the wire filename — there
is no separate "markdown" or "html" content type.

## Producers and consumers

- **Sent by**
  - `OutgoingMessageWriter.sendFile(at:filename:mimeType:replyToMessageId:)` →
    `OutgoingMessageWriter.sendFileAttachment(...)` →
    standard XMTP `RemoteAttachmentCodec`.
- **Received by**
  - Dispatched in `DecodedMessage+DBRepresentation.swift` (`case ContentTypeRemoteAttachment:`).
  - Hydrated by `MessagesRepository.hydrateAttachment(...)` with
    `mediaType = .file` (or special-cased to `.image`/`.video`/`.audio` if
    the extension implies one).
  - Local file caching for tap-to-preview: `FileAttachmentLoader.swift`.
- **Rendered by** — generic file bubble; tap to preview via QuickLook.

## Notes / gotchas

- The caller's `mimeType` is **not transmitted**. The receiver re-infers
  from the filename extension; if a sender supplies `application/pdf` but
  names the file `report.bin`, the receiver will see `application/octet-stream`
  or similar. Always pick a filename with a meaningful extension.
- A nil mimeType (filename without extension) is treated as `.image` by the
  current renderer. Senders should never omit the extension.
- For inline-tiny attachments (≤1 MB), iOS still uses `RemoteAttachmentCodec`
  via the upload path, not the XMTP-standard `AttachmentCodec`. The
  inline-codec path exists in the dispatcher
  (`DecodedMessage+DBRepresentation.handleAttachmentContent`) for received
  inline attachments, but Convos's outgoing sender always uploads.
- Background docs: `docs/plans/file-attachments.md` in the iOS repo.
