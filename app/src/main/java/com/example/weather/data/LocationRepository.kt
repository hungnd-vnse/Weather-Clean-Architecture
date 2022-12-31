package com.example.weather.data

import android.annotation.SuppressLint
import com.example.weather.di.DefaultDispatcher
import com.example.weather.di.IoDispatcher
import com.example.weather.model.geocoding.Coordinate
import com.example.weather.utils.Result
import com.example.weather.utils.Result.Error
import com.example.weather.utils.Result.Success
import com.example.weather.utils.toCoordinate
import com.example.weather.utils.toLocation
import com.example.weather.utils.toUnifiedCoordinate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks.await
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Interface for Repository of Location DataType
 */
interface LocationRepository {

    /**
     * Get Coordinate by call Remote Data Source to update Local Data Source if needed then
     * call Local Data Source to get result
     * @param city CityName will be converted to get Coordinate
     */
    suspend fun getCoordinateByCity(city: String, forceUpdate: Boolean): Result<Coordinate>

    /**
     * Get CityName by call Remote Data Source to update Local Data Source if needed then
     * call Local Data Source to get result
     * @param coordinate Location will be converted to get CityName
     */
    suspend fun getCityByCoordinate(coordinate: Coordinate, forceUpdate: Boolean): Result<String>

    /**
     * Get the Current Location of the Device
     */
    suspend fun getCurrentCoordinate(): Coordinate
}

/**
 * Implementation for Repository of Location DataType
 */
class DefaultLocationRepository(
    private val locationLocalDataSource: LocationDataSource,
    private val locationRemoteDataSource: LocationDataSource,
    private val client: FusedLocationProviderClient,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocationRepository {

    override suspend fun getCoordinateByCity(city: String, forceUpdate: Boolean): Result<Coordinate> {
        return withContext(ioDispatcher) {
            if (forceUpdate) {
                try {
                    updateLocationFromRemoteDataSource(city)
                } catch (ex: Exception) {
                    Error(ex)
                }
            }
            locationLocalDataSource.getCoordinate(city)
        }
    }

    override suspend fun getCityByCoordinate(coordinate: Coordinate, forceUpdate: Boolean): Result<String> {
        return withContext(ioDispatcher) {
            if (forceUpdate) {
                try {
                    updateLocationFromRemoteDataSource(coordinate)
                } catch (ex: Exception) {
                    Error(ex)
                }
            }
            locationLocalDataSource.getCity(coordinate.toUnifiedCoordinate())
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentCoordinate(): Coordinate = withContext(defaultDispatcher) {
        val locationTask = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        await(locationTask).toCoordinate()
    }

    private suspend fun updateLocationFromRemoteDataSource(city: String) {
        when (val coordinate = locationRemoteDataSource.getCoordinate(city)) {
            is Success -> {
                locationLocalDataSource.saveLocation(
                    coordinate.data.toUnifiedCoordinate().toLocation(city)
                )
            }
            is Error -> throw coordinate.exception
        }
    }

    private suspend fun updateLocationFromRemoteDataSource(coordinate: Coordinate) {
        when (val city = locationRemoteDataSource.getCity(coordinate)) {
            is Success -> {
                locationLocalDataSource.saveLocation(
                    coordinate.toUnifiedCoordinate().toLocation(city.data)
                )
            }
            is Error -> throw city.exception
        }
    }
}
