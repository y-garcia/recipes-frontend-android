package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipesViewHolder> {

    private static final String TAG = "YGQ: " + RecipeAdapter.class.getSimpleName();

    private List<Recipe> mRecipes; // Cached copy of recipes

    private final RecipeListActivity mParentActivity;
    private final boolean mTwoPane;

    // Constructors

    RecipeAdapter(RecipeListActivity parent, boolean twoPane) {
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    // Internal classes

    class RecipesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView recipeItemView;

        private RecipesViewHolder(View itemView) {
            super(itemView);
            recipeItemView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final long recipeId = mRecipes.get(getAdapterPosition()).getId();

            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putLong(RecipeDetailFragment.EXTRA_RECIPE_ID, recipeId);
                RecipeDetailFragment fragment = new RecipeDetailFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.recipe_detail_container, fragment)
                        .commit();
            } else {
                Intent intent = new Intent(mParentActivity, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailFragment.EXTRA_RECIPE_ID, recipeId);

                mParentActivity.startActivity(intent);
            }
        }
    }

    // Overrides

    @NonNull
    @Override
    public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mParentActivity).inflate(R.layout.recipe_list_content, parent, false);
        return new RecipesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesViewHolder holder, int position) {
        if (mRecipes != null) {
            final Recipe currentRecipe = mRecipes.get(position);
            holder.recipeItemView.setText(currentRecipe.getName());
        } else {
            // Covers the case of data not being ready yet.
            holder.recipeItemView.setText(R.string.no_recipes);
        }
    }

    void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mRecipes has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mRecipes != null) {
            return mRecipes.size();
        }
        return 0;
    }
}
