#!/bin/bash

# Script de monitoring mémoire pour diagnostiquer les problèmes de heap space
# Usage: ./memory_monitor.sh [pid]

echo "🔍 MONITORING MÉMOIRE - DIAGNOSTIC HEAP SPACE"
echo "=============================================="

# Fonction pour afficher l'utilisation mémoire
show_memory_usage() {
    echo "📊 Utilisation mémoire système:"
    echo "   - Mémoire physique: $(echo "$(sysctl -n hw.memsize) / 1024 / 1024 / 1024" | bc 2>/dev/null || echo "N/A")GB"

    if command -v vm_stat >/dev/null 2>&1; then
        echo "   - Pages libres: $(vm_stat | grep "Pages free" | awk '{print $3}' | tr -d '.')"
        echo "   - Pages actives: $(vm_stat | grep "Pages active" | awk '{print $3}' | tr -d '.')"
        echo "   - Pages inactives: $(vm_stat | grep "Pages inactive" | awk '{print $3}' | tr -d '.')"
        echo "   - Pages purgéables: $(vm_stat | grep "Pages purgeable" | awk '{print $3}' | tr -d '.')"
    fi

    echo ""
    echo "💾 Espace disque:"
    df -h | grep -E "(Filesystem|/$)"

    if command -v memory_pressure >/dev/null 2>&1; then
        echo ""
        echo "🧠 Pression mémoire:"
        memory_pressure
    fi
}

# Fonction pour analyser un processus Java spécifique
analyze_java_process() {
    local pid=$1
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
        echo ""
        echo "🔍 Analyse du processus Java (PID: $pid):"
        echo "   - Commande: $(ps -p $pid -o comm=)"
        echo "   - Utilisation mémoire: $(ps -p $pid -o rss= | tr -d ' ')KB"
        echo "   - CPU: $(ps -p $pid -o pcpu= | tr -d ' ')%"
        echo "   - Threads: $(ps -p $pid -o thcount= | tr -d ' ')"

        # Analyse des arguments JVM
        if ps -p $pid -o args= | grep -q "java"; then
            echo ""
            echo "⚙️ Arguments JVM détectés:"
            ps -p $pid -o args= | tr ' ' '\n' | grep -E "^-X" | sort | uniq
        fi

        # GC et threads
        echo ""
        echo "🔧 Threads et GC:"
        jstack $pid 2>/dev/null | grep -E "(GC|Finalizer|Reference|Cleaner)" | wc -l | xargs echo "   - Threads GC/Cleanup:" lines
    fi
}

# Fonction pour analyser les processus Gradle/Kotlin
analyze_build_processes() {
    echo ""
    echo "🏗️ Processus de build détectés:"

    # Gradle daemon
    GRADLE_PIDS=$(pgrep -f "gradle.*daemon" 2>/dev/null || echo "")
    if [ -n "$GRADLE_PIDS" ]; then
        echo "   - Gradle Daemon: $(echo $GRADLE_PIDS | tr ' ' ', ') PIDs"
        for pid in $GRADLE_PIDS; do
            echo "     PID $pid: $(ps -p $pid -o rss= | tr -d ' ')KB, $(ps -p $pid -o pcpu= | tr -d ' ')% CPU"
        done
    else
        echo "   - Aucun Gradle Daemon détecté"
    fi

    # Kotlin compiler daemon
    KOTLIN_PIDS=$(pgrep -f "kotlin.*daemon" 2>/dev/null || echo "")
    if [ -n "$KOTLIN_PIDS" ]; then
        echo "   - Kotlin Compiler Daemon: $(echo $KOTLIN_PIDS | tr ' ' ', ') PIDs"
        for pid in $KOTLIN_PIDS; do
            echo "     PID $pid: $(ps -p $pid -o rss= | tr -d ' ')KB, $(ps -p $pid -o pcpu= | tr -d ' ')% CPU"
        done
    else
        echo "   - Aucun Kotlin Compiler Daemon détecté"
    fi

    # Xcode process si applicable
    XCODE_PIDS=$(pgrep -f "Xcode|Simulator" 2>/dev/null || echo "")
    if [ -n "$XCODE_PIDS" ]; then
        echo "   - Xcode/Simulator: $(echo $XCODE_PIDS | tr ' ' ', ') PIDs"
    fi
}

