package com.example.mealjsonexample

data class CategoriesState(
    var isLoading: Boolean = false,
    var isError: Boolean = false,
    var error: String? = null,
    var result: List<Category> = listOf()
)

data class MealsState(
    var isLoading: Boolean = false,
    var isError: Boolean = false,
    var error: String? = null,
    var result: List<Meal> = listOf()
)

class MealsRepository {
    private var _categoriesState = CategoriesState()

    val categoriesState get() = _categoriesState

    private var _mealsState = MealsState()

    val mealsState get() = _mealsState

    private var _savedLocations = mutableListOf<Triple<String, Double, Double>>()

    suspend fun getAllCategories(): CategoriesResponse {
        return mealService.getAllCategories()
    }

    suspend fun getAllMealsByCategoryName(categoryName: String): MealsResponse {
        return mealService.getAllDishesByCategoryName(categoryName)
    }

    suspend fun getMealDetails(mealId: String): MealDetails? {
        val response = mealService.getMealDetails(mealId)
        return response.meals.firstOrNull()
    }

    fun saveMealLocation(meal: String, latitude: Double, longitude: Double) {
        _savedLocations.add(Triple(meal, latitude, longitude))
    }
}