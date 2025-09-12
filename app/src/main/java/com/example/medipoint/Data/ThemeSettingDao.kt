package com.example.medipoint.Repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medipoint.Data.ThemeSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeSettingDao {
    @Query("SELECT * FROM theme_settings WHERE id = 1")
    fun getThemeSetting(): Flow<ThemeSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemeSetting(themeSetting: ThemeSetting)

    @Update
    suspend fun updateThemeSetting(themeSetting: ThemeSetting)
}
