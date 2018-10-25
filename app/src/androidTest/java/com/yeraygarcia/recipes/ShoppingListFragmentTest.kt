package com.yeraygarcia.recipes

import android.arch.lifecycle.MutableLiveData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.yeraygarcia.recipes.database.UUIDTypeConverter.newUUID
import com.yeraygarcia.recipes.database.entity.Unit
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import com.yeraygarcia.recipes.util.FragmentInjector
import com.yeraygarcia.recipes.util.FragmentInjector.Companion.TAG_FRAGMENT_SHOPPING_LIST
import com.yeraygarcia.recipes.util.RecyclerViewMatcher
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
class ShoppingListFragmentTest {
    @Rule
    @JvmField
    var activityRule = ActivityTestRule(MainActivity::class.java, true, true)

    private lateinit var fragmentInjector: FragmentInjector
    private lateinit var viewModel: RecipeViewModel
    private val shoppingListItems = MutableLiveData<List<UiShoppingListItem>>()
    private val units = MutableLiveData<List<Unit>>()
    private val unitsAndIngredientNames = MutableLiveData<List<String>>()

    private val testFragment = ShoppingListFragment()

    @Before
    fun init() {
        viewModel = mock(RecipeViewModel::class.java)
        `when`(viewModel.shoppingListItems).thenReturn(shoppingListItems)
        `when`(viewModel.units).thenReturn(units)
        `when`(viewModel.unitsAndIngredientNames).thenReturn(unitsAndIngredientNames)
        `when`(viewModel.clearAllFromShoppingList()).then { shoppingListItems.postValue(emptyList()) }

        testFragment.viewModel = viewModel

        fragmentInjector = mock(FragmentInjector::class.java)
        `when`(fragmentInjector.getFragment(TAG_FRAGMENT_SHOPPING_LIST)).thenReturn(testFragment)

        activityRule.activity.fragmentInjector = fragmentInjector

        loadData()

        onView(withId(R.id.navigationShoppingList)).check(matches(isDisplayed()))
        onView(withId(R.id.navigationShoppingList)).perform(click())
    }

    @Test
    fun loadShoppingListItems() {

        onView(withId(R.id.recyclerViewShoppingList)).check(matches(isDisplayed()))
        onView(listMatcher().atPosition(0)).apply {
            check(matches(hasDescendant(withText("Reis"))))
            check(matches(hasDescendant(withText("Packungen"))))
            check(matches(hasDescendant(withText("2"))))
        }
        onView(listMatcher().atPosition(1)).apply {
            check(matches(hasDescendant(withText("Butter"))))
            check(matches(hasDescendant(withText("g"))))
            check(matches(hasDescendant(withText("250"))))
        }
        onView(listMatcher().atPosition(2)).apply {
            check(matches(hasDescendant(withText("Bohnen"))))
            check(matches(hasDescendant(withText("Dose"))))
            check(matches(hasDescendant(withText("1"))))
            check(matches(hasDescendant(allOf(withId(R.id.checkboxItemCompleted), isChecked()))))
        }
    }

    @Test
    fun clickShoppingListItem() {
        onView(withId(R.id.recyclerViewShoppingList)).check(matches(isDisplayed()))

        onView(listMatcher().atPosition(0)).perform(click())

        onView(withId(R.id.editTextIngredientName))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextIngredientQuantity))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(R.id.autoCompleteTextViewIngredientUnit))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    private fun loadData() {
        val items = listOf(
            UiShoppingListItem(
                newUUID(), 2.0, "Packung", "Packungen", "Reis", false
            ),
            UiShoppingListItem(
                newUUID(), 250.0, "g", "g", "Butter", false
            ),
            UiShoppingListItem(
                newUUID(), 1.0, "Dose", "Dosen", "Bohnen", true
            )
        )
        shoppingListItems.postValue(items)
    }

    private fun listMatcher() = RecyclerViewMatcher(R.id.recyclerViewShoppingList)
}