# ANALYSE ET PLAN D'OPTIMISATION MÉMOIRE - VETNUTRIMP

## 🚨 DIAGNOSTIC DES PROBLÈMES DE HEAP SPACE

### 1. FICHIERS DE DONNÉES VOLUMINEUX IDENTIFIÉS

#### Fichiers JSON critiques (>1MB) :
- **`vetnutri_export_init.json`** : 18MB (587,588 lignes)
  - Contient la base complète des aliments (plus de 10,000 entrées)
  - Chargé en mémoire lors de l'initialisation
- **`animaux.json`** : 19MB (566,909 lignes)
  - Données d'animaux avec consultations complètes
- **`food.json`** : 15MB (818,703 lignes)
  - Base de données d'aliments alternative
- **`ani.json`** : 17MB (566,023 lignes)
  - Données animales avec historique

#### Impact mémoire :
- **Chargement initial** : ~70MB de JSON pur
- **Parsing + objets Kotlin** : ~200-300MB après transformation
- **Cache + StateFlow** : Doublement de la consommation mémoire

### 2. CLASSES ET STRUCTURES PROBLÉMATIQUES

#### 2.1 HtmlDocumentBuilder.kt (550 lignes)
- **Problème** : Méthode `buildHtml()` génère des chaînes HTML massives
- **Impact** : Construction de documents HTML complets en mémoire
- **Optimisation prioritaire** : Streaming HTML ou pagination

#### 2.2 DatabaseFoodRepository.kt (1,356 lignes)
- **Problème** : Cache en mémoire `cachedFoods` conservé indéfiniment
- **Impact** : Liste complète des aliments en mémoire permanente
- **Solution** : Cache LRU avec TTL

#### 2.3 AlimentEv.kt
- **Problème** : Map `valMap` contient tous les nutriments par aliment
- **Impact** : ~50-100 valeurs nutritionnelles par aliment
- **Solution** : Chargement lazy des nutriments

#### 2.4 SettingsViewModel.kt (467 lignes)
- **Problème** : StateFlow pour chaque catégorie de nutriments
- **Impact** : `entries.toList()` pour chaque énumération en init
- **Solution** : Single source of truth

### 3. CONFIGURATIONS DE BUILD iOS

#### 3.1 gradle.properties
```
org.gradle.jvmargs=-Xmx12g -XX:MaxMetaspaceSize=6g
```
- **Problème** : Configuration mémoire excessive pour développement
- **Impact** : Cache Gradle massif, pas optimisé pour iOS

#### 3.2 Scripts de compilation iOS
- **compile_ios_minimal.sh** : Configuration ultra-conservative
- **Problème** : `-Xmx4g` insuffisant pour la base de données
- **Solution** : Configuration mémoire progressive

## 📊 PLAN D'OPTIMISATION STRATÉGIQUE

### PHASE 1 : OPTIMISATIONS IMMÉDIATES (URGENT)

#### 1.1 Réduction du chargement initial
**Objectif** : Diviser par 3 la mémoire initiale

```kotlin
// 1. Chargement paresseux des données
class ResourceReader {
    private val cache = mutableMapOf<String, String>()
    private val maxCacheSize = 5

    fun readResourceLazy(name: String): String {
        return cache.getOrPut(name) {
            // Chargement réel seulement si nécessaire
        }
    }
}
```

#### 1.2 Cache intelligent avec TTL
**Objectif** : Éviter l'accumulation mémoire

```kotlin
class SmartCache<T>(
    private val maxSize: Int = 100,
    private val ttlMs: Long = 5 * 60 * 1000 // 5 minutes
) {
    private val cache = mutableMapOf<String, CacheEntry<T>>()

    data class CacheEntry<T>(
        val value: T,
        val timestamp: Long
    )

    fun get(key: String): T? {
        val entry = cache[key]
        return if (entry != null &&
                   (System.currentTimeMillis() - entry.timestamp) < ttlMs) {
            entry.value
        } else {
            cache.remove(key)
            null
        }
    }
}
```

#### 1.3 Streaming HTML
**Objectif** : Éviter la construction massive de chaînes

