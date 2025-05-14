interface FirebaseApi {
    @GET("scores.json?orderBy=%22score%22&limitToLast=10")
    fun getTopScores(): Call<Map<String, ScoreEntry>>
}
