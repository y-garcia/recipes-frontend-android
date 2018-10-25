package com.yeraygarcia.recipes.util

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.yeraygarcia.recipes.*
import com.yeraygarcia.recipes.testing.OpenForTesting

@OpenForTesting
class FragmentInjector(private val supportFragmentManager: FragmentManager) {

    fun getFragment(tag: String): Fragment {
        return supportFragmentManager.findFragmentByTag(tag)
            ?: when (tag) {
                TAG_FRAGMENT_SHOPPING_LIST -> ShoppingListFragment()
                TAG_FRAGMENT_TODAY -> TodayListFragment()
                else -> RecipeListFragment()
            }
    }

    companion object {
        const val TAG_FRAGMENT_RECIPES = "tagRecipesFragment"
        const val TAG_FRAGMENT_SHOPPING_LIST = "tagShoppingListFragment"
        const val TAG_FRAGMENT_TODAY = "tagTodayFragment"
    }
}