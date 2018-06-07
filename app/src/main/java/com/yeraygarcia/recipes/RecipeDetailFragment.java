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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeraygarcia.recipes.adapter.RecipeDetailAdapter;
import com.yeraygarcia.recipes.database.AppDatabase;
import com.yeraygarcia.recipes.database.entity.custom.CustomRecipeIngredient;
import com.yeraygarcia.recipes.database.entity.custom.RecipeDetail;
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

    private RecipeDetailAdapter mRecipeDetailAdapter;

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

            mRecipeDetailAdapter = new RecipeDetailAdapter(mParentActivity);

            // get ViewModel from id, observe recipe and populate ui with it
            RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(database, mRecipeId);
            final RecipeDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);
            viewModel.getRecipeDetail().observe(this, new Observer<RecipeDetail>() {
                @Override
                public void onChanged(@Nullable RecipeDetail recipeDetail) {
                    if (recipeDetail == null) {
                        return;
                    }
                    mAppBarLayout.setTitle(recipeDetail.getRecipe().getName());
                    mRecipeDetailAdapter.setRecipe(recipeDetail);
                    mRecipeDetailAdapter.setSteps(recipeDetail.getSteps());
                }
            });
            viewModel.getRecipeIngredients().observe(this, new Observer<List<CustomRecipeIngredient>>() {
                @Override
                public void onChanged(@Nullable List<CustomRecipeIngredient> recipeIngredients) {
                    mRecipeDetailAdapter.setIngredients(recipeIngredients);
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

        RecyclerView recipeDetailRecyclerView = rootView.findViewById(R.id.recyclerview_recipe_details);
        recipeDetailRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        recipeDetailRecyclerView.setAdapter(mRecipeDetailAdapter);
        //mRecipeDetailRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16, 0));
        recipeDetailRecyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }
}
