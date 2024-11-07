package com.example.mealjsonexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealsViewModel : ViewModel() {
    private var mealsRepository = MealsRepository()

    private var _categories = MutableStateFlow(mealsRepository.categoriesState)
    val categoriesState = _categories.asStateFlow()

    private var _meals = MutableStateFlow(mealsRepository.mealsState)
    val mealsState = _meals.asStateFlow()

    private var _chosenCategory = MutableStateFlow<String?>(null)
    val chosenCategory = _chosenCategory.asStateFlow()

    private var _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var _savedLocations = MutableStateFlow(listOf<Triple<String, Double, Double>>())
    val savedLocations = _savedLocations.asStateFlow()

    private var _currentLocation = MutableStateFlow<Triple<Double, Double, Boolean>?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    init {
        getAllCategories()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterMealsBySearchQuery(query)
    }

    private fun filterMealsBySearchQuery(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                getAllMealsByCategoryName(_chosenCategory.value ?: "")
            } else {
                val filteredMeals = mealsRepository.mealsState.result.filter {
                    it.mealName.contains(query, ignoreCase = true)
                }
                _meals.value = _meals.value.copy(result = filteredMeals)
            }
        }
    }

    fun setChosenCategory(category: String) {
        _chosenCategory.value = category
        getAllMealsByCategoryName(_chosenCategory.value!!)
    }

    private fun getAllMealsByCategoryName(categoryName: String) {
        viewModelScope.launch {
            try {
                _meals.value = _meals.value.copy(isLoading = true)
                val response = mealsRepository.getAllMealsByCategoryName(categoryName)
                _meals.value = _meals.value.copy(
                    isLoading = false,
                    isError = false,
                    result = response.meals
                )
            } catch (e: Exception) {
                _meals.value = _meals.value.copy(
                    isError = true,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun getAllCategories() {
        viewModelScope.launch {
            try {
                _categories.value = _categories.value.copy(isLoading = true)
                val response = mealsRepository.getAllCategories()
                _categories.value = _categories.value.copy(
                    isLoading = false,
                    isError = false,
                    result = response.categories
                )
            } catch (e: Exception) {
                _categories.value = _categories.value.copy(
                    isError = true,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private var _selectedMealDetails = MutableStateFlow<MealDetails?>(null)
    val selectedMealDetails = _selectedMealDetails.asStateFlow()

    fun getMealDetails(mealId: String) {
        viewModelScope.launch {
            try {
                val details = mealsRepository.getMealDetails(mealId)
                _selectedMealDetails.value = details
            } catch (_: Exception) {
            }
        }
    }

    fun saveMealLocation(meal: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            mealsRepository.saveMealLocation(meal, latitude, longitude)
            _savedLocations.value += (Triple(meal, latitude, longitude))
        }
    }

    fun getSavedLocations(): List<Triple<String, Double, Double>> {
        return _savedLocations as List<Triple<String, Double, Double>>
    }

    fun setCurrentLocation(latitude: Double, longitude: Double, isLoading: Boolean) {
        _currentLocation.value = Triple(latitude, longitude, isLoading)
    }
}
