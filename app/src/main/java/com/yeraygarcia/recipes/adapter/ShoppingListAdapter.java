package com.yeraygarcia.recipes.adapter;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yeraygarcia.recipes.EditShoppingListItemDialog;
import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.List;
import java.util.UUID;

import static com.yeraygarcia.recipes.EditShoppingListItemDialog.TAG_FRAGMENT_EDIT_DIALOG;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    private final FragmentActivity mParentActivity;
    private List<UiShoppingListItem> mShoppingListItems;

    private LayoutInflater mInflater;
    private RecipeViewModel mViewModel;

    // Constructors

    public ShoppingListAdapter(FragmentActivity parent, RecipeViewModel viewModel) {
        mParentActivity = parent;
        mInflater = LayoutInflater.from(parent);
        mViewModel = viewModel;
    }

    public List<UiShoppingListItem> getShoppingListItems() {
        return mShoppingListItems;
    }

    // Internal classes

    class ShoppingListViewHolder extends RecyclerView.ViewHolder {

        LinearLayout itemContainer;
        TextView itemQuantity;
        TextView itemUnit;
        TextView itemName;
        CheckBox itemCompleted;

        private ShoppingListViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.container_shopping_list_item);
            itemQuantity = itemView.findViewById(R.id.textview_item_quantity);
            itemUnit = itemView.findViewById(R.id.textview_item_unit);
            itemName = itemView.findViewById(R.id.textview_item_name);
            itemCompleted = itemView.findViewById(R.id.checkbox_item_completed);

            itemContainer.setOnClickListener(view -> {
                UUID shoppingListItemId = mShoppingListItems.get(getAdapterPosition()).getId();
                showEditDialog(shoppingListItemId);
            });
            itemCompleted.setOnClickListener(view -> {
                boolean checked = ((CheckBox) view).isChecked();
                mViewModel.markComplete(getItemAt(getAdapterPosition()), checked);
            });
        }
    }

    private void showEditDialog(UUID shoppingListItemId) {
        DialogFragment dialog = EditShoppingListItemDialog.newInstance(shoppingListItemId);
        dialog.show(mParentActivity.getSupportFragmentManager(), TAG_FRAGMENT_EDIT_DIALOG);
    }

//    private void saveQuantityToDraft(String newQuantityText, int position) {
//        Debug.d(this, "saveQuantityToDraft(newQuantityText = " + newQuantityText + ", position = " + position + ")");
//        if (position == RecyclerView.NO_POSITION) {
//            return;
//        }
//
//        UiShoppingListItem shoppingListItem = mShoppingListItems.get(position);
//        Double oldQuantity = shoppingListItem.getQuantity();
//
//        try {
//            if (newQuantityText.isEmpty() && oldQuantity != null
//                    || !newQuantityText.isEmpty() && oldQuantity == null
//                    || !Double.valueOf(newQuantityText).equals(oldQuantity)) {
//                // value has changed
//                Double newQuantity = newQuantityText.isEmpty() ? null : Double.valueOf(newQuantityText);
//                shoppingListItem.setQuantity(newQuantity);
//                mViewModel.saveDraft(shoppingListItem);
//            }
//        } catch (NumberFormatException ignored) {
//            Debug.d(this, "The entered value isn't a valid number");
//        }
//    }

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
        int strikeThrough = completed ?
                holder.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                holder.itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG);

        TextViewCompat.setTextAppearance(holder.itemName, textAppearance);
        holder.itemName.setPaintFlags(strikeThrough);
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
