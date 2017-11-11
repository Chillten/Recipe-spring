package com.bogovich.recipe.models;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Data
@ToString(of = {"id", "description"})
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String description;
    @Min(0)
    private Integer prepTime;
    @Min(0)
    private Integer cookTime;
    @Min(0)
    private String servings;
    private String source;
    @URL
    private String url;
    @Lob
    @NotBlank
    private String directions;

    @Enumerated(value = EnumType.STRING)
    private Difficulty difficulty;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recipe")
    private Set<Ingredient> ingredients = new HashSet<>();

    @Lob
    private Byte[] image;

    @OneToOne(cascade = CascadeType.ALL)
    private Notes notes;

    @ManyToMany
    @JoinTable(name = "recipe_category",
               joinColumns = @JoinColumn(name = "recipe_id"),
               inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    public void setNotes(Notes notes) {
        this.notes = notes;
        notes.setRecipe(this);
    }

    public Recipe addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        ingredient.setRecipe(this);
        return this;
    }

    public Recipe updateIngredient(Ingredient newIngredientValue) {
        final Ingredient ingredient = ingredients.stream()
                                                 .filter(i -> i.getId()
                                                               .equals(newIngredientValue.getId()))
                                                 .findFirst()
                                                 .map(i -> i.updateValue(newIngredientValue))
                                                 .orElse(newIngredientValue);
        if (ingredient.getId() == null) {
            this.addIngredient(ingredient);
        }
        return this;
    }
}

