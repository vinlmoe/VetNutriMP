package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class Espece(
  override  val label: String ?,
    val categorie: Int,
    val uuid: String
) :Labelable{
    CH("ALL", 2, "ALL"),
    CHIEN("DOG", 0, "0"),
    CHAT("CAT", 1, "1"),
    PRIMATE("PRIMATE", 3, "3"),
    RAT("RAT", 4, "4"),
    SOURIS("SOURIS", 5, "5"),
    FURET("FURET", 6, "6"),
    LAPIN("LAPIN", 7, "7"),
    CHEVAL("CHEVAL", 8, "8"),
    FELIN("FELIN", 9, "9"),
    CANIN("CANIN", 10, "10"), // Corrected from LAPIN to CANIN as per context
    HERBIVORE("HERBIVORE", 11, "11"),
    FOLIVORE("FOLIVORE", 12, "12");

    fun nameToString(): String {
        return label ?: ""
    }

   

    companion object {
        private val categoryMap: Map<Int, Espece> = values().associateBy { it.categorie }
        private val nameMap: Map<String, Espece> = values().associateBy { it.label ?: "" }
        private val uuidMap: Map<String, Espece> = values().associateBy { it.uuid }

        fun getStringFromInt(id: Int): String {
            return categoryMap[id]?.nameToString() ?: CHIEN.nameToString() // Returns CHIEN name if not found
        }

        fun getEnumFromInt(id: Int): Espece {
            return categoryMap[id] ?: CHIEN // Returns CHIEN enum if not found
        }

        fun getEnumFromString(id: String): Espece {
            return nameMap[id] ?: CHIEN // Returns CHIEN enum if not found
        }

        fun getFromId(i: String): Espece{ // Return type changed to Espece? to handle null if not found
            return uuidMap[i]  ?: CHIEN// Returns null if not found
        }

        fun valuesExcept(): List<Espece> {
            return values().filterNot { it == CH }
        }
    }
}