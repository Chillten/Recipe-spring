package com.bogovich.recipe.services;

import com.bogovich.recipe.models.Ingredient;
import com.bogovich.recipe.models.Recipe;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IngredientServiceImplTest {

    private IngredientService ingredientService;

    @Mock
    private RecipeService recipeService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ingredientService = new IngredientServiceImpl(recipeService);
    }


    @Test
    public void findByRecipeIdAndIngridientId() throws Exception {
        Recipe recipe = setUpRecipe(randomUUID().toString());
        Ingredient ingredient1 = setUpIngredient(recipe);
        Ingredient ingredient2 = setUpIngredient(recipe);

        when(recipeService.findById(recipe.getId())).thenReturn(Mono.just(recipe));

        assertEquals(ingredient1,
                     ingredientService.findByRecipeIdAndIngridientId(recipe.getId(),
                                                                     ingredient1.getId()).block());
        verify(recipeService, times(1)).findById(recipe.getId());

        assertEquals(ingredient2,
                     ingredientService.findByRecipeIdAndIngridientId(recipe.getId(),
                                                                     ingredient2.getId()).block());
        verify(recipeService, times(2)).findById(recipe.getId());
    }

    @Test(expected = RuntimeException.class)
    public void findByRecipeIdAndIngridientIdInvalidRecipeId() throws Exception {
        when(recipeService.findById(anyString())).thenReturn(null);
        ingredientService.findByRecipeIdAndIngridientId(randomUUID().toString(), randomUUID().toString()).block();
    }

    @Test(expected = RuntimeException.class)
    public void findByRecipeIdAndIngridientIdRecipeNotContainSuchIng() throws Exception {
        when(recipeService.findById(anyString())).thenReturn(Mono.just(new Recipe()));
        ingredientService.findByRecipeIdAndIngridientId(randomUUID().toString(), randomUUID().toString()).block();
    }

    @Test
    public void saveIngredientNew() throws Exception {
        Recipe recipe = setUpRecipe(randomUUID().toString());
        Ingredient ingredient1 = setUpIngredient(recipe);
        Ingredient ingredient2 = setUpIngredient(recipe);
        Ingredient ingredientNew = new Ingredient();

        when(recipeService.findById(recipe.getId())).thenReturn(Mono.just(recipe));
        when(recipeService.saveRecipe(any())).thenReturn(Mono.empty());
        ingredientService.saveIngredient(recipe.getId(), ingredientNew).block();

        assertTrue(recipe.getIngredients().contains(ingredientNew));
        assertEquals(3, recipe.getIngredients().size());
    }

    @Test
    public void saveIngredientUpdate() throws Exception {
        Recipe recipe = setUpRecipe(randomUUID().toString());
        Ingredient ingredient1 = setUpIngredient(recipe);
        Ingredient ingredient2 = setUpIngredient(recipe);
        Ingredient ingredientUpdate = new Ingredient();
        ingredientUpdate.setId(ingredient1.getId());
        ingredientUpdate.setAmount(new BigDecimal(100));
        ingredientUpdate.setDescription("123");

        when(recipeService.findById(recipe.getId())).thenReturn(Mono.just(recipe));
        when(recipeService.saveRecipe(any())).thenReturn(Mono.empty());
        ingredientService.saveIngredient(recipe.getId(), ingredientUpdate).block();

        assertTrue(recipe.getIngredients()
                         .stream()
                         .anyMatch(i -> Ingredient.isEqualByValue(i, ingredientUpdate)));
        assertEquals(2, recipe.getIngredients().size());
    }

    @Test(expected = RuntimeException.class)
    public void saveIngredientRecipeNotExist() throws Exception {
        when(recipeService.findById(anyString())).thenReturn(null);
        ingredientService.saveIngredient(randomUUID().toString(), new Ingredient()).block();
    }

    @Test
    public void deleteIngredient() throws Exception {
        Recipe recipe = setUpRecipe(randomUUID().toString());
        Ingredient ingredient1 = setUpIngredient(recipe);
        Ingredient ingredient2 = setUpIngredient(recipe);

        when(recipeService.findById(recipe.getId())).thenReturn(Mono.just(recipe));
        when(recipeService.saveRecipe(any())).thenReturn(Mono.empty());
        ingredientService.deleteIngredient(recipe.getId(), ingredient1.getId()).block();

        assertFalse(recipe.getIngredients()
                          .stream()
                          .anyMatch(i -> Ingredient.isEqualByValue(i, ingredient1)));
        assertEquals(1, recipe.getIngredients().size());
        verify(recipeService, times(1)).findById(recipe.getId());
        verify(recipeService, times(1)).saveRecipe(any());

        ingredientService.deleteIngredient(recipe.getId(), ingredient2.getId()).block();
        assertFalse(recipe.getIngredients()
                          .stream()
                          .anyMatch(i -> Ingredient.isEqualByValue(i, ingredient2)));
        assertEquals(0, recipe.getIngredients().size());
        verify(recipeService, times(2)).findById(recipe.getId());
        verify(recipeService, times(2)).saveRecipe(any());
    }

    @Test
    public void deleteIngredientNotContain() throws Exception {
        Recipe recipe = setUpRecipe(randomUUID().toString());
        Ingredient ingredient1 = setUpIngredient(recipe);
        Ingredient ingredient2 = new Ingredient();

        when(recipeService.findById(recipe.getId())).thenReturn(Mono.just(recipe));
        when(recipeService.saveRecipe(any())).thenReturn(Mono.empty());

        assertFalse(recipe.getIngredients().contains(ingredient2));
        ingredientService.deleteIngredient(recipe.getId(), ingredient2.getId()).block();
        assertFalse(recipe.getIngredients().contains(ingredient2));
        assertEquals(1, recipe.getIngredients().size());

        verify(recipeService, times(1)).findById(recipe.getId());
        verify(recipeService, times(1)).saveRecipe(any());
    }

    private Recipe setUpRecipe(String id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        return recipe;
    }

    private Ingredient setUpIngredient(Recipe recipe) {
        final Ingredient ingredient = new Ingredient();
        recipe.addIngredient(ingredient);
        return ingredient;
    }
}
