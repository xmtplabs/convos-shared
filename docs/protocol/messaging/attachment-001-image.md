# Image Attachment

> **Attachment encoding #1** — first added 2026‑02‑14.

This is **not** a Convos-specific XMTP codec — it's an *encoding convention*
layered on top of the XMTP-standard `RemoteAttachmentCodec` (or, for tiny
attachments, `AttachmentCodec`). The wire format is XMTP; what's documented
here is the Convos contract around mimeType, filename, file format, and which
metadata fields are populated by the sender.

## Purpose

Send a still image (JPEG-compressed) as a chat attachment. Renders inline in
the conversation as a tap-to-expand photo bubble.

## Identity

| Field | Value |
|---|---|
| Underlying XMTP codec | `xmtp.org/remoteStaticAttachment:1.0` (`RemoteAttachmentCodec`) |
| MIME type | `image/jpeg` |
| Filename pattern | `photo_<unix-seconds>_<uuid-prefix-8>.jpg` |
| File format | JPEG (compressed by `PhotoAttachmentService`) |
| Receiver discriminator | `mimeType.hasPrefix("image/")` ⇒ `MediaType.image` |
| Sender helper | `PhotoAttachmentService` (`ConvosCore/Sources/ConvosCore/Messaging/PhotoAttachmentService.swift`) |
| Send entry point | `OutgoingMessageWriter.sendEagerPhoto(trackingKey:)` |

## Wire shape

The bytes that travel between devices are exactly an XMTP `RemoteAttachment`:

```
RemoteAttachment {
    url:            String     // HTTPS URL to encrypted ciphertext
    contentDigest:  String     // SHA‑256 of ciphertext
    secret:         bytes      // symmetric key (encrypted under MLS)
    salt:           bytes      // HKDF salt
    nonce:          bytes      // AES‑GCM nonce
    scheme:         "https"
    contentLength:  null
    filename:       "photo_1737945600_a1b2c3d4.jpg"
}
```

The encrypted payload at `url` is a standard XMTP `Attachment` blob:

```
Attachment {
    filename: "photo_1737945600_a1b2c3d4.jpg"
    mimeType: "image/jpeg"
    data:     <JPEG bytes>
}
```

## Sender-only metadata (`StoredRemoteAttachment`, local DB only)

The sender writes these to its own SQLite via `attachmentLocalStateWriter.saveWithDimensions(...)` and includes them in the `StoredRemoteAttachment` JSON used as the local attachment key. **They do not travel over the wire** — the receiver reconstructs `StoredRemoteAttachment` from the wire `RemoteAttachment` fields plus a `mimeType` inferred from the filename extension:

| Field | Sender populates | Receiver gets |
|---|---|---|
| `url` | yes | yes |
| `contentDigest` | yes | yes |
| `secret`, `salt`, `nonce` | yes | yes |
| `filename` | `photo_<ts>_<uuid>.jpg` | yes |
| `mimeType` | `image/jpeg` | inferred from filename ext ⇒ `image/jpeg` |
| `mediaWidth` | yes (pixels) | **lost** |
| `mediaHeight` | yes (pixels) | **lost** |
| `mediaDuration` | — | — |
| `thumbnailDataBase64` | — | — |

Dimensions are populated locally so the bubble can lay out at the correct
aspect ratio before the bytes are loaded. On the receiver, dimensions are
recovered after download by inspecting the decoded JPEG.

## Receiver detection

```swift
// HydratedAttachment.mediaType in HydratedAttachment.swift
guard let mimeType else { return .image }            // null defaults to .image
if mimeType.hasPrefix("image/") { return .image }
if mimeType.hasPrefix("video/") { return .video }
if mimeType.hasPrefix("audio/") { return .audio }
return .file
```

`mimeType` is inferred from `filename` via `UTType(filenameExtension:)` in
`DecodedMessage+DBRepresentation.handleRemoteAttachmentContent()`.

## Producers and consumers

- **Sent by**
  - `ConvosCore/Sources/ConvosCore/Storage/Writers/OutgoingMessageWriter.swift` — `sendEagerPhoto(...)` and the eager-upload pipeline (`startEagerUpload` → background upload → `sendEagerPhoto`).
  - Filename generator: `PhotoAttachmentService.generateFilename()`.
- **Received by**
  - Dispatched in `DecodedMessage+DBRepresentation.swift` (`case ContentTypeRemoteAttachment:`).
  - Hydrated by `MessagesRepository.hydrateAttachment(...)` into a `HydratedAttachment` with `mediaType = .image`.
- **Rendered by** — the photo bubble in the conversation transcript.

## Notes / gotchas

- The `image/jpeg` choice is hard-coded — sending a PNG would mean changing
  the mime type *and* the filename extension *and* potentially the JPEG
  compression step in `PhotoAttachmentService`. Don't assume PNG support.
- The filename prefix `photo_` is purely a sender convention for local-cache
  organization; the receiver does not match on it. Discrimination is mime-type-only.
- Dimensions and any thumbnail are sender-only. Port implementations that
  want pre-download aspect-ratio hints would need to add a side channel
  (Convos iOS currently doesn't have one for photos — it just renders
  square placeholders until the bytes arrive).
- See [Multi-photo sends](#) — multiple photos in one message ride on the
  XMTP-standard `MultiRemoteAttachmentCodec` (`xmtp.org/multiRemoteStaticAttachment:1.0`).
  Each entry uses the same conventions as above.
