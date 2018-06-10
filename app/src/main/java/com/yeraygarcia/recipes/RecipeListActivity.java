package com.yeraygarcia.recipes;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.yeraygarcia.recipes.adapter.RecipeAdapter;
import com.yeraygarcia.recipes.adapter.TagAdapter;
import com.yeraygarcia.recipes.database.entity.Recipe;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.ShortDividerItemDecoration;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.List;

/**
 * An activity representing a list of Recipes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link RecipeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class RecipeListActivity extends AppCompatActivity {

    private static final String EXTRA_TAG_FILTER = "mTagFilter";
    private RecipeAdapter mRecipeAdapter;
    private RecipeViewModel mViewModel;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    // Lifeclye Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        Debug.d(this, "onCreate(savedInstaceState)");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        if (findViewById(R.id.recipe_detail_container) != null) {
            // we are on a tablet, since tablet-specific view exists
            mTwoPane = true;
        }

        mRecipeAdapter = new RecipeAdapter(this, mTwoPane);
        RecyclerView recipesRecyclerView = findViewById(R.id.recyclerview_recipe_list);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(mRecipeAdapter);
        recipesRecyclerView.addItemDecoration(new ShortDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 16));

        TagAdapter tagAdapter = new TagAdapter(this);
        RecyclerView tagsRecyclerView = findViewById(R.id.recyclerview_tag_chips);
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tagsRecyclerView.setAdapter(tagAdapter);

        mViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TAG_FILTER)) {
            long[] tagFilter = savedInstanceState.getLongArray(EXTRA_TAG_FILTER);
            mViewModel.setTagFilter(tagFilter);
        }
        mViewModel.getRecipes().observe(this, mRecipeAdapter::setRecipes);
        mViewModel.getTags().observe(this, tagAdapter::setTags);
        mViewModel.getTagFilter().observe(this, tagAdapter::setTagFilter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Debug.d(this, "onSaveInstanceState(outState)");
        outState.putLongArray(EXTRA_TAG_FILTER, mViewModel.getTagFilterAsArray());
    }

    @Override
    protected void onDestroy() {
        Debug.d(this, "onDestroy()");
        super.onDestroy();
        mViewModel.updateTagUsage();
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

    // Overrides

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setOnQueryTextListener(new OnFilterRecipesListener());

        return true;
    }
}
