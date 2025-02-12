package fr.vetbrain.vetnutri_mp.Database

import fr.vetbrain.vetnutri_mp.Data.*

interface DatabaseInterface {
    suspend fun getAllAnimals(): List<AnimalEv>
    suspend fun getAnimalById(id: String): AnimalEv?
    suspend fun insertAnimal(animal: AnimalEv)
    suspend fun updateAnimal(animal: AnimalEv)
    suspend fun deleteAnimal(animal: AnimalEv)

    suspend fun getAllConsultations(): List<ConsultationEv>
    suspend fun getConsultationById(id: String): ConsultationEv?
    suspend fun insertConsultation(consultation: ConsultationEv)
    suspend fun updateConsultation(consultation: ConsultationEv)
    suspend fun deleteConsultation(consultation: ConsultationEv)

    suspend fun getAllRations(): List<Ration>
    suspend fun getRationById(id: String): Ration?
    suspend fun insertRation(ration: Ration)
    suspend fun updateRation(ration: Ration)
    suspend fun deleteRation(ration: Ration)
}
