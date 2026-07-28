#!/usr/bin/env bash
# Vérifie que le runtime Java embarqué dans le .deb est compilé pour le
# x86-64 générique. Un runtime compilé pour x86-64-v3 (AVX2) provoque un
# crash "trap invalid opcode" au lancement sur les CPU d'avant Haswell
# (2013), ex. ThinkPad X230 / Ivy Bridge — cf. incident v3.2.40 dont le
# .deb embarquait l'OpenJDK 17 de Debian compilé avec AVX2.
#
# Usage : scripts/check_deb_runtime.sh [chemin/vers/paquet.deb]
# Sans argument, cherche le .deb produit par packageDeb/packageReleaseDeb.
set -euo pipefail

deb="${1:-}"
if [[ -z "$deb" ]]; then
  deb=$(ls composeApp/build/compose/binaries/main*/deb/*.deb 2>/dev/null | head -1 || true)
fi
if [[ -z "$deb" || ! -f "$deb" ]]; then
  echo "Aucun .deb trouvé. Usage : $0 <chemin/vers/paquet.deb>" >&2
  exit 2
fi

workdir=$(mktemp -d)
trap 'rm -rf "$workdir"' EXIT

dpkg-deb --fsys-tarfile "$deb" | tar -x -C "$workdir" --wildcards \
  '*/lib/runtime/lib/server/libjvm.so' '*/lib/runtime/release'

libjvm=$(find "$workdir" -name libjvm.so | head -1)
release=$(find "$workdir" -name release | head -1)

echo "Paquet  : $deb"
echo "Runtime embarqué :"
[[ -n "$release" ]] && sed 's/^/  /' "$release"
strings "$libjvm" | grep -m1 'OpenJDK 64-Bit Server VM' | sed 's/^/  /' || true

# Instructions marqueurs AVX2 : absentes d'un build x86-64 générique,
# présentes par milliers dans un build ciblant x86-64-v3.
count=$(objdump -d "$libjvm" \
  | grep -cE '\bv(inserti128|extracti128|perm2i128|pbroadcast[bwdq]|pgatherd[dq])\b' || true)

echo "Instructions AVX2 détectées dans libjvm.so : $count"
if (( count > 0 )); then
  cat >&2 <<'EOF'
ERREUR : le runtime Java embarqué contient des instructions AVX2
(cible x86-64-v3). L'application plantera avec SIGILL ("invalid opcode")
au lancement sur tout CPU d'avant Haswell (2013).
Rebuilder avec un JDK générique x86-64 comme JAVA_HOME (ex. un tarball
Temurin officiel), pas l'OpenJDK de la distribution de la machine de build.
EOF
  exit 1
fi
echo "OK : runtime compatible x86-64 générique."
