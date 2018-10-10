package com.yeraygarcia.recipes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yeraygarcia.recipes.adapter.EditRecipeAdapter
import com.yeraygarcia.recipes.database.repository.RecipeDetailRepository
import com.yeraygarcia.recipes.util.Debug
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import kotlinx.android.synthetic.main.fragment_recipe_edit.view.*
import java.util.*

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class EditRecipeFragment : Fragment() {

    private var recipeId = DEFAULT_RECIPE_ID
    private lateinit var parentActivity: FragmentActivity
    private lateinit var editRecipeAdapter: EditRecipeAdapter
    private lateinit var viewModel: RecipeDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        Debug.d(this, "onCreate(savedInstanceState)")
        super.onCreate(savedInstanceState)

        activity?.let { parent ->

            parentActivity = parent
            setupRecipeId(savedInstanceState, parent.intent)
            setupViewModel(parent)
            setupAdapter(parent)

            viewModel.recipe.observe(this, Observer {
                toolbarLayout.title = it?.name
                editRecipeAdapter.recipe = it
            })
            viewModel.recipeSteps.observe(
                this,
                Observer { editRecipeAdapter.steps = it ?: emptyList() }
            )
            viewModel.recipeIngredients.observe(
                this,
                Observer { editRecipeAdapter.ingredients = it ?: emptyList() }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Debug.d(this, "onSaveInstanceState(outState)")
        outState.putSerializable(RecipeDetailFragment.KEY_RECIPE_ID, recipeId)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        Debug.d(this, "onPause()")
        viewModel.persistDraft()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)")

        val rootView = inflater.inflate(R.layout.fragment_recipe_edit, container, false)

        if (::parentActivity.isInitialized && ::editRecipeAdapter.isInitialized) {
            rootView.recyclerview_recipe_edit.apply {
                layoutManager = LinearLayoutManager(parentActivity)
                adapter = editRecipeAdapter
                isNestedScrollingEnabled = false
            }
        }

        return rootView
    }

    private fun setupRecipeId(savedInstanceState: Bundle?, intent: Intent?) {
        savedInstanceState?.apply {
            if (containsKey(KEY_RECIPE_ID)) {
                recipeId = getSerializable(KEY_RECIPE_ID) as UUID
            }
        }

        intent?.apply {
            if (hasExtra(ARG_RECIPE_ID)) {
                if (recipeId == DEFAULT_RECIPE_ID) {
                    recipeId = getSerializableExtra(ARG_RECIPE_ID) as UUID
                }
            }
        }
    }

    private fun setupViewModel(parent: FragmentActivity) {
        val repository = RecipeDetailRepository(parent.application)
        val factory = RecipeDetailViewModelFactory(repository, recipeId)
        viewModel = ViewModelProviders.of(this, factory).get(RecipeDetailViewModel::class.java)
    }

    private fun setupAdapter(parent: FragmentActivity) {
        editRecipeAdapter = EditRecipeAdapter(parent, viewModel)
    }

    companion object {

        const val ARG_RECIPE_ID = "argRecipeId"
        const val KEY_RECIPE_ID = "keyRecipeId"
        val DEFAULT_RECIPE_ID = UUID.randomUUID()!!

        fun newInstance(recipeId: UUID): EditRecipeFragment {
            val fragment = EditRecipeFragment()
            val args = Bundle()
            args.putString(ARG_RECIPE_ID, recipeId.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
