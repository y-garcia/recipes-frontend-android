package com.yeraygarcia.recipes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.ArrayAdapter
import com.yeraygarcia.recipes.adapter.ShoppingListAdapter
import com.yeraygarcia.recipes.util.ShortDivider
import com.yeraygarcia.recipes.util.SpaceTokenizer
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.fragment_shopping_list.view.*
import timber.log.Timber

class ShoppingListFragment : Fragment() {

    lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView(inflater, container, savedInstanceState)")
        val rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        setHasOptionsMenu(true)

        activity?.let { activity ->

            if (!::viewModel.isInitialized) {
                Timber.d("ViewModel was empty. Initializing...")
                viewModel = ViewModelProviders.of(activity).get(RecipeViewModel::class.java)
            }

            val newItemAdapter = ArrayAdapter(
                activity,
                android.R.layout.simple_dropdown_item_1line,
                mutableListOf<String>()
            )
            val newItemEditText = rootView.editTextNewItem.apply {
                setAdapter(newItemAdapter)
                setTokenizer(SpaceTokenizer())
            }

            rootView.imageButtonAddItem.setOnClickListener {
                val newItem = newItemEditText.text.toString()
                viewModel.addItemToShoppingList(newItem)
                newItemEditText.setText("")
            }

            val shoppingListAdapter = ShoppingListAdapter(activity, viewModel)
            val shoppingListRecyclerView = rootView.recyclerViewShoppingList.apply {
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(ShortDivider(activity, DividerItemDecoration.VERTICAL, 16))
                adapter = shoppingListAdapter
            }

            viewModel.shoppingListItems.observe(activity, Observer {
                shoppingListAdapter.shoppingListItems = it ?: emptyList()
            })
            viewModel.units.observe(activity, Observer {
                /* RecipeViewModel.getUnits() needs to be observed in order to fill RecipeViewModel.mUnits
                 * which is used in RecipeViewModel.getUnitIdByName()
                 * which is used in RecipeViewModel.addItemToShoppingList
                 * which is used further up */
            })
            viewModel.unitsAndIngredientNames.observe(activity, Observer {
                newItemAdapter.clear()
                newItemAdapter.addAll(it ?: mutableListOf())
            })

            // recognize when a user swipes an item
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                    val position = viewHolder.adapterPosition
                    val shoppingListItem = shoppingListAdapter.shoppingListItems[position]
                    viewModel.removeFromShoppingList(shoppingListItem)
                }
            }).attachToRecyclerView(shoppingListRecyclerView)
        }

        return rootView
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause()")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Timber.d("onCreateOptionsMenu(menu, inflated)")
        inflater?.inflate(R.menu.options_menu_shopping_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.clear_completed_button -> {
                viewModel.clearCompletedFromShoppingList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
