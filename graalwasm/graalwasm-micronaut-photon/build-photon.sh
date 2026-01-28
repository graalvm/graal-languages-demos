#!/usr/bin/env bash
#
# Copyright (c) 2025, 2026, Oracle and/or its affiliates.
#
# Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
#

set -o errexit
set -o nounset

if [[ $# -eq 1 && ("$1" == "--release" || "$1" == "--dev") ]]; then
    PROFILE="$1"
else
    echo "Usage: $0 [--release|--dev]"
    exit 1
fi

PHOTON_COMMIT="e4ef13d602828b171e04bf232741d63621dfec14"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function ensure_command() {
    local cmd=$1
    if ! command -v "${cmd}" > /dev/null; then
        cat <<EOF
${cmd} not found.

Please install '${cmd}' on your system and restart.
EOF
        fail ""
    fi
}

ensure_command "curl"
ensure_command "unzip"
ensure_command "wasm-pack"

echo "Building Photon with '${PROFILE}' from source..."

mkdir -p target/photon
pushd target/photon > /dev/null

if [[ ! -f photon-src.zip ]]; then
    curl -sL -o photon-src.zip "https://github.com/silvia-odwyer/photon/archive/${PHOTON_COMMIT}.zip"
fi
if [[ ! -d "photon-${PHOTON_COMMIT}" ]]; then
    unzip -q photon-src.zip
fi
pushd "photon-${PHOTON_COMMIT}" > /dev/null

if [[ "${PROFILE}" = "--dev" ]]; then
    if ! grep -Fxq "dwarf-debug-info = true" crate/Cargo.toml; then
      echo "Enabling DWARF debug info..."
      cat >> crate/Cargo.toml << EOF

[package.metadata.wasm-pack.profile.dev.wasm-bindgen]
dwarf-debug-info = true

EOF
    fi
fi

wasm-pack build ${PROFILE} --target bundler --out-name photon --out-dir "${SCRIPT_DIR}"/target/classes/photon ./crate

echo "Copying example image..."

cp crate/examples/input_images/daisies_fuji.jpg "${SCRIPT_DIR}"/target/classes

popd > /dev/null
popd > /dev/null
