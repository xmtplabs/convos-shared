# Voice Memo

> **Attachment encoding #3** — first added 2026‑04‑06.

This is **not** a Convos-specific XMTP codec — it's an *encoding convention*
layered on top of the XMTP-standard `RemoteAttachmentCodec`. The wire format
is XMTP; what's documented here is the Convos contract around mimeType,
filename, audio codec, and the local waveform/duration handling — including
**how the visible waveform is reconstructed on the receiver**, which is a
common point of confusion (the waveform does *not* travel over the wire).

## Purpose

Send a short voice recording. Recorded as AAC m4a; rendered as a bubble with a
duration label and a waveform. The waveform is **computed locally** on both
sides — the sender from live `AVAudioRecorder` averagePower samples, the
receiver from PCM analysis of the downloaded audio.

## Identity

| Field | Value |
|---|---|
| Underlying XMTP codec | `xmtp.org/remoteStaticAttachment:1.0` (`RemoteAttachmentCodec`) |
| MIME type | `audio/m4a` |
| Filename pattern | `voice_memo_<unix-seconds>_<uuid-prefix-8>.m4a` |
| File format | AAC in MPEG‑4 container (`AVAudioRecorder` defaults: mono, 44.1 kHz, AAC LC) |
| Receiver discriminator | `mimeType.hasPrefix("audio/")` ⇒ `MediaType.audio` |
| Sender helper | `VoiceMemoRecorder` (`ConvosCoreiOS/VoiceMemoRecorder.swift`) |
| Send entry point | `OutgoingMessageWriter.sendVoiceMemo(at:duration:waveformLevels:replyToMessageId:)` |
| Waveform analyzer | `VoiceMemoWaveformAnalyzer` (`ConvosCore/Sources/ConvosCore/Messaging/`) |

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
    filename:       "voice_memo_1737945600_a1b2c3d4.m4a"
}
```

Encrypted payload is a standard XMTP `Attachment`:

```
Attachment {
    filename: "voice_memo_1737945600_a1b2c3d4.m4a"
    mimeType: "audio/m4a"
    data:     <AAC m4a bytes>
}
```

## Sender-only metadata (`StoredRemoteAttachment`, local DB only)

| Field | Sender populates | Receiver gets |
|---|---|---|
| `url`, `contentDigest`, `secret`, `salt`, `nonce` | yes | yes |
| `filename` | `voice_memo_<ts>_<uuid>.m4a` | yes |
| `mimeType` | `audio/m4a` | inferred from `.m4a` ⇒ `audio/x-m4a` (or `audio/m4a`, see UTType behaviour) |
| `mediaDuration` | yes (seconds, from recorder) | **lost** |
| `mediaWidth` / `mediaHeight` / `thumbnailDataBase64` | — | — |

### Local-only sidecar: waveform `[Float]`

Stored in the `AttachmentLocalState` SQLite table, column
`waveformLevels TEXT` — a JSON-encoded `[Float]` (typically 40 elements
normalized to 0…1). **This never leaves the device that wrote it.**

| Stage | What happens |
|---|---|
| Sender records | `VoiceMemoRecorder.audioLevels` accumulates from `AVAudioRecorder.averagePower(forChannel: 0)` (sampled ~60Hz) |
| Sender publishes | `OutgoingMessageWriter` calls `attachmentLocalStateWriter.saveWaveformLevels(levels, for: trackingKey)` — JSON-encodes `[Float]` into a SQLite TEXT column keyed by the local cache URL |
| Sender's row migration | After upload, `migrateKey(from: trackingKey, to: storedRemoteAttachmentJSON)` re-keys the row to the published `StoredRemoteAttachment.toJSON()` string |
| Sender renders | `MessagesRepository.hydrateAttachment` reads `localState?.waveformLevels`, JSON-decodes, sets `HydratedAttachment.waveformLevels` |
| Receiver renders | `localState?.waveformLevels` is `nil` (no row exists for them). `VoiceMemoBubble` shows `placeholderLevels` (40 zeros), then triggers `loadAndCacheAnalysis()` |
| Receiver computes | `VoiceMemoWaveformAnalyzer.analyze(from: <decrypted PCM>)` — buckets PCM into `sampleCount` (default 40) absolute-amplitude averages, normalized against a fixed reference (0.15). Result lives in the view's `analyzedLevels` state |
| Receiver persistence | **Not persisted** — the receive path never calls `saveWaveformLevels`. Each fresh bubble mount re-runs the analyzer (RemoteAttachmentLoader data cache makes it fast) |

The waveform shown on the receiver is therefore *similar to but not
bit-identical to* the sender's, because the sender's samples come from the
recorder's real-time average-power feed (~60 Hz, normalized to dB → linear),
while the receiver's come from offline absolute-amplitude bucketing of
decoded PCM. Both produce a `[Float]` of length 40 in the 0…1 range, but
the values won't match exactly.

## Receiver detection

`mimeType` is inferred from the `.m4a` extension via
`UTType(filenameExtension:)`. `HydratedAttachment.mediaType` returns
`.audio` for anything starting with `audio/`. The receiver cannot
distinguish a voice memo from a generic audio attachment at the protocol
level — the filename prefix `voice_memo_` is sender-side only.

## Producers and consumers

- **Sent by**
  - `OutgoingMessageWriter.sendVoiceMemo(...)` →
    `OutgoingMessageWriter.sendFileAttachment(...)` →
    standard XMTP `RemoteAttachmentCodec`.
  - Recording: `VoiceMemoRecorder` (`AVAudioRecorder`).
  - Local waveform persist: `AttachmentLocalStateWriter.saveWaveformLevels`.
- **Received by**
  - Dispatched in `DecodedMessage+DBRepresentation.swift` (`case ContentTypeRemoteAttachment:`).
  - Hydrated by `MessagesRepository.hydrateAttachment(...)`.
  - Waveform computed by `VoiceMemoWaveformAnalyzer.analyze(from:sampleCount:)`.
- **Rendered by** — `VoiceMemoBubble.swift`.

## Notes / gotchas

- **Waveform is not on the wire.** Any platform port must compute its own
  waveform from the downloaded audio if it wants to show one. Match the
  analyzer's parameters (40 buckets, abs-amplitude averaging, normalize to
  0…1 with a 0.15 reference) for visual parity with iOS.
- The receiver's `MediaType` is `.audio` not `voice_memo`. A user-uploaded
  generic `audio/mp3` file would render in the same bubble. If you need to
  distinguish, you'd have to introduce a marker — e.g. an MLS appData field
  or a sentinel in `filename`. Currently iOS does not.
- `mediaDuration` is sender-only. The receiver computes duration from the
  audio file itself during analyzer run, so the duration label appears only
  after download.
- The audio recording defaults (mono, 44.1 kHz, AAC LC) are iOS-side
  choices; port implementations may choose differently as long as the file
  remains decodable as MPEG‑4 audio and the mimeType / extension stay
  `audio/m4a` / `.m4a` for receiver discrimination.
- Background docs: `docs/plans/voice-memos.md` and
  `docs/plans/voice-memo-transcription.md` in the iOS repo.
