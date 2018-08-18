package com.yeraygarcia.recipes.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.yeraygarcia.recipes.database.UUIDTypeConverter;

import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "recipe_tag",
        indices = {
                @Index(value = {"recipe_id", "tag_id"}, unique = true),
                @Index("recipe_id"),
                @Index("tag_id")
        },
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = CASCADE),
                @ForeignKey(entity = Tag.class, parentColumns = "id", childColumns = "tag_id", onDelete = CASCADE)
        }
)
public class RecipeTag {

    @PrimaryKey
    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private UUID id;

    @NonNull
    @ColumnInfo(name = "recipe_id")
    @SerializedName("recipe_id")
    private UUID recipeId;

    @ColumnInfo(name = "tag_id")
    @SerializedName("tag_id")
    private UUID tagId;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    @Ignore
    public RecipeTag(@NonNull UUID recipeId, UUID tagId) {
        this.id = UUIDTypeConverter.newUUID();
        this.recipeId = recipeId;
        this.tagId = tagId;
    }

    public RecipeTag(@NonNull UUID id, @NonNull UUID recipeId, UUID tagId) {
        this.id = id;
        this.recipeId = recipeId;
        this.tagId = tagId;
    }

    // Getters and Setters /////////////////////////////////////////////////////////////////////////

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    @NonNull
    public UUID getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(@NonNull UUID recipeId) {
        this.recipeId = recipeId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }
}
