package com.example.weatherjourney.weather.presentation.search

import com.example.weatherjourney.weather.domain.model.SavedCity
import com.example.weatherjourney.weather.domain.model.SuggestionCity

data class WeatherSearchUiState(
    val cityAddress: String = "",
    val savedCities: List<SavedCity> = emptyList(),
    val suggestionCities: List<SuggestionCity> = emptyList(),
    val isLoading: Boolean = false
)
