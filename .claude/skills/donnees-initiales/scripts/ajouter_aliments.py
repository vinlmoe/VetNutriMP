#!/usr/bin/env python3
"""Ajoute ou remplace des aliments dans les trois copies du JSON INIT sans reformater le fichier.
Usage (racine du dépôt) :
    python3 .claude/skills/donnees-initiales/scripts/ajouter_aliments.py nouveaux.json [--remplacer]
`nouveaux.json` contient une liste d'aliments au format de la section "foods".
Sans --remplacer, un uuid déjà présent est une erreur. Avec --remplacer, l'aliment existant de même
uuid est remplacé (la mise à jour d'un aliment garde son uuid).
"""
import json, sys

COPIES = [
    "composeApp/src/commonMain/resources/data/vetnutri_export_init.json",
    "composeApp/src/androidMain/assets/data/vetnutri_export_init.json",
    "iosApp/iosApp/Resources/vetnutri_export_init.json",
]
OBLIGATOIRES = ["uuid", "name", "group", "kind", "brand", "dataB", "lastUpdateDate",
                "species", "indications", "nutrients"]

def bloc(aliment):
    """Aliment indenté comme dans le fichier (élément de "foods" : 4 espaces)."""
    texte = json.dumps(aliment, ensure_ascii=False, indent=2)
    return "\n".join("    " + ligne for ligne in texte.splitlines())

def positions_foods(source):
    """(début, fin, uuid) de chaque aliment dans le texte, pour ne réécrire que ce qui change."""
    decodeur = json.JSONDecoder()
    pos = source.index("[", source.index('"foods":')) + 1
    spans = []
    while True:
        while source[pos] in " \t\r\n,":
            pos += 1
        if source[pos] == "]":
            return spans
        objet, fin = decodeur.raw_decode(source, pos)
        spans.append((pos, fin, objet.get("uuid")))
        pos = fin

def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    remplacer = "--remplacer" in sys.argv
    nouveaux = json.load(open(args[0], encoding="utf-8"))
    for a in nouveaux:
        manquants = [c for c in OBLIGATOIRES if c not in a]
        if manquants:
            sys.exit(f"{a.get('uuid', '?')} : champs manquants {manquants}")
    uuids = [a["uuid"] for a in nouveaux]
    if len(set(uuids)) != len(uuids):
        sys.exit("uuid en double dans le fichier d'entrée")

    source = open(COPIES[0], encoding="utf-8").read()
    spans = positions_foods(source)
    existants = {u for _, _, u in spans}
    deja = [u for u in uuids if u in existants]
    if deja and not remplacer:
        sys.exit(f"uuid déjà présents (utiliser --remplacer pour une mise à jour) : {deja}")

    par_uuid = {a["uuid"]: a for a in nouveaux}
    resultat = source
    # Remplacements en partant de la fin pour garder les positions valides.
    for debut, fin, uuid in reversed(spans):
        if uuid in par_uuid and uuid in deja:
            resultat = resultat[:debut] + bloc(par_uuid[uuid]).lstrip() + resultat[fin:]
    ajouts = [a for a in nouveaux if a["uuid"] not in existants]
    if ajouts:
        fin_dernier = spans[-1][1] if spans else source.index("[", source.index('"foods":')) + 1
        # Tous les remplacements précèdent la fin du dernier aliment : décaler d'autant.
        point = fin_dernier + (len(resultat) - len(source))
        resultat = resultat[:point] + ",\n" + ",\n".join(bloc(a) for a in ajouts) + resultat[point:]

    version = json.loads(resultat)["version"]  # le fichier produit doit rester un JSON valide
    for chemin in COPIES:
        open(chemin, "w", encoding="utf-8").write(resultat)
    print(f"{len(ajouts)} ajouté(s), {len(deja)} remplacé(s) dans {len(COPIES)} copies. "
          f"Version du fichier : {version}")

if __name__ == "__main__":
    main()