```kotlin
class StreamingHtmlBuilder {
    private val stringBuilder = StringBuilder()
    private val bufferSize = 8192

    fun appendSection(content: String) {
        if (stringBuilder.length > bufferSize) {
            flushToFile()
        }
        stringBuilder.append(content)
    }

    private fun flushToFile() {
        // Écrire par chunks dans un fichier temporaire
    }
}
```

### PHASE 2 : REFACTORING ARCHITECTURAL (COURT TERME)

#### 2.1 Séparation des données
**Objectif** : Diviser les grosses structures

```kotlin
// Avant
data class AlimentEv(
    val uuid: String,
    val valMap: MutableMap<Nutrient, NutrientQuantity> // TOUS les nutriments
)

// Après
data class AlimentEv(
    val uuid: String,
    val essentialNutrients: Map<Nutrient, Double>, // Nutriments fréquents
    val allNutrients: Lazy<Map<Nutrient, Double>>   // Chargement différé
)

class LazyNutrientLoader(private val alimentId: String) {
    operator fun get(nutrient: Nutrient): Double {
        return loadFromDatabase(alimentId, nutrient)
    }
}
```

#### 2.2 Pagination des exports
**Objectif** : Traiter les données par batches

```kotlin
class PaginatedExporter(
    private val pageSize: Int = 100
) {
    fun exportLargeDataset(data: List<Any>): List<File> {
        return data.chunked(pageSize).map { chunk ->
            generatePageFile(chunk)
        }
    }
}
```

#### 2.3 Repository optimisé
**Objectif** : Remplacer le cache permanent

```kotlin
class OptimizedFoodRepository(
    private val foodDao: FoodDao,
    private val lruCache: LruCache<String, AlimentEv> = LruCache(500)
) {
    suspend fun getFood(uuid: String): AlimentEv? {
        return lruCache.get(uuid) ?: run {
            foodDao.getFood(uuid)?.also { food ->
                lruCache.put(uuid, food)
            }
        }
    }
}
```

### PHASE 3 : OPTIMISATIONS BUILD iOS (MOYEN TERME)

#### 3.1 Configuration mémoire progressive

```bash
#!/bin/bash
# compile_ios_optimized.sh
export GRADLE_OPTS="-Xmx8g -XX:+UseG1GC -XX:MaxMetaspaceSize=3g"
export JAVA_OPTS="-Xmx6g"

# Compilation par étapes
./gradlew :composeApp:compileKotlinIosArm64 \
    --max-workers=2 \
    --parallel=true \
    -Dkotlin.native.binary.optimization=size

./gradlew :composeApp:linkDebugFrameworkIosArm64 \
    -Dkotlin.native.binary.freezing=enabled
```

#### 3.2 Configuration Gradle optimisée

```kotlin
// build.gradle.kts - Section iOS
iosArm64().binaries.framework {
    baseName = "ComposeApp"
    isStatic = true
    linkerOpts.add("-lsqlite3")
    // Optimisation pour l'export
    freeCompilerArgs.add("-Xexport-library-symbols")
}

// Configuration mémoire adaptative
if (System.getProperty("os.name").contains("Mac")) {
    // Configuration macOS optimisée
    jvmArgs("-Xmx8g", "-XX:+UseG1GC")
} else {
    // Configuration standard
    jvmArgs("-Xmx4g")
}
```

### PHASE 4 : STRATÉGIES LONG TERME

#### 4.1 Architecture modulaire
**Objectif** : Chargement à la demande des modules

```kotlin
// Module system
sealed class AppModule {
    object FoodDatabase : AppModule()
    object AnimalDatabase : AppModule()
    object ExportEngine : AppModule()
    object CalculationEngine : AppModule()
}

class ModuleManager {
    private val loadedModules = mutableSetOf<AppModule>()

    suspend fun loadModule(module: AppModule): Boolean {
        if (loadedModules.contains(module)) return true

        return when (module) {
            is AppModule.FoodDatabase -> loadFoodDatabase()
            is AppModule.ExportEngine -> loadExportEngine()
            // ...
        }
    }
}
```

#### 4.2 Base de données optimisée

