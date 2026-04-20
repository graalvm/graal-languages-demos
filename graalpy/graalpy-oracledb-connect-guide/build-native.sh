#!/usr/bin/env bash

set -euo pipefail

GRAALPY="${GRAALPY:-graalpy}"
VENV_DIR="${VENV_DIR:-target/venv}"
OUTPUT="${OUTPUT:-target/standalone-app}"

mkdir -p "$(dirname "${OUTPUT}")"

"${GRAALPY}" -m venv "${VENV_DIR}"
"${VENV_DIR}/bin/graalpy" -m pip install -r requirements.txt

"${GRAALPY}" -m standalone native \
  --module app.py \
  --output "${OUTPUT}" \
  --venv "${VENV_DIR}"
