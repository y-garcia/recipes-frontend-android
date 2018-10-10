package com.yeraygarcia.recipes.adapter

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.RecipeDetailActivity
import com.yeraygarcia.recipes.RecipeDetailFragment
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.item_recipe.view.*
import kotlinx.android.synthetic.main.view_servings_cart.view.*
import java.util.*
import kotlin.collections.ArrayList

class RecipeAdapter(parent: FragmentActivity) :
    RecyclerView.Adapter<RecipeAdapter.RecipesViewHolder>(), Filterable {

    private val context = parent
    private val inflater = parent.layoutInflater
    private val viewModel = ViewModelProviders.of(parent).get(RecipeViewModel::class.java)

    var recipes: List<Recipe> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var recipeIdsInShoppingList: List<UUID> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val filter: RecipeFilter by lazy { RecipeFilter(recipes) }

    private inner class RecipeFilter(originalList: List<Recipe>) : Filter() {

        private val originalList: List<Recipe> = LinkedList(originalList)
        private val filteredList: MutableList<Recipe> = ArrayList()

        override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {
            filteredList.clear()
            val results = Filter.FilterResults()

            if (charSequence.isEmpty()) {
                filteredList.addAll(originalList)
            } else {
                val filterPattern = charSequence.toString().toLowerCase().trim()
                for (recipe in originalList) {
                    if (recipe.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(recipe)
                    }
                }
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(
            charSequence: CharSequence,
            filterResults: Filter.FilterResults
        ) {
            recipes = filteredList
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    inner class RecipesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemName: TextView = itemView.textview_recipe_name
        val itemDuration: TextView = itemView.textview_recipe_info
        val itemServingsContainer: ConstraintLayout = itemView.include_servings as ConstraintLayout
        val itemDecrease: ImageView = itemView.imageview_decrease_servings
        val itemServings: TextView = itemView.textview_servings
        val itemAddToCart: ImageView = itemView.imageview_add_to_cart
        private val itemServingsLabel: TextView = itemView.textview_servings_label
        private val itemIncrease: ImageView = itemView.imageview_increase_servings
        private val itemNameContainer: LinearLayout = itemView.linearlayout_recipe_name

        init {

            itemNameContainer.setOnClickListener {
                val recipeId = recipes[adapterPosition].id
                // open Recipe Detail activity
                val intent = Intent(context, RecipeDetailActivity::class.java)
                intent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, recipeId)
                context.startActivity(intent)
            }

            val addToCartListener: (View) -> Unit = {
                val recipe = recipes[adapterPosition]
                val isInShoppingList = recipeIdsInShoppingList.contains(recipe.id)

                if (isInShoppingList) {
                    recipe.increasePortions()
                    viewModel.updatePortionsInShoppingList(recipe)
                } else {
                    viewModel.addRecipeToShoppingList(recipe)
                }
            }

            itemAddToCart.setOnClickListener(addToCartListener)
            itemIncrease.setOnClickListener(addToCartListener)
            itemServings.setOnClickListener(addToCartListener)
            itemServingsLabel.setOnClickListener(addToCartListener)

            itemDecrease.setOnClickListener {
                val recipe = recipes[adapterPosition]

                if (recipeIdsInShoppingList.contains(recipe.id)) {
                    if (recipe.portions == 1) {
                        viewModel.deleteRecipeFromShoppingList(recipe)
                    } else {
                        recipe.decreasePortions()
                        viewModel.updatePortionsInShoppingList(recipe)
                    }
                }
            }

            itemDecrease.setOnLongClickListener {
                val recipe = recipes[adapterPosition]

                if (recipeIdsInShoppingList.contains(recipe.id)) {
                    viewModel.deleteRecipeFromShoppingList(recipe)
                }

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesViewHolder {
        val view = inflater.inflate(R.layout.item_recipe, parent, false)
        return RecipesViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipesViewHolder, position: Int) {
        val currentRecipe = recipes[position]

        holder.itemName.text = currentRecipe.name
        holder.itemDuration.text = currentRecipe.getFormattedDuration(context)
        holder.itemServings.text = String.format(Locale.getDefault(), "%d", currentRecipe.portions)

        if (currentRecipe.portions == 1) {
            holder.itemDecrease.setImageResource(R.drawable.ic_remove_shopping_cart_24px)
        } else {
            holder.itemDecrease.setImageResource(R.drawable.ic_remove_24px)
        }

        val isInShoppingList = recipeIdsInShoppingList.contains(currentRecipe.id)
        holder.itemServingsContainer.visibility = if (isInShoppingList) View.VISIBLE else View.GONE
        holder.itemAddToCart.visibility = if (isInShoppingList) View.GONE else View.VISIBLE
    }

    // getItemCount() is called many times, and when it is first called,
    // mRecipes has not been updated (means initially, it's null, and we can't return null).
    override fun getItemCount(): Int {
        return recipes.size
    }
}
