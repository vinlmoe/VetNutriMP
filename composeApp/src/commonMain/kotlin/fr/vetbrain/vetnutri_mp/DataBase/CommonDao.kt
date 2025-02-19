package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface AnimalDao {
    @Insert suspend fun insert(animal: AnimalEntity)

    @Update suspend fun update(animal:  AnimalEntity)

    @Delete suspend fun delete(animal: AnimalEntity)

    @Query("SELECT * FROM animals") suspend fun getAllAnimals(): List<AnimalEntity>

    @Query("SELECT * FROM animals WHERE id = :id") suspend fun getAnimalById(id: Int): AnimalEntity?
}

@Dao
interface FoodDao {
    @Insert suspend fun insert(food: FoodEntity)

    @Update suspend fun update(food: FoodEntity)

    @Delete suspend fun delete(food: FoodEntity)

    @Query("SELECT * FROM foods") suspend fun getAllFoods(): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE id = :id") suspend fun getFoodById(id: Int): FoodEntity?
}
