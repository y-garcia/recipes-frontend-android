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

import com.yeraygarcia.recipes.database.entity.Unit;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditShoppingListItemDialogFragment extends DialogFragment {

    private static final String ARG_SHOPPING_LIST_ITEM_ID = "argShoppingListItemId";
    private static final String KEY_SHOPPING_LIST_ITEM_ID = "keyShoppingListItemId";
    private static final long DEFAULT_SHOPPING_LIST_ITEM_ID = -1;

    private FragmentActivity mParentActivity;
    private long mShoppingListItemId;

    public EditShoppingListItemDialogFragment() {

    }

    public static EditShoppingListItemDialogFragment newInstance(long shoppingListItemId) {
        EditShoppingListItemDialogFragment fragment = new EditShoppingListItemDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mParentActivity = getActivity();
        mShoppingListItemId = getShoppingListItemIdFromArgs();

        LayoutInflater inflater = mParentActivity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_edit_shopping_list_item, null);

        EditText ingredientEditText = dialogLayout.findViewById(R.id.edittext_ingredient_name);
        EditText quantityEditText = dialogLayout.findViewById(R.id.edittext_ingredient_quantity);

        AutoCompleteTextView unitEditText = dialogLayout.findViewById(R.id.autocompletetextview_ingredient_unit);
        unitEditText.setThreshold(1);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(mParentActivity, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        unitEditText.setAdapter(unitAdapter);

        RecipeViewModel mViewModel = ViewModelProviders.of(mParentActivity).get(RecipeViewModel.class);
        mViewModel.getShoppingListItem(mShoppingListItemId).observe(mParentActivity, shoppingListItem -> {
            if (shoppingListItem != null) {
                ingredientEditText.setText(shoppingListItem.getName());
                quantityEditText.setText(shoppingListItem.getFormattedQuantity());
                unitEditText.setAdapter(null);
                unitEditText.setText(shoppingListItem.getFormattedUnit());
                unitEditText.setAdapter(unitAdapter);
            }
        });
        mViewModel.getUnits().observe(mParentActivity, units -> {
            if (units != null) {
                unitAdapter.clear();
                List<String> unitNames = new ArrayList<>();
                for (Unit unit : units) {
                    unitNames.add(unit.getNameSingular());
                    if (!unit.getNameSingular().equals(unit.getNamePlural())) {
                        unitNames.add(unit.getNamePlural());
                    }
                }
                unitAdapter.addAll(unitNames);
            }
        });

        Dialog alertDialog = new AlertDialog.Builder(mParentActivity)
                .setView(dialogLayout)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }


        alertDialog.setOnShowListener(dialogInterface -> {

            quantityEditText.requestFocus();

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

                mViewModel.updateShoppingListItem(mShoppingListItemId, ingredient, quantity, unitId);
                alertDialog.dismiss();
            });
        });

        return alertDialog;
    }

    private long getShoppingListItemIdFromArgs() {
        long id = DEFAULT_SHOPPING_LIST_ITEM_ID;

        if (getArguments() != null) {
            id = getArguments().getLong(ARG_SHOPPING_LIST_ITEM_ID, DEFAULT_SHOPPING_LIST_ITEM_ID);
        }

        return id;
    }
}