package com.yeraygarcia.recipes;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.custom.CustomRecipeIngredient;
import com.yeraygarcia.recipes.database.entity.custom.RecipeDetail;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.RecipeIngredient;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

import java.util.List;

/**
 * A fragment representing a single Recipe detail screen.
 * This fragment is either contained in a {@link RecipeListActivity}
 * in two-pane mode (on tablets) or a {@link RecipeDetailActivity}
 * on handsets.
 */
public class RecipeDetailFragment extends Fragment {

    public static final String EXTRA_RECIPE_ID = "recipeId";
    public static final long DEFAULT_RECIPE_ID = -1;
    public static final String INSTANCE_RECIPE_ID = "instanceTaskId";

    private long mRecipeId = DEFAULT_RECIPE_ID;

    private Activity mParentActivity;
    private CollapsingToolbarLayout mAppBarLayout;
    private TextView mDetailTextView;
    private TextView mIngredientsTextView;
    private TextView mStepsTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.d(this, "onCreate(savedInstanceState)");
        super.onCreate(savedInstanceState);

        mParentActivity = getActivity();

        if (mParentActivity != null) {

            AppDatabase database = AppDatabase.getDatabase(getContext());

            // get recipe id from savedInstanceState (if not empty)
            if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_RECIPE_ID)) {
                mRecipeId = savedInstanceState.getLong(INSTANCE_RECIPE_ID, DEFAULT_RECIPE_ID);
            }

            // get recipe id from intent
            Intent intent = mParentActivity.getIntent();
            if (intent != null && intent.hasExtra(EXTRA_RECIPE_ID)) {
                if (mRecipeId == DEFAULT_RECIPE_ID) {
                    mRecipeId = intent.getLongExtra(EXTRA_RECIPE_ID, DEFAULT_RECIPE_ID);
                }
            }

            // get ViewModel from id, observe recipe and populate ui with it
            RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(database, mRecipeId);
            final RecipeDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);
            viewModel.getRecipeDetail().observe(this, new Observer<RecipeDetail>() {
                @Override
                public void onChanged(@Nullable RecipeDetail recipeDetail) {
                    viewModel.getRecipeDetail().removeObserver(this);
                    populateUI(recipeDetail.getRecipe());
                    populateStepsUI(recipeDetail.getSteps());
                }
            });
            viewModel.getRecipeIngredients().observe(this, new Observer<List<CustomRecipeIngredient>>() {
                @Override
                public void onChanged(@Nullable List<CustomRecipeIngredient> recipeIngredient) {
                    viewModel.getRecipeIngredients().removeObserver(this);
                    populateIngredientsUI(recipeIngredient);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Debug.d(this, "onSaveInstanceState(outState)");
        outState.putLong(RecipeDetailFragment.INSTANCE_RECIPE_ID, mRecipeId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)");
        View rootView = inflater.inflate(R.layout.recipe_detail, container, false);

        mAppBarLayout = mParentActivity.findViewById(R.id.toolbar_layout);
        mDetailTextView = rootView.findViewById(R.id.recipe_general_detail);
        mIngredientsTextView = rootView.findViewById(R.id.recipe_ingredients_detail);
        mStepsTextView = rootView.findViewById(R.id.recipe_steps_detail);

        return rootView;
    }

    private void populateUI(Recipe recipe) {
        Debug.d(this, "populateUI(recipe)");

        if (recipe == null) {
            return;
        }

        if (mAppBarLayout != null && mDetailTextView != null) {
            mAppBarLayout.setTitle(recipe.getName());
            mDetailTextView.setText(recipe.toString());
        }
    }

    private void populateIngredientsUI(List<CustomRecipeIngredient> recipeIngredients) {
        Debug.d(this, "populateUI(recipeIngredients)");

        if (recipeIngredients == null) {
            return;
        }

        if (mIngredientsTextView != null) {
            StringBuilder text = new StringBuilder();
            for (CustomRecipeIngredient ingredient : recipeIngredients) {
                text.append(ingredient).append("\n");
            }
            mIngredientsTextView.setText(text);
        }
    }

    private void populateStepsUI(List<RecipeStep> recipeSteps) {
        Debug.d(this, "populateStepsUI(recipeSteps)");

        if (recipeSteps == null) {
            return;
        }

        if (mStepsTextView != null) {
            StringBuilder text = new StringBuilder();
            for (RecipeStep step : recipeSteps) {
                text.append(step).append("\n");
            }
            mStepsTextView.setText(text);
        }
    }
}
