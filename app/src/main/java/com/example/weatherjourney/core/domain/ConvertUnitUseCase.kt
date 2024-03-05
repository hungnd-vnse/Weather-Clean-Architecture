package com.example.weatherjourney.core.domain

import com.example.weatherjourney.core.common.constant.DATE_24_PATTERN
import com.example.weatherjourney.core.common.constant.DATE_AM_PM_PATTERN
import com.example.weatherjourney.core.common.constant.GENERAL_TIME_FORMATTER

import com.example.weatherjourney.core.model.location.SavedCity
import com.example.weatherjourney.core.model.unit.AllUnit
import com.example.weatherjourney.core.model.unit.PressureUnit
import com.example.weatherjourney.core.model.unit.TemperatureUnit
import com.example.weatherjourney.core.model.unit.TimeFormatUnit
import com.example.weatherjourney.core.model.unit.WindSpeedUnit
import com.example.weatherjourney.core.model.weather.AllWeather
import com.example.weatherjourney.core.model.weather.convertPressureUnit
import com.example.weatherjourney.core.model.weather.convertTemperatureUnit
import com.example.weatherjourney.core.model.weather.convertTimeFormatUnit
import com.example.weatherjourney.core.model.weather.convertWindSpeedUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ConvertUnitUseCase @Inject constructor() {

    operator fun invoke(allWeather: AllWeather, allUnit: AllUnit?): AllWeather {
        var tempWeather = when (allUnit?.temperature) {
            TemperatureUnit.FAHRENHEIT -> allWeather.convertTemperatureUnit {
                convertCelsiusToFahrenheit(it)
            }

            else -> allWeather
        }

        tempWeather = when (allUnit?.windSpeed) {
            WindSpeedUnit.METER_PER_SECOND -> tempWeather.convertWindSpeedUnit { convertKmhToMs(it) }
            WindSpeedUnit.MILE_PER_HOUR -> tempWeather.convertWindSpeedUnit { convertKmhToMph(it) }
            else -> tempWeather
        }

        tempWeather = when (allUnit?.pressure) {
            PressureUnit.INCH_OF_MERCURY -> tempWeather.convertPressureUnit { convertHPaToInHg(it) }
            else -> tempWeather
        }

        tempWeather = when (allUnit?.timeFormat) {
            TimeFormatUnit.AM_PM -> tempWeather.convertTimeFormatUnit { convertTimeFormatToAmPm(it) }
            else -> tempWeather.convertTimeFormatUnit { convertTimeFormatTo24(it) }
        }

        return tempWeather
    }

    operator fun invoke(cities: List<SavedCity>, tUnit: TemperatureUnit?) = cities.map {
        it.copy(
            temp = when (tUnit) {
                TemperatureUnit.FAHRENHEIT -> convertCelsiusToFahrenheit(it.temp)
                else -> it.temp
            },
        )
    }

    private fun convertCelsiusToFahrenheit(celsius: Double) = celsius * 9 / 5 + 32

    private fun convertKmhToMs(kmh: Double) = kmh / 3.6

    private fun convertKmhToMph(kmh: Double) = kmh / 1.609344

    private fun convertHPaToInHg(hPa: Double) = hPa * 0.0295299830714

    private fun convertTimeFormatToAmPm(timeStr: String): String {
        val todayDateTime = LocalDateTime.parse(timeStr, GENERAL_TIME_FORMATTER)
        return todayDateTime.format(DateTimeFormatter.ofPattern(DATE_AM_PM_PATTERN))
    }

    private fun convertTimeFormatTo24(timeStr: String): String {
        val todayDateTime = LocalDateTime.parse(timeStr, GENERAL_TIME_FORMATTER)
        return todayDateTime.format(DateTimeFormatter.ofPattern(DATE_24_PATTERN))
    }
}