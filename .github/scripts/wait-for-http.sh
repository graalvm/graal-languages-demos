#!/usr/bin/env bash
set -euo pipefail

# Usage: wait-for-http.sh PID URL [TIMEOUT_SECONDS]
pid="${1:?Usage: wait-for-http.sh PID URL [TIMEOUT_SECONDS]}"
url="${2:?Usage: wait-for-http.sh PID URL [TIMEOUT_SECONDS]}"
timeout_seconds="${3:-120}"

if timeout "$timeout_seconds" bash -c '
  while kill -0 "$1" 2>/dev/null; do
    if curl --fail-with-body --silent --max-time 5 --dump-header - -o /dev/null "$2"; then
      exit 0
    fi
    sleep 2
  done
  echo "Application (PID $1) exited before $2 became ready" >&2
  exit 1
' _ "$pid" "$url"; then
  exit 0
else
  status=$?
  if [ "$status" -eq 124 ]; then
    echo "Timed out after ${timeout_seconds}s waiting for $url (PID $pid)" >&2
  fi
  exit "$status"
fi
