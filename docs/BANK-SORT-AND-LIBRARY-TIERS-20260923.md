# Bank sorting and equipment library ordering

The existing bank plus control is517:250. Its static Select operation has no
cache-authored click hook; CS13830 reconstructs the tooltip and enabled operation
as bank capacity updates. The staged minimal CS13830 patch preserves the capacity
display, binds native tooltip8799 to "Click to sort this tab", and keeps the plus
operation available at full capacity. It does not open the store or buy space.

The server handles the click within existing bank reach/session checks. It
cancels pending quantity input, advances the bank epoch, sorts the current bank
array, and publishes an authoritative refresh. All source slots are considered
changed for the rest of that tick, including same-ID items with distinct state.
Sort order is combat style, descending cache tier, equipment family/set, armour
slot or weapon hand, then ammunition/runes, supplies, utility and other items.
Unknown definitions are retained at the end. Stable sorting moves exact Item
references: no stack merging, recreation, charge changes or item deletion.
Other stored tab arrays remain untouched. This does not add bank-tab creation or
change the existing native bank storage model.

Library gear pages preserve their curated membership and complete groups, then
order equipment by descending cache tier. Resource stacks follow equipment.
Best is expanded with equipment and supplies from the four validated strongest
style loadouts, deduplicated by item ID. The shared plus in the library explains
that its catalogue is already ordered; it cannot mutate real bank property.
Global search, infinite stock, native presets and the bank-focus fix are retained.

The update is staged for the user's Apply Staged Update.cmd workflow. No live
server restart, save edit or live cache replacement is performed. Exact native
appearance/hover and sorting in the live client remain a final user check.

Catalogue audit: all600 previous category/item pairs retained; Best40 ->83;
643 total entries. No other tab membership changes. Tiers are actual cache
metadata (including the T100 Masterwork bow), never capped or guessed by name.
New base variants equivalent to existing dyed/augmented favourites are skipped.
The native script patch decodes/re-encodes with unchanged original capacity
instructions0..254 and the original branch destinations. No interface asset or
custom visual replacement is introduced.

Pre-edit backup: dd31f25f5970185bd7b621d4b92bcd84ee03c9f2, with local protected
snapshot backups/pre-edit-20260923-035634-105.

Verified: 1,482 JUnit tests, zero failures/errors, two skipped. Real-cache bank
acceptance passed149 encrypted actions/97 ticks/7,057 frames, including sorting,
charged-object preservation, stale-slot/X rejection and24 bank-focus close cycles.
EquipmentLibrary and LibraryFollowup acceptance passed. A read-only actual-cache
loader probe decoded the staged CS13830/reference and original plus component.
Staged engine SHA256: 83E04F6C1E8CB3275D5CF32AE3B76363B185F8C8004CC956E46A073E2A21CAC2.
