package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumerise.Espece
import fr.vetbrain.vetnutri_mp.Enumerise.Sex
import kotlinx.datetime.LocalDate

class AnimalViewModel {
    var name by mutableStateOf<String?>(null)
    var dead by mutableStateOf<Boolean>(false)
    var id by mutableStateOf<String?>(null)
    var selectedSex by mutableStateOf<Sex?>(null)
    var selectedEspece by mutableStateOf<Espece?>(null)
    var ownerName by mutableStateOf<String?>(null)
    var birthdate by mutableStateOf<LocalDate?>(null)
    var race by mutableStateOf<String?>(null)
    var summary by mutableStateOf<String?>(null)

    fun createAnimal(): AnimalEv? {
        return selectedEspece?.let { espece ->
            AnimalEv(
                    name = name,
                    dead = dead,
                    id = id,
                    sexId = selectedSex?.id,
                    specieId = espece.uuid,
                    ownerName = ownerName,
                    birthdate = birthdate,
                    race = race,
                    summary = summary
            )
        }
    }

    fun isValid(): Boolean {
        return !name.isNullOrBlank() && selectedSex != null && selectedEspece != null
    }
}
