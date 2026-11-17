package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LruCacheTest {

    // ── État initial ───────────────────────────────────────────────────────────

    @Test
    fun size_emptyCache_returnsZero() = runTest {
        val cache = LruCache<String, Int>()
        assertEquals(0, cache.size())
    }

    @Test
    fun get_emptyCache_returnsNull() = runTest {
        val cache = LruCache<String, Int>()
        assertNull(cache.get("absent"))
    }

    // ── put / get ──────────────────────────────────────────────────────────────

    @Test
    fun put_thenGet_returnsStoredValue() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("a", 42)
        assertEquals(42, cache.get("a"))
    }

    @Test
    fun put_multipleEntries_allRetrievable() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("a", 1)
        cache.put("b", 2)
        cache.put("c", 3)
        assertEquals(1, cache.get("a"))
        assertEquals(2, cache.get("b"))
        assertEquals(3, cache.get("c"))
    }

    @Test
    fun put_overwrites_existingEntry() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("k", 1)
        cache.put("k", 99)
        assertEquals(99, cache.get("k"))
    }

    @Test
    fun size_afterPuts_reflectsCount() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("a", 1)
        cache.put("b", 2)
        assertEquals(2, cache.size())
    }

    // ── remove ─────────────────────────────────────────────────────────────────

    @Test
    fun remove_existingKey_returnsValue() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("a", 7)
        val removed = cache.remove("a")
        assertEquals(7, removed)
    }

    @Test
    fun remove_existingKey_entryNoLongerRetrievable() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("a", 7)
        cache.remove("a")
        assertNull(cache.get("a"))
    }

    @Test
    fun remove_absentKey_returnsNull() = runTest {
        val cache = LruCache<String, Int>()
        assertNull(cache.remove("absent"))
    }

    // ── clear ──────────────────────────────────────────────────────────────────

    @Test
    fun clear_emptiesAllEntries() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("a", 1)
        cache.put("b", 2)
        cache.clear()
        assertEquals(0, cache.size())
        assertNull(cache.get("a"))
        assertNull(cache.get("b"))
    }

    // ── TTL expiration ─────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun get_afterTtlExpired_returnsNull() = runTest {
        val cache = LruCache<String, Int>(
            maxSize = 10,
            ttlMs = 1L,
            clock = { testScheduler.currentTime }
        )
        cache.put("a", 42)
        delay(5)
        assertNull(cache.get("a"))
    }

    @Test
    fun get_beforeTtlExpired_returnsValue() = runTest {
        val cache = LruCache<String, Int>(maxSize = 10, ttlMs = Long.MAX_VALUE)
        cache.put("a", 42)
        assertNotNull(cache.get("a"))
    }

    // ── Éviction LRU par taille ────────────────────────────────────────────────

    @Test
    fun put_exceedsMaxSize_reducesSize() = runTest {
        val cache = LruCache<String, Int>(maxSize = 2)
        cache.put("a", 1)
        cache.put("b", 2)
        cache.put("c", 3)  // triggers eviction
        assertTrue(cache.size() <= 2)
    }

    // ── getStats ───────────────────────────────────────────────────────────────

    @Test
    fun getStats_afterPuts_sizeMatches() = runTest {
        val cache = LruCache<String, Int>(maxSize = 100)
        cache.put("a", 1)
        cache.put("b", 2)
        val stats = cache.getStats()
        assertEquals(2, stats.size)
        assertEquals(100, stats.maxSize)
    }

    @Test
    fun getStats_emptyCache_sizeIsZero() = runTest {
        val cache = LruCache<String, Int>()
        val stats = cache.getStats()
        assertEquals(0, stats.size)
        assertEquals(0, stats.expiredCount)
    }

    // ── Extension getOrPut ─────────────────────────────────────────────────────

    @Test
    fun getOrPut_missingKey_computesAndStores() = runTest {
        val cache = LruCache<String, Int>()
        val result = cache.getOrPut("k") { 55 }
        assertEquals(55, result)
        assertEquals(55, cache.get("k"))
    }

    @Test
    fun getOrPut_existingKey_returnsStoredWithoutComputing() = runTest {
        val cache = LruCache<String, Int>()
        cache.put("k", 10)
        var calls = 0
        val result = cache.getOrPut("k") { calls++; 99 }
        assertEquals(10, result)
        assertEquals(0, calls)
    }
}
