# Typing Indicator (`convos.org/typing_indicator:1.0`)

> **Codec #7** — first added 2026‑04‑03.

## Purpose

Carries ephemeral "user is typing" presence as an XMTP group message. Sent
when a member starts composing and again with `isTyping = false` when they
stop or send. Receivers track the state per conversation with a short
client-side expiry so a missed "stopped" frame doesn't leave the bubble
animating forever.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `typing_indicator` |
| Version | `1.0` |
| Encoding | JSON (default `JSONEncoder` / `JSONDecoder`) |
| Fallback text | `nil` — never surfaced to clients that don't know the codec |
| `shouldPush` | `false` — typing presence must not generate notifications |
| Defined in | `ConvosCore/Sources/ConvosCore/Custom Content Types/TypingIndicatorCodec.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `isTyping` | bool | yes | `true` while the user is actively composing, `false` when they stop, send, or background the app. |

## Example encoding

```json
{ "isTyping": true }
```

## Producers and consumers

- **Sent by**
  - `ConvosCore/Sources/ConvosCore/Messaging/MessagingService.swift` —
    `sendTypingIndicator(isTyping:for:)` delegates to the underlying
    XMTP message sender.
- **Received by**
  - `ConvosCore/Sources/ConvosCore/Storage/Writers/ConversationWriter.swift`
    filters incoming messages by content type and forwards them to
    `TypingIndicatorManager`.
  - `ConvosCore/Sources/ConvosCore/Messaging/TypingIndicatorManager.swift`
    holds per-conversation state with a ~15‑second expiry safety net.
- **Stored as** — not persisted to the message DB; treated as transient
  presence only.

## Notes / gotchas

- This codec is intentionally noisy on the wire (one message per state edge).
  Push must stay disabled to keep these messages cheap.
- A receiver should still expire the indicator after a few seconds even if no
  "stopped" frame arrives — senders may crash or disconnect mid-composition.
- Background docs: `docs/plans/typing-indicators.md` in the iOS repo.
