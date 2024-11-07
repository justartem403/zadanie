package com.example.mealjsonexample

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun Navigation(
    modifier: Modifier,
    navigationController: NavHostController,
) {
    val viewModel: MealsViewModel = viewModel()
    NavHost(
        modifier = modifier,
        navController = navigationController,
        startDestination = Graph.mainScreen.route
    ) {
        composable(route = Graph.mainScreen.route) {
            MainScreen(viewModel, navigationController)
        }
        composable(route = Graph.secondScreen.route) {
            SecondScreen(viewModel, navigationController)
        }
        composable(route = "${Graph.mealDetailsScreen.route}/{mealId}") { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")
            mealId?.let { viewModel.getMealDetails(it) }
            MealDetailsScreen(viewModel) {
                navigationController.popBackStack()
            }
        }
        composable(route = Graph.locationScreen.route) {
            LocationSelectionScreen(viewModel, navigationController)
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun LocationSelectionScreen(
    viewModel: MealsViewModel,
    navigationController: NavHostController
) {
    val meal = viewModel.selectedMealDetails.collectAsState()
    var currentLocation by remember {
        mutableStateOf(LatLng(0.0,0.0)) }
    val cameraPositionState= rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                currentLocation = latLng
            }
        ) {
            Marker(
                state = MarkerState(position = currentLocation),
                title = "Выбранное местоположение"
            )
        }

        Button(
            onClick = {
                viewModel.saveMealLocation(meal = meal.value!!.strMeal,  currentLocation.latitude, currentLocation.longitude)
                navigationController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Сохранить местоположение")
        }
    }
}
@Composable
fun SecondScreen(viewModel: MealsViewModel, navigationController: NavHostController) {
    val categoryName = viewModel.chosenCategory.collectAsState()
    val dishesState = viewModel.mealsState.collectAsState()
    val searchQuery = viewModel.searchQuery.collectAsState()

    Column {
        TextField(
            value = searchQuery.value,
            onValueChange = { newValue ->
                viewModel.updateSearchQuery(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Поиск блюд") },
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.updateSearchQuery("")
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        when {
            dishesState.value.isLoading -> {
                LoadingScreen()
            }
            dishesState.value.isError -> {
                ErrorScreen(dishesState.value.error!!)
            }
            dishesState.value.result.isNotEmpty() -> {
                val filteredDishes = dishesState.value.result.filter {
                    it.mealName.contains(searchQuery.value, ignoreCase = true)
                }
                DishesScreen(filteredDishes, navigationController)
            }
            else -> {
                Text(
                    text = "Блюд нет",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun DishesScreen(result: List<Meal>, navigationController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(result) { meal ->
            DishItem(meal) { mealId ->
                navigationController.navigate("${Graph.mealDetailsScreen.route}/$mealId")
            }
        }
    }
}


@Composable
fun DishItem(meal: Meal, onItemClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(color = Color.LightGray)
            .clickable { onItemClick(meal.idMeal) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                modifier = Modifier.height(80.dp),
                model = meal.strMealThumb,
                contentDescription = null
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = meal.mealName
            )
        }
    }
}


@Composable
fun MainScreen(viewModel: MealsViewModel, navigationController: NavHostController) {
    val categoriesState = viewModel.categoriesState.collectAsState()
    val savedLocations = viewModel.savedLocations.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Кнопка для перехода к выбору местоположения
        Button(
            onClick = { navigationController.navigate(Graph.locationScreen.route) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Выбрать местоположение",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Выбрать местоположение")
        }

        // Отображение сохраненных местоположений
        if (savedLocations.value.isNotEmpty()) {
            Text(
                text = "Сохраненные местоположения:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            savedLocations.value.forEach { location ->
                Text(
                    text = "Широта: ${location}, Долгота: $location",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Основной контент с категориями
        when {
            categoriesState.value.isLoading -> {
                LoadingScreen()
            }
            categoriesState.value.isError -> {
                ErrorScreen(categoriesState.value.error!!)
            }
            categoriesState.value.result.isNotEmpty() -> {
                CategoriesScreen(viewModel, categoriesState.value.result, navigationController)
            }
        }
    }
}

@Composable
fun CategoriesScreen(
    viewModel: MealsViewModel,
    result: List<Category>,
    navigationController: NavHostController
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(result) {
            CategoryItem(viewModel, it, navigationController)
        }
    }
}

@Composable
fun CategoryItem(
    viewModel: MealsViewModel,
    category: Category,
    navigationController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(color = Color.Transparent)
            .clickable {
                viewModel.setChosenCategory(category.strCategory)
                navigationController.navigate(Graph.secondScreen.route)
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = category.strCategoryThumb,
                contentDescription = null,
                modifier = Modifier.height(100.dp)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = category.strCategory,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            if (category.latitude != 0.0 && category.longitude != 0.0) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Местоположение сохранено",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(error: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error)
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun MealDetailsScreen(viewModel: MealsViewModel, onBackClick: () -> Unit) {
    val mealDetails by viewModel.selectedMealDetails.collectAsState()
    val location by viewModel.currentLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        mealDetails?.let { details ->
            AsyncImage(
                model = details.strMealThumb,
                contentDescription = details.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Text(
                text = details.strMeal,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Отображение местоположения, если оно сохранено
            if (location != null) {
                Text(
                    text = "Местоположение:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Широта: ${location}, Долгота: ${location?.second}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Категория: ${details.strCategory}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Регион: ${details.strArea}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Инструкция по приготовлению:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = details.strInstructions,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Назад")
            }
        }
    }
}