# Fonction pour analyser les logs de crash
analyze_crash_logs() {
    echo ""
    echo "📝 Analyse des logs de crash:"

    # Logs système récents
    if [ -d ~/Library/Logs ]; then
        echo "   - Logs système récents:"
        find ~/Library/Logs -name "*.log" -mtime -1 -exec ls -la {} \; 2>/dev/null | head -5
    fi

    # Logs Gradle
    if [ -d ~/.gradle/daemon ]; then
        echo "   - Logs Gradle daemon:"
        find ~/.gradle/daemon -name "*.log" -mtime -1 -exec ls -la {} \; 2>/dev/null | head -3
    fi

    # Core dumps
    echo "   - Core dumps:"
    find /cores -name "core.*" -mtime -1 -exec ls -la {} \; 2>/dev/null || echo "   - Aucun core dump récent"
}

# Fonction pour recommander des optimisations
recommend_optimizations() {
    echo ""
    echo "💡 RECOMMANDATIONS D'OPTIMISATION:"

    MEMORY_GB=$(echo "$(sysctl -n hw.memsize) / 1024 / 1024 / 1024" | bc 2>/dev/null || echo "8")

    if [ "$MEMORY_GB" -lt 8 ]; then
        echo "   ⚠️  Mémoire insuffisante (${MEMORY_GB}GB)"
        echo "      → Fermez toutes les applications inutiles"
        echo "      → Utilisez le script compile_ios_minimal.sh"
        echo "      → Considérez l'ajout de RAM"
    elif [ "$MEMORY_GB" -lt 16 ]; then
        echo "   🟡 Mémoire modérée (${MEMORY_GB}GB)"
        echo "      → Utilisez le script compile_ios_optimized.sh"
        echo "      → Activez les caches LRU dans les repositories"
        echo "      → Archivez les anciennes données"
    else
        echo "   🟢 Mémoire suffisante (${MEMORY_GB}GB)"
        echo "      → Configuration standard recommandée"
        echo "      → Monitoring recommandé"
    fi

    # Vérifier l'espace disque
    DISK_FREE=$(df / | tail -1 | awk '{print $4}' | tr -d 'G')
    if [ "$DISK_FREE" -lt 50 ]; then
        echo "   ⚠️  Espace disque insuffisant (<50GB)"
        echo "      → Libérez de l'espace disque"
        echo "      → Nettoyez les builds anciens"
    fi

    echo ""
    echo "🔧 Actions recommandées:"
    echo "   1. Exécutez: ./clean_build.sh"
    echo "   2. Redémarrez votre Mac"
    echo "   3. Utilisez le script approprié selon votre mémoire"
    echo "   4. Monitorer l'utilisation pendant la compilation"
}

# Script principal
main() {
    echo "Timestamp: $(date)"
    echo ""

    # PID spécifique fourni en argument
    if [ -n "$1" ] && [ "$1" != "auto" ]; then
        analyze_java_process "$1"
    else
        # Recherche automatique du processus principal
        JAVA_PIDS=$(pgrep -f "java.*gradle\|kotlin.*daemon\|java.*kotlin" | head -3)
        if [ -n "$JAVA_PIDS" ]; then
            for pid in $JAVA_PIDS; do
                analyze_java_process "$pid"
            done
        fi
    fi

    show_memory_usage
    analyze_build_processes
    analyze_crash_logs
    recommend_optimizations

    echo ""
    echo "🔄 Utilisation: $0 [PID] pour analyser un processus spécifique"
    echo "🔄 Utilisation: $0 auto pour détection automatique"
}

main "$@"







