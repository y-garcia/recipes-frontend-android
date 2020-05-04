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
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.yeraygarcia.recipes.database.remote.RetrofitClient
import com.yeraygarcia.recipes.util.FragmentInjector
import com.yeraygarcia.recipes.util.FragmentInjector.Companion.TAG_FRAGMENT_RECIPES
import com.yeraygarcia.recipes.util.FragmentInjector.Companion.TAG_FRAGMENT_SHOPPING_LIST
import com.yeraygarcia.recipes.util.FragmentInjector.Companion.TAG_FRAGMENT_TODAY
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    var fragmentInjector = FragmentInjector(supportFragmentManager)

    private var currentFragmentId: Int = 0
    private var currentFragment: Fragment? = null
    private lateinit var viewModel: RecipeViewModel
    private val tags = mapOf(
        R.id.navigationRecipes to TAG_FRAGMENT_RECIPES,
        R.id.navigationShoppingList to TAG_FRAGMENT_SHOPPING_LIST,
        R.id.navigationToday to TAG_FRAGMENT_TODAY
    )
    private val titles = mapOf(
        R.id.navigationRecipes to R.string.title_recipe_list,
        R.id.navigationShoppingList to R.string.title_shopping_list,
        R.id.navigationToday to R.string.title_today
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.d("onCreate(savedInstaceState)")

        setSupportActionBar(toolbar)

        navigation.setOnNavigationItemSelectedListener { selectFragmentByItemId(it.itemId) }

        viewModel = ViewModelProviders.of(this).get(RecipeViewModel::class.java)
        viewModel.updateTagUsage()

        navigation.selectedItemId = getFragmentIdFromArguments(savedInstanceState, intent)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart(): getLastSignedInAccount()")
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account?.isExpired == false) {
            Timber.d("onStart(): account != null && !account.isExpired()")
            updateUI(account)
        } else {
            Timber.d("onStart(): account == null || account.isExpired()")
            signIn()
        }
    }

    private fun signIn() {
        Timber.d("signIn(): show sign-in dialog")
        RetrofitClient.get(this).googleSignInClient.silentSignIn().addOnCompleteListener(this) {
            this.handleSignInResult(it)
        }
    }

    private fun signOut() {
        Timber.d("signOut()")
        RetrofitClient.get(this).googleSignInClient.signOut().addOnCompleteListener(this) {
            viewModel.deleteAll()
            RetrofitClient.clear()
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Timber.d("handleSignInResult(task)")
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account?.idToken != null) {
                updateUI(account)
            } else {
                updateUI(null)
            }
        } catch (e: ApiException) {
            Timber.d("signInResult:failed code=${e.statusCode}")
            Toast
                .makeText(this, "SignIn failed with code = ${e.statusCode}", Toast.LENGTH_LONG)
                .show()
            updateUI(null)
        }

    }

    private fun updateUI(account: GoogleSignInAccount?) {
        Timber.d("updateUI(account, errorMessage)")
        if (account != null) {
            RetrofitClient.get(this).setIdToken(account.idToken)
        } else {
            signOut()
        }
    }

    override fun onNewIntent(intent: Intent) {
        Timber.d("onNewIntent(intent)")
        super.onNewIntent(intent)
        navigation.selectedItemId = getFragmentIdFromIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("onSaveInstanceState(outState)")
        super.onSaveInstanceState(outState)
        Timber.d("savedInstanceState.$KEY_FRAGMENT_ID = $currentFragmentId")
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

        tags[itemId]?.let { tag ->
            selectFragment(tag)
            currentFragmentId = itemId
            titles[itemId]?.let { title -> toolbar.setTitle(title) }
            return true
        }

        return false
    }

    private fun selectFragment(tag: String) {
        val fragment = fragmentInjector.getFragment(tag)
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
        private const val DEFAULT_FRAGMENT_ID = R.id.navigationRecipes
    }
}
