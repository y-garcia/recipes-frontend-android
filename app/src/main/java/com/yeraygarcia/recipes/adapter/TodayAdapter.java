package com.yeraygarcia.recipes.adapter;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.RecipeDetailActivity;
import com.yeraygarcia.recipes.RecipeDetailFragment;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.List;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.RecipesViewHolder> {

    private List<Recipe> mRecipes;

    private final FragmentActivity mParentActivity;

    // Constructors

    public TodayAdapter(FragmentActivity parent) {
        mParentActivity = parent;
    }

    // Internal classes

    class RecipesViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;

        private RecipesViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.textview_recipe_name);

            itemName.setOnClickListener(view -> {
                final long recipeId = mRecipes.get(getAdapterPosition()).getId();
                // open Recipe Detail activity
                Intent intent = new Intent(mParentActivity, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, recipeId);
                mParentActivity.startActivity(intent);
            });
        }
    }

    // Adapter methods

    @NonNull
    @Override
    public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mParentActivity).inflate(R.layout.item_recipe_today, parent, false);
        return new RecipesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesViewHolder holder, int position) {
        if (mRecipes != null) {
            final Recipe currentRecipe = mRecipes.get(position);
            holder.itemName.setText(currentRecipe.getName());
        } else {
            // Covers the case of data not being ready yet.
            holder.itemName.setText(R.string.no_recipes);
        }
    }

    @Override
    public int getItemCount() {
        if (mRecipes != null) {
            return mRecipes.size();
        }
        return 0;
    }

    // Methods

    public void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
        notifyDataSetChanged();
    }
}
