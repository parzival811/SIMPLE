#!/usr/bin/env bash

set -euo pipefail

if command -v javac >/dev/null 2>&1; then
  JAVA_CMD="$(dirname "$(readlink -f "$(command -v javac)")")/java"
  if [[ -x "$JAVA_CMD" ]]; then
    printf '%s\n' "$JAVA_CMD"
    exit 0
  fi
fi

printf '%s\n' "${JAVA:-java}"
