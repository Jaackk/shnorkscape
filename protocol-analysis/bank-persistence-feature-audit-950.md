# Bank feature persistence audit — revision 950

Date: 2026-09-12. Read-only source audit; no account files, server processes or runtime state were changed. The accompanying bank controls/transfer implementation is proceeding separately. This document describes the persistence boundary as inspected, not a claim that every visible 950 control has been verified.

## Finding

The original bank framework contains tabs, placeholders, preferences and preset concepts worth retaining. The native 950 character format currently persists only one compact bank inventory of item IDs and positive quantities. Unlocking legacy controls cannot make those richer features durable. Some original handlers would mutate the real containers into states that native snapshots explicitly reject.

Scalar preferences can use the existing settings section without changing its byte format, provided live capture and restore are implemented. Tabs, placeholders and presets require an explicit bank-state extension and corresponding transaction rules.

## What can persist with the current format

| Feature | Existing 950 storage | Additional work required |
| --- | --- | --- |
| Ordinary bank contents and quantities | Yes: at most 600 unique item IDs, each with positive quantity; order is stored | Continue current-cache validation and staged transfers |
| Withdraw/deposit 1, 5, 10, X, all | Resulting item quantities persist | Last-X and selected default amount do not currently persist; add scalar preference binding |
| Notes and unnoted items | IDs and quantities are representable | Resolve current-cache note links and conserve quantity; the note-mode preference needs binding |
| Deposit equipped items | Backpack, bank and equipment already persist | Use the native equipment/bank transaction and restrictions; no new storage shape needed |
| Search/filter display | It can remain session/client state | Preserve authoritative source-slot identity; do not feed a filtered display index directly to a transfer |
| Reorder within the one compact bank | Existing array order is representable | Provide a staged reorder, refresh visible/source mapping and invalidate stale actions |
| Selected quantity, last-X, note mode, layout preference | Integer settings format can represent them | Define bounded keys and explicit packet-free capture/restore; currently absent |
| Multiple named/icon tabs and membership | Not represented | New bank layout state; UI mapping and migration |
| Empty placeholders | Not represented; zero quantities are rejected | Separate placeholder records and capacity/refill/delete semantics |
| Inventory/equipment presets | Not represented | Bounded saved templates and a native plan/validate/commit loader |
| Beast-of-burden preset/contents | Not represented by the native save | Familiar/container lifecycle and persistence must precede a real BoB preset load |
| Item instance attributes, charges, invention data | Not represented and explicitly rejected | An item-instance schema and identity model; do not silently flatten to ID/quantity |

A visible setting and its stored behavior are different concerns. Saving a placeholder preference before placeholders are implemented does not justify enabling placeholder withdrawals.

## Current source evidence

### Character format and settings

`Ataraxia950/game/com/rs/game/player/client/Native950Save.java` uses schema 3, client revision 950. Its bank payload is `bankIds[]` / `bankAmounts[]`; construction rejects more than 600 entries, nonpositive amounts and duplicate bank IDs (lines 194–220). There are no tab, placeholder or preset fields. BANK dirty detection compares those two arrays only.

The SETTINGS map permits at most 32 integer keys with bounded names. `Player.nativeSettingsSnapshot()` / `applyNativeSettings()` currently bind exactly four: `chatEffects`, `profanityFilter`, `mouseButtons`, `acceptAid` (`Player.java`, around line 1891). Capture creates a fresh map; restore ignores unknown keys. Therefore simply adding a bank key to a save object or decoder would not round-trip it through a live session.

`Native950PlayerBinder.capture()` and `restore()` use these Player methods. A bank preference extension needs explicit live bindings, validated values/defaults, and packet-free hydration; send the verified 950 client variables once transport/UI state is ready.

### Bank container invariants

`Native950Containers.restore()` creates `bank.bankTabs = new Item[][] {nextBank}`. `saveSnapshot()` and `bankSnapshot()` read only `bankTabs[0]`. `validateState()` rejects any shape with more than one tab, null bank slots, nonpositive quantities, charges, attributes or invention data (around lines 506–533). Copies contain only `new Item(id, amount)`.

Current transfer plans operate against that compact array. Removing an item can shift later physical slots. `Native950Interactions` pins quantity prompts to bank instance/epoch/slot/item and tracks changed slots. Tabs, filtering and sorting must preserve those protections when translating displayed positions back to stored entries.

The current native bank adapter already treats ordinary note conversion and equipment deposits as transfers over these containers. Neither operation inherently needs a new character format. Their mode/preferences are separate from the persisted item result.

### Reader/writer compatibility

`Native950SaveStore.java` reads schemas 1–3 at revision 950 only. Schema 3 requires exactly five tagged sections in a fixed order: skills, vitals, settings, appearance, identity. Unknown sections and trailing bytes fail. Do not append a bank section while continuing to call the payload schema 3.

The writer hashes the payload, validates the existing profile before replacement, writes a same-directory temporary file, forces its contents and uses atomic replacement. Preserve these protections. The existing `.pre29.bak` mechanism covers a specific skill migration; it is not a general bank-schema migration backup.

