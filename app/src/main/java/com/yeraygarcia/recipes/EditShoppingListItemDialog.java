package com.yeraygarcia.recipes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.ArrayList;
import java.util.UUID;

public class EditShoppingListItemDialog extends DialogFragment {

    public static final String TAG_FRAGMENT_EDIT_DIALOG = "tagEditDialogFragment";
    private static final String ARG_SHOPPING_LIST_ITEM_ID = "argShoppingListItemId";
    private static final String DEFAULT_SHOPPING_LIST_ITEM_ID = UUID.randomUUID().toString();

    private UUID mShoppingListItemId;

    public EditShoppingListItemDialog() {

    }

    public static EditShoppingListItemDialog newInstance(UUID shoppingListItemId) {
        EditShoppingListItemDialog fragment = new EditShoppingListItemDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        FragmentActivity mParentActivity = requireActivity();
        mShoppingListItemId = getShoppingListItemIdFromArgs();

        // setup all views
        LayoutInflater inflater = mParentActivity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_edit_shopping_list_item, null);

        AutoCompleteTextView ingredientEditText = dialogLayout.findViewById(R.id.edittext_ingredient_name);
        ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<>(mParentActivity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        ingredientEditText.setAdapter(ingredientAdapter);

        EditText quantityEditText = dialogLayout.findViewById(R.id.edittext_ingredient_quantity);

        AutoCompleteTextView unitEditText = dialogLayout.findViewById(R.id.autocompletetextview_ingredient_unit);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(mParentActivity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        unitEditText.setAdapter(unitAdapter);

        // setup view model
        RecipeViewModel mViewModel = ViewModelProviders.of(mParentActivity).get(RecipeViewModel.class);
        mViewModel.getShoppingListItem(mShoppingListItemId).observe(mParentActivity, shoppingListItem -> {
            if (shoppingListItem != null) {
                ingredientEditText.setText(shoppingListItem.getName());
                quantityEditText.setText(shoppingListItem.getFormattedQuantity());
                unitEditText.setText(shoppingListItem.getFormattedUnit());
            }
        });
        mViewModel.getIngredientNames().observe(mParentActivity, ingredientNames -> {
            if (ingredientNames != null) {
                ingredientAdapter.clear();
                ingredientAdapter.addAll(ingredientNames);
            }
        });
        mViewModel.getUnitNames().observe(mParentActivity, unitNames -> {
            if (unitNames != null) {
                unitAdapter.clear();
                unitAdapter.addAll(unitNames);
            }
        });
        mViewModel.getUnits().observe(mParentActivity, units -> {
            /* RecipeViewModel.getUnits() needs to be observed in order to fill RecipeViewModel.mUnits
             * which is used in RecipeViewModel.getUnitIdByName()
             * which is used in the dialog */
        });

        // create dialog
        Dialog alertDialog = new AlertDialog.Builder(mParentActivity)
                .setView(dialogLayout)
                .setPositiveButton(R.string.save, null) // the listener is defined further down
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (alertDialog.getWindow() != null) {
            // always show the keyboard
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        alertDialog.setOnShowListener(dialogInterface -> {

            // focus the quantity field by default
            quantityEditText.requestFocus();

            // define what happens when 'save' is clicked
            Button positiveButton = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                final String ingredient = ingredientEditText.getText().toString().trim();
                if (ingredient.isEmpty()) {
                    Toast.makeText(getContext(), R.string.ingredient_not_empty, Toast.LENGTH_LONG).show();
                    return;
                }

                Double quantity = null;
                String quantityText = quantityEditText.getText().toString().trim();
                if (!quantityText.isEmpty()) {
                    try {
                        quantity = Double.valueOf(quantityText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), R.string.quantity_valid_number, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                UUID unitId = null;
                String unitName = unitEditText.getText().toString().trim();
                if (!unitName.isEmpty()) {
                    unitId = mViewModel.getUnitIdByName(unitName);
                    if (unitId == null) {
                        Toast.makeText(getContext(), R.string.unit_does_not_exist, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (quantity == null) {
                    unitId = null;
                }

                mViewModel.updateShoppingListItem(mShoppingListItemId, ingredient, quantity, unitId);
                alertDialog.dismiss();
            });
        });

        return alertDialog;
    }

    private UUID getShoppingListItemIdFromArgs() {
        UUID id = UUID.fromString(DEFAULT_SHOPPING_LIST_ITEM_ID);

        if (getArguments() != null) {
            id = UUID.fromString(getArguments().getString(ARG_SHOPPING_LIST_ITEM_ID, DEFAULT_SHOPPING_LIST_ITEM_ID));
        }

        return id;
    }
}