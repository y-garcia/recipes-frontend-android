package com.yeraygarcia.recipes.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import kotlinx.android.synthetic.main.item_duration_source.view.*
import kotlinx.android.synthetic.main.item_header_generic.view.*
import kotlinx.android.synthetic.main.item_ingredient.view.*
import kotlinx.android.synthetic.main.item_step.view.*
import kotlinx.android.synthetic.main.view_servings.view.*
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class RecipeDetailAdapter(
    private val context: Context,
    private val viewModel: RecipeDetailViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    private var recipe: Recipe? = null
    private var ingredients: List<UiRecipeIngredient> = emptyList()
    private var steps: List<RecipeStep> = emptyList()

    var selectedIngredient = RecyclerView.NO_POSITION
    var selectedStep = RecyclerView.NO_POSITION

    internal inner class IngredientsHeaderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val itemDecrease: ImageView = itemView.imageViewDecreaseServings
        val itemServings: TextView = itemView.textViewServings
        private val itemIncrease: ImageView = itemView.imageViewIncreaseServings

        init {
            itemDecrease.setOnClickListener {
                recipe?.let { recipe ->
                    if (recipe.portions > 1) {
                        recipe.decreasePortions()
                        viewModel.update(recipe)
                    }
                }
            }

            itemIncrease.setOnClickListener {
                recipe?.let { recipe ->
                    recipe.increasePortions()
                    viewModel.update(recipe)
                }
            }
        }
    }

    internal inner class HeaderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.textview_title
    }

    internal inner class DurationSourceViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val itemSourceIcon: ImageView = itemView.imageview_source_icon
        val itemDuration: TextView = itemView.textview_duration
        val itemSource: TextView = itemView.textview_source

        init {
            itemSource.setOnClickListener { v ->
                val urlString = v.tag as String

                if (URLUtil.isValidUrl(urlString)) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                    context.startActivity(browserIntent)
                }
            }
        }
    }

    internal inner class IngredientsViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val itemQuantity: TextView = itemView.textview_ingredient_quantity
        val itemUnit: TextView = itemView.textview_ingredient_unit
        val itemIngredientName: TextView = itemView.textview_ingredient_name

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // updating old position
                    notifyItemChanged(selectedIngredient)
                    // deselect current item if it's already selected and the user clicks again on it
                    selectedIngredient =
                            if (adapterPosition == selectedIngredient) RecyclerView.NO_POSITION else adapterPosition
                    // update new position
                    notifyItemChanged(selectedIngredient)
                }
            }
        }
    }

    internal inner class StepsViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val itemStepNumber: TextView = itemView.textview_step_number
        val itemStepDescription: TextView = itemView.textview_step_description

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // updating old position
                    notifyItemChanged(selectedStep)
                    // deselect current item if it's already selected and the user clicks again on it
                    selectedStep =
                            if (adapterPosition == selectedStep) RecyclerView.NO_POSITION else adapterPosition
                    // update new position
                    notifyItemChanged(selectedStep)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // there is always at least one ingredient item to show the "no ingredients" when empty
        val ingredientsCount = maxOf(ingredients.size, 1)

        return when {
            position == 0 -> VIEWTYPE_DURATION_SOURCE
            position == 1 -> VIEWTYPE_INGREDIENTS_HEADER
            position < ingredientsCount + 2 -> VIEWTYPE_INGREDIENT
            position == ingredientsCount + 2 -> VIEWTYPE_STEPS_HEADER
            else -> VIEWTYPE_STEP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEWTYPE_INGREDIENTS_HEADER -> {
                val view = inflater.inflate(R.layout.item_header_ingredients, parent, false)
                IngredientsHeaderViewHolder(view)
            }

            VIEWTYPE_STEPS_HEADER -> {
                val view = inflater.inflate(R.layout.item_header_generic, parent, false)
                HeaderViewHolder(view)
            }

            VIEWTYPE_DURATION_SOURCE -> {
                val view = inflater.inflate(R.layout.item_duration_source, parent, false)
                DurationSourceViewHolder(view)
            }

            VIEWTYPE_INGREDIENT -> {
                val view = inflater.inflate(R.layout.item_ingredient, parent, false)
                IngredientsViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_step, parent, false)
                StepsViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {

            VIEWTYPE_INGREDIENTS_HEADER -> recipe?.let { recipe ->
                val portions = String.format(Locale.getDefault(), "%d", recipe.portions)
                val decreaseVisibility = if (recipe.portions > 1) View.VISIBLE else View.GONE
                (holder as IngredientsHeaderViewHolder).apply {
                    itemServings.text = portions
                    itemDecrease.visibility = decreaseVisibility
                }
            }

            VIEWTYPE_STEPS_HEADER -> (holder as HeaderViewHolder).itemTitle.setText(R.string.header_steps)

            VIEWTYPE_DURATION_SOURCE -> recipe?.let { recipe ->
                formatDurationSourceView(
                    holder as DurationSourceViewHolder,
                    recipe.getFormattedDuration(context),
                    recipe.url
                )
            }

            VIEWTYPE_INGREDIENT -> (holder as IngredientsViewHolder).apply {
                itemView.isSelected = selectedIngredient == position
                if (ingredients.isNotEmpty()) {
                    getIngredientAt(position).apply {
                        itemQuantity.text = formattedQuantity
                        itemUnit.text = formattedUnit
                        itemIngredientName.text = name
                    }
                } else {
                    Timber.d("No ingredients")
                    itemIngredientName.setText(R.string.no_ingredients)
                }
            }

            else -> (holder as StepsViewHolder).apply {
                itemView.isSelected = selectedStep == position
                if (steps.isNotEmpty()) {
                    getStepAt(position).apply {
                        itemStepNumber.text = formatSortOrder(sortOrder)
                        itemStepDescription.text = description
                    }
                } else {
                    Timber.d("No steps")
                    itemStepDescription.setText(R.string.no_steps)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return (if (recipe != null) 3 else 0) + maxOf(ingredients.size, 1) + maxOf(steps.size, 1)

    }

    private fun getIngredientPosition(position: Int): Int {
        return maxOf(position - 2, 0)
    }

    private fun getStepPosition(position: Int): Int {
        return maxOf(position - maxOf(ingredients.size, 1) - 3, 0)
    }

    private fun getIngredientAt(position: Int): UiRecipeIngredient {
        return ingredients[getIngredientPosition(position)]
    }

    private fun getStepAt(position: Int): RecipeStep {
        return steps[getStepPosition(position)]
    }

    fun setRecipe(recipe: Recipe?) {
        Timber.d("setRecipe($recipe)")
        this.recipe = recipe
        notifyDataSetChanged()
    }

    fun setSteps(steps: List<RecipeStep>) {
        Timber.d("setSteps(steps($steps}))")
        this.steps = steps
        notifyDataSetChanged()
    }

    fun setIngredients(ingredients: List<UiRecipeIngredient>) {
        Timber.d("setIngredients(ingredients($ingredients))")
        this.ingredients = ingredients
        notifyDataSetChanged()
    }

    private fun formatSortOrder(sortOrder: Int): String {
        return String.format(Locale.getDefault(), "%d", sortOrder)
    }

    private fun formatDurationSourceView(
        viewHolder: DurationSourceViewHolder,
        duration: String,
        url: String?
    ) {

        viewHolder.itemDuration.text = duration

        if (url == null) {
            viewHolder.itemSourceIcon.visibility = View.INVISIBLE
            viewHolder.itemSource.visibility = View.INVISIBLE
            viewHolder.itemSource.tag = null
        } else {
            viewHolder.itemSourceIcon.visibility = View.VISIBLE
            viewHolder.itemSource.visibility = View.VISIBLE
            viewHolder.itemSource.tag = url

            if (URLUtil.isValidUrl(url)) {
                val formattedUrl = formatUrl(url)
                TextViewCompat.setTextAppearance(viewHolder.itemSource, R.style.source_link)
                val content = SpannableString(formattedUrl)
                content.setSpan(UnderlineSpan(), 0, formattedUrl.length, 0)
                viewHolder.itemSource.text = content
            } else {
                TextViewCompat.setTextAppearance(viewHolder.itemSource, R.style.source_text)
                viewHolder.itemSource.text = url
            }
        }
    }

    private fun formatUrl(urlString: String?): String {

        if (urlString == null) {
            return context.getString(R.string.no_source)
        }

        return try {
            URL(urlString).host
        } catch (e: MalformedURLException) {
            urlString // urlString is not a proper url, just return it as is
        }
    }

    companion object {
        private const val VIEWTYPE_DURATION_SOURCE = 1
        private const val VIEWTYPE_INGREDIENTS_HEADER = 2
        private const val VIEWTYPE_STEPS_HEADER = 3
        private const val VIEWTYPE_INGREDIENT = 4
        private const val VIEWTYPE_STEP = 5
    }
}
