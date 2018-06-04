package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(
        tableName = "recipe_tag",
        indices = {
                @Index(value = {"recipe_id", "tag_id"}, unique = true),
                @Index("recipe_id"),
                @Index("tag_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id"),
                @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tag_id")
        }
)
public class RecipeTag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "recipe_id")
    private long recipeId;

    @ColumnInfo(name = "tag_id")
    private long tagId;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public RecipeTag(long recipeId, long tagId) {
        this.recipeId = recipeId;
        this.tagId = tagId;
    }

    public RecipeTag(long id, long recipeId, long tagId) {
        this.id = id;
        this.recipeId = recipeId;
        this.tagId = tagId;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
