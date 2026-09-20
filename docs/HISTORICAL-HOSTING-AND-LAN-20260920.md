# Historical hosting and LAN findings

Read-only local evidence; no firewall, router, database or other project's files
were modified. Private endpoint addresses and raw player records are not included.

## Darkan

Local project: `Documents/RSPS/Darkan Server/world-server` and `client-dev`.
The configured MongoDB database `darkan-server` on local port27017 contains
`players.username = nooby`. Verified with a read-only, username-only projection;
the game DB manager was not instantiated (its initialization creates indexes).
This establishes an actual saved account, not just a name in source comments.

Current `data/worldConfig.json`: world11, game port43595, activity Shnorkville.
`client-dev/.../Loader.java`: build727.1, default world11, lobby/JS5 fallback
port43595; world descriptors can override the port. `docker-compose.yml` exposes
43595 and43596 for the world service, and27017 for MongoDB. The latter is an old
deployment declaration, NOT a recommendation to expose MongoDB on the LAN.

The inspected files do not prove which router rules were actually installed.
Ports in configuration must not be reported as confirmed forwarded ports.

## EOC/Vernox

`Documents/RSPS/Vernox830/Vernox RS3 - Trikru/Vernox Source/data/logs/commands/admin/nooby.txt`
contains dated in-game command activity on 12 April2026, corroborating Nooby's
use of this EOC project. Backup copies exist. The separate Matrix project also
contains a nooby character file; that alone does not identify the EOC session.

Vernox current configuration: game bind0.0.0.0 base port43593; the client MODS
socket path adds the world ID, giving43594 for world1. Local login-server port
43599, login-client service53595, website-client port43598. Client host routing
supports `vernox.host`, `vernox.webHost`, and `vernox.worldHost` system properties.
The current default is loopback, not proof of the historical remote endpoint.

No reviewed evidence conclusively links the remembered forced safe-mode graphics
to this particular copy. Treat that detail, and historical router forwarding,
as unresolved rather than inferred from Nooby's presence.

## Current SHNORKSCAPE boundary

HTTP/JS5:127.0.0.2:8950, HTTP alias80; lobby/world transport:127.0.0.2:43650.
Normal client configuration contains loopback content/world routes as well as
the initial jav_config endpoint. Merely changing the first URL is insufficient.

More importantly, `AuthoritativeLoginProcessor.process` unconditionally returns
SUCCESS. Account/session isolation after login does not authenticate a username.
Changing the bind to0.0.0.0 would expose Jaxa and development privileges to anyone
who could reach it. No such exposure was enabled.

A safe LAN implementation needs an explicit private-interface listener, remote
account authentication/invitations before lobby/game admission, per-connection
advertised routes (so local Play and Jaxa capture remain unchanged), and a friend
launcher with fresh isolated local state. Only necessary service ports should be
allowed on the Windows Private profile/local subnet. Workspace enrollment must
remain Jaxa-only. LAN connectivity is NOT implemented or live verified by this
checkpoint; there are no valid friend-join instructions yet.
