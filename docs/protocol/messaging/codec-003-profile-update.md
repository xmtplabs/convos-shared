# Profile Update (`convos.org/profile_update:1.0`)

> **Codec #3** — first added 2026‑03‑05 (same commit as [Profile Snapshot](./codec-004-profile-snapshot.md)).

## Purpose

A member's authoritative announcement of their own profile (display name,
avatar, kind, metadata). Sent as a regular MLS group message so it lives in
the transcript and is delivered to every group member, including those who
were offline.

The sender's identity is taken from the XMTP envelope (`senderInboxId`), not
from a field in the payload — a member can only publish their own profile,
which prevents spoofing without an explicit signature.

Sending a `ProfileUpdate` with neither a `name` nor an `encrypted_image`
clears the profile.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `profile_update` |
| Version | `1.0` |
| Encoding | **Binary Protocol Buffers** (SwiftProtobuf) |
| Fallback text | `nil` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Profiles/ProfileMessages/ProfileUpdateCodec.swift` |
| Schema | `ConvosCore/Sources/ConvosCore/Profiles/Proto/profile_messages.proto` |

## Payload schema

```protobuf
message ProfileUpdate {
    optional string name = 1;
    optional EncryptedProfileImageRef encrypted_image = 2;
    MemberKind member_kind = 3;
    map<string, MetadataValue> metadata = 4;
}

enum MemberKind {
    MEMBER_KIND_UNSPECIFIED = 0;  // legacy clients / regular members
    MEMBER_KIND_AGENT = 1;
}

message MetadataValue {
    oneof value {
        string string_value = 1;
        double number_value = 2;
        bool   bool_value   = 3;
    }
}

message EncryptedProfileImageRef {
    string url   = 1;  // URL to encrypted ciphertext (e.g. S3)
    bytes  salt  = 2;  // 32-byte HKDF salt for key derivation
    bytes  nonce = 3;  // 12-byte AES-GCM nonce
}
```

| Field | Proto # | Type | Notes |
|---|---|---|---|
| `name` | 1 | optional string | Display name. Omit (or send `ProfileUpdate` with no name and no image) to clear it. |
| `encrypted_image` | 2 | optional `EncryptedProfileImageRef` | See encryption notes below. |
| `member_kind` | 3 | `MemberKind` enum | Defaults to `MEMBER_KIND_UNSPECIFIED` (i.e. regular human member). Clients that don't set this are treated as regular members for backward compatibility. |
| `metadata` | 4 | `map<string, MetadataValue>` | Free-form per-member metadata. Values are typed via the `MetadataValue` oneof (`string_value`, `number_value`, `bool_value`). |

### `EncryptedProfileImageRef`

The avatar URL points at AES‑256‑GCM ciphertext. The per-member symmetric key
is derived via HKDF using the `salt` field; the `nonce` is the AES‑GCM IV.
The HKDF input keying material is the conversation's group secret, so only
members of the same MLS group can decrypt. See `docs/adr/005-member-profile-system.md`
and `docs/adr/009-encrypted-conversation-images.md` in the iOS repo for the
full crypto rationale.

| Field | Proto # | Type | Length / Notes |
|---|---|---|---|
| `url` | 1 | string | HTTPS URL to the encrypted blob. |
| `salt` | 2 | bytes | 32 bytes. HKDF salt. |
| `nonce` | 3 | bytes | 12 bytes. AES‑GCM IV. |

## Example encoding

Protobuf text-format (illustrative — wire is binary):

```textproto
name: "Alice"
encrypted_image: {
  url:   "https://convos-cdn.example.com/img/abcd1234"
  salt:  "\x83\xa1...32 bytes total..."
  nonce: "\x07\x2e...12 bytes total..."
}
member_kind: MEMBER_KIND_UNSPECIFIED
metadata: {
  key: "pronouns"
  value: { string_value: "they/them" }
}
```

## Producers and consumers

- **Sent by** — profile-edit flow (settings screen). The local profile writer
  enqueues a `ProfileUpdate` into the affected groups.
- **Received by** — codec decodes; downstream the contacts pipeline mirrors
  the encrypted image material (`avatarSalt`, `avatarNonce`, `avatarKey`) into
  the local `contact` row. See `contacts-changes.md` in the iOS repo.
- **Stored as** — not stored as a transcript message; consumed for its
  side-effect on member profile state.

## Notes / gotchas

- `member_kind = MEMBER_KIND_UNSPECIFIED` is the wire default for legacy
  clients. Decoders must treat unspecified as "regular member", not as an
  error.
- A `ProfileUpdate` with neither `name` nor `encrypted_image` (and no
  metadata) is interpreted as a profile *clear* — not a no-op.
- Per ADR‑005, the precedence rule for assembling member profiles from
  history is: most-recent `ProfileUpdate` from that member, falling back to
  the most-recent `ProfileSnapshot` containing them.
