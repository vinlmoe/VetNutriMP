# Contribuer à VetNutriMP

Ce document décrit le workflow Git du dépôt et ce que la CI vérifie.

---

## Workflow Git

`main` est la branche de référence : elle doit rester déployable. On n'y pousse
pas directement — tout passe par une branche de travail et une pull request.

```
main ──●──────────────●──────────────●──►
        \            /              /
         ●──●──●────●          ●───●
       feat/analyse-ration   fix/room-migration-36
```

### Nommer sa branche

| Préfixe | Usage |
|---------|-------|
| `feat/` | nouvelle fonctionnalité |
| `fix/` | correction de bug |
| `refactor/` | réorganisation sans changement de comportement |
| `data/` | mise à jour des références nutritionnelles / JSON initiaux |
| `build/` | Gradle, dépendances, empaquetage |
| `ci/` | workflows GitHub Actions |
| `claude/` | branches ouvertes par un agent |

### Messages de commit

Format [Conventional Commits](https://www.conventionalcommits.org/) :

```
<type>(<portée>): <description à l'impératif>
```

Exemples tirés de l'historique du dépôt :

```
fix(nutrition): convertir les seuils g/1000kcal avec le BEE, pas le BE réel
test(nutrition): couvrir la conversion g/1000kcal (BEE vs BE réel)
data: ajouter l'équation BEE (loi de Kleiber) aux références Hospitalisation
```

Un message comme `kk`, `j` ou `FFF` ne permet ni de relire une release, ni de
retrouver la cause d'une régression — c'est précisément ce que ce format évite.

### Cycle d'une contribution

1. Partir de `main` à jour : `git switch main && git pull origin main`
2. Créer la branche : `git switch -c feat/ma-fonctionnalite`
3. Développer, commiter par unité logique
4. Pousser : `git push -u origin feat/ma-fonctionnalite`
5. Ouvrir une PR — le gabarit se remplit automatiquement
6. La CI doit être verte avant fusion
7. Fusionner dans `main` (squash de préférence), puis supprimer la branche

---

## Ce que vérifie la CI

Le pipeline `.github/workflows/ci.yml` tourne sur chaque PR vers `main` et sur
chaque push des branches de travail.

| Job | Runner | Contenu |
|-----|--------|---------|
| **Tests JVM** | ubuntu | `:composeApp:desktopTest` — `commonTest` + `desktopTest` |
| **Android** | ubuntu | `:composeApp:testDebugUnitTest` puis `:composeApp:assembleDebug` |
| **iOS** | macOS | `:composeApp:compileKotlinIosSimulatorArm64` |
| **Paquet Debian** | ubuntu | `:composeApp:packageReleaseDeb` + contrôle du runtime embarqué |
| **Résultat CI** | ubuntu | agrège les jobs ci-dessus |

### Jobs conditionnels

- **iOS** ne tourne pas sur chaque PR (runner macOS facturé 10×). Il se déclenche
  sur les push vers `main`, via `workflow_dispatch`, ou en posant le label
  **`ci:ios`** sur une PR qui touche du code iOS.
- **Paquet Debian** ne tourne que sur `main`, `master`, `revetBackUp` ou via
  `workflow_dispatch`, et seulement si les tests JVM et Android sont passés.
  Auparavant (`build-deb.yml`), un `.deb` était produit même depuis une branche
  cassée.

### Protection de branche

Exiger le seul check **`Résultat CI`** : il agrège tous les jobs et traite
correctement ceux qui sont volontairement ignorés (iOS hors `main`, paquet
Debian sur une PR). Exiger les jobs individuellement bloquerait toute PR où
le job iOS est ignoré.

---

## Points d'attention CI

### Git LFS

Les PNG, JPG, SVG et autres binaires sont suivis par Git LFS (voir
`.gitattributes`). Tous les jobs font un checkout avec `lfs: true` : sans cela,
`composeApp/src/desktopMain/resources/icon.png` arrive sous forme de pointeur
texte de 131 octets et AAPT2 échoue sur les `mipmap/`.

Localement, installer LFS avant le premier clone :

```bash
git lfs install
git lfs pull
```

### Schémas Room

`composeApp/schemas/` contient les 35 schémas exportés. La CI échoue si une
entité a changé sans que le schéma correspondant soit régénéré et commité —
sinon les migrations divergent du code et Room plante au démarrage chez
l'utilisateur. Pour régénérer :

```bash
./gradlew :composeApp:desktopTest
git add composeApp/schemas/
```

### Secrets jsonbin

`AppSecretsTest` exige que `JSONBIN_CREATE_KEY` et `JSONBIN_READ_KEY` soient non
vides. En local, les renseigner dans `local.properties` :

```properties
jsonbin.create.key=...
jsonbin.read.key=...
```

En CI, les secrets de dépôt du même nom sont utilisés s'ils existent ; à défaut,
une valeur factice permet aux tests de passer (ils valident l'injection
build-time, pas la validité de la clé). Pour tester réellement l'accès à
jsonbin, définir ces deux secrets dans *Settings → Secrets and variables →
Actions*.

### Mémoire

`gradle.properties` demande 8 Go pour Gradle et 8 Go pour le daemon Kotlin :
c'est calibré pour un poste de développement. L'action composite
`.github/actions/setup-build` ramène ces valeurs à 4 Go / 3 Go, faute de quoi le
runner GitHub (4 vCPU / 16 Go) déclenche un OOM kill.

---

## Commandes locales

```bash
./gradlew :composeApp:desktopTest        # tests common + desktop
./gradlew :composeApp:testDebugUnitTest  # tests unitaires Android
./gradlew :composeApp:assembleDebug      # APK debug
./gradlew :composeApp:run                # lancer l'app desktop
./gradlew :composeApp:packageReleaseDeb  # paquet Debian
```

Les conventions d'architecture (couches, suffixe `Ev`, nommage FR/EN, ajout d'un
écran ou d'un nutriment) sont décrites dans [`CLAUDE.md`](CLAUDE.md).
