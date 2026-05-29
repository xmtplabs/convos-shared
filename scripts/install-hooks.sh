#!/usr/bin/env bash
# Point this clone at the tracked hooks under .githooks/.

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit

echo "Installed hooks: core.hooksPath=.githooks"
