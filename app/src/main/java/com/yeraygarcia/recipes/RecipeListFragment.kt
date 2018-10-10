package com.yeraygarcia.recipes

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Toast
import com.yeraygarcia.recipes.adapter.RecipeAdapter
import com.yeraygarcia.recipes.adapter.TagAdapter
import com.yeraygarcia.recipes.database.remote.RetrofitClient
import com.yeraygarcia.recipes.database.remote.Status
import com.yeraygarcia.recipes.util.ShortDivider
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.fragment_recipe_list.view.*
import timber.log.Timber

class RecipeListFragment : Fragment() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var viewModel: RecipeViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Timber.d("onCreateView(inflater, container, savedInstanceState)")
        val rootView = inflater.inflate(R.layout.fragment_recipe_list, container, false)

        rootView.fabAddRecipe.setOnClickListener { view ->
            Snackbar
                .make(view, R.string.feature_not_implemented, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        setHasOptionsMenu(true)

        activity?.let { parent ->

            swipeRefreshLayout = rootView.swipeRefreshLayout.apply {
                setOnRefreshListener {
                    Timber.d("onRefresh called from SwipeRefreshLayout")
                    refreshData()
                }
            }

            recipeAdapter = RecipeAdapter(parent)
            rootView.recyclerViewRecipeList.apply {
                layoutManager = LinearLayoutManager(parent)
                adapter = recipeAdapter
                addItemDecoration(ShortDivider(parent, DividerItemDecoration.VERTICAL, 16))
            }

            val tagAdapter = TagAdapter(parent)
            rootView.recyclerViewTagChips.apply {
                layoutManager = LinearLayoutManager(parent, LinearLayoutManager.HORIZONTAL, false)
                adapter = tagAdapter
            }

            viewModel = ViewModelProviders.of(parent).get(RecipeViewModel::class.java)
            savedInstanceState?.apply {
                if (containsKey(EXTRA_TAG_FILTER)) {
                    val tagFilter = getStringArray(EXTRA_TAG_FILTER)
                    viewModel.setTagFilter(tagFilter)
                }
            }
            viewModel.recipes.observe(this, Observer { resource ->
                Timber.d("getRecipes().observe($resource)")
                when (resource?.status) {
                    Status.LOADING -> swipeRefreshLayout.isRefreshing = true
                    Status.SUCCESS -> swipeRefreshLayout.isRefreshing = false
                    Status.ERROR -> {
                        Timber.d(resource.toString())
                        Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
                recipeAdapter.recipes = resource?.data ?: emptyList()
            })
            viewModel.tags.observe(this, Observer {
                Timber.d("getTags().observe(tags)")
                tagAdapter.tags = it ?: emptyList()
            })
            viewModel.tagFilter.observe(this, Observer {
                tagAdapter.selectedTagIds = it ?: mutableListOf()
            })
            viewModel.recipeIdsInShoppingList.observe(this, Observer {
                recipeAdapter.recipeIdsInShoppingList = it ?: mutableListOf()
            })

        }

        return rootView
    }

    private fun refreshData() {
        context?.let {
            RetrofitClient.get(it).setIdToken(null)
            viewModel.refreshAll()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("onSaveInstanceState(outState)")
        outState.putStringArrayList(EXTRA_TAG_FILTER, viewModel.tagFilterAsArray)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Timber.d("onCreateOptionsMenu(menu, inflated)")
        inflater?.inflate(R.menu.options_menu_recipe_list, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        val searchView = menu?.findItem(R.id.search_button)?.actionView as SearchView?
        searchView?.apply {
            setSearchableInfo(searchManager?.getSearchableInfo(activity?.componentName))
            setOnQueryTextListener(OnFilterRecipesListener())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.refresh_button -> {
                swipeRefreshLayout.isRefreshing = true
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class OnFilterRecipesListener : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            recipeAdapter.filter.filter(newText)
            return false
        }
    }

    companion object {
        private const val EXTRA_TAG_FILTER = "mTagFilter"
    }
}
