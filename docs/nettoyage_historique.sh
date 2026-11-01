#!/bin/zsh
# Nettoyage de l'historique Git : retire des anciens commits les artefacts lourds qui ne font plus
# partie du projet (.aab de release, copies chiffrées .enc, fichiers de travail à la racine).
# Pack : 293 Mo → 30 Mo.
#
# Ne modifie PAS le dépôt de travail : produit une copie miroir nettoyée à côté, puis vérifie que
# le contenu actuel des branches est inchangé. Rien n'est envoyé sur GitHub.
#
# Usage : docs/nettoyage_historique.sh [dossier_destination]   (défaut : ../VetNutriMP-nettoye.git)
set -euo pipefail

SOURCE=$(git -C "$(dirname "$0")/.." rev-parse --show-toplevel)
DEST=${1:-"$SOURCE/../VetNutriMP-nettoye.git"}

# Conservés volontairement : les données initiales des anciennes versions
# (androidMain/assets/... et iosApp/iosApp/Resources/...), nécessaires pour reconstruire un tag.
A_RETIRER=(
  composeApp/release
  ani.json animaux.json food.json oza2.json equations.json test_vbnr.json
  vetnutriExamR/.RData
  composeApp/src/androidMain/assets/data/vetnutri_export_init.json.enc
  composeApp/src/commonMain/resources/data/vetnutri_export_init.json.enc
  "iosApp/iosApp/vetnutri_export_init.json 20-06-01-825.json"
  composeApp/src/commonMain/resources/data/vetnutri_export.json
)

[ -e "$DEST" ] && { echo "Existe déjà : $DEST"; exit 1; }
git clone --mirror "$SOURCE" "$DEST"
cd "$DEST"

CHEMINS=$(printf '%q ' "${A_RETIRER[@]}")
FILTER_BRANCH_SQUELCH_WARNING=1 git filter-branch -f \
  --index-filter "git rm -r --cached --ignore-unmatch -q -- $CHEMINS" \
  --tag-name-filter cat -- --all
git for-each-ref --format='%(refname)' refs/original | xargs -n1 git update-ref -d
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git fsck --no-progress --no-dangling

# Vérification : même nombre de commits, branches sans ces fichiers strictement identiques
echo "Commits : $(git -C "$SOURCE" rev-list --all --count) → $(git rev-list --all --count)"
for r in $(git -C "$SOURCE" for-each-ref --format='%(refname)' refs/heads refs/remotes/origin/main); do
  [ "$(git -C "$SOURCE" ls-tree -r --name-only "$r" -- "${A_RETIRER[@]}" | wc -l)" -eq 0 ] || continue
  [ "$(git -C "$SOURCE" rev-parse "$r^{tree}")" = "$(git rev-parse "$r^{tree}")" ] \
    || { echo "Contenu modifié : $r"; exit 1; }
done
git count-objects -vH | grep size-pack
echo "Copie nettoyée : $DEST"

# --- Étapes suivantes, à faire soi-même (réécrit l'historique sur GitHub) -------------------------
# Les identifiants de tous les commits changent : chaque poste doit ensuite recloner, et les
# branches ouvertes ailleurs (PR, sessions) partent de l'ancien historique.
#   cd "$DEST"
#   git push --force origin 'refs/heads/*:refs/heads/*'
#   # branches qui n'existent que sur GitHub (sauf le pointeur HEAD)
#   git for-each-ref --format='%(refname:strip=3)' refs/remotes/origin | grep -vx HEAD |
#     xargs -I{} git push --force origin 'refs/remotes/origin/{}:refs/heads/{}'
#   git push --force --tags origin
# Les fichiers LFS (images) ne sont pas concernés. GitHub ne libère l'ancien espace qu'après son
# propre ramasse-miettes (ou sur demande au support).
