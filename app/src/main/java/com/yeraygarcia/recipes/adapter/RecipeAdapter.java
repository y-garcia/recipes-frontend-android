package com.yeraygarcia.recipes.adapter;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.RecipeDetailActivity;
import com.yeraygarcia.recipes.RecipeDetailFragment;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipesViewHolder> implements Filterable {

    private List<Recipe> mRecipes;
    private List<Long> mRecipeIdsInShoppingList = new ArrayList<>();

    private RecipeFilter mFilter;

    private final FragmentActivity mParentActivity;
    private final RecipeViewModel mViewModel;

    // Constructors

    public RecipeAdapter(FragmentActivity parent) {
        mParentActivity = parent;
        mViewModel = ViewModelProviders.of(parent).get(RecipeViewModel.class);
    }

    // Internal classes

    private class RecipeFilter extends Filter {

        private final List<Recipe> originalList;
        private final List<Recipe> filteredList;

        private RecipeFilter(List<Recipe> originalList) {
            super();
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (charSequence.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Recipe recipe : originalList) {
                    if (recipe.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(recipe);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            setRecipes(filteredList);
        }
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new RecipeFilter(mRecipes);
        }
        return mFilter;
    }

    class RecipesViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;
        ConstraintLayout itemServingsContainer;
        ImageView itemDecrease;
        TextView itemServings;
        TextView itemServingsLabel;
        ImageView itemIncrease;
        ImageView itemAddToCart;

        private RecipesViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.textview_recipe_name);
            itemServingsContainer = itemView.findViewById(R.id.include_servings);
            itemDecrease = itemView.findViewById(R.id.imageview_decrease_servings);
            itemIncrease = itemView.findViewById(R.id.imageview_increase_servings);
            itemServings = itemView.findViewById(R.id.textview_servings);
            itemServingsLabel = itemView.findViewById(R.id.textview_servings_label);
            itemAddToCart = itemView.findViewById(R.id.imageview_add_to_cart);

            itemName.setOnClickListener(view -> {
                final long recipeId = mRecipes.get(getAdapterPosition()).getId();
                // open Recipe Detail activity
                Intent intent = new Intent(mParentActivity, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, recipeId);
                mParentActivity.startActivity(intent);
            });

            View.OnClickListener addToCartListener = view -> {
                Recipe recipe = mRecipes.get(getAdapterPosition());
                boolean isInShoppingList = mRecipeIdsInShoppingList.contains(recipe.getId());

                if (isInShoppingList) {
                    recipe.increasePortions();
                    mViewModel.updatePortionsInShoppingList(recipe);
                } else {
                    mViewModel.addRecipeToShoppingList(recipe);
                }
            };

            itemAddToCart.setOnClickListener(addToCartListener);
            itemIncrease.setOnClickListener(addToCartListener);
            itemServings.setOnClickListener(addToCartListener);
            itemServingsLabel.setOnClickListener(addToCartListener);

            itemDecrease.setOnClickListener(view -> {
                Recipe recipe = mRecipes.get(getAdapterPosition());
                boolean isInShoppingList = mRecipeIdsInShoppingList.contains(recipe.getId());

                if (isInShoppingList) {
                    if (recipe.getPortions() == 1) {
                        mViewModel.deleteRecipeFromShoppingList(recipe);
                    } else {
                        recipe.decreasePortions();
                        mViewModel.updatePortionsInShoppingList(recipe);
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mParentActivity).inflate(R.layout.item_recipe_name, parent, false);
        return new RecipesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesViewHolder holder, int position) {
        if (mRecipes != null) {
            final Recipe currentRecipe = mRecipes.get(position);

            holder.itemName.setText(currentRecipe.getName());
            holder.itemServings.setText(String.format(Locale.getDefault(), "%d", currentRecipe.getPortions()));

            if (currentRecipe.getPortions() == 1) {
                holder.itemDecrease.setImageResource(R.drawable.ic_remove_shopping_cart_24px);
            } else {
                holder.itemDecrease.setImageResource(R.drawable.ic_remove_24px);
            }

            boolean isInShoppingList = mRecipeIdsInShoppingList.contains(currentRecipe.getId());
            holder.itemServingsContainer.setVisibility(isInShoppingList ? View.VISIBLE : View.GONE);
            holder.itemAddToCart.setVisibility(isInShoppingList ? View.GONE : View.VISIBLE);
        } else {
            // Covers the case of data not being ready yet.
            holder.itemName.setText(R.string.no_recipes);
        }
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

    // Methods

    public List<Recipe> getRecipes() {
        return mRecipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
        notifyDataSetChanged();
    }

    public void setRecipeIdsInShoppingList(List<Long> recipeIds) {
        mRecipeIdsInShoppingList = recipeIds;
        notifyDataSetChanged();
    }
}
