package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_FRAGMENT_ID = "extraFragmentId";
    private static final String KEY_FRAGMENT_ID = "keyFragmentId";
    private static final String TAG_FRAGMENT_RECIPES = "tagRecipesFragment";
    private static final String TAG_FRAGMENT_SHOPPING_LIST = "tagShoppingListFragment";
    private static final String TAG_FRAGMENT_TODAY = "tagTodayFragment";

    private static final int DEFAULT_FRAGMENT_ID = R.id.navigation_recipes;

    private Toolbar mToolbar;
    BottomNavigationView mNavigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> selectFragmentByItemId(item.getItemId());

    private Fragment mCurrentFragment;
    private int mCurrentFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Debug.d(this, "onCreate(savedInstaceState)");

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mNavigation = findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        RecipeViewModel mViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        mViewModel.updateTagUsage();

        mCurrentFragmentId = getFragmentIdFromArguments(savedInstanceState, getIntent());
        mNavigation.setSelectedItemId(mCurrentFragmentId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Debug.d(this, "onNewIntent(intent)");
        super.onNewIntent(intent);

        mCurrentFragmentId = getFragmentIdFromIntent(intent);
        mNavigation.setSelectedItemId(mCurrentFragmentId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Debug.d(this, "onSaveInstanceState(outState)");
        super.onSaveInstanceState(outState);
        Debug.d(this, "savedInstanceState." + KEY_FRAGMENT_ID + " = " + mCurrentFragmentId);
        outState.putInt(KEY_FRAGMENT_ID, mCurrentFragmentId);
    }

    private int getFragmentIdFromArguments(Bundle savedInstanceState, Intent intent) {

        // get recipe id from savedInstanceState (if not empty)
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FRAGMENT_ID)) {
            long id = savedInstanceState.getInt(KEY_FRAGMENT_ID, DEFAULT_FRAGMENT_ID);
            Debug.d(this, "savedInstanceState." + KEY_FRAGMENT_ID + " = " + id);
            return savedInstanceState.getInt(KEY_FRAGMENT_ID, DEFAULT_FRAGMENT_ID);
        }

        // get recipe id from intent (if savedInstanceState was empty)
        return getFragmentIdFromIntent(intent);
    }

    private int getFragmentIdFromIntent(Intent intent) {

        // get recipe id from intent (if savedInstanceState was empty)
        if (intent != null && intent.hasExtra(EXTRA_FRAGMENT_ID)) {
            long id = intent.getIntExtra(EXTRA_FRAGMENT_ID, DEFAULT_FRAGMENT_ID);
            Debug.d(this, "Intent." + EXTRA_FRAGMENT_ID + " = " + id);
            return intent.getIntExtra(EXTRA_FRAGMENT_ID, DEFAULT_FRAGMENT_ID);
        }

        return DEFAULT_FRAGMENT_ID;
    }

    private void selectFragment(Fragment fragment, String tag) {
        if (!fragment.equals(mCurrentFragment)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
            mCurrentFragment = fragment;
        }
    }

    private boolean selectFragmentByItemId(int itemId) {
        Fragment fragment;
        switch (itemId) {
            case R.id.navigation_recipes:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_RECIPES);
                if (fragment == null) {
                    fragment = new RecipeListFragment();
                }
                selectFragment(fragment, TAG_FRAGMENT_RECIPES);
                mCurrentFragmentId = itemId;
                mToolbar.setTitle(R.string.title_recipe_list);
                return true;

            case R.id.navigation_shopping_list:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_SHOPPING_LIST);
                if (fragment == null) {
                    fragment = new ShoppingListFragment();
                }
                selectFragment(fragment, TAG_FRAGMENT_SHOPPING_LIST);
                mToolbar.setTitle(R.string.title_shopping_list);
                mCurrentFragmentId = itemId;
                return true;

            case R.id.navigation_today:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TODAY);
                if (fragment == null) {
                    fragment = new TodayListFragment();
                }
                selectFragment(fragment, TAG_FRAGMENT_TODAY);
                mCurrentFragmentId = itemId;
                mToolbar.setTitle(R.string.title_today);
                return true;
        }
        return false;
    }
}
