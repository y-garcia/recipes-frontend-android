package com.yeraygarcia.recipes;

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

public class EditItemDialogFragment extends DialogFragment {

    public static final String TAG_FRAGMENT_EDIT_ITEM_DIALOG = "tagEditItemDialogFragment";

    private static final String ARG_ITEM_ID = "argItemId";
    private static final long DEFAULT_ITEM_ID = -1;

    private long mItemId;
    private RecipeViewModel mViewModel;
    private int mPortions;

    public EditItemDialogFragment() {

    }

    public static EditItemDialogFragment newInstance(long shoppingListItemId) {
        EditItemDialogFragment fragment = new EditItemDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ITEM_ID, shoppingListItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        FragmentActivity mParentActivity = requireActivity();
        mItemId = getItemIdFromArgs();

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
        mViewModel = ViewModelProviders.of(mParentActivity).get(RecipeViewModel.class);
        mViewModel.getIngredient(mItemId).observe(mParentActivity, item -> {
            if (item != null) {
                ingredientEditText.setText(item.getName());
                quantityEditText.setText(item.getFormattedQuantity());
                unitEditText.setText(item.getFormattedUnit());
                mPortions = item.getPortions();
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
                        if (mPortions != 0) {
                            quantity /= (double) mPortions;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), R.string.quantity_valid_number, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                Long unitId = null;
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

                mViewModel.updateRecipeIngredient(mItemId, ingredient, quantity, unitId);
                alertDialog.dismiss();
            });
        });

        return alertDialog;
    }

    private long getItemIdFromArgs() {
        long id = DEFAULT_ITEM_ID;

        if (getArguments() != null) {
            id = getArguments().getLong(ARG_ITEM_ID, DEFAULT_ITEM_ID);
        }

        return id;
    }
}