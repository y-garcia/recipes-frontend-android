package com.yeraygarcia.recipes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yeraygarcia.recipes.adapter.EditRecipeAdapter
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.fragment_recipe_edit.view.*
import timber.log.Timber
import java.util.*

class EditRecipeFragment : Fragment() {

    private lateinit var recipeId: UUID
    private lateinit var editRecipeAdapter: EditRecipeAdapter
    private lateinit var viewModel: RecipeDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate(savedInstanceState)")
        super.onCreate(savedInstanceState)

        activity?.let { activity ->

            recipeId = savedInstanceState?.getSerializable(KEY_RECIPE_ID) as UUID?
                    ?: arguments?.getSerializable(ARG_RECIPE_ID) as UUID?
                    ?: activity.intent?.getSerializableExtra(ARG_RECIPE_ID) as UUID?
                    ?: UUID.randomUUID()

            val repository = RecipeDetailRepository(activity.application)
            val factory = RecipeDetailViewModelFactory(repository, recipeId)
            viewModel =
                    ViewModelProviders.of(activity, factory).get(RecipeDetailViewModel::class.java)

            editRecipeAdapter = EditRecipeAdapter(activity, viewModel)

            viewModel.recipe.observe(activity, Observer {
                activity.toolbarLayout.title = it?.name
                editRecipeAdapter.recipe = it
            })
            viewModel.recipeSteps.observe(activity, Observer {
                editRecipeAdapter.steps = it ?: emptyList()
            })
            viewModel.recipeIngredients.observe(activity, Observer {
                editRecipeAdapter.ingredients = it ?: emptyList()
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("onSaveInstanceState($outState)")
        outState.putSerializable(RecipeDetailFragment.KEY_RECIPE_ID, recipeId)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause()")
        viewModel.persistDraft(editRecipeAdapter.recipe)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView(inflater, container, savedInstanceState)")

        val rootView = inflater.inflate(R.layout.fragment_recipe_edit, container, false)

        return rootView.recyclerviewRecipeEdit.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = editRecipeAdapter
            isNestedScrollingEnabled = false
        }
    }

    companion object {

        const val ARG_RECIPE_ID = "argRecipeId"
        const val KEY_RECIPE_ID = "keyRecipeId"

        fun newInstance(recipeId: UUID): EditRecipeFragment {
            return EditRecipeFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_RECIPE_ID, recipeId) }
            }
        }
    }
}
