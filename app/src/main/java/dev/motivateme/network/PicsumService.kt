package dev.motivateme.network

import dev.motivateme.data.PicsumInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PicsumService {
    @GET("id/{id}/info")
    suspend fun getImageInfo(@Path("id") imageId: Int): Response<PicsumInfo>
}