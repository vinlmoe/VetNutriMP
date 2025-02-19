package fr.vetbrain.vetnutri_mp.Database

import fr.vetbrain.vetnutri_mp.Data.*
import kotlinx.coroutines.flow.Flow

interface DatabaseInterface {
    fun getAllAnimals(): Flow<List<AnimalEv>>
    suspend fun getAnimalByUuid(uuid: String): AnimalEv?
    suspend fun insertAnimal(animal: AnimalEv): Long
    suspend fun updateAnimal(animal: AnimalEv)
    suspend fun deleteAnimal(animal: AnimalEv)

    fun getAllConsultations(): Flow<List<ConsultationEv>>
    suspend fun getConsultationByUuid(uuid: String): ConsultationEv?
    suspend fun insertConsultation(consultation: ConsultationEv): Long
    suspend fun updateConsultation(consultation: ConsultationEv)
    suspend fun deleteConsultation(consultation: ConsultationEv)

    fun getAllRations(): Flow<List<Ration>>
    suspend fun getRationByUuid(uuid: String): Ration?
    suspend fun insertRation(ration: Ration): Long
    suspend fun updateRation(ration: Ration)
    suspend fun deleteRation(ration: Ration)
}
