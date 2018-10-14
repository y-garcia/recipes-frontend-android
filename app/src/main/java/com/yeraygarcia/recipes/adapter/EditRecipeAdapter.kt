package com.yeraygarcia.recipes.adapter

import android.graphics.Typeface.BOLD
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.yeraygarcia.recipes.EditIngredientDialog
import com.yeraygarcia.recipes.EditIngredientDialog.Companion.TAG_DIALOG_EDIT_INGREDIENT
import com.yeraygarcia.recipes.EditStepDialog
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import kotlinx.android.synthetic.main.item_edit_duration.view.*
import kotlinx.android.synthetic.main.item_edit_ingredient.view.*
import kotlinx.android.synthetic.main.item_edit_source.view.*
import kotlinx.android.synthetic.main.item_edit_step.view.*
import kotlinx.android.synthetic.main.item_header_generic.view.*
import kotlinx.android.synthetic.main.view_servings.view.*
import timber.log.Timber
import java.util.*


class EditRecipeAdapter(val activity: FragmentActivity, val viewModel: RecipeDetailViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater = LayoutInflater.from(activity)

    var recipe: Recipe? = null
        set(value) {
            Timber.d("setRecipe(recipe)")
            field = value
            calculateItemCount()
            notifyDataSetChanged()
        }

    var ingredients: List<UiRecipeIngredient> = emptyList()
        set(value) {
            Timber.d("setIngredients(ingredients(${value.size}))")
            field = value
            calculateItemCount()
            notifyDataSetChanged()
        }

    var steps: List<RecipeStep> = emptyList()
        set(value) {
            Timber.d("setSteps(steps(${value.size}))")
            field = value
            calculateItemCount()
            notifyDataSetChanged()
        }

    private var itemCount: Int = -1

    fun EditText.onChange(cb: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                cb(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    internal inner class DurationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemDuration: EditText = itemView.edittext_duration.apply {
            onChange { viewModel.saveDurationToDraft(it, recipe) }
        }
    }

    internal inner class SourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemSource: EditText = itemView.edittext_source.apply {
            onChange { viewModel.saveSourceToDraft(it, recipe) }
        }
    }

    internal inner class IngredientsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemIngredientName: TextView = itemView.textview_ingredient_name.apply {
            setOnClickListener {
                val ingredient = ingredients[getIngredientPosition(adapterPosition)]
                showEditIngredientDialog(ingredient.id)
            }
        }
    }

    internal inner class IngredientsHeaderViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val itemServings: TextView = itemView.textview_servings
        val itemDecrease: ImageView = itemView.imageview_decrease_servings.apply {
            setOnClickListener {
                recipe?.let { recipe ->
                    recipe.decreasePortions()
                    viewModel.update(recipe)
                }
            }
        }
        val itemIncrease: ImageView = itemView.imageview_increase_servings.apply {
            setOnClickListener {
                recipe?.let { recipe ->
                    recipe.increasePortions()
                    viewModel.update(recipe)
                }
            }
        }
    }

    internal inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.textview_title
    }

    internal inner class StepsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemStepNumber: TextView = itemView.textview_step_number
        val itemStepDescription: TextView = itemView.textview_step_description.apply {
            setOnClickListener { showEditStepDialog(getStepAt(adapterPosition).id) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // there is always at least one ingredient item to show the "no ingredients" when empty
        val ingredientsCount = maxOf(ingredients.size, 1)

        return when {
            position == 0 -> VIEW_TYPE_DURATION
            position == 1 -> VIEW_TYPE_SOURCE
            position == 2 -> VIEW_TYPE_INGREDIENTS_HEADER
            position < ingredientsCount + 3 -> VIEW_TYPE_INGREDIENT
            position == ingredientsCount + 3 -> VIEW_TYPE_STEPS_HEADER
            else -> VIEW_TYPE_STEP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_INGREDIENTS_HEADER -> IngredientsHeaderViewHolder(
                inflater.inflate(R.layout.item_header_ingredients, parent, false)
            )

            VIEW_TYPE_STEPS_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_header_generic, parent, false)
            )


            VIEW_TYPE_DURATION -> DurationViewHolder(
                inflater.inflate(R.layout.item_edit_duration, parent, false)
            )

            VIEW_TYPE_SOURCE -> SourceViewHolder(
                inflater.inflate(R.layout.item_edit_source, parent, false)
            )

            VIEW_TYPE_INGREDIENT -> IngredientsViewHolder(
                inflater.inflate(R.layout.item_edit_ingredient, parent, false)
            )

            else -> StepsViewHolder(
                inflater.inflate(R.layout.item_edit_step, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        if (itemCount == -1) {
            calculateItemCount()
        }
        return itemCount
    }

    private fun calculateItemCount() {
        itemCount = (if (recipe != null) 4 else 0) +
                maxOf(ingredients.size, 1) +
                maxOf(steps.size, 1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {

            VIEW_TYPE_INGREDIENTS_HEADER -> (holder as IngredientsHeaderViewHolder).apply {
                recipe?.apply {
                    itemServings.text = String.format(Locale.getDefault(), "%d", portions)
                    itemDecrease.visibility = if (portions > 1) View.VISIBLE else View.GONE
                }
            }

            VIEW_TYPE_STEPS_HEADER -> (holder as HeaderViewHolder).itemTitle.setText(R.string.header_steps)

            VIEW_TYPE_DURATION -> recipe?.let { it ->
                val recipe = viewModel.recipeDraft ?: it
                val duration = formatDuration(recipe.durationInMinutes)
                (holder as DurationViewHolder).itemDuration.setText(duration)
            }

            VIEW_TYPE_SOURCE -> (holder as SourceViewHolder).itemSource.setText(recipe?.url)

            VIEW_TYPE_INGREDIENT -> (holder as IngredientsViewHolder).apply {
                if (ingredients.isNotEmpty()) {
                    getIngredientAt(position).apply {
                        itemIngredientName.text = makeBold(formattedQuantityAndUnit, toString())
                    }
                } else {
                    Timber.d("No ingredients")
                    // Covers the case of data not being ready yet.
                    itemIngredientName.setText(R.string.no_ingredients)
                }
            }

            else -> (holder as StepsViewHolder).apply {
                if (steps.isNotEmpty()) {
                    getStepAt(position).apply {
                        itemStepNumber.text = formatSortOrder(sortOrder)
                        itemStepDescription.text = description
                    }
                } else {
                    // Covers the case of data not being ready yet.
                    itemStepDescription.setText(R.string.no_steps)
                }
            }
        }
    }

    private fun makeBold(boldText: String, text: String): SpannableStringBuilder {
        val start = text.indexOf(boldText)
        val end = start + boldText.length
        val str = SpannableStringBuilder(text)
        str.setSpan(StyleSpan(BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return str
    }

    private fun getIngredientPosition(position: Int): Int {
        return maxOf(position - 3, 0)
    }

    private fun getStepPosition(position: Int): Int {
        return maxOf(position - maxOf(ingredients.size, 1) - 4, 0)
    }

    private fun getIngredientAt(position: Int): UiRecipeIngredient {
        return ingredients[getIngredientPosition(position)]
    }

    private fun getStepAt(position: Int): RecipeStep {
        return steps[getStepPosition(position)]
    }

    private fun showEditIngredientDialog(ingredientId: UUID) {
        EditIngredientDialog.newInstance(ingredientId)
            .show(activity.supportFragmentManager, TAG_DIALOG_EDIT_INGREDIENT)
    }

    private fun showEditStepDialog(stepId: UUID) {
        EditStepDialog.newInstance(stepId)
            .show(activity.supportFragmentManager, TAG_DIALOG_EDIT_INGREDIENT)
    }

    private fun formatSortOrder(sortOrder: Int): String {
        return String.format(Locale.getDefault(), "%d", sortOrder)
    }

    private fun formatDuration(duration: Long): String {
        return String.format(Locale.getDefault(), "%d", duration)
    }
    
    companion object {
        private const val VIEW_TYPE_DURATION = 1
        private const val VIEW_TYPE_SOURCE = 2
        private const val VIEW_TYPE_INGREDIENTS_HEADER = 3
        private const val VIEW_TYPE_STEPS_HEADER = 4
        private const val VIEW_TYPE_INGREDIENT = 5
        private const val VIEW_TYPE_STEP = 6
    }
}