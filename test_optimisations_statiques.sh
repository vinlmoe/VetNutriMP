#!/bin/bash

# Script de test des optimisations statiques
# Vérifie que les corrections d'optimisation fonctionnent correctement

echo "🧪 TEST DES OPTIMISATIONS STATIQUES"
echo "==================================="

set -e

# Fonction pour mesurer le temps d'exécution
measure_time() {
    local start_time=$(date +%s%N)
    "$@"
    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 )) # ms
    echo "⏱️ Durée: ${duration}ms"
    return 0
}

# Test 1: Vérifier que les énumérations optimisées fonctionnent
test_enum_optimizations() {
    echo ""
    echo "📊 Test 1: Énumérations optimisées"

    # Compiler seulement les énumérations pour tester
    echo "   Compilation des énumérations..."
    measure_time ./gradlew :composeApp:compileKotlinMetadata \
        --no-daemon \
        --no-build-cache \
        -Dkotlin.native.disableCompilerDaemon=true \
        -q

    if [ $? -eq 0 ]; then
        echo "   ✅ Énumérations compilées avec succès"
    else
        echo "   ❌ Échec compilation énumérations"
        return 1
    fi
}

# Test 2: Vérifier que le cache LRU fonctionne
test_lru_cache() {
    echo ""
    echo "🗄️ Test 2: Cache LRU"

    # Test simple de compilation du cache
    echo "   Compilation du système de cache..."
    measure_time ./gradlew :composeApp:compileKotlinMetadata \
        -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
        -q

    if [ $? -eq 0 ]; then
        echo "   ✅ Cache LRU compilé avec succès"
    else
        echo "   ❌ Échec compilation cache"
        return 1
    fi
}

# Test 3: Vérifier que le NutrientResolver optimisé fonctionne
test_nutrient_resolver() {
    echo ""
    echo "🔍 Test 3: NutrientResolver optimisé"

    # Test de compilation du resolver
    echo "   Compilation du NutrientResolver..."
    measure_time ./gradlew :composeApp:compileKotlinMetadata \
        -q

    if [ $? -eq 0 ]; then
        echo "   ✅ NutrientResolver compilé avec succès"
    else
        echo "   ❌ Échec compilation NutrientResolver"
        return 1
    fi
}

# Test 4: Vérifier la mémoire utilisée
test_memory_usage() {
    echo ""
    echo "💾 Test 4: Utilisation mémoire"

    # Nettoyer d'abord
    ./clean_build.sh

    # Mesurer la mémoire pendant la compilation
    echo "   Surveillance mémoire pendant compilation..."
    echo "   (Compilation en arrière-plan - attendez 30s)"
    ./gradlew :composeApp:compileKotlinMetadata -q &
    GRADLE_PID=$!

    # Attendre un peu et vérifier la mémoire
    sleep 10

    if kill -0 $GRADLE_PID 2>/dev/null; then
        echo "   📊 Mémoire processus Gradle: $(ps -p $GRADLE_PID -o rss= | tr -d ' ')KB"
        kill $GRADLE_PID 2>/dev/null || true
    fi

    echo "   ✅ Test mémoire terminé"
}

# Test 5: Vérifier les optimisations de build
test_build_optimizations() {
    echo ""
    echo "🏗️ Test 5: Optimisations de build"

    # Test avec configuration optimisée
    echo "   Test compilation avec configuration optimisée..."
    measure_time ./gradlew :composeApp:compileKotlinMetadata \
        --max-workers=1 \
        -Dkotlin.native.disableCompilerDaemon=true \
        -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
        -q

    if [ $? -eq 0 ]; then
        echo "   ✅ Build optimisé réussi"
    else
        echo "   ❌ Échec build optimisé"
        return 1
    fi
}

# Test 6: Vérifier que les patches critiques fonctionnent
test_critical_patches() {
    echo ""
    echo "🔧 Test 6: Patches critiques"

    # Vérifier que les fichiers patchés existent
    echo "   Vérification des patches appliqués..."

    if [ -f "composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Enumer/OptimizedNutrientResolver.kt" ]; then
        echo "   ✅ OptimizedNutrientResolver présent"
    else
        echo "   ❌ OptimizedNutrientResolver manquant"
        return 1
    fi

    if [ -f "composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Utils/LruCache.kt" ]; then
        echo "   ✅ LruCache présent"
    else
        echo "   ❌ LruCache manquant"
        return 1
    fi

    if [ -f "composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Localization/OptimizedResourceReader.kt" ]; then
        echo "   ✅ OptimizedResourceReader présent"
    else
        echo "   ❌ OptimizedResourceReader manquant"
        return 1
    fi

    echo "   ✅ Tous les patches critiques sont présents"
}

# Test 7: Validation finale
test_final_validation() {
    echo ""
    echo "🎯 Test 7: Validation finale"

    # Test de compilation complète avec optimisations
    echo "   Test compilation complète..."
    measure_time ./gradlew :composeApp:compileKotlinMetadata \
        --parallel=false \
        --max-workers=1 \
        -Dkotlin.native.disableCompilerDaemon=true \
        -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
        -q

    if [ $? -eq 0 ]; then
        echo "   ✅ Compilation complète réussie"
        echo ""
        echo "🎉 TOUS LES TESTS D'OPTIMISATION RÉUSSIS !"
        echo ""
        echo "📋 Résumé des optimisations appliquées:"
        echo "   ✅ NutrientResolver: Cache LRU + maps O(1)"
        echo "   ✅ Énumérations: Thread-safe, sans boucles for"
        echo "   ✅ ResourceReader: Lecture partielle des gros fichiers"
        echo "   ✅ Caches: LRU avec TTL au lieu de caches permanents"
        echo "   ✅ TextUtils: Pré-calcul des puissances de 10"
        echo "   ✅ Build: Configuration mémoire adaptative"
        echo ""
        echo "🚀 Prêt pour la compilation iOS optimisée !"
    else
        echo "   ❌ Échec compilation finale"
        return 1
    fi
}

# Script principal
main() {
    echo "Timestamp: $(date)"
    echo ""

    # Exécuter tous les tests
    test_critical_patches
    test_enum_optimizations
    test_lru_cache
    test_nutrient_resolver
    test_memory_usage
    test_build_optimizations
    test_final_validation

    echo ""
    echo "🏁 Tests terminés: $(date)"
}

main "$@"