```sql
-- Optimisation des index
CREATE INDEX CONCURRENTLY idx_food_nutrients_essential
ON food_nutrients (food_id, nutrient_id)
WHERE nutrient_id IN (1,2,3,4,5); -- Nutriments essentiels seulement

-- Partitionnement des données volumineuses
CREATE TABLE food_data_2024 PARTITION OF food_data
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

## 🎯 RECOMMANDATIONS IMMÉDIATES

### 1. CORRECTION URGENTE (Heap Space)

#### Action 1 : Patch d'urgence HtmlDocumentBuilder

```kotlin
object HtmlDocumentBuilder {
    private const val MAX_HTML_SIZE = 1024 * 1024 // 1MB par section

    fun buildHtmlSafe(documentType: DocumentType, data: ExportData): String {
        val sections = generateSections(data)

        return sections.joinToString("\n") { section ->
            if (section.length > MAX_HTML_SIZE) {
                generatePaginatedSection(section)
            } else {
                section
            }
        }
    }
}
```

#### Action 2 : Cache LRU pour DatabaseFoodRepository

```kotlin
class DatabaseFoodRepository(
    private val foodDao: FoodDao,
    private val cache: LruCache<String, AlimentEv> = LruCache(200)
) {
    override suspend fun getAllFoods(): List<AlimentEv> {
        // Utiliser pagination si > 1000 éléments
        return if (foodDao.count() > 1000) {
            getFoodsPaginated()
        } else {
            foodDao.getAll().map { it.toAlimentEv() }
        }
    }
}
```

### 2. OUTILS DE MONITORING

```kotlin
class MemoryMonitor {
    fun logMemoryUsage(tag: String) {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()

        println("[$tag] Memory: ${used/1024/1024}MB / ${max/1024/1024}MB")
    }
}
```

### 3. MÉTRIQUES DE SUCCÈS

- **Avant** : Heap space à ~2-3GB lors export iOS
- **Après Phase 1** : Heap space < 1GB
- **Après Phase 2** : Heap space < 512MB
- **Temps d'export** : < 30 secondes

## 🔧 CONFIGURATIONS TECHNIQUES RECOMMANDÉES

### Configuration Gradle optimisée

```kotlin
kotlin {
    androidTarget {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                "-Xno-param-assertions",
                "-Xinline-max-instruction-count=100",
                "-Xdisable-phases=DevirtualizationAnalysis"
            )
        }
    }

    iosArm64 {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xruntime-logs=gc",
                "-Xexport-library-symbols"
            )
        }
        binaries.framework {
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }
}
```

### Configuration mémoire adaptative

```kotlin
// Détection automatique de l'environnement
val isLowMemory = Runtime.getRuntime().maxMemory() < 8L * 1024 * 1024 * 1024 // 8GB
val isIOSBuild = project.properties["iosBuild"] == "true"

if (isIOSBuild) {
    // Configuration iOS
    jvmArgs("-Xmx6g", "-XX:+UseG1GC", "-XX:MaxMetaspaceSize=2g")
} else if (isLowMemory) {
    // Configuration mémoire faible
    jvmArgs("-Xmx4g", "-XX:+UseG1GC")
} else {
    // Configuration standard
    jvmArgs("-Xmx8g", "-XX:+UseG1GC")
}
```

## 📈 PLAN DE MISE EN ŒUVRE

### Semaine 1-2 : Corrections urgentes
- [ ] Patch HtmlDocumentBuilder streaming
- [ ] Implémentation cache LRU
- [ ] Configuration mémoire adaptative
- [ ] Tests des exports iOS

### Semaine 3-4 : Refactoring architectural
- [ ] Séparation des données essentielles
- [ ] Pagination des exports
- [ ] Optimisation des repositories
- [ ] Tests de performance

### Semaine 5-6 : Optimisations avancées
- [ ] Architecture modulaire
- [ ] Base de données partitionnée
- [ ] Monitoring en production
- [ ] Documentation des optimisations

## 🎉 BÉNÉFICES ATTENDUS

1. **Stabilité** : Plus d'OutOfMemoryError lors des exports iOS
2. **Performance** : Exports 3x plus rapides
3. **Mémoire** : Consommation réduite de 75%
4. **Maintenance** : Code plus modulaire et testable
5. **Évolutivité** : Support de bases de données plus volumineuses

---

**Note** : Ce plan doit être implémenté de manière incrémentale avec tests à chaque étape pour éviter les régressions.







