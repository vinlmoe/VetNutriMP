package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LruMapTest {

    // ── Construction ───────────────────────────────────────────────────────────

    @Test
    fun constructor_zeroMaxSize_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> { LruMap<String, String>(0) }
    }

    // ── Accès de base ──────────────────────────────────────────────────────────

    @Test
    fun get_existingKey_returnsValue() {
        val map = LruMap<String, Int>(10)
        map["key"] = 42
        assertEquals(42, map["key"])
    }

    @Test
    fun get_absentKey_returnsNull() {
        val map = LruMap<String, Int>(10)
        assertNull(map["absent"])
    }

    @Test
    fun set_overwritesExistingKey() {
        val map = LruMap<String, Int>(10)
        map["k"] = 1
        map["k"] = 2
        assertEquals(2, map["k"])
    }

    // ── Éviction LRU ──────────────────────────────────────────────────────────

    @Test
    fun set_exceedsMaxSize_evictsLruEntry() {
        val map = LruMap<String, Int>(2)
        map["a"] = 1
        map["b"] = 2
        map["c"] = 3  // "a" should be evicted (least recently used)
        assertNull(map["a"])
        assertEquals(2, map["b"])
        assertEquals(3, map["c"])
    }

    @Test
    fun get_promotesKey_preventsEviction() {
        val map = LruMap<String, Int>(2)
        map["a"] = 1
        map["b"] = 2
        map["a"]    // access "a" → promotes it → "b" becomes LRU
        map["c"] = 3  // "b" should be evicted, not "a"
        assertNull(map["b"])
        assertEquals(1, map["a"])
        assertEquals(3, map["c"])
    }

    // ── Callback onEvict ───────────────────────────────────────────────────────

    @Test
    fun onEvict_calledWithEvictedKey() {
        var evicted = ""
        val map = LruMap<String, Int>(1, onEvict = { evicted = it })
        map["a"] = 1
        map["b"] = 2  // "a" evicted
        assertEquals("a", evicted)
    }

    @Test
    fun onEvict_notCalledWhenNoEviction() {
        var evicted = ""
        val map = LruMap<String, Int>(5, onEvict = { evicted = it })
        map["a"] = 1
        assertTrue(evicted.isEmpty())
    }

    // ── getOrPut ───────────────────────────────────────────────────────────────

    @Test
    fun getOrPut_missingKey_computesAndStoresValue() {
        val map = LruMap<String, Int>(10)
        val result = map.getOrPut("k") { 99 }
        assertEquals(99, result)
        assertEquals(99, map["k"])
    }

    @Test
    fun getOrPut_existingKey_doesNotRecompute() {
        val map = LruMap<String, Int>(10)
        map["k"] = 1
        var calls = 0
        map.getOrPut("k") { calls++; 99 }
        map.getOrPut("k") { calls++; 99 }
        assertEquals(0, calls)
    }

    // ── remove ─────────────────────────────────────────────────────────────────

    @Test
    fun remove_existingKey_returnsOldValue() {
        val map = LruMap<String, Int>(10)
        map["k"] = 7
        val removed = map.remove("k")
        assertEquals(7, removed)
        assertNull(map["k"])
    }

    @Test
    fun remove_absentKey_returnsNull() {
        val map = LruMap<String, Int>(10)
        assertNull(map.remove("absent"))
    }

    // ── clear ──────────────────────────────────────────────────────────────────

    @Test
    fun clear_emptyMap_afterClear() {
        val map = LruMap<String, Int>(10)
        map["a"] = 1
        map["b"] = 2
        map.clear()
        assertNull(map["a"])
        assertNull(map["b"])
    }

    @Test
    fun clear_thenSet_worksNormally() {
        val map = LruMap<String, Int>(2)
        map["a"] = 1
        map.clear()
        map["x"] = 10
        assertEquals(10, map["x"])
    }
}
