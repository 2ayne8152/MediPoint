package com.example.medipoint.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMedicalInfo(medicalInfo: MedicalInfoEntity)

    // @Update // This is redundant if insertOrUpdateMedicalInfo uses REPLACE
    // suspend fun updateMedicalInfo(medicalInfo: MedicalInfoEntity)

    @Query("SELECT * FROM medical_info WHERE userId = :userId LIMIT 1")
    fun getMedicalInfoByUserId(userId: String): Flow<MedicalInfoEntity?>

    @Query("SELECT * FROM medical_info WHERE userId = :userId LIMIT 1")
    suspend fun getMedicalInfoByUserIdOnce(userId: String): MedicalInfoEntity?

    @Query("DELETE FROM medical_info WHERE userId = :userId")
    suspend fun deleteMedicalInfoByUserId(userId: String) // Renamed for clarity

    @Query("DELETE FROM medical_info")
    suspend fun clearAllMedicalInfo()
}