The full file is capped at 16,384 bytes. Any new section needs an explicit bounded encoding and total-size calculation; ten raw 28-slot inventory plus 19-slot equipment preset pairs alone require 3,760 bytes for IDs/amounts, before flags, labels, layout, section headers or future instance attributes.

## Original 910 model and why direct handlers are unsafe here

Reference: `C:/Users/developer/Desktop/Ataraxia-PS/game/com/rs/game/player/Bank.java`.

- `Bank` implements `Serializable`; it stores `Item[][] bankTabs`, `TabDetails[]`, ten `BankPreset` slots and a `BobPreset`. Old Player serialization naturally includes these nontransient fields. Native950Save does not serialize the Bank object.
- `createTab()` permits up to 15 total tabs. `TabDetails` stores `originalIndex`, `nameIndex`, `iconIndex` (around line 1736). Legacy tab move/collapse/reorder operations mutate the arrays directly. They immediately violate the native one-tab invariant unless the native model is deliberately extended.
- Legacy placeholders are actual `Item(id, 0, charges)` values with copied attributes (`removeItem`, around line 802). Native item/save validation rejects zero quantity. Relaxing that validator alone would leave transfers, capacity calculations, compact snapshots and persistence ambiguous.
- Legacy item lookup can distinguish attributes; native bank persistence requires a single entry per numeric ID. Importing charged/attributed legacy variants without a new identity model would merge or lose distinctions.
- Last-X, selected quantity, current tab/filter, placeholder/layout flags and preset data are nontransient legacy fields. Withdraw-as-notes, insert mode, search state and selected preset are transient. Legacy opening also resets some modes. Choose intended reopen/relogin behavior explicitly instead of treating every legacy field as a persisted preference.
- The legacy search methods are largely commented out; routing a button to those methods does not supply a complete search implementation.

`BankPreset` (around line 2716) contains a name-label index, inventory[28], equipment[BodyDefinitions size], and include-inventory/equipment/BoB flags. `BobPreset` contains pouch ID, items[32] and remember-items. These are desired-layout templates, not extra owned items. The separate custom `content/.../presets/Preset.java` also stores spellbook/prayer/pouch choices; it is a different feature and should not be confused with the bank UI's ten presets.

Legacy `loadPreset()` (around line 2469) deposits and clears existing slots in sequence, then withdraws targets and directly installs equipment. It can report missing items after earlier mutations. It creates placeholders, copies old charges/attributes, uses old equipment handling and refresh IDs, and does not use the new generic 950 equipment transaction. Copying this method would bypass current slot/conflict/requirement/removal/controller/stale-state rules. A late rejection would leave earlier changes applied.

## Proposed compatible next phase

1. **Finish scalar preferences on schema 3.** Add named integer keys such as bank last-X, quantity mode and note mode through a small explicit bank preference snapshot/restore boundary. Validate enums and quantities, give missing keys safe defaults, retain existing unrelated settings, and test close/reopen and save/reload separately. Keep search text and transient prompts session-local. Do not reuse a legacy magic quantity sentinel as an undocumented persistent enum.
2. **Add a versioned BankState model.** Keep the current positive-quantity holdings authoritative. Add ordered tab descriptors and layout references to those holdings, plus explicit placeholder-only records. For today's ordinary items, numeric item ID is a sufficient unique holding key; design a replaceable key boundary for future attributed instances. A placeholder is metadata with zero owned quantity, not an ordinary inventory Item. Validate each positive holding appears exactly once in the layout, every reference resolves, tabs/entries are bounded, and placeholder keys cannot masquerade as owned stacks. Preserve a single authoritative quantity count.
3. **Use schema 4 for structural bank data.** Retain the exact schema 1–3 reader branches. On older saves, synthesize one default tab in the stored array order, no placeholders, and empty presets; preserve IDs, quantities, skills, equipment and identity exactly. Add bounded bank/preset sections, capture/restore and dirty detection together. Before the first upgraded write, retain a recoverable original profile. Do not reset unreadable data or silently import revision-910/947 serialized profiles.
4. **Render tabs from the new layout.** Prefer a native layout layer rather than allowing legacy `bankTabs` mutation while native code still assumes one array. Generate a view-to-entry mapping with a version/epoch for every reorder/filter/tab change; validate current identity again when an action arrives. Confirm current950 tab/icon/name enums and capacity behavior independently of the 910 constants.
5. **Load presets as one staged transfer plan.** Store bounded ordinary ID/quantity templates, flags and current950-valid label identifiers. Resolve every desired item against current metadata, validate equipment slots, stackability, conflicts, base requirements, restrictions and all participating containers before commit. Recheck identities after controller callbacks. A missing-item result should either leave all containers unchanged or use a deliberately specified partial plan that is itself validated and committed atomically. Preserve total item quantities across bank/backpack/equipment; preset templates never contribute owned quantity. Defer BoB and instance-attribute presets until those states have explicit native support.

Migration verification should cover older save fixtures, maximum-sized banks/layouts/presets, corrupt/truncated/unknown sections, reordered/filtered stale actions, placeholder refill/removal, full capacity and overflow, preset missing items and equipment conflicts, callback-induced changes, and conservation through save/reload. Use isolated fixtures; no automatic changes to live account files are necessary for this audit.