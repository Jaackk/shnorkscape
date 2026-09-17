# Active950 protocol subset
Six native-reviewed server mappings are installed: IF_OPENTOP1,IF_OPENSUB100,IF_SETHIDE67,RUNCLIENTSCRIPT35,SERVER_TICK_END160,NO_TIMEOUT183. Both direction length tables are from the actual950client. The remaining copied field files are inactive because their names are not mapped. Unmapped client packets are framed and logged, without947handler guesses. See950-LOBBY-IMPLEMENTATION.md at the test root for layouts, validation and live-test status.

World-list support added: client108 WORLDLIST_FETCH (big-endian checksum), client104 NO_TIMEOUT, server129 WORLDLIST_FETCH_REPLY. Lobby rendering has been confirmed by the user; world entry remains under test.
