package com.yeraygarcia.recipes;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeraygarcia.recipes.adapter.RecipeAdapter;
import com.yeraygarcia.recipes.adapter.TagAdapter;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.ShortDividerItemDecoration;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

public class RecipeListFragment extends Fragment {

    private static final String EXTRA_TAG_FILTER = "mTagFilter";

    private FragmentActivity mParentActivity;
    private RecipeAdapter mRecipeAdapter;
    private RecipeViewModel mViewModel;

    public RecipeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentActivity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)");
        View rootView = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        FloatingActionButton fab = rootView.findViewById(R.id.fab_add_recipe);
        fab.setOnClickListener(view -> Snackbar.make(view, R.string.feature_not_implemented, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        setHasOptionsMenu(true);

        mRecipeAdapter = new RecipeAdapter(mParentActivity);
        RecyclerView recipesRecyclerView = rootView.findViewById(R.id.recyclerview_recipe_list);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        recipesRecyclerView.setAdapter(mRecipeAdapter);
        recipesRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16));

        TagAdapter tagAdapter = new TagAdapter(mParentActivity);
        RecyclerView tagsRecyclerView = rootView.findViewById(R.id.recyclerview_tag_chips);
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity, LinearLayoutManager.HORIZONTAL, false));
        tagsRecyclerView.setAdapter(tagAdapter);

        mViewModel = ViewModelProviders.of(mParentActivity).get(RecipeViewModel.class);
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TAG_FILTER)) {
            long[] tagFilter = savedInstanceState.getLongArray(EXTRA_TAG_FILTER);
            mViewModel.setTagFilter(tagFilter);
        }
        mViewModel.getRecipes().observe(this, mRecipeAdapter::setRecipes);
        mViewModel.getTags().observe(this, tagAdapter::setTags);
        mViewModel.getTagFilter().observe(this, tagAdapter::setTagFilter);
        mViewModel.getRecipeIdsInShoppingList().observe(this, mRecipeAdapter::setRecipeIdsInShoppingList);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Debug.d(this, "onSaveInstanceState(outState)");
        outState.putLongArray(EXTRA_TAG_FILTER, mViewModel.getTagFilterAsArray());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Debug.d(this, "onCreateOptionsMenu(menu, inflated)");
        inflater.inflate(R.menu.options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) mParentActivity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(mParentActivity.getComponentName()));
        }
        searchView.setOnQueryTextListener(new OnFilterRecipesListener());
    }

    // Internal classes

    private class OnFilterRecipesListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mRecipeAdapter.getFilter().filter(newText);
            return false;
        }
    }
}
