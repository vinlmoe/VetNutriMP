# Note de décision — clés jsonbin.io embarquées dans les binaires

**Statut : à trancher.** Cette note décrit le problème, les deux options réalistes et une recommandation. Elle ne modifie aucun code.

## Constat

Les deux clés jsonbin.io (`JSONBIN_CREATE_KEY`, `JSONBIN_READ_KEY`) sont lues depuis `local.properties` (ou les variables d'environnement en CI) par la tâche `generateSecrets` de `composeApp/build.gradle.kts`, qui écrit `AppSecretsGenerated.kt` avec deux `const val`. Elles sont donc **compilées en clair dans chaque binaire livré** : APK/AAB, .deb, .dmg, .exe, framework iOS. Un `strings` sur l'exécutable, ou la décompilation de l'APK, suffit à les lire. Ce n'est pas un défaut d'implémentation mais une conséquence du choix « client qui parle directement à jsonbin.io ».

## Ce qu'un attaquant peut faire avec ces clés

- **Clé de création/mise à jour (X-Access-Key)** : créer des bins à volonté sous le compte VetBrain (épuisement du quota, coût), et surtout **écraser** un bin existant dont il connaît l'identifiant (`PUT /b/{id}`) — un identifiant qui circule par URL et QR code.
- **Clé de lecture** : lire tout bin privé du compte, donc tout partage dont l'identifiant est deviné ou intercepté. Sans le chiffrement de bout en bout, il lirait directement les données patients.
- **Révocation** : si la clé fuit, la régénérer sur jsonbin.io casse le partage sur **tous** les binaires déjà installés jusqu'à mise à jour.

Le chiffrement actuel (`CryptoUtils`, AES-CBC + HMAC-SHA256 depuis cette itération, clé aléatoire par envoi transmise par QR code) protège la confidentialité et l'intégrité du contenu ; il ne protège pas le compte jsonbin ni la disponibilité des bins.

## Option 1 — relais serveur sur vetbrain.fr

Un petit service HTTP sur `vetbrain.fr` détient seul les clés jsonbin et expose deux routes (`POST /partage`, `GET /partage/{id}`). Les clients ne contiennent plus aucun secret.

- Avantages : secret révocable sans redéployer les applications ; possibilité de limiter le débit, de journaliser, d'ajouter une expiration côté serveur, voire de remplacer jsonbin.io plus tard sans toucher aux clients.
- Inconvénients : un service à héberger, surveiller et maintenir (certificat, disponibilité) ; le relais voit transiter les bins (chiffrés, donc opaques, si l'option 2 est conservée en parallèle) ; dépendance à l'infrastructure VetBrain pour une fonction aujourd'hui autonome.

## Option 2 — chiffrement de bout en bout obligatoire, bins publics éphémères, clé rotative

Rendre le chiffrement **non désactivable** (`ShareOptions.encryptJson` toujours `true`, suppression du chemin clair dans `AnimalListViewModel`/`AnimalDetailView`), n'utiliser que des bins **publics à expiration courte** (quelques jours), et considérer la clé de création comme un simple jeton d'anti-abus que l'on fait **tourner à chaque version** publiée.

- Avantages : aucune infrastructure ; les données restent illisibles pour quiconque n'a pas le QR code, y compris jsonbin.io et le détenteur des clés ; une fuite de clé n'expose que du quota, pas des données.
- Inconvénients : la clé reste dans les binaires (abus de quota, écrasement d'un bin dont on connaît l'identifiant — l'étiquette HMAC fait alors échouer le déchiffrement côté destinataire, ce qui transforme l'attaque en déni de service, pas en fuite) ; la rotation impose de livrer une version pour changer de clé ; la lecture d'un bin privé devient impossible.

## Recommandation

**Option 2 immédiatement, option 1 à moyen terme.**

1. Dès la prochaine version : chiffrement obligatoire (plus de partage en clair), expiration systématique, clé de création régénérée à chaque release et clé de lecture supprimée du code (les bins publics n'en ont pas besoin). C'est quelques lignes, sans infrastructure, et cela ramène le risque résiduel au déni de service.
2. Quand `vetbrain.fr` disposera d'un hébergement applicatif : mettre en place le relais, en conservant le chiffrement de bout en bout. Les clients ne contiendront alors plus de secret, et la rotation deviendra une opération serveur.

À faire quelle que soit l'option retenue : régénérer les deux clés actuelles sur jsonbin.io, car toutes les versions déjà distribuées les contiennent.
