#!/bin/bash
set -euo pipefail

# Only relevant for Claude Code on the web (Claude Code Remote) sessions, where each
# session starts from a fresh container without the user's git identity configured.
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

git config user.name "vinlmoe"
git config user.email "sebastien.lefebvre@vetbrain.fr"
