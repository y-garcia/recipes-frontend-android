package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeraygarcia.recipes.adapter.RecipeDetailAdapter;
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

import java.util.UUID;

public class RecipeDetailFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "argRecipeId";
    public static final String KEY_RECIPE_ID = "keyRecipeId";
    public static final UUID DEFAULT_RECIPE_ID = UUID.randomUUID();

    private static final String KEY_SELECTED_INGREDIENT = "keySelectedIngredient";
    private static final String KEY_SELECTED_STEP = "keySelectedStep";

    private UUID mRecipeId = DEFAULT_RECIPE_ID;

    private FragmentActivity mParentActivity;
    private CollapsingToolbarLayout mAppBarLayout;

    private RecipeDetailAdapter mRecipeDetailAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeDetailFragment() {
    }

    public static RecipeDetailFragment newInstance(UUID recipeId) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.d(this, "onCreate(savedInstanceState)");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mParentActivity = getActivity();

        if (mParentActivity != null) {

            // get recipe id from savedInstanceState (if not empty)
            if (savedInstanceState != null && savedInstanceState.containsKey(KEY_RECIPE_ID)) {
                mRecipeId = (UUID) savedInstanceState.getSerializable(KEY_RECIPE_ID);
            }

            // get recipe id from intent (if savedInstanceState was empty)
            Intent intent = mParentActivity.getIntent();
            if (intent != null && intent.hasExtra(ARG_RECIPE_ID)) {
                if (mRecipeId.equals(DEFAULT_RECIPE_ID)) {
                    mRecipeId = (UUID) intent.getSerializableExtra(ARG_RECIPE_ID);
                }
            }

            // get ViewModel from recipe id
            RecipeDetailRepository repository = new RecipeDetailRepository(getActivity().getApplication());
            RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(repository, mRecipeId);
            RecipeDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);

            mRecipeDetailAdapter = new RecipeDetailAdapter(mParentActivity, viewModel);

            // get selected ingredient and step from savedInstanceState (if not empty)
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(KEY_SELECTED_INGREDIENT)) {
                    mRecipeDetailAdapter.setSelectedIngredient(savedInstanceState.getInt(KEY_SELECTED_INGREDIENT, RecyclerView.NO_POSITION));
                }
                if (savedInstanceState.containsKey(KEY_SELECTED_STEP)) {
                    mRecipeDetailAdapter.setSelectedStep(savedInstanceState.getInt(KEY_SELECTED_STEP, RecyclerView.NO_POSITION));
                }
            }

            // observe recipe and populate ui with it
            viewModel.getRecipeDetail().observe(this, uiRecipe -> {
                if (uiRecipe == null) {
                    return;
                }
                mAppBarLayout.setTitle(uiRecipe.getRecipe().getName());
                mRecipeDetailAdapter.setRecipe(uiRecipe);
            });
            viewModel.getRecipeIngredients().observe(this, mRecipeDetailAdapter::setIngredients);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Debug.d(this, "onSaveInstanceState(outState)");
        outState.putSerializable(KEY_RECIPE_ID, mRecipeId);
        outState.putInt(KEY_SELECTED_INGREDIENT, mRecipeDetailAdapter.getSelectedIngredient());
        outState.putInt(KEY_SELECTED_STEP, mRecipeDetailAdapter.getSelectedStep());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)");
        View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        mAppBarLayout = mParentActivity.findViewById(R.id.toolbar_layout);

        RecyclerView recipeDetailRecyclerView = rootView.findViewById(R.id.recyclerview_recipe_details);
        recipeDetailRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        recipeDetailRecyclerView.setAdapter(mRecipeDetailAdapter);
        //mRecipeDetailRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16, 0));
        recipeDetailRecyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }
}
