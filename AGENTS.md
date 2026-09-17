# Required pre-edit backup

The user requires a successful GitHub backup BEFORE every task that edits any
files in this project. This includes code, configuration, scripts and docs.

1. Before any edits, inspect `git status --short` and the proposed changes for
   secrets or private data. Never discard someone else's work.
2. Run `./Backup-BeforeEdit.ps1 -Task 'short task description'` from this folder.
   It commits outstanding trackable changes, pushes the current branch to
   https://github.com/Jaackk/shnorkscape and verifies the remote commit.
   It also snapshots character saves and deployed engine/override files locally.
3. If the script fails, STOP before editing. Explain the failure; do not silently
   substitute a local-only Git commit. An already-clean, pushed revision needs
   no duplicate commit, but still run the check and local snapshot.
4. GitHub is a SOURCE backup, not a full runnable installation. Cache, client
   assets, JDK/compiler/dependency distributions, generated output, logs, private
   saves and local backups are excluded. Before changing any excluded file,
   separately copy that file/directory into the timestamped local backup first.
   Never upload personal credentials or character saves to GitHub.
5. After verification, commit and push completed task changes too, reporting any
   failure honestly. Never force-push or rewrite existing published history.

Protect Jaxa's character. Do not alter unrelated games or their worlds. This rule
is agent guidance, not an OS-level lock against manual or external edits.
