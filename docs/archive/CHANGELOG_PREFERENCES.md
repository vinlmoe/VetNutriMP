# Changelog des Préférences VetNutri MP

## Version 2.0 - Expression par 1000 kcal par défaut

### 🆕 Nouveautés

- **Nouvelle valeur par défaut** : L'expression des besoins nutritionnels utilise maintenant **PAR_KCAL** (par 1000 kcal) au lieu de **PAR_KG** (par kg)
- **Configuration centralisée** : Toutes les préférences par défaut sont maintenant centralisées dans `DefaultPreferencesConfig.kt`
- **Système modulaire** : Possibilité de créer des configurations personnalisées pour différents environnements

### 🔄 Changements

#### Types d'Expression des Besoins

**AVANT :**
- Valeur par défaut : `PAR_KG` (par kilogramme de poids corporel)
- Utilisé pour : Chiens, chats, rongeurs, lapins, furets, primates

**APRÈS :**
- Valeur par défaut : `PAR_KCAL` (par 1000 kcal de besoin énergétique)
- Utilisé pour : **TOUTES** les espèces par défaut

#### Espèces Modifiées

| Espèce | Ancien Type | Nouveau Type | Commentaire |
|--------|-------------|--------------|-------------|
| CHIEN | PAR_KG | PAR_KCAL | ✅ Modifié |
| CHAT | PAR_KG | PAR_KCAL | ✅ Modifié |
| LAPIN | PAR_KG | PAR_KCAL | ✅ Modifié |
| FURET | PAR_KG | PAR_KCAL | ✅ Modifié |
| RAT/SOURIS | PAR_KG | PAR_KCAL | ✅ Modifié |
| PRIMATE | PAR_KG | PAR_KCAL | ✅ Modifié |
| CHEVAL | PAR_KG_METABOLIQUE | PAR_KCAL | ✅ Modifié |
| HERBIVORE | PAR_KG_METABOLIQUE | PAR_KCAL | ✅ Modifié |
| FOLIVORE | PAR_KG_METABOLIQUE | PAR_KCAL | ✅ Modifié |
| FELIN | PAR_KG | PAR_KCAL | ✅ Modifié |
| CANIN | PAR_KG | PAR_KCAL | ✅ Modifié |

### 📁 Fichiers Modifiés

1. **`DefaultPreferencesConfig.kt`**
   - Changement de `DEFAULT_EXPRESSION_TYPE` de `PAR_KG` à `PAR_KCAL`
   - Mise à jour de toutes les espèces pour utiliser `PAR_KCAL`

2. **`PreferencesEspece.kt`**
   - Intégration avec `DefaultPreferencesConfig`
   - Méthode `createDefault()` utilise maintenant la nouvelle configuration

3. **`PreferencesRepository.kt`**
   - Utilisation de `PreferencesApplication.createDefault()`
   - Gestion des erreurs avec les nouvelles valeurs par défaut

4. **Documentation**
   - `docs/guide_preferences_par_defaut.md` mis à jour
   - `README_PREFERENCES.md` mis à jour
   - Nouveau fichier `CHANGELOG_PREFERENCES.md`

### 🆘 Rétrocompatibilité

- **Préférences existantes** : Les préférences déjà sauvegardées par les utilisateurs ne sont **PAS** affectées
- **Migration automatique** : Les nouvelles installations utilisent automatiquement les nouvelles valeurs par défaut
- **Rétroversion possible** : Vous pouvez facilement revenir aux anciennes valeurs en modifiant `DefaultPreferencesConfig.kt`

### 🔧 Personnalisation

#### Revenir à l'ancienne configuration

Si vous souhaitez revenir à l'expression par kg pour certaines espèces :

```kotlin
/** Préférences par défaut pour les chiens */
val CHIEN = PreferencesEspece(
    espece = Espece.CHIEN.name,
    typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id, // Retour à l'ancienne valeur
    // ... reste des préférences
)
```

#### Configuration mixte

Vous pouvez également créer une configuration mixte :

```kotlin
// Petits animaux par kg
val CHIEN = PreferencesEspece(..., TypeExpressionBesoin.PAR_KG.id, ...)
val CHAT = PreferencesEspece(..., TypeExpressionBesoin.PAR_KG.id, ...)

// Gros animaux par 1000 kcal
val CHEVAL = PreferencesEspece(..., TypeExpressionBesoin.PAR_KCAL.id, ...)
val HERBIVORE = PreferencesEspece(..., TypeExpressionBesoin.PAR_KCAL.id, ...)
```

### 📊 Impact des Changements

#### Avantages de PAR_KCAL

1. **Précision nutritionnelle** : Expression relative aux besoins énergétiques réels
2. **Comparabilité** : Plus facile de comparer les besoins entre animaux de tailles différentes
3. **Standards internationaux** : Alignement avec les pratiques de nutrition vétérinaire modernes
4. **Flexibilité** : Adapte automatiquement les besoins selon l'activité et le métabolisme

#### Cas d'Usage

- **PAR_KCAL** (défaut) : Chiens, chats, rongeurs, animaux de compagnie
- **PAR_KG** : Animaux avec besoins très spécifiques par kg
- **PAR_KG_METABOLIQUE** : Chevaux, herbivores de grande taille
- **PAR_KJ** : Recherche internationale, publications scientifiques

### 🧪 Tests Recommandés

Après ces modifications, testez :

1. **Nouveaux utilisateurs** : Vérifiez que les préférences par défaut sont correctes
2. **Calculs nutritionnels** : Vérifiez que les rations sont calculées correctement
3. **Interface utilisateur** : Vérifiez que les préférences s'affichent correctement
4. **Persistance** : Vérifiez que les préférences sont sauvegardées et rechargées

### 📚 Documentation

- **Guide complet** : `docs/guide_preferences_par_defaut.md`
- **README principal** : `README_PREFERENCES.md`
- **Exemples** : `CustomPreferencesExample.kt`
- **Code source** : `DefaultPreferencesConfig.kt`

### 🔮 Versions Futures

- **Configuration par environnement** : Possibilité de charger différentes configurations selon le contexte
- **Profils utilisateur** : Différents profils de préférences (clinique, recherche, éducation)
- **Import/Export** : Sauvegarde et partage de configurations personnalisées
- **Validation avancée** : Vérification automatique de la cohérence des préférences

---

**Date de modification** : $(date)
**Version** : 2.0
**Auteur** : Assistant IA
**Statut** : ✅ Terminé
