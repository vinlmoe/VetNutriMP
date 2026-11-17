#!/usr/bin/env bash
set -euo pipefail
module_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
repo_root="${1:-${VETNUTRI_MP_ROOT:-$(dirname "$module_root")}}"
cd -- "$repo_root"
exec ./gradlew --init-script "$module_root/tools/kotlin-parity.init.gradle" \
  "-Dvetnutri.dog.module=$module_root" \
  :composeApp:desktopTest --tests fr.vetbrain.vetnutri_mp.Interop.InitRParityTest
