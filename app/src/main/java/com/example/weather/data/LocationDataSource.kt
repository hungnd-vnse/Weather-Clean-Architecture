package com.example.weather.data

import com.example.weather.model.database.Location
import com.example.weather.model.geocoding.Coordinate
import com.example.weather.network.ApiService
import com.example.weather.utils.Result
import com.example.weather.utils.Result.Error
import com.example.weather.utils.Result.Success
import com.example.weather.utils.toCoordinate
import java.net.UnknownHostException

/**
 * Interface for Data Source of Location DataType.
 */
interface LocationDataSource {

    /**
     * Get Coordinate by call Api or query Local Data Source.
     */
    suspend fun getCoordinate(city: String): Result<Coordinate>

    /**
     * Get CityName by call Api or query Local Data Source.
     */
    suspend fun getCityName(coordinate: Coordinate): Result<String>

    /**
     * Save Location in Local Data Source.
     */
    suspend fun saveLocation(location: Location)
}

/**
 * Implementation for Remote Data Source of Location DataType.
 */
class LocationRemoteDataSource(private val apiService: ApiService) : LocationDataSource {
    override suspend fun getCoordinate(city: String): Result<Coordinate> {
        return try {
            val result = apiService.getForwardGeocoding(city)
            Success(result.results.first().coordinate)
        } catch (ex: UnknownHostException) {
            Error(ex)
        } catch (ex: NoSuchElementException) {
            Error(NoSuchElementException("Invalid location"))
        }
    }

    override suspend fun getCityName(coordinate: Coordinate): Result<String> {
        return try {
            val result = apiService.getReverseGeocoding(
                "${coordinate.latitude}+${coordinate.longitude}"
            )
            Success(result.results.first().components.city)
        } catch (ex: UnknownHostException) {
            Error(ex)
        }
    }

    override suspend fun saveLocation(location: Location) {
        // Not required for the remote data source
    }
}

/**
 * Implementation for Local Data Source of Location DataType.
 */
class LocationLocalDataSource(private val locationDao: LocationDao) : LocationDataSource {
    override suspend fun getCoordinate(city: String): Result<Coordinate> {
        val location = locationDao.getLocationByCity(city)
        return if (location != null) {
            Success(location.toCoordinate())
        } else {
            Error(Exception("Location not found"))
        }
    }

    override suspend fun getCityName(coordinate: Coordinate): Result<String> {
        val location =
            locationDao.getLocationByCoordinate(coordinate.latitude, coordinate.longitude)
        return if (location != null) {
            Success(location.city)
        } else {
            Error(Exception("City not found"))
        }
    }

    override suspend fun saveLocation(location: Location) {
        locationDao.insertLocation(location)
    }
}