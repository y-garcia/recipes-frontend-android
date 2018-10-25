package com.yeraygarcia.recipes.adapter

import android.graphics.Paint
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.yeraygarcia.recipes.EditShoppingListItemDialog
import com.yeraygarcia.recipes.EditShoppingListItemDialog.Companion.TAG_FRAGMENT_EDIT_DIALOG
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.item_shopping_list_item.view.*
import timber.log.Timber
import java.util.UUID
import kotlin.collections.ArrayList

class ShoppingListAdapter(
    private val parent: FragmentActivity,
    private val viewModel: RecipeViewModel
) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {

    var shoppingListItems: List<UiShoppingListItem> = ArrayList()
        set(shoppingListItems) {
            Timber.d("setShoppingListItems(shoppingListItems -> ${shoppingListItems.size})")
            field = shoppingListItems
            notifyDataSetChanged()
        }

    private val inflater = LayoutInflater.from(parent)

    inner class ShoppingListViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val itemQuantity: TextView = itemView.textViewItemQuantity
        val itemUnit: TextView = itemView.textViewItemUnit
        val itemName: TextView = itemView.textViewItemName
        val itemCompleted: CheckBox = itemView.checkboxItemCompleted
        private val itemContainer: LinearLayout = itemView.containerShoppingListItem

        init {
            itemContainer.setOnClickListener { showEditDialog(shoppingListItems[adapterPosition].id) }
            itemCompleted.setOnClickListener { view ->
                viewModel.markComplete(
                    getItemAt(
                        adapterPosition
                    ), (view as CheckBox).isChecked
                )
            }
        }
    }

    private fun showEditDialog(shoppingListItemId: UUID?) {
        val dialog = EditShoppingListItemDialog.newInstance(shoppingListItemId!!)
        dialog.show(parent.supportFragmentManager, TAG_FRAGMENT_EDIT_DIALOG)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val view = inflater.inflate(R.layout.item_shopping_list_item, parent, false)
        return ShoppingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        if (shoppingListItems.isNotEmpty()) {
            val currentItem = shoppingListItems[position]
            holder.apply {
                itemQuantity.text = currentItem.formattedQuantity
                itemUnit.text = currentItem.formattedUnit
                itemName.text = currentItem.name
                itemCompleted.isChecked = currentItem.completed
                formatCompletedView(this, currentItem.completed)
            }
        } else {
            Timber.d("no items")
            holder.apply {
                itemQuantity.text = ""
                itemUnit.text = ""
                itemName.setText(R.string.no_shopping_list_items)
                itemCompleted.isChecked = false
                formatCompletedView(this, false)
            }
        }
    }

    override fun getItemCount(): Int {
        return shoppingListItems.size
    }

    private fun formatCompletedView(holder: ShoppingListViewHolder, completed: Boolean) {
        val textAppearance =
            if (completed) R.style.shopping_list_item_completed else R.style.shopping_list_item
        val strikeThrough = if (completed)
            holder.itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        TextViewCompat.setTextAppearance(holder.itemName, textAppearance)
        holder.itemName.paintFlags = strikeThrough
    }

    private fun getItemAt(position: Int): UiShoppingListItem {
        return shoppingListItems[position]
    }
}
