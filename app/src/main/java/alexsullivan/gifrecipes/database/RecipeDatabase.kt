package alexsullivan.gifrecipes.database

interface RecipeDatabase {

    fun gifRecipeDao(): GifRecipeDao
}