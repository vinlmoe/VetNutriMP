---
name: traduire-interface
description: Ajouter ou modifier un texte traduit de VetNutriMP — clé dans LocalizationKeys, valeurs dans strings_<langue>.json (commonMain/resources ET androidMain/assets), traduction des enums, textes avec paramètres. À utiliser quand on ajoute un libellé d'interface, un texte d'export/ordonnance, qu'on change de langue, ou qu'une clé brute (ex. « general.save ») s'affiche à l'écran.
---

# Traduire l'interface (VetNutriMP)

Préfixe Kotlin : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/`.

## Fonctionnement

- `Localization/LocalizationManager.kt` charge **un seul** fichier, `strings_<locale>.json`
  (`{"translations": {"clé": "texte"}}`). Clé absente ⇒ `translate()` renvoie **la clé brute**,
  pas le français. Fichier illisible ⇒ repli sur `fr`.
- Langues : `fr`, `en`, `zh` sont complètes (~1970 clés) ; `de`, `es`, `it`, `ja`, `ro`, `uk` sont
  partielles (~620 clés) et affichent donc des clés brutes pour tout le reste.
- **Deux copies de chaque fichier**, à modifier ensemble :
  - `composeApp/src/commonMain/resources/strings_*.json` (desktop, iOS, tests) ;
  - `composeApp/src/androidMain/assets/strings_*.json` (Android lit `assets`, voir
    `androidMain/.../Localization/ResourceReader.kt`).
  Elles ont déjà divergé (ex. `consultation.observation` absente côté Android) : ne pas aggraver.
  Ignorer `strings_de copy.json`.

## Choisir : texte traduit ou français en dur ?

Le code mélange les deux. Règle : **suivre le fichier voisin**.
- Fichier qui utilise déjà `translate(...)` / `.translate()` / `LocalizationKeys` (réglages, listes,
  export d'ordonnance, plan évolutif…) ⇒ tout nouveau texte passe par une clé.
- Écrans métier récents en français en dur (multiration, troupeau) ⇒ rester en français, ne pas
  convertir partiellement un écran.
- Tout texte qui sort de l'application vers le propriétaire (ordonnance, export HTML/PDF) doit être
  traduit : c'est ce qui a motivé le passage du plan évolutif aux clés `evolutive.*`.

## Ajouter un texte traduit

1. **Clé** dans `Localization/LocalizationKeys.kt`, dans l'`object` de la fonctionnalité (en créer un
   si besoin) :
   ```kotlin
   object Evolutive {
       val AUTO_STEP_NAME = "evolutive.autoStepName"
   }
   ```
   Format : `<domaine>.<camelCase>` ; réutiliser `General.*` (`general.save`, `general.cancel`…) avant
   d'en créer une. Vérifier l'unicité : `grep -n '"evolutive.autoStepName"' composeApp/src/commonMain/resources/strings_fr.json`.
2. **Valeurs** dans les 6 fichiers `fr`, `en`, `zh` (×2 copies). Pour les langues partielles, ajouter
   la valeur anglaise ou une traduction si elle est sûre ; sinon le signaler à l'utilisateur.
   Éditer avec un script qui préserve l'ordre et l'UTF-8 :
   ```bash
   python3 - <<'EOF'
   import json, collections
   ajouts = {"fr": {"evolutive.autoStepName": "Étape %1$s"},
             "en": {"evolutive.autoStepName": "Stage %1$s"},
             "zh": {"evolutive.autoStepName": "阶段 %1$s"}}
   for dossier in ["composeApp/src/commonMain/resources", "composeApp/src/androidMain/assets"]:
       for langue, cles in ajouts.items():
           chemin = f"{dossier}/strings_{langue}.json"
           d = json.load(open(chemin, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
           d["translations"].update(cles)
           json.dump(d, open(chemin, "w", encoding="utf-8"), ensure_ascii=False, indent=4)
           open(chemin, "a", encoding="utf-8").write("\n")
   EOF
   git diff --stat -- '*strings_*.json'
   ```
   Contrôler que le diff ne touche que les lignes ajoutées (indentation d'origine : 4 espaces) ;
   sinon adapter `indent`.
3. **Utilisation** :
   ```kotlin
   Text(translate(LocalizationKeys.General.CLOSE))
   Text(LocalizationKeys.Preferences.TITLE.translate())
   translate(LocalizationKeys.Evolutive.AUTO_STEP_NAME, base)   // remplace %1$s, %2$s…
   ```
   Paramètres positionnels `%1$s`, `%2$s` (aussi `%1$d`), passés en `String`.
4. **Enums** : `valeur.translateEnum()` (`Localization/EnumLocalization.kt`) cherche
   `enum.<Classe>.<label>` puis des clés heuristiques (nutriments) et retombe sur le libellé.
   Pour traduire une nouvelle valeur d'enum, ajouter `enum.<Classe>.<LABEL>` dans les fichiers de chaînes.

## Pièges

- Ne pas traduire une valeur **stockée** ou comparée (nom d'étape, label) : comparer à la valeur
  recalculée dans la langue courante, comme `PlanEvolutif.nomEstAutomatique`.
- Les tests ne doivent pas dépendre de la langue : comparer à `translate(clé, …)` plutôt qu'à un texte
  français en dur.
- `sync_zh.py` (racine) est un script ponctuel avec un dictionnaire de traductions figé : ne pas le
  relancer, il écraserait les traductions chinoises plus récentes.

## Vérifier

```bash
python3 - <<'EOF'
import json
for d in ["composeApp/src/commonMain/resources", "composeApp/src/androidMain/assets"]:
    fr = set(json.load(open(f"{d}/strings_fr.json"))["translations"])
    for l in ["en", "zh"]:
        autre = set(json.load(open(f"{d}/strings_{l}.json"))["translations"])
        print(d.split("/")[2], l, "manquantes:", sorted(fr - autre)[:10])
EOF
./gradlew :composeApp:desktopTest
```
