package com.example.mealjsonexample

object Graph {
    val mainScreen: Screen = Screen("MainScreen")
    val secondScreen: Screen = Screen("SecondScreen")
    val mealDetailsScreen: Screen = Screen("MealDetailsScreen")
    val locationScreen:  Screen = Screen("LocationSelectionScreen")
}

data class Screen(
    val route: String,
)