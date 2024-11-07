package com.example.mealjsonexample

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private val retrofit = Retrofit.Builder()
    .baseUrl("https://www.themealdb.com/api/json/v1/1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val mealService = retrofit.create(ApiService::class.java)

interface ApiService {
    @GET("categories.php")
    suspend fun getAllCategories(): CategoriesResponse

    @GET("filter.php")
    suspend fun getAllDishesByCategoryName(@Query("c") categoryName: String) : MealsResponse

    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") mealId: String): MealDetailsResponse
}