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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        mSwipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Debug.d(this, "onRefresh called from SwipeRefreshLayout");
                        refreshData();
                    }
                }
        );

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
        mViewModel.getRecipes().observe(this, resource -> {
            Debug.d(this, "getRecipes().observe(" + resource + ")");
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        Debug.d(this, resource.toString());
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    case LOADING:
                        mSwipeRefreshLayout.setRefreshing(true);
                        break;
                }
                if(resource.data != null){
                    mRecipeAdapter.setRecipes(resource.data);
                }
            } else {
                Toast.makeText(getContext(), "Recipe resource is null", Toast.LENGTH_LONG).show();
            }
        });
        mViewModel.getTags().observe(this, tags -> {
            Debug.d(this, "getTags().observe(" + tags + ")");
            if (tags != null) {
                tagAdapter.setTags(tags);
            } else {
                Toast.makeText(getContext(), "Tags entity is null", Toast.LENGTH_LONG).show();
            }
        });
        mViewModel.getTagFilter().observe(this, tagAdapter::setTagFilter);
        mViewModel.getRecipeIdsInShoppingList().observe(this, mRecipeAdapter::setRecipeIdsInShoppingList);

        return rootView;
    }

    private void refreshData() {
        mViewModel.refreshAll();
        mSwipeRefreshLayout.setRefreshing(false);
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
        inflater.inflate(R.menu.options_menu_recipe_list, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) mParentActivity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(mParentActivity.getComponentName()));
        }
        // Detect typing in SearchView
        searchView.setOnQueryTextListener(new OnFilterRecipesListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_button:
                mSwipeRefreshLayout.setRefreshing(true);
                refreshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
