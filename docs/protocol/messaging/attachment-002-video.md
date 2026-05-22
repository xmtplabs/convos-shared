# Video Message

> **Attachment encoding #2** — first added 2026‑04‑03.

This is **not** a Convos-specific XMTP codec — it's an *encoding convention*
layered on top of the XMTP-standard `RemoteAttachmentCodec`. The wire format
is XMTP; what's documented here is the Convos contract around mimeType,
filename, codec choice, compression, and which metadata fields are populated
by the sender.

## Purpose

Send a short video clip as a chat attachment. The sender compresses the source
video to H.264 mp4 (size/dimension constrained), uploads the encrypted blob,
and publishes a `RemoteAttachment`. Receivers download, decrypt, and play.

## Identity

| Field | Value |
|---|---|
| Underlying XMTP codec | `xmtp.org/remoteStaticAttachment:1.0` (`RemoteAttachmentCodec`) |
| MIME type | `video/mp4` |
| Filename pattern | `video_<unix-seconds>_<uuid-prefix-8>.mp4` |
| File format | H.264 mp4 (compressed by `VideoCompressionService`) |
| Receiver discriminator | `mimeType.hasPrefix("video/")` ⇒ `MediaType.video` |
| Sender helper | `VideoCompressionService` |
| Send entry point | `OutgoingMessageWriter.sendVideo(at:replyToMessageId:)` |

## Wire shape

```
RemoteAttachment {
    url:            String     // HTTPS URL to encrypted ciphertext
    contentDigest:  String     // SHA‑256 of ciphertext
    secret:         bytes
    salt:           bytes
    nonce:          bytes
    scheme:         "https"
    contentLength:  null
    filename:       "video_1737945600_a1b2c3d4.mp4"
}
```

Encrypted payload at `url` is a standard XMTP `Attachment`:

```
Attachment {
    filename: "video_1737945600_a1b2c3d4.mp4"
    mimeType: "video/mp4"
    data:     <H.264 mp4 bytes>
}
```

## Sender-only metadata (`StoredRemoteAttachment`, local DB only)

| Field | Sender populates | Receiver gets |
|---|---|---|
| `url`, `contentDigest`, `secret`, `salt`, `nonce` | yes | yes |
| `filename` | `video_<ts>_<uuid>.mp4` | yes |
| `mimeType` | `video/mp4` | inferred from `.mp4` ⇒ `video/mp4` |
| `mediaWidth` | yes (compressed dims) | **lost** |
| `mediaHeight` | yes (compressed dims) | **lost** |
| `mediaDuration` | yes (seconds) | **lost** |
| `thumbnailDataBase64` | yes (JPEG poster frame) | **lost** |

The thumbnail is generated locally by `VideoCompressionService` and stored as
a base64 JPEG inside `StoredRemoteAttachment` on the sender's device. It is
shown immediately on the sender's bubble before the upload completes.
**The receiver does not get this thumbnail** — they see a placeholder until
the encrypted mp4 is fetched.

## Receiver detection

`mimeType` is inferred from the `.mp4` extension via
`UTType(filenameExtension:)` in
`DecodedMessage+DBRepresentation.handleRemoteAttachmentContent()`, which
gives `video/mp4`. `HydratedAttachment.mediaType` then returns `.video`.

## Producers and consumers

- **Sent by**
  - `ConvosCore/Sources/ConvosCore/Storage/Writers/OutgoingMessageWriter.swift` — `sendVideo(at:replyToMessageId:)` (and the eager-video pipeline starting at `startEagerVideoUpload`).
  - Compression: `VideoCompressionService` (`ConvosCore/Sources/ConvosCore/Messaging/`).
- **Received by**
  - Dispatched in `DecodedMessage+DBRepresentation.swift` (`case ContentTypeRemoteAttachment:`).
  - Hydrated into `HydratedAttachment` with `mediaType = .video`.
- **Rendered by** — video bubble; tap to play in-place.

## Notes / gotchas

- The compression step (resolution cap, bitrate, fps) is a **sender-side**
  policy in `VideoCompressionService`. The wire only carries the resulting
  mp4 bytes — receivers should be prepared for any H.264 mp4 within the
  envelope's size constraints, not a specific resolution.
- Dimensions, duration, and thumbnail are all sender-only. A port wanting
  pre-download poster frames would need to introduce a side channel
  (currently none exists).
- `video/mp4` is hard-coded; sending `video/quicktime` or `video/webm` is
  not supported by the current iOS sender, though a receiver would still
  discriminate them as `.video` from the mimeType prefix.
- Background docs: `docs/plans/video-messages.md` in the iOS repo.
