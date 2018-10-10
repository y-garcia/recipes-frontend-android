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
import android.widget.Toast
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.dialog_edit_shopping_list_item.view.*
import java.util.*

class EditShoppingListItemDialog : DialogFragment() {

    private lateinit var shoppingListItemId: UUID

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val activity = requireActivity()
        shoppingListItemId = getShoppingListItemIdFromArgs()

        val dialogLayout =
            activity.layoutInflater.inflate(R.layout.dialog_edit_shopping_list_item, null)

        val ingredientAdapter =
            ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ArrayList<String>())
        val unitAdapter =
            ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ArrayList<String>())

        val ingredientEditText = dialogLayout.editTextIngredientName.apply {
            setAdapter(ingredientAdapter)
        }
        val quantityEditText = dialogLayout.editTextIngredientQuantity
        val unitEditText = dialogLayout.autoCompleteTextViewIngredientUnit.apply {
            setAdapter(unitAdapter)
        }

        // setup view model
        val viewModel = ViewModelProviders.of(activity).get(RecipeViewModel::class.java)
        viewModel.getShoppingListItem(shoppingListItemId)
            .observe(activity, Observer { shoppingListItem ->
                shoppingListItem?.let {
                    ingredientEditText.setText(it.name)
                    quantityEditText.setText(it.formattedQuantity)
                    unitEditText.setText(it.formattedUnit)
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
                    Toast.makeText(context, R.string.ingredient_not_empty, Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }

                var quantity: Double? = null
                val quantityText = quantityEditText.text.toString().trim()
                if (!quantityText.isEmpty()) {
                    try {
                        quantity = java.lang.Double.valueOf(quantityText)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, R.string.quantity_valid_number, Toast.LENGTH_LONG)
                            .show()
                        return@setOnClickListener
                    }

                }

                var unitId: UUID? = null

                if (quantity != null) {
                    val unitName = unitEditText.text.toString().trim()
                    if (!unitName.isEmpty()) {
                        unitId = viewModel.getUnitIdByName(unitName)
                        if (unitId == null) {
                            Toast.makeText(context, R.string.unit_does_not_exist, Toast.LENGTH_LONG)
                                .show()
                            return@setOnClickListener
                        }
                    }
                }

                viewModel.updateShoppingListItem(shoppingListItemId, ingredient, quantity, unitId)
                alertDialog.dismiss()
            }
        }

        return alertDialog
    }

    private fun getShoppingListItemIdFromArgs(): UUID {
        return if (arguments?.containsKey(ARG_SHOPPING_LIST_ITEM_ID) == true) {
            UUID.fromString(arguments?.getString(ARG_SHOPPING_LIST_ITEM_ID))
        } else {
            UUID.fromString(DEFAULT_SHOPPING_LIST_ITEM_ID)
        }
    }

    companion object {

        const val TAG_FRAGMENT_EDIT_DIALOG = "tagEditDialogFragment"
        private const val ARG_SHOPPING_LIST_ITEM_ID = "argShoppingListItemId"
        private val DEFAULT_SHOPPING_LIST_ITEM_ID = UUID.randomUUID().toString()

        fun newInstance(shoppingListItemId: UUID): EditShoppingListItemDialog {
            return EditShoppingListItemDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId.toString())
                }
            }
        }
    }
}