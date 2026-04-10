interface NominatimApiService {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String
    ): List<NominatimResult>
}