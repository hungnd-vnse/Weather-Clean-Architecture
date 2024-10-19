package com.example.weatherjourney.core.domain

import com.example.weatherjourney.core.common.util.TimeUtils
import com.example.weatherjourney.core.model.info.Weather
import com.example.weatherjourney.core.model.info.convertPressure
import com.example.weatherjourney.core.model.info.convertTemperature
import com.example.weatherjourney.core.model.info.convertTimeFormat
import com.example.weatherjourney.core.model.info.convertWindSpeed
import com.example.weatherjourney.core.model.unit.PressureUnit
import com.example.weatherjourney.core.model.unit.TemperatureUnit
import com.example.weatherjourney.core.model.unit.TimeFormatUnit
import com.example.weatherjourney.core.model.unit.WindSpeedUnit
import javax.inject.Inject

class ConvertUnitUseCase @Inject constructor() {

    operator fun invoke(
        weather: Weather,
        temperatureUnit: TemperatureUnit? = null,
        windSpeedUnit: WindSpeedUnit? = null,
        pressureUnit: PressureUnit? = null,
        timeFormatUnit: TimeFormatUnit? = null
    ): Weather = weather.apply {
        temperatureUnit?.let { convertTemperatureIfNeeded(it) }
        windSpeedUnit?.let { convertWindSpeedIfNeeded(it) }
        pressureUnit?.let { convertPressureIfNeeded(it) }
        timeFormatUnit?.let { convertTimeFormatIfNeeded(it) }
    }

    private fun Weather.convertTemperatureIfNeeded(unit: TemperatureUnit): Weather {
        return when (unit) {
            TemperatureUnit.FAHRENHEIT -> this.convertTemperature(::convertCelsiusToFahrenheit)
            else -> this
        }
    }

    private fun Weather.convertWindSpeedIfNeeded(unit: WindSpeedUnit): Weather {
        return when (unit) {
            WindSpeedUnit.METER_PER_SECOND -> this.convertWindSpeed(::convertKmhToMs)
            WindSpeedUnit.MILE_PER_HOUR -> this.convertWindSpeed(::convertKmhToMph)
            else -> this
        }
    }

    private fun Weather.convertPressureIfNeeded(unit: PressureUnit): Weather {
        return when (unit) {
            PressureUnit.INCH_OF_MERCURY -> this.convertPressure(::convertHPaToInHg)
            else -> this
        }
    }

    private fun Weather.convertTimeFormatIfNeeded(unit: TimeFormatUnit): Weather {
        return when (unit) {
            TimeFormatUnit.AM_PM -> this.convertTimeFormat(TimeUtils::formatTimeToAmPm)
            else -> this.convertTimeFormat(TimeUtils::formatTimeTo24Hour)
        }
    }

    private fun convertCelsiusToFahrenheit(celsius: Double) = celsius * 9 / 5 + 32
    private fun convertKmhToMs(kmh: Double) = kmh / 3.6
    private fun convertKmhToMph(kmh: Double) = kmh / 1.609344
    private fun convertHPaToInHg(hPa: Double) = hPa * 0.02953
}
