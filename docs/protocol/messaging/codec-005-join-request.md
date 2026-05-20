# Join Request (`convos.org/join_request:1.0`)

> **Codec #5** — first added 2026‑03‑05.

## Purpose

Sent by a joiner accepting a Convos invite. The signed invite token names a
host inbox; the joiner publishes a `join_request` DM to that host carrying
the invite slug, the joiner's display profile, and any opaque metadata the
invite flow chose to include. The host evaluates the request and either adds
the joiner to the underlying MLS group or replies with an
[`invite_join_error`](./codec-002-invite-join-error.md).

Background: `docs/adr/001-invite-system-architecture.md` and
`docs/plans/invite-system-single-inbox.md` in the iOS repo.

## Identity

| Field | Value |
|---|---|
| Authority ID | `convos.org` |
| Type ID | `join_request` |
| Version | `1.0` |
| Encoding | JSON (default `JSONEncoder` / `JSONDecoder`) |
| Fallback text | `inviteSlug` |
| `shouldPush` | `true` — the host needs to be notified so they can react |
| Defined in | `ConvosInvites/Sources/ConvosInvites/ContentTypes/JoinRequestCodec.swift` |

## Payload schema

| Field | Type | Required | Notes |
|---|---|---|---|
| `inviteSlug` | string | yes | The 10‑character random alphanumeric tag derived from the invite token. The host uses it to look up the target conversation. |
| `profile` | `JoinRequestProfile` | optional | Joiner's profile, included so the host UI can preview them before deciding. Nullable for backwards-compatibility with older joiners. |
| `metadata` | map<string, string> | optional | Free-form joiner-supplied metadata. The invite flow uses it for context-passing (e.g. UTM-style attribution); the host can ignore it. |

### `JoinRequestProfile`

| Field | Type | Notes |
|---|---|---|
| `name` | string (optional) | Display name. |
| `imageURL` | string (optional) | URL of the joiner's avatar — note this is *plaintext*; the encrypted-image flow used in [`profile_update`](./codec-003-profile-update.md) does not apply here because the joiner is not yet an MLS group member and has no shared secret with the host. |
| `memberKind` | string (optional) | Free-form member-kind string. iOS does not currently constrain the values; treat unknown values as "regular member" for forward compatibility. |

## Example encoding

```json
{
  "inviteSlug": "h2kfn39ax8",
  "profile": {
    "name": "Alice",
    "imageURL": "https://convos-cdn.example.com/img/alice.jpg",
    "memberKind": null
  },
  "metadata": {
    "source": "share-sheet"
  }
}
```

## Producers and consumers

- **Sent by** — invite acceptance flow in the joiner's client
  (`ConvosInvites` package).
- **Received by** — invites subsystem in the host's client. Handler logic
  lives in `ConvosInvites/Sources/ConvosInvites/`.

## Notes / gotchas

- `inviteSlug` is **not** secret on its own; the cryptographic binding is in
  the surrounding invite token. The slug is purely a routing key.
- Failures on the host side are returned as
  [`invite_join_error`](./codec-002-invite-join-error.md) messages keyed by the same
  `inviteSlug` (under the `inviteTag` field there).
- `imageURL` is **plaintext** by design — a joiner who hasn't been admitted
  yet has no shared key with the host.
