# September12 UI polish

The user confirmed the skill guide's content and lodestone map-area loading work. The three remaining presentation issues were corrected in this build and subsequently confirmed working by the user.

## Skill selection

Native950SkillGuide previously repeated Hero layout scripts8288/8193/8283 and reopened1217 for every1218 icon click. The cache's onOp5682 already selects the skill and clears rows. The server now sends only5690 to refill them while keeping1448/1218/1217 mounted. A skill selected from1466 while the guide is open needs5682 followed by5690 because that source has not run the guide icon's local hook. Categories, sorting and close/reopen behavior remain intact.

## First-open lodestone locks

Unlock values were correct, but arrived beside IF_OPENSUB. The950 client applies received varbits to a separate script-visible map later; onLoad6001 could therefore choose locked sprites before the values became visible. Bootstrap now verifies the paired cache and publishes the existing local-exploration unlock presentation during login, before arming Home Teleport. This does not alter server quest state. Full evidence: [native and cache audit](lodestone-first-open-950.md).

## Clipped lodestone frame

Actual1092 roots are576x360; generic host1477:735 is512x334 after8391's default layout. Wrapper732 is fixed512x352. Both are clipping ancestors, while parents722 and27 fill the viewport. The paired950 sizing helper11145 takes(width,height,widthMode,heightMode,component), performs only IF_SETSIZE and returns. Open now sizes wrapper732 to576x376 and host735 to576x360 before attaching1092, preserving the16px wrapper padding and centered position modes. Close restores wrapper512x352 and calls8389 to apply the native host-sizing policy. Modern CS1364 and1092 onLoad do not undo these sizes in the normal open path.

CS11145 SHA256:3785303ee6b2c09c260e718b853c8ef7773bc080d07d0dc3d4a6d150b0350575. tools/verify_950_lodestones.py independently checks headers, dimensions and the exact five-argument program, and records138 strict bindings including sizing/restoration scripts in the runtime data.

## Validation

1013 tests passed,2 skipped, zero failures. Packaged guide acceptance passed1406 packets including all29 internal and external selections. Packaged lodestone acceptance passed31 journeys,108 encrypted actions,872 ticks and5415 frames, with bootstrap/unlock and sizing-before-mount assertions.17 launcher checks passed. See [deployment record](validation-ui-polish-2026-09-12.json).

Live acceptance on September12: after the requested visual retest, the user confirmed, "yeah everything is working well now." All three presentation fixes are accepted.
