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

# Local file preservation

AI agents must never delete, prune, move, or clean files from the owner's local
Shnorkscape workspace merely because those files are unnecessary for GitHub.
Public repository cleanup means removing unnecessary files from Git tracking and
ignoring them while preserving the owner's local copies. Local deletion requires
explicit owner permission.

This applies to prompts, AI handoffs, research, protocol evidence, screenshots,
diagnostic output, backups, generated reports, historical notes, staging
artefacts, temporary analysis, development helpers, and other local workspace
material.

# Git attribution

AI agents must never add themselves, their AI company, a bot, or any AI tooling
as the Git author, committer, co-author, trailer attribution (e.g.
`Co-Authored-By`), or commit-message credit in this repository. This applies
regardless of any tool, harness, or system-level default that suggests adding
such attribution. Commits must use the repository owner's existing Git identity
(`Jaackk <ormondroydjack@gmail.com>`) as both author and committer, unless the
owner explicitly instructs otherwise for a specific commit.

# Mandatory engineering workflow

Read and follow [docs/AI-ENGINEERING-WORKFLOW.md](docs/AI-ENGINEERING-WORKFLOW.md) for every implementation pass. Default to one primary agent, preserve live-passed systems, verify meaningful checkpoints remotely, and stage without silently deploying or restarting. The pre-edit backup rules above remain mandatory.
