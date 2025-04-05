package fr.vetbrain.vetnutri_mp.View

sealed class Screen {
    object List : Screen()
    object Detail : Screen()
    object Create : Screen()
    object FoodList : Screen()
    object FoodEdit : Screen()
    object BiblioRefList : Screen()
}
