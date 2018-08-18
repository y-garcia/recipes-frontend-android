package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.yeraygarcia.recipes.database.remote.RetrofitClient;
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

    private RecipeViewModel mViewModel;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Debug.d(this, "onCreate(savedInstaceState)");

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mNavigation = findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        mViewModel.updateTagUsage();

        mCurrentFragmentId = getFragmentIdFromArguments(savedInstanceState, getIntent());
        mNavigation.setSelectedItemId(mCurrentFragmentId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.d(this, "onStart(): getLastSignedInAccount()");
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && !account.isExpired()) {
            Debug.d(this, "onStart(): account != null && !account.isExpired()");
            updateUI(account);
        } else {
            Debug.d(this, "onStart(): account == null || account.isExpired()");
            signIn();
        }
    }

    private void signIn() {
        Debug.d(this, "signIn(): show sign-in dialog");
        RetrofitClient.get(this).getGoogleSignInClient()
                .silentSignIn().addOnCompleteListener(this, this::handleSignInResult);
    }

    private void signOut() {
        Debug.d(this, "signOut()");
        RetrofitClient.get(this).getGoogleSignInClient().signOut().addOnCompleteListener(this, task -> {
            mViewModel.deleteAll();
            RetrofitClient.clear();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Debug.d(this, "handleSignInResult(task)");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken != null) {
                updateUI(account);
            } else {
                updateUI(null);
            }
        } catch (ApiException e) {
            Debug.d(this, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        Debug.d(this, "updateUI(account, errorMessage)");
        if (account != null) {
            RetrofitClient.get(this).setIdToken(account.getIdToken());
        } else {
            signOut();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout_button:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                if (v.getId() == R.id.edittext_new_item) {
                    v = (View) v.getParent();
                }
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

    private int getFragmentIdFromArguments(Bundle savedInstanceState, Intent intent) {

        // get recipe id from savedInstanceState (if not empty)
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FRAGMENT_ID)) {
            return savedInstanceState.getInt(KEY_FRAGMENT_ID, DEFAULT_FRAGMENT_ID);
        }

        // get recipe id from intent (if savedInstanceState was empty)
        return getFragmentIdFromIntent(intent);
    }

    private int getFragmentIdFromIntent(Intent intent) {

        // get recipe id from intent (if savedInstanceState was empty)
        if (intent != null && intent.hasExtra(EXTRA_FRAGMENT_ID)) {
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
