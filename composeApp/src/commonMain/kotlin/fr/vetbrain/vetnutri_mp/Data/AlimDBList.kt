package fr.vetbrain.vetnutri_mp.Data

data class AlimDBList(val db: MutableMap<String, AlimDB> = mutableMapOf()) {
    fun add(dbEntry: AlimDB) {
        db[dbEntry.uuid] = dbEntry
    }

    fun setNumber(uuid: String, number: Int) {
        db[uuid]?.number = number
    }

    operator fun get(key: String): AlimDB? = db[key]
    fun values(): Collection<AlimDB> = db.values
}

data class AlimDB(val uuid: String, val sNom: String?, val compNom: String?, var number: Int = 0)
