#!/usr/bin/env python3
"""Contrôle des JSON INIT de VetNutriMP : copies identiques, uuid uniques, labels de nutriments
connus de Kotlin, champs énumérés plausibles. Usage (racine du dépôt) :
    python3 .claude/skills/donnees-initiales/scripts/verifier_init.py
"""
import collections, json, re, sys
from pathlib import Path

COPIES = [
    "composeApp/src/commonMain/resources/data/vetnutri_export_init.json",
    "composeApp/src/androidMain/assets/data/vetnutri_export_init.json",
    "iosApp/iosApp/Resources/vetnutri_export_init.json",
]
ENUMER = Path("composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Enumer")
# Position du label parmi les chaînes littérales de chaque entrée (cf. vetnutriDogR).
FAMILLES = {"NutrientMain": 2, "NutrientMacro": 2, "NutrientMin": 2, "NutrientLipid": 2,
            "NutrientVitam": 3, "NutrientOther": 2, "AAEnum": 1, "NutrientAnalysis": 2,
            "NutrientEnergy": 2}

def labels_kotlin():
    """Même découpage que vn_kotlin_dictionaries (vetnutriDogR) : une entrée peut tenir sur plusieurs lignes."""
    labels = set()
    for famille, idx in FAMILLES.items():
        texte = (ENUMER / f"{famille}.kt").read_text(encoding="utf-8").split("companion object")[0]
        debuts = [m.start() for m in re.finditer(
            r'(?m)^[ \t]*(?!listOf\b)[A-Za-z][A-Za-z0-9_]*\s*\(\s*"', texte)]
        if not debuts:
            raise SystemExit(f"Schéma Kotlin non reconnu : {famille}.kt")
        for debut, fin in zip(debuts, debuts[1:] + [len(texte)]):
            chaines = re.findall(r'"([^"\n]*)"', texte[debut:fin])
            if len(chaines) > idx:
                labels.add(chaines[idx].upper())
    return labels

def main():
    erreurs, avertissements, sans_espece = [], [], 0
    contenus = [Path(c).read_bytes() for c in COPIES]
    if len(set(contenus)) != 1:
        erreurs.append("Les trois copies diffèrent : " + ", ".join(
            c for c, b in zip(COPIES, contenus) if b != contenus[0]))
    d = json.loads(contenus[0])
    print(f"version={d.get('version')} generatedAtEpochMs={d.get('generatedAtEpochMs')}")
    for section in ("foods", "references", "equations", "biblioRefs", "recipes", "conseils"):
        ids = [x.get("uuid") or x.get("id") for x in d.get(section, []) if isinstance(x, dict)]
        doublons = [u for u, n in collections.Counter(ids).items() if n > 1]
        if doublons:
            erreurs.append(f"{section} : uuid en double {doublons[:10]}")
        print(f"{section}: {len(d.get(section, []))}")
    connus = labels_kotlin()
    inconnus = collections.Counter()
    for f in d["foods"]:
        for label, valeur in (f.get("nutrients") or {}).items():
            if label.upper() not in connus:
                inconnus[label] += 1
            if not isinstance(valeur, (int, float)):
                erreurs.append(f"{f['uuid']} : valeur non numérique pour {label} ({valeur!r})")
            elif valeur < 0:
                erreurs.append(f"{f['uuid']} : valeur négative pour {label}")
        date = f.get("lastUpdateDate", "")
        if date and not re.match(r"\d{4}-\d\d-\d\d", date):
            avertissements.append(f["uuid"])
        if not f.get("species"):
            sans_espece += 1
    if inconnus:
        print("Labels absents des enums Kotlin (deviendront des nutriments personnalisés) :",
              dict(inconnus.most_common(20)))
    if avertissements:
        print(f"{len(avertissements)} aliments avec lastUpdateDate non ISO (ex. {avertissements[:3]})")
    if sans_espece:
        print(f"{sans_espece} aliments sans espèce (normal pour les ingrédients ménagers MEN)")
    for e in erreurs[:50]:
        print("ERREUR", e)
    sys.exit(1 if erreurs else 0)

if __name__ == "__main__":
    main()
