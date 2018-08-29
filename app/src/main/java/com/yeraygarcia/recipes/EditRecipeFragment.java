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

import com.yeraygarcia.recipes.adapter.EditRecipeAdapter;
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

import java.util.UUID;

public class EditRecipeFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "argRecipeId";
    public static final String KEY_RECIPE_ID = "keyRecipeId";
    public static final UUID DEFAULT_RECIPE_ID = UUID.randomUUID();

    private UUID mRecipeId = DEFAULT_RECIPE_ID;

    private FragmentActivity mParentActivity;
    private CollapsingToolbarLayout mAppBarLayout;

    private EditRecipeAdapter mEditRecipeAdapter;
    private RecipeDetailViewModel mViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EditRecipeFragment() {
    }

    public static EditRecipeFragment newInstance(UUID recipeId) {
        EditRecipeFragment fragment = new EditRecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId.toString());
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
                mRecipeId = (UUID) savedInstanceState.getSerializable(KEY_RECIPE_ID);
            }

            // get recipe id from intent (if savedInstanceState was empty)
            Intent intent = mParentActivity.getIntent();
            if (intent != null && intent.hasExtra(ARG_RECIPE_ID)) {
                if (mRecipeId.equals(DEFAULT_RECIPE_ID)) {
                    mRecipeId = (UUID) intent.getSerializableExtra(ARG_RECIPE_ID);
                }
            }

            RecipeDetailRepository repository = new RecipeDetailRepository(mParentActivity.getApplication());

            // get ViewModel from id, observe recipe and populate ui with it
            RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(repository, mRecipeId);
            mViewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);

            mEditRecipeAdapter = new EditRecipeAdapter(mParentActivity, mViewModel);

            mViewModel.getRecipe().observe(this, recipe -> {
                if (recipe == null) {
                    return;
                }
                mAppBarLayout.setTitle(recipe.getName());
                mEditRecipeAdapter.setRecipe(recipe);
            });
            mViewModel.getRecipeSteps().observe(this, mEditRecipeAdapter::setSteps);
            mViewModel.getRecipeIngredients().observe(this, mEditRecipeAdapter::setIngredients);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Debug.d(this, "onSaveInstanceState(outState)");
        outState.putSerializable(RecipeDetailFragment.KEY_RECIPE_ID, mRecipeId);
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
        //recipeDetailRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16));
        recipeDetailRecyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }
}
