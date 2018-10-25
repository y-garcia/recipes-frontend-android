package com.yeraygarcia.recipes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import java.util.*

class RecipeDetailActivity : AppCompatActivity() {

    private var currentFragment: String? = null
    private lateinit var recipeId: UUID
    private lateinit var viewModel: RecipeDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setSupportActionBar(detail_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab_edit_recipe.setOnClickListener { selectFragment(TAG_FRAGMENT_RECIPE_EDIT) }
        fab_save_recipe.setOnClickListener { selectFragment(TAG_FRAGMENT_RECIPE_DETAIL) }

        recipeId = intent.getSerializableExtra(RecipeDetailFragment.ARG_RECIPE_ID) as UUID

        selectFragment(getFragmentTagFromArguments(savedInstanceState))

        // get ViewModel for recipe id
        val repository = RecipeDetailRepository(application)
        val factory = RecipeDetailViewModelFactory(repository, recipeId)
        viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel::class.java)

        viewModel.isInShoppingList.observe(this, Observer { isInShoppingList ->
            if (isInShoppingList != null && isInShoppingList) {
                fab_add_to_cart.setOnClickListener { view ->
                    viewModel.removeFromShoppingList(recipeId)
                    Snackbar.make(view, R.string.removed_from_shopping_list, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_open) { openShoppingListActivity() }
                        .show()
                }
                fab_add_to_cart.setImageResource(R.drawable.ic_remove_shopping_cart_24px)
            } else {
                fab_add_to_cart.setOnClickListener { view ->
                    viewModel.addToShoppingList(recipeId)
                    Snackbar.make(view, R.string.added_to_shopping_list, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_open) { openShoppingListActivity() }
                        .show()
                }
                fab_add_to_cart.setImageResource(R.drawable.ic_add_shopping_cart_24px)
            }
        })
    }

    private fun openShoppingListActivity() {
        Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_FRAGMENT_ID, R.id.navigationShoppingList)
            startActivity(this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_FRAGMENT_TAG, currentFragment)
    }

    private fun getFragmentTagFromArguments(savedInstanceState: Bundle?): String {

        // get recipe id from savedInstanceState (if not empty)
        return if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FRAGMENT_TAG)) {
            savedInstanceState.getString(KEY_FRAGMENT_TAG, DEFAULT_FRAGMENT_TAG)
        } else DEFAULT_FRAGMENT_TAG

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isVisible(TAG_FRAGMENT_RECIPE_EDIT)) {
                    selectFragment(TAG_FRAGMENT_RECIPE_DETAIL)
                } else {
                    navigateUpTo(Intent(this, MainActivity::class.java))
                }
                return true
            }
        }
        return false
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


    override fun onBackPressed() {
        if (isVisible(TAG_FRAGMENT_RECIPE_EDIT)) {
            selectFragment(TAG_FRAGMENT_RECIPE_DETAIL)
        } else {
            super.onBackPressed()
        }
    }

    private fun isVisible(tag: String): Boolean {
        return supportFragmentManager.findFragmentByTag(tag) != null
    }

    private fun selectFragment(tag: String) {
        val fragment = getFragmentByTag(tag)
        if (fragment.tag == null || fragment.tag != currentFragment) {
            if (tag == TAG_FRAGMENT_RECIPE_DETAIL) {
                supportFragmentManager.popBackStack(
                    TAG_FRAGMENT_RECIPE_DETAIL,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.recipe_detail_container, fragment, tag)
                    .commit()
            } else {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.recipe_detail_container, fragment, tag)
                    .addToBackStack(TAG_FRAGMENT_RECIPE_DETAIL)
                    .commit()
            }

            currentFragment = tag
        }
        showButtonsByTag(tag)
    }

    private fun getFragmentByTag(tag: String): Fragment {
        return supportFragmentManager.findFragmentByTag(tag)
            ?: if (tag == TAG_FRAGMENT_RECIPE_EDIT) {
                EditRecipeFragment.newInstance(recipeId)
            } else {
                RecipeDetailFragment.newInstance(recipeId)
            }
    }

    private fun showButtonsByTag(tag: String) {
        if (tag == TAG_FRAGMENT_RECIPE_EDIT) {
            showSaveButton()
        } else {
            showCartAndEditButtons()
        }
    }

    private fun showCartAndEditButtons() {
        fab_edit_recipe.visibility = View.VISIBLE
        fab_add_to_cart.visibility = View.VISIBLE
        fab_save_recipe.visibility = View.GONE
    }

    private fun showSaveButton() {
        fab_edit_recipe.visibility = View.GONE
        fab_add_to_cart.visibility = View.GONE
        fab_save_recipe.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG_FRAGMENT_RECIPE_DETAIL = "tagRecipeDetailFragment"
        private const val TAG_FRAGMENT_RECIPE_EDIT = "tagEditRecipeFragment"
        private const val DEFAULT_FRAGMENT_TAG = TAG_FRAGMENT_RECIPE_DETAIL
        private const val KEY_FRAGMENT_TAG = "keyFragmentTag"
    }
}
