package com.yeraygarcia.recipes.adapter

import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.database.entity.Tag
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.item_tag_chip.view.*
import timber.log.Timber
import java.util.*

class TagAdapter(parent: FragmentActivity) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private val inflater = LayoutInflater.from(parent)
    private val viewModel = ViewModelProviders.of(parent).get(RecipeViewModel::class.java)

    var tags: List<Tag> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var selectedTagIds: List<UUID> = ArrayList()
        set(value) {
            field = value
            Timber.d("setSelectedTagIds(tagIds(${value.size}))")
            notifyDataSetChanged()
        }

    inner class TagViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val itemTag: TextView = itemView.textview_tag

        init {
            itemTag.setOnClickListener { v ->
                val tag = tags[adapterPosition]

                if (v.isSelected) {
                    viewModel.removeTagFromFilter(tag)
                    v.isSelected = false
                } else {
                    viewModel.setTagFilter(tag)
                    v.isSelected = true
                }
            }
            itemTag.setOnLongClickListener { v ->
                val tag = tags[adapterPosition]

                if (v.isSelected) {
                    viewModel.removeTagFromFilter(tag)
                    v.isSelected = false
                } else {
                    viewModel.addTagToFilter(tag)
                    v.isSelected = true
                }

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = inflater.inflate(R.layout.item_tag_chip, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val currentTag = tags[position]
        holder.itemTag.text = currentTag.name
        holder.itemTag.tag = currentTag.id
        holder.itemTag.isSelected = selectedTagIds.contains(currentTag.id)
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}
