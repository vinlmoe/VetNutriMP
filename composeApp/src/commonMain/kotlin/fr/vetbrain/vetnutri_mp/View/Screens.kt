package fr.vetbrain.vetnutri_mp.View

public sealed class NavigationScreen {
    public object List : NavigationScreen()
    public object Detail : NavigationScreen()
    public object Create : NavigationScreen()
    public object FoodList : NavigationScreen()
    public object FoodEdit : NavigationScreen()
    public object BiblioRefList : NavigationScreen()
    public object BiblioRefEdit : NavigationScreen()
    public object EquationList : NavigationScreen()
    public object EquationEdit : NavigationScreen()
    public object ReferenceEvTabs : NavigationScreen()
}
