package com.yeraygarcia.recipes

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.yeraygarcia.recipes.database.remote.RetrofitClient
import com.yeraygarcia.recipes.util.Debug
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var currentFragment: Fragment? = null
    private var currentFragmentId: Int = 0
    private lateinit var viewModel: RecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Debug.d(this, "onCreate(savedInstaceState)")

        setSupportActionBar(toolbar)

        navigation.setOnNavigationItemSelectedListener { selectFragmentByItemId(it.itemId) }

        viewModel = ViewModelProviders.of(this).get(RecipeViewModel::class.java)
        viewModel.updateTagUsage()

        currentFragmentId = getFragmentIdFromArguments(savedInstanceState, intent)

        navigation.selectedItemId = currentFragmentId
    }

    override fun onStart() {
        super.onStart()
        Debug.d(this, "onStart(): getLastSignedInAccount()")
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account?.isExpired == false) {
            Debug.d(this, "onStart(): account != null && !account.isExpired()")
            updateUI(account)
        } else {
            Debug.d(this, "onStart(): account == null || account.isExpired()")
            signIn()
        }
    }

    private fun signIn() {
        Debug.d(this, "signIn(): show sign-in dialog")
        RetrofitClient.get(this).googleSignInClient.silentSignIn().addOnCompleteListener(this) {
            this.handleSignInResult(it)
        }
    }

    private fun signOut() {
        Debug.d(this, "signOut()")
        RetrofitClient.get(this).googleSignInClient.signOut().addOnCompleteListener(this) {
            viewModel.deleteAll()
            RetrofitClient.clear()
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Debug.d(this, "handleSignInResult(task)")
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account?.idToken != null) {
                updateUI(account)
            } else {
                updateUI(null)
            }
        } catch (e: ApiException) {
            Debug.d(this, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }

    }

    private fun updateUI(account: GoogleSignInAccount?) {
        Debug.d(this, "updateUI(account, errorMessage)")
        if (account != null) {
            RetrofitClient.get(this).setIdToken(account.idToken)
        } else {
            signOut()
        }
    }

    override fun onNewIntent(intent: Intent) {
        Debug.d(this, "onNewIntent(intent)")
        super.onNewIntent(intent)

        currentFragmentId = getFragmentIdFromIntent(intent)
        navigation.selectedItemId = currentFragmentId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Debug.d(this, "onSaveInstanceState(outState)")
        super.onSaveInstanceState(outState)
        Debug.d(this, "savedInstanceState.$KEY_FRAGMENT_ID = $currentFragmentId")
        outState.putInt(KEY_FRAGMENT_ID, currentFragmentId)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signout_button -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            var view = currentFocus
            if (view is EditText) {
                if (view.id == R.id.editTextNewItem) {
                    view = view.parent as View
                }
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getFragmentIdFromArguments(savedInstanceState: Bundle?, intent: Intent): Int {
        return if (savedInstanceState?.containsKey(KEY_FRAGMENT_ID) == true) {
            savedInstanceState.getInt(KEY_FRAGMENT_ID, DEFAULT_FRAGMENT_ID)
        } else {
            getFragmentIdFromIntent(intent)
        }
    }

    private fun getFragmentIdFromIntent(intent: Intent): Int {
        return intent.getIntExtra(EXTRA_FRAGMENT_ID, DEFAULT_FRAGMENT_ID)
    }

    private fun selectFragmentByItemId(itemId: Int): Boolean {
        return when (itemId) {
            R.id.navigation_recipes -> {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_RECIPES)
                    ?: RecipeListFragment()
                selectFragment(fragment, TAG_FRAGMENT_RECIPES)
                currentFragmentId = itemId
                toolbar.setTitle(R.string.title_recipe_list)
                true
            }

            R.id.navigation_shopping_list -> {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_SHOPPING_LIST)
                    ?: ShoppingListFragment()
                selectFragment(fragment, TAG_FRAGMENT_SHOPPING_LIST)
                toolbar.setTitle(R.string.title_shopping_list)
                currentFragmentId = itemId
                true
            }

            R.id.navigation_today -> {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_TODAY)
                    ?: TodayListFragment()
                selectFragment(fragment, TAG_FRAGMENT_TODAY)
                currentFragmentId = itemId
                toolbar.setTitle(R.string.title_today)
                true
            }

            else -> false
        }
    }

    private fun selectFragment(fragment: Fragment, tag: String) {
        if (fragment != currentFragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()
            currentFragment = fragment
        }
    }

    companion object {
        const val EXTRA_FRAGMENT_ID = "extraFragmentId"
        private const val KEY_FRAGMENT_ID = "keyFragmentId"
        private const val TAG_FRAGMENT_RECIPES = "tagRecipesFragment"
        private const val TAG_FRAGMENT_SHOPPING_LIST = "tagShoppingListFragment"
        private const val TAG_FRAGMENT_TODAY = "tagTodayFragment"
        private const val DEFAULT_FRAGMENT_ID = R.id.navigation_recipes
    }
}
