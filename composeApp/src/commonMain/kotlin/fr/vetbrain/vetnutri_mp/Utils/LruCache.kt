package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cache LRU (Least Recently Used) optimisé pour éviter les fuites mémoire.
 * Thread-safe et avec nettoyage automatique.
 */
class LruCache<K, V>(
    private val maxSize: Int = 100,
    private val ttlMs: Long = 5 * 60 * 1000L // 5 minutes par défaut
) {
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val accessOrder = mutableListOf<K>()
    private val mutex = Mutex()

    private data class CacheEntry<V>(
        val value: V,
        val timestamp: Long
    )

    /**
     * Récupère une valeur du cache ou null si expirée/absente
     */
    suspend fun get(key: K): V? {
        return mutex.withLock {
            val entry = cache[key]

            if (entry == null) {
                null
            } else {
                val now = currentTimeMillis()
                if (isExpired(entry, now)) {
                    removeFromCache(key)
                    null
                } else {
                    updateAccessOrder(key)
                    entry.value
                }
            }
        }
    }

    /**
     * Stocke une valeur dans le cache
     */
    suspend fun put(key: K, value: V) {
        mutex.withLock {
            val now = currentTimeMillis()

            // Supprimer les entrées expirées
            cleanupExpiredEntries(now)

            // Si cache plein, supprimer les moins utilisés
            if (cache.size >= maxSize) {
                evictLeastRecentlyUsed()
            }

            cache[key] = CacheEntry(value, now)
            updateAccessOrder(key)
        }
    }

    /**
     * Supprime une entrée du cache
     */
    suspend fun remove(key: K): V? {
        return mutex.withLock {
            cache.remove(key)?.value
        }
    }

    /**
     * Vide complètement le cache
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }

    /**
     * Retourne la taille actuelle du cache
     */
    suspend fun size(): Int {
        return mutex.withLock { cache.size }
    }

    /**
     * Statistiques du cache pour monitoring
     */
    suspend fun getStats(): CacheStats {
        return mutex.withLock {
            val now = currentTimeMillis()
            var expiredCount = 0
            var oldestAccess: Long = Long.MAX_VALUE
            var newestAccess: Long = 0

            cache.values.forEach { entry ->
                if (entry.timestamp < oldestAccess) oldestAccess = entry.timestamp
                if (entry.timestamp > newestAccess) newestAccess = entry.timestamp
                if (isExpired(entry, now)) expiredCount++
            }

            CacheStats(
                size = cache.size,
                maxSize = maxSize,
                expiredCount = expiredCount,
                totalAccessCount = cache.size, // Approximation simplifiée
                oldestEntryAge = if (oldestAccess != Long.MAX_VALUE) now - oldestAccess else 0,
                newestEntryAge = if (newestAccess != 0L) now - newestAccess else 0
            )
        }
    }

    private fun isExpired(entry: CacheEntry<V>, now: Long): Boolean {
        return (now - entry.timestamp) > ttlMs
    }


    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }

    private fun updateAccessOrder(key: K) {
        accessOrder.remove(key)
        accessOrder.add(key)
    }

    private fun removeFromCache(key: K) {
        cache.remove(key)
        accessOrder.remove(key)
    }

    private fun cleanupExpiredEntries(now: Long) {
        val keysToRemove = mutableListOf<K>()
        cache.forEach { (key, entry) ->
            if (isExpired(entry, now)) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { removeFromCache(it) }
    }

    private fun evictLeastRecentlyUsed() {
        if (accessOrder.isEmpty()) return

        // Supprimer les entrées les moins récemment utilisées (du début de la liste)
        val keysToRemove = mutableListOf<K>()
        val targetToRemove = (maxSize / 2).coerceAtLeast(1)

        for (i in 0 until minOf(targetToRemove, accessOrder.size)) {
            val key = accessOrder[i]
            if (cache.containsKey(key)) {
                keysToRemove.add(key)
            }
        }

        keysToRemove.forEach { removeFromCache(it) }
    }

    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val expiredCount: Int,
        val totalAccessCount: Int,
        val oldestEntryAge: Long,
        val newestEntryAge: Long
    )
}

/**
 * Factory pour créer des caches spécialisés selon le type de données
 */
object CacheFactory {
    fun createFoodCache(): LruCache<String, fr.vetbrain.vetnutri_mp.Data.AlimentEv> {
        return LruCache(
            maxSize = 200, // Moins d'aliments que d'animaux
            ttlMs = 10 * 60 * 1000L // 10 minutes pour les aliments (plus stable)
        )
    }

    fun createAnimalCache(): LruCache<String, fr.vetbrain.vetnutri_mp.Data.AnimalEv> {
        return LruCache(
            maxSize = 500, // Plus d'animaux que d'aliments
            ttlMs = 5 * 60 * 1000L // 5 minutes pour les animaux (plus dynamique)
        )
    }

    fun createReferenceCache(): LruCache<String, fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {
        return LruCache(
            maxSize = 100, // Peu de références
            ttlMs = 30 * 60 * 1000L // 30 minutes (très stable)
        )
    }

    fun createCalculationCache(): LruCache<String, Double> {
        return LruCache(
            maxSize = 1000, // Beaucoup de calculs possibles
            ttlMs = 2 * 60 * 1000L // 2 minutes (résultats de calculs)
        )
    }
}

/**
 * Extension pour faciliter l'utilisation des caches
 */
suspend fun <K, V> LruCache<K, V>.getOrPut(key: K, defaultValue: suspend () -> V): V {
    return get(key) ?: run {
        val value = defaultValue()
        put(key, value)
        value
    }
}
