package com.example.medipoint.Repository

import com.example.medipoint.Data.ThemeSetting
import kotlinx.coroutines.flow.Flow

class ThemeRepository(private val dao: ThemeSettingDao) {

    val themeSetting: Flow<ThemeSetting?> = dao.getThemeSetting()

    suspend fun saveTheme(isDarkMode: Boolean) {
        dao.insertThemeSetting(ThemeSetting(id = 1, isDarkMode = isDarkMode))
    }
}
