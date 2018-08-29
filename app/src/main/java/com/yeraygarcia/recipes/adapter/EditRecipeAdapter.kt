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
import com.yeraygarcia.recipes.EditIngredientDialog.TAG_DIALOG_EDIT_INGREDIENT
import com.yeraygarcia.recipes.EditStepDialog
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.database.entity.Recipe
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient
import com.yeraygarcia.recipes.util.Debug
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import kotlinx.android.synthetic.main.item_edit_duration.view.*
import kotlinx.android.synthetic.main.item_edit_ingredient.view.*
import kotlinx.android.synthetic.main.item_edit_source.view.*
import kotlinx.android.synthetic.main.item_edit_step.view.*
import kotlinx.android.synthetic.main.item_header_generic.view.*
import kotlinx.android.synthetic.main.view_servings.view.*
import java.util.*


class EditRecipeAdapter(val parent: FragmentActivity, val viewModel: RecipeDetailViewModel)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewTypeDuration = 1
    private val viewTypeSource = 2
    private val viewTypeIngredientHeader = 3
    private val viewTypeStepsHeader = 4
    private val viewTypeIngredient = 5
    private val viewTypeStep = 6

    private val inflater = LayoutInflater.from(parent)

    var recipe: Recipe? = null
        set(value) {
            Debug.d(this, "setRecipe(recipe)")
            field = value
            calculateItemCount()
            notifyDataSetChanged()
        }

    var ingredients: List<UiRecipeIngredient> = emptyList()
        set(value) {
            Debug.d(this, "setIngredients(ingredients(" + value.size + "))")
            field = value
            calculateItemCount()
            notifyDataSetChanged()
        }

    var steps: List<RecipeStep> = emptyList()
        set(value) {
            Debug.d(this, "setSteps(steps(" + value.size + "))")
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

        val itemDuration: EditText = itemView.edittext_duration

        init {
            itemDuration.onChange { viewModel.saveDurationToDraft(it, recipe) }
        }
    }

    internal inner class SourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemSource: EditText = itemView.edittext_source

        init {
            itemSource.onChange { viewModel.saveSourceToDraft(it, recipe) }
        }
    }

    internal inner class IngredientsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemIngredientName: TextView = itemView.textview_ingredient_name

        init {
            itemIngredientName.setOnClickListener {
                val ingredient = ingredients[getIngredientPosition(adapterPosition)]
                showEditIngredientDialog(ingredient.id)
            }
        }
    }

    internal inner class IngredientsHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemDecrease: ImageView = itemView.imageview_decrease_servings
        private val itemIncrease: ImageView = itemView.imageview_increase_servings
        val itemServings: TextView = itemView.textview_servings

        init {
            itemDecrease.setOnClickListener {
                recipe?.decreasePortions()
                viewModel.update(recipe)
            }

            itemIncrease.setOnClickListener {
                recipe?.increasePortions()
                viewModel.update(recipe)
            }
        }
    }

    internal inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.textview_title
    }

    internal inner class StepsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemStepNumber: TextView = itemView.textview_step_number
        val itemStepDescription: TextView = itemView.textview_step_description

        init {
            itemStepDescription.setOnClickListener {
                val step = steps[getStepPosition(adapterPosition)]
                showEditStepDialog(step.id)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // there is always at least one ingredient item to show the "no ingredients" when empty
        val ingredientsCount = maxOf(ingredients.size, 1)

        return when {
            position == 0 -> viewTypeDuration
            position == 1 -> viewTypeSource
            position == 2 -> viewTypeIngredientHeader
            position < ingredientsCount + 3 -> viewTypeIngredient
            position == ingredientsCount + 3 -> viewTypeStepsHeader
            else -> viewTypeStep
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewTypeIngredientHeader -> {
                val view = inflater.inflate(R.layout.item_header_ingredients, parent, false)
                IngredientsHeaderViewHolder(view)
            }

            viewTypeStepsHeader -> {
                val view = inflater.inflate(R.layout.item_header_generic, parent, false)
                HeaderViewHolder(view)
            }

            viewTypeDuration -> {
                val view = inflater.inflate(R.layout.item_edit_duration, parent, false)
                DurationViewHolder(view)
            }

            viewTypeSource -> {
                val view = inflater.inflate(R.layout.item_edit_source, parent, false)
                SourceViewHolder(view)
            }

            viewTypeIngredient -> {
                val view = inflater.inflate(R.layout.item_edit_ingredient, parent, false)
                IngredientsViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_edit_step, parent, false)
                StepsViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        if (itemCount == -1) {
            calculateItemCount()
        }
        return itemCount
    }

    private fun calculateItemCount() {
        itemCount = maxOf(ingredients.size, 1) + maxOf(steps.size, 1)
        if (recipe != null) {
            itemCount += 4
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {

            viewTypeIngredientHeader -> if (recipe != null) {
                val portions = String.format(Locale.getDefault(), "%d", recipe?.portions)
                (holder as IngredientsHeaderViewHolder).itemServings.text = portions

                val decreaseVisibility = if (recipe?.portions!! > 1) View.VISIBLE else View.GONE
                holder.itemDecrease.visibility = decreaseVisibility
            }

            viewTypeStepsHeader -> (holder as HeaderViewHolder).itemTitle.setText(R.string.header_steps)

            viewTypeDuration -> if (recipe != null) {
                val recipe = viewModel.getRecipeDraft(recipe)
                val duration = formatDuration(recipe.durationInMinutes)
                (holder as DurationViewHolder).itemDuration.setText(duration)
            }

            viewTypeSource -> (holder as SourceViewHolder).itemSource.setText(recipe?.url)

            viewTypeIngredient -> {
                val viewHolder = holder as IngredientsViewHolder
                if (ingredients.isNotEmpty()) {
                    val ingredient = ingredients[getIngredientPosition(position)]
                    viewHolder.itemIngredientName.text = makeBold(ingredient.formattedQuantityAndUnit, ingredient.toString())
                } else {
                    Debug.d(this, "no ingredients")
                    // Covers the case of data not being ready yet.
                    viewHolder.itemIngredientName.setText(R.string.no_ingredients)
                }
            }

            else -> {
                val viewHolder = holder as StepsViewHolder
                if (steps.isNotEmpty()) {
                    val currentStep = steps[getStepPosition(position)]
                    viewHolder.itemStepNumber.text = formatSortOrder(currentStep.sortOrder)
                    viewHolder.itemStepDescription.text = currentStep.description
                } else {
                    // Covers the case of data not being ready yet.
                    viewHolder.itemStepDescription.setText(R.string.no_steps)
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
        return maxOf(position - ingredients.size - 4, 0)
    }

    private fun showEditIngredientDialog(ingredientId: UUID) {
        EditIngredientDialog.newInstance(ingredientId)
                .show(parent.supportFragmentManager, TAG_DIALOG_EDIT_INGREDIENT)
    }

    private fun showEditStepDialog(stepId: UUID) {
        EditStepDialog.newInstance(stepId)
                .show(parent.supportFragmentManager, TAG_DIALOG_EDIT_INGREDIENT)
    }

    private fun formatSortOrder(sortOrder: Int): String {
        return String.format(Locale.getDefault(), "%d", sortOrder)
    }

    private fun formatDuration(duration: Long): String {
        return String.format(Locale.getDefault(), "%d", duration)
    }
}