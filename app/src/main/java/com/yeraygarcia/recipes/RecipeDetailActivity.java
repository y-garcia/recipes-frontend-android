package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(RecipeDetailFragment.ARG_RECIPE_ID, RecipeDetailFragment.DEFAULT_RECIPE_ID);

        RecipeDetailRepository repository = new RecipeDetailRepository(getApplication());
        RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(repository, recipeId);
        RecipeDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);

        FloatingActionButton fab = findViewById(R.id.fab_add_to_cart);

        View.OnClickListener onClickOpenShoppingList = v -> {
            // open Recipe Detail activity
            Intent intent = new Intent(RecipeDetailActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FRAGMENT_ID, R.id.navigation_shopping_list);
            Debug.d(RecipeDetailActivity.this, "set Intent." + MainActivity.EXTRA_FRAGMENT_ID + " = " + R.id.navigation_shopping_list);
            navigateUpTo(intent);
        };

        viewModel.isInShoppingList().observe(this, isInShoppingList -> {
            if (isInShoppingList != null && isInShoppingList) {
                fab.setOnClickListener(view -> {
                    viewModel.removeFromShoppingList(recipeId);
                    Snackbar.make(view, R.string.removed_from_shopping_list, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, onClickOpenShoppingList)
                            .show();
                });
                fab.setImageResource(R.drawable.ic_remove_shopping_cart_24px);
            } else {
                fab.setOnClickListener(view -> {
                    viewModel.addToShoppingList(recipeId);
                    Snackbar.make(view, R.string.added_to_shopping_list, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, onClickOpenShoppingList)
                            .show();
                });
                fab.setImageResource(R.drawable.ic_add_shopping_cart_24px);
            }
        });

        // only create fragment when creating this activity for the first time (not when rotating)
        if (savedInstanceState == null) {
            RecipeDetailFragment fragment = RecipeDetailFragment.newInstance(recipeId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.recipe_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
