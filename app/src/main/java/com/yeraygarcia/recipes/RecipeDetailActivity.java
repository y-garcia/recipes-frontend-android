package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory;

import java.util.UUID;

public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT_RECIPE_DETAIL = "tagRecipeDetailFragment";
    private static final String TAG_FRAGMENT_RECIPE_EDIT = "tagEditRecipeFragment";

    private static final String DEFAULT_FRAGMENT_TAG = TAG_FRAGMENT_RECIPE_DETAIL;
    private static final String KEY_FRAGMENT_TAG = "keyFragmentTag";

    private String mCurrentFragment;
    private UUID mRecipeId;

    private RecipeDetailViewModel mViewModel;
    private FloatingActionButton mFabAddToCart;
    private FloatingActionButton mFabEdit;
    private FloatingActionButton mFabSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        mFabAddToCart = findViewById(R.id.fab_add_to_cart);
        mFabEdit = findViewById(R.id.fab_edit_recipe);
        mFabSave = findViewById(R.id.fab_save_recipe);

        mFabEdit.setOnClickListener(editFab -> selectFragment(TAG_FRAGMENT_RECIPE_EDIT));
        mFabSave.setOnClickListener(saveFab -> selectFragment(TAG_FRAGMENT_RECIPE_DETAIL));

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecipeId = (UUID) getIntent().getSerializableExtra(RecipeDetailFragment.ARG_RECIPE_ID);

        selectFragment(getFragmentTagFromArguments(savedInstanceState));

        // get ViewModel for recipe id
        RecipeDetailRepository repository = new RecipeDetailRepository(getApplication());
        RecipeDetailViewModelFactory factory = new RecipeDetailViewModelFactory(repository, mRecipeId);
        mViewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel.class);

        // OnClickListener for the Snackbar button
        View.OnClickListener onClickOpenShoppingList = v -> {
            Intent newIntent = new Intent(this, MainActivity.class);
            newIntent.putExtra(MainActivity.EXTRA_FRAGMENT_ID, R.id.navigation_shopping_list);
            Debug.d(RecipeDetailActivity.this, "set Intent." + MainActivity.EXTRA_FRAGMENT_ID + " = " + R.id.navigation_shopping_list);
            startActivity(newIntent);
        };

        mViewModel.isInShoppingList().observe(this, isInShoppingList -> {
            if (isInShoppingList != null && isInShoppingList) {
                mFabAddToCart.setOnClickListener(view -> {
                    mViewModel.removeFromShoppingList(mRecipeId);
                    Snackbar.make(view, R.string.removed_from_shopping_list, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, onClickOpenShoppingList)
                            .show();
                });
                mFabAddToCart.setImageResource(R.drawable.ic_remove_shopping_cart_24px);
            } else {
                mFabAddToCart.setOnClickListener(view -> {
                    mViewModel.addToShoppingList(mRecipeId);
                    Snackbar.make(view, R.string.added_to_shopping_list, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, onClickOpenShoppingList)
                            .show();
                });
                mFabAddToCart.setImageResource(R.drawable.ic_add_shopping_cart_24px);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FRAGMENT_TAG, mCurrentFragment);
    }

    private String getFragmentTagFromArguments(Bundle savedInstanceState) {

        // get recipe id from savedInstanceState (if not empty)
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FRAGMENT_TAG)) {
            return savedInstanceState.getString(KEY_FRAGMENT_TAG, DEFAULT_FRAGMENT_TAG);
        }

        return DEFAULT_FRAGMENT_TAG;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isVisible(TAG_FRAGMENT_RECIPE_EDIT)) {
                    selectFragment(TAG_FRAGMENT_RECIPE_DETAIL);
                } else {
                    navigateUpTo(new Intent(this, MainActivity.class));
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public void onBackPressed() {
        if (isVisible(TAG_FRAGMENT_RECIPE_EDIT)) {
            selectFragment(TAG_FRAGMENT_RECIPE_DETAIL);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isVisible(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag) != null;
    }

    private void selectFragment(String tag) {
        Fragment fragment = getFragmentByTag(tag);
        if (fragment.getTag() == null || !fragment.getTag().equals(mCurrentFragment)) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            if (tag.equals(TAG_FRAGMENT_RECIPE_DETAIL)) {
                fragmentManager.popBackStack(TAG_FRAGMENT_RECIPE_DETAIL, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.recipe_detail_container, fragment, tag)
                        .commit();
            } else {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.recipe_detail_container, fragment, tag)
                        .addToBackStack(TAG_FRAGMENT_RECIPE_DETAIL)
                        .commit();
            }

            mCurrentFragment = tag;
        }
        showButtonsByTag(tag);
    }

    private Fragment getFragmentByTag(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case TAG_FRAGMENT_RECIPE_EDIT:
                    return EditRecipeFragment.newInstance(mRecipeId);
                case TAG_FRAGMENT_RECIPE_DETAIL:
                    return RecipeDetailFragment.newInstance(mRecipeId);
            }
        }
        return fragment;
    }

    private void showButtonsByTag(String tag) {
        switch (tag) {
            case TAG_FRAGMENT_RECIPE_EDIT:
                showSaveButton();
                break;
            case TAG_FRAGMENT_RECIPE_DETAIL:
            default:
                showCartAndEditButtons();
        }
    }

    private void showCartAndEditButtons() {
        mFabEdit.setVisibility(View.VISIBLE);
        mFabAddToCart.setVisibility(View.VISIBLE);
        mFabSave.setVisibility(View.GONE);
    }

    private void showSaveButton() {
        mFabEdit.setVisibility(View.GONE);
        mFabAddToCart.setVisibility(View.GONE);
        mFabSave.setVisibility(View.VISIBLE);
    }
}
