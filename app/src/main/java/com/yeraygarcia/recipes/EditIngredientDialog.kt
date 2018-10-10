package com.yeraygarcia.recipes

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.dialog_edit_shopping_list_item.view.*
import java.util.*

class EditIngredientDialog : DialogFragment() {

    private lateinit var ingredientId: UUID
    private lateinit var viewModel: RecipeViewModel
    private var portions: Int = 0

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val activity = requireActivity()
        ingredientId = getIngredientIdFromArgs()

        // setup all views
        val dialogLayout =
            activity.layoutInflater.inflate(R.layout.dialog_edit_shopping_list_item, null)

        val ingredientEditText = dialogLayout.editTextIngredientName
        val ingredientAdapter = ArrayAdapter(
            activity, android.R.layout.simple_dropdown_item_1line, ArrayList<String>()
        )
        ingredientEditText.setAdapter(ingredientAdapter)

        val quantityEditText =
            dialogLayout.findViewById<EditText>(R.id.editTextIngredientQuantity)

        val unitEditText =
            dialogLayout.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewIngredientUnit)
        val unitAdapter =
            ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ArrayList<String>())
        unitEditText.setAdapter(unitAdapter)

        // setup view model
        viewModel = ViewModelProviders.of(activity).get(RecipeViewModel::class.java)
        viewModel.getIngredient(ingredientId).observe(activity, Observer { ingredient ->
            ingredient?.let {
                ingredientEditText.setText(it.name)
                quantityEditText.setText(it.formattedQuantity)
                unitEditText.setText(it.formattedUnit)
                portions = it.portions
            }
        })
        viewModel.ingredientNames.observe(activity, Observer { ingredientNames ->
            ingredientAdapter.clear()
            ingredientAdapter.addAll(ingredientNames ?: emptyList())
        })
        viewModel.unitNames.observe(activity, Observer { unitNames ->
            unitAdapter.clear()
            unitAdapter.addAll(unitNames ?: emptyList())
        })
        viewModel.units.observe(activity, Observer {
            /* RecipeViewModel.getUnits() needs to be observed in order to fill RecipeViewModel.mUnits
             * which is used in RecipeViewModel.getUnitIdByName()
             * which is used in the dialog */
        })

        // create dialog
        val alertDialog = AlertDialog.Builder(activity)
            .setView(dialogLayout)
            .setPositiveButton(R.string.save, null) // the listener is defined further down
            .setNegativeButton(R.string.cancel, null)
            .create()

        // always show the keyboard
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        alertDialog.setOnShowListener { _ ->

            // focus the quantity field by default
            quantityEditText.requestFocus()

            // define what happens when 'save' is clicked
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val ingredient = ingredientEditText.text.toString().trim()
                if (ingredient.isEmpty()) {
                    Toast.makeText(context, R.string.ingredient_not_empty, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                var quantity: Double? = null
                val quantityText = quantityEditText.text.toString().trim()
                if (!quantityText.isEmpty()) {
                    try {
                        quantity = java.lang.Double.valueOf(quantityText.replace(",", "."))
                        if (portions != 0) {
                            quantity /= portions.toDouble()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, R.string.quantity_valid_number, Toast.LENGTH_LONG)
                            .show()
                        return@setOnClickListener
                    }

                }

                var unitId: UUID? = null
                val unitName = unitEditText.text.toString().trim()
                if (!unitName.isEmpty()) {
                    unitId = viewModel.getUnitIdByName(unitName)
                    if (unitId == null) {
                        Toast.makeText(context, R.string.unit_does_not_exist, Toast.LENGTH_LONG)
                            .show()
                        return@setOnClickListener
                    }
                }

                if (quantity == null) {
                    unitId = null
                }

                viewModel.updateRecipeIngredient(ingredientId, ingredient, quantity, unitId)
                alertDialog.dismiss()
            }
        }

        return alertDialog
    }

    private fun getIngredientIdFromArgs(): UUID {
        return if (arguments != null) {
            UUID.fromString(arguments?.getString(ARG_INGREDIENT_ID))
        } else {
            UUID.randomUUID()
        }
    }

    companion object {

        const val TAG_DIALOG_EDIT_INGREDIENT = "tagDialogEditIngredient"

        private const val ARG_INGREDIENT_ID = "argItemId"

        fun newInstance(ingredientId: UUID): EditIngredientDialog {
            return EditIngredientDialog().apply {
                arguments = Bundle().apply { putString(ARG_INGREDIENT_ID, ingredientId.toString()) }
            }
        }
    }
}