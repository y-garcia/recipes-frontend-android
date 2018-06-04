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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

/**
 * A fragment representing a single Recipe detail screen.
 * This fragment is either contained in a {@link RecipeListActivity}
 * in two-pane mode (on tablets) or a {@link RecipeDetailActivity}
 * on handsets.
 */
public class RecipeDetailFragment extends Fragment {

    private static final String TAG = "YGQ: " + RecipeDetailFragment.class.getSimpleName();
    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final long DEFAULT_RECIPE_ID = -1;
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_RECIPE_ID = "instanceTaskId";

    private long mRecipeId = DEFAULT_RECIPE_ID;

    private Activity mParentActivity;
    private CollapsingToolbarLayout mAppBarLayout;
    private TextView mDetailTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(savedInstanceState)");
        super.onCreate(savedInstanceState);

        if (initViews(getActivity())) {

            // initialize database
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

            // get viewmodel, observe recipe and populate ui with it
            RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(database, mRecipeId);
            final RecipeDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);
            viewModel.getRecipe().observe(this, new Observer<Recipe>() {
                @Override
                public void onChanged(@Nullable Recipe recipe) {
                    viewModel.getRecipe().removeObserver(this);
                    populateUI(recipe);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState(outState)");
        outState.putLong(RecipeDetailFragment.INSTANCE_RECIPE_ID, mRecipeId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView(inflater, container, savedInstanceState)");
        return inflater.inflate(R.layout.recipe_detail, container, false);
    }

    private boolean initViews(Activity activity) {
        Log.d(TAG, "initView(activity)");

        if (activity == null) {
            return false;
        }

        mParentActivity = activity;
        mDetailTextView = mParentActivity.findViewById(R.id.recipe_detail);
        mAppBarLayout = mParentActivity.findViewById(R.id.toolbar_layout);

        return true;
    }

    /**
     * populate the UI when in update mode
     *
     * @param recipe the recipe to populate the UI
     */
    private void populateUI(Recipe recipe) {
        Log.d(TAG, "populateUI(recipe)");

        if (recipe == null) {
            return;
        }

        if (mAppBarLayout != null && mDetailTextView != null || initViews(getActivity())) {
            mAppBarLayout.setTitle(recipe.getName());
            mDetailTextView.setText(recipe.toString());
        }
    }
}
