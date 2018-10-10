package com.yeraygarcia.recipes.adapter

import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.RecipeDetailActivity
import com.yeraygarcia.recipes.RecipeDetailFragment
import com.yeraygarcia.recipes.database.entity.Recipe
import kotlinx.android.synthetic.main.item_recipe_today.view.*

class TodayAdapter(private val parent: FragmentActivity) :
    RecyclerView.Adapter<TodayAdapter.RecipesViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(parent)

    var recipes: List<Recipe> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class RecipesViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var itemName: TextView = itemView.textview_recipe_name

        init {
            itemName.setOnClickListener {
                val recipeId = recipes[adapterPosition].id
                // open Recipe Detail activity
                val intent = Intent(parent, RecipeDetailActivity::class.java)
                intent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, recipeId)
                parent.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesViewHolder {
        val view = inflater.inflate(R.layout.item_recipe_today, parent, false)
        return RecipesViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipesViewHolder, position: Int) {
        holder.itemName.text = recipes[position].name
    }

    override fun getItemCount(): Int {
        return recipes.size
    }
}
