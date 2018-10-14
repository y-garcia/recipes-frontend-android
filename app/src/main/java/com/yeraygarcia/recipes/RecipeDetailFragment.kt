package com.yeraygarcia.recipes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yeraygarcia.recipes.adapter.RecipeDetailAdapter
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.fragment_recipe_detail.view.*
import timber.log.Timber
import java.util.*

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class RecipeDetailFragment : Fragment() {

    private lateinit var recipeId: UUID

    private lateinit var recipeDetailAdapter: RecipeDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate($savedInstanceState)")
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        activity?.let { activity ->

            // get recipe id from savedInstanceState (if not empty)
            recipeId = getRecipeIdFromArguments(savedInstanceState, activity.intent)

            // get ViewModel from recipe id
            val repository = RecipeDetailRepository(activity.application)
            val factory = RecipeDetailViewModelFactory(repository, recipeId)
            val viewModel =
                ViewModelProviders.of(activity, factory).get(RecipeDetailViewModel::class.java)

            recipeDetailAdapter = RecipeDetailAdapter(activity, viewModel)

            // get selected ingredient and step from savedInstanceState (if not empty)
            recipeDetailAdapter.selectedIngredient =
                    savedInstanceState?.getInt(KEY_SELECTED_INGREDIENT, RecyclerView.NO_POSITION)
                    ?: RecyclerView.NO_POSITION
            recipeDetailAdapter.selectedStep =
                    savedInstanceState?.getInt(KEY_SELECTED_STEP, RecyclerView.NO_POSITION)
                    ?: RecyclerView.NO_POSITION

            // observe recipe and populate ui with it
            viewModel.recipe.observe(activity, Observer {
                activity.toolbarLayout.title = it?.name
                recipeDetailAdapter.setRecipe(it)
            })
            viewModel.recipeSteps.observe(activity, Observer {
                recipeDetailAdapter.setSteps(it ?: emptyList())
            })
            viewModel.recipeIngredients.observe(activity, Observer {
                recipeDetailAdapter.setIngredients(it ?: emptyList())
            })
        }
    }

    private fun getRecipeIdFromArguments(savedInstanceState: Bundle?, intent: Intent): UUID {
        return savedInstanceState?.getSerializable(KEY_RECIPE_ID) as UUID?
            ?: intent.getSerializableExtra(ARG_RECIPE_ID) as UUID?
            ?: DEFAULT_RECIPE_ID
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("onSaveInstanceState(outState)")
        outState.apply {
            putSerializable(KEY_RECIPE_ID, recipeId)
            putInt(KEY_SELECTED_INGREDIENT, recipeDetailAdapter.selectedIngredient)
            putInt(KEY_SELECTED_STEP, recipeDetailAdapter.selectedStep)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView(inflater, container, savedInstanceState)")
        val rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false)

        rootView.recyclerViewRecipeDetails.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = recipeDetailAdapter
            isNestedScrollingEnabled = false
        }

        return rootView
    }

    companion object {

        const val ARG_RECIPE_ID = "argRecipeId"
        const val KEY_RECIPE_ID = "keyRecipeId"
        val DEFAULT_RECIPE_ID: UUID = UUID.randomUUID()

        private const val KEY_SELECTED_INGREDIENT = "keySelectedIngredient"
        private const val KEY_SELECTED_STEP = "keySelectedStep"

        fun newInstance(recipeId: UUID): RecipeDetailFragment {
            return RecipeDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_RECIPE_ID, recipeId.toString()) }
            }
        }
    }
}
