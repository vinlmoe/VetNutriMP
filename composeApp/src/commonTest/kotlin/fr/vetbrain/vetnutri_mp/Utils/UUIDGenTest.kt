package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UUIDGenTest {
    
    @Test
    fun testGenUUID() {
        val uuid1 = genUUID()
        val uuid2 = genUUID()
        
        // Vérifier que les UUIDs sont différents
        assertNotEquals(uuid1, uuid2)
        
        // Vérifier que les UUIDs ont le bon format (36 caractères avec tirets)
        assertTrue(uuid1.length == 36)
        assertTrue(uuid2.length == 36)
        assertTrue(uuid1.contains("-"))
        assertTrue(uuid2.contains("-"))
    }
    
    @Test
    fun testGenUniqueUUID() {
        val uuid1 = genUniqueUUID()
        val uuid2 = genUniqueUUID()
        
        // Vérifier que les UUIDs sont différents
        assertNotEquals(uuid1, uuid2)
        
        // Vérifier que les UUIDs ont le bon format (timestamp-random)
        assertTrue(uuid1.contains("-"))
        assertTrue(uuid2.contains("-"))
        
        // Vérifier que les UUIDs commencent par un timestamp (nombre)
        val timestamp1 = uuid1.split("-")[0].toLongOrNull()
        val timestamp2 = uuid2.split("-")[1].toLongOrNull()
        
        assertTrue(timestamp1 != null)
        assertTrue(timestamp2 != null)
    }
    
    @Test
    fun testGenUniqueUUIDUniqueness() {
        val uuids = mutableSetOf<String>()
        val iterations = 1000
        
        // Générer 1000 UUIDs uniques
        repeat(iterations) {
            val uuid = genUniqueUUID()
            uuids.add(uuid)
        }
        
        // Vérifier qu'il n'y a pas de doublons
        assertTrue(uuids.size == iterations, "Tous les UUIDs doivent être uniques")
    }
}
