# Profile Snapshot (`convos.org/profile_snapshot:1.0`)

> **Codec #4** — first added 2026‑03‑05 (same commit as [Profile Update](./codec-003-profile-update.md)).

## Purpose

A bulk catch-up of every current member's profile, sent by the member who
adds new members to a group. MLS forward secrecy means new joiners can't read
historical messages, so without this they'd see a group full of anonymous
inbox IDs. The snapshot fills that gap: the inviter packages everyone's
latest profile into a single message that the new member can decrypt.

The same precedence rule applies on the receive side: a member's own most
recent `ProfileUpdate` always wins over their slot in any `ProfileSnapshot`.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `profile_snapshot` |
| Version | `1.0` |
| Encoding | **Binary Protocol Buffers** (SwiftProtobuf) |
| Fallback text | `nil` |
| `shouldPush` | `false` |
| Defined in | `ConvosCore/Sources/ConvosCore/Profiles/ProfileMessages/ProfileSnapshotCodec.swift` |
| Schema | `ConvosCore/Sources/ConvosCore/Profiles/Proto/profile_messages.proto` |

## Payload schema

```protobuf
message ProfileSnapshot {
    repeated MemberProfile profiles = 1;
}

message MemberProfile {
    bytes  inbox_id        = 1;  // XMTP inbox ID as hex-decoded bytes (32 bytes)
    optional string name             = 2;
    optional EncryptedProfileImageRef encrypted_image = 3;
    MemberKind member_kind  = 4;
    map<string, MetadataValue> metadata = 5;
}
```

`MemberKind`, `MetadataValue`, and `EncryptedProfileImageRef` are the same
messages used by [`ProfileUpdate`](./codec-003-profile-update.md).

| Field | Proto # | Type | Notes |
|---|---|---|---|
| `profiles` | 1 | repeated `MemberProfile` | One entry per *current* group member, including the sender. |

### `MemberProfile`

| Field | Proto # | Type | Notes |
|---|---|---|---|
| `inbox_id` | 1 | bytes | XMTP inbox ID, **as hex-decoded bytes (32 bytes)**, not the hex string. |
| `name` | 2 | optional string | Display name. |
| `encrypted_image` | 3 | optional `EncryptedProfileImageRef` | See [profile-update.md](./codec-003-profile-update.md#encryptedprofileimageref). |
| `member_kind` | 4 | `MemberKind` | Defaults to `MEMBER_KIND_UNSPECIFIED`. |
| `metadata` | 5 | `map<string, MetadataValue>` | Same shape as `ProfileUpdate.metadata`. |

## Example encoding

Protobuf text-format (illustrative — wire is binary):

```textproto
profiles: {
  inbox_id: "\x9a\x1c...32 bytes..."
  name: "Alice"
  member_kind: MEMBER_KIND_UNSPECIFIED
}
profiles: {
  inbox_id: "\x44\x7e...32 bytes..."
  name: "Helper Bot"
  member_kind: MEMBER_KIND_AGENT
  metadata: {
    key: "model"
    value: { string_value: "claude-opus-4.7" }
  }
}
```

## Producers and consumers

- **Sent by** — the inviter, immediately after adding new members to the
  group. The snapshot is composed from local state using the same precedence
  rule as receivers apply: latest `ProfileUpdate` per member, falling back to
  the most recent `ProfileSnapshot` that contains them.
- **Received by** — codec decodes; downstream profile pipeline writes each
  entry into the local profile store, again under the precedence rule.
- **Stored as** — not stored as a transcript message; consumed for its
  side-effect on member profile state.

## Notes / gotchas

- `inbox_id` is **bytes**, not a hex string. Implementers porting this format
  must hex-decode their inbox IDs before encoding and hex-encode after decode
  if they want to compare against XMTP string-form IDs.
- A snapshot should always include the sender's profile so a new joiner has
  it without waiting for the next `ProfileUpdate`.
- Per ADR‑005, snapshots should be omitted entirely when no new members are
  being added — there's no benefit to broadcasting profiles every member
  already has.
