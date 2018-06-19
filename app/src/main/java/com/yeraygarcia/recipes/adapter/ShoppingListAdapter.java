package com.yeraygarcia.recipes.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    private List<UiShoppingListItem> mShoppingListItems;

    private LayoutInflater mInflater;
    private RecipeViewModel mViewModel;

    // Constructors

    public ShoppingListAdapter(FragmentActivity parent, RecipeViewModel viewModel) {
        mInflater = LayoutInflater.from(parent);
        mViewModel = viewModel;
    }

    public List<UiShoppingListItem> getShoppingListItems() {
        return mShoppingListItems;
    }

    // Internal classes

    class ShoppingListViewHolder extends RecyclerView.ViewHolder {

        LinearLayout itemContainer;
        EditText itemQuantity;
        TextView itemUnit;
        TextView itemName;
        CheckBox itemCompleted;

        private ShoppingListViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.container_shopping_list_item);
            itemQuantity = itemView.findViewById(R.id.edittext_item_quantity);
            itemUnit = itemView.findViewById(R.id.textview_item_unit);
            itemName = itemView.findViewById(R.id.textview_item_name);
            itemCompleted = itemView.findViewById(R.id.checkbox_item_completed);

            itemQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Debug.d(this, "afterTextChanged(" + s.toString() + ")");
                    saveQuantityToDraft(s.toString(), getAdapterPosition());
                }
            });

            itemCompleted.setOnClickListener(view -> {
                boolean checked = ((CheckBox) view).isChecked();

                mViewModel.markComplete(getItemAt(getAdapterPosition()), checked);
            });
        }
    }

    private void saveQuantityToDraft(String newQuantityText, int position) {
        Debug.d(this, "saveQuantityToDraft(newQuantityText = " + newQuantityText + ", position = " + position + ")");
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        UiShoppingListItem shoppingListItem = mShoppingListItems.get(position);
        Double oldQuantity = shoppingListItem.getQuantity();

        try {
            if (newQuantityText.isEmpty() && oldQuantity != null
                    || !newQuantityText.isEmpty() && oldQuantity == null
                    || !Double.valueOf(newQuantityText).equals(oldQuantity)) {
                // value has changed
                Double newQuantity = newQuantityText.isEmpty() ? null : Double.valueOf(newQuantityText);
                shoppingListItem.setQuantity(newQuantity);
                mViewModel.saveDraft(shoppingListItem);
            }
        } catch (NumberFormatException ignored) {
            Debug.d(this, "The entered value isn't a valid number");
        }
    }

    // Overrides

    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_shopping_list_item, parent, false);
        return new ShoppingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {
        if (mShoppingListItems != null && mShoppingListItems.size() > 0) {
            final UiShoppingListItem currentItem = mShoppingListItems.get(position);
            holder.itemQuantity.setText(currentItem.getFormattedQuantity());
            holder.itemUnit.setText(currentItem.getFormattedUnit());
            holder.itemName.setText(currentItem.getName());
            holder.itemCompleted.setChecked(currentItem.getCompleted());
            formatCompletedView(holder, currentItem.getCompleted());
        } else {
            Debug.d(this, "no items");
            // Covers the case of data not being ready yet.
            holder.itemQuantity.setText("");
            holder.itemUnit.setText("");
            holder.itemName.setText(R.string.no_shopping_list_items);
            holder.itemCompleted.setChecked(false);
            formatCompletedView(holder, false);
        }
    }

    @Override
    public int getItemCount() {
        if (mShoppingListItems != null) {
            return mShoppingListItems.size();
        }
        return 0;
    }

    private void formatCompletedView(ShoppingListViewHolder holder, boolean completed) {
        int textAppearance = completed ? R.style.shopping_list_item_completed : R.style.shopping_list_item;
        int strikeThrough = completed ? R.drawable.selector_strikethrough : 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.itemQuantity.setEnabled(!completed);
            holder.itemQuantity.setTextAppearance(textAppearance);
            holder.itemUnit.setTextAppearance(textAppearance);
            holder.itemName.setTextAppearance(textAppearance);
            holder.itemContainer.setBackgroundResource(strikeThrough);
        } else {
            holder.itemQuantity.setTextAppearance(mInflater.getContext(), textAppearance);
            holder.itemUnit.setTextAppearance(mInflater.getContext(), textAppearance);
            holder.itemName.setTextAppearance(mInflater.getContext(), textAppearance);
            holder.itemContainer.setBackgroundResource(strikeThrough);
        }
    }

    private UiShoppingListItem getItemAt(int position) {
        return mShoppingListItems.get(position);
    }

    public void setShoppingListItems(List<UiShoppingListItem> shoppingListItems) {
        Debug.d(this, "setShoppingListItems(shoppingListItems -> " + shoppingListItems.size() + ")");
        mShoppingListItems = shoppingListItems;
        notifyDataSetChanged();
    }
}
