package com.yeraygarcia.recipes;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeraygarcia.recipes.adapter.EditRecipeAdapter;
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

public class EditRecipeFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "argRecipeId";
    public static final String KEY_RECIPE_ID = "keyRecipeId";
    public static final long DEFAULT_RECIPE_ID = -1;

    private long mRecipeId = DEFAULT_RECIPE_ID;

    private Activity mParentActivity;
    private CollapsingToolbarLayout mAppBarLayout;
    private RecipeDetailViewModel mViewModel;

    private EditRecipeAdapter mEditRecipeAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EditRecipeFragment() {
    }

    public static EditRecipeFragment newInstance(long recipeId) {
        EditRecipeFragment fragment = new EditRecipeFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.d(this, "onCreate(savedInstanceState)");
        super.onCreate(savedInstanceState);

        mParentActivity = getActivity();

        if (mParentActivity != null) {

            // get recipe id from savedInstanceState (if not empty)
            if (savedInstanceState != null && savedInstanceState.containsKey(KEY_RECIPE_ID)) {
                mRecipeId = savedInstanceState.getLong(KEY_RECIPE_ID, DEFAULT_RECIPE_ID);
            }

            // get recipe id from intent (if savedInstanceState was empty)
            Intent intent = mParentActivity.getIntent();
            if (intent != null && intent.hasExtra(ARG_RECIPE_ID)) {
                if (mRecipeId == DEFAULT_RECIPE_ID) {
                    mRecipeId = intent.getLongExtra(ARG_RECIPE_ID, DEFAULT_RECIPE_ID);
                }
            }

            RecipeDetailRepository repository = new RecipeDetailRepository(mParentActivity.getApplication());

            // get ViewModel from id, observe recipe and populate ui with it
            RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(repository, mRecipeId);
            mViewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);

            mEditRecipeAdapter = new EditRecipeAdapter(mParentActivity, mViewModel);

            mViewModel.getRecipeDetail().observe(this, uiRecipe -> {
                if (uiRecipe == null) {
                    return;
                }
                mAppBarLayout.setTitle(uiRecipe.getRecipe().getName());
                mEditRecipeAdapter.setRecipe(uiRecipe);
            });
            mViewModel.getUnitPluralNames().observe(this, mEditRecipeAdapter::setUnits);
            mViewModel.getRecipeIngredients().observe(this, mEditRecipeAdapter::setIngredients);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Debug.d(this, "onSaveInstanceState(outState)");
        outState.putLong(RecipeDetailFragment.KEY_RECIPE_ID, mRecipeId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Debug.d(this, "onPause()");
        mViewModel.persistDraft();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)");
        View rootView = inflater.inflate(R.layout.fragment_recipe_edit, container, false);

        mAppBarLayout = mParentActivity.findViewById(R.id.toolbar_layout);

        RecyclerView recipeDetailRecyclerView = rootView.findViewById(R.id.recyclerview_recipe_edit);
        recipeDetailRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        recipeDetailRecyclerView.setAdapter(mEditRecipeAdapter);
        //mRecipeDetailRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16, 0));
        recipeDetailRecyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }
}
