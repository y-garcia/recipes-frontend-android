package com.yeraygarcia.recipes.adapter;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.RecipeListActivity;
import com.yeraygarcia.recipes.database.entity.Tag;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<Tag> mTags;

    private LayoutInflater mInflater;
    private RecipeViewModel mViewModel;

    private List<Long> mSelectedTagIds = new ArrayList<>();

    // Constructors

    public TagAdapter(RecipeListActivity parent) {
        mInflater = LayoutInflater.from(parent);
        mViewModel = ViewModelProviders.of(parent).get(RecipeViewModel.class);
    }

    // Internal classes

    class TagViewHolder extends RecyclerView.ViewHolder {

        TextView itemTag;

        private TagViewHolder(View itemView) {
            super(itemView);
            itemTag = itemView.findViewById(R.id.textview_tag);
            itemTag.setOnClickListener(v -> {
                final Tag tag = mTags.get(getAdapterPosition());

                if(v.isSelected()){
                    mViewModel.removeTagFromFilter(tag);
                    v.setSelected(false);
                } else {
                    mViewModel.addTagToFilter(tag);
                    v.setSelected(true);
                }
            });
        }
    }

    // Overrides

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_tag_chip, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        if (mTags != null) {
            final Tag currentTag = mTags.get(position);
            holder.itemTag.setText(currentTag.getName());
            holder.itemTag.setTag(currentTag.getId());
            holder.itemTag.setSelected(mSelectedTagIds.contains(currentTag.getId()));
        } else {
            // Covers the case of data not being ready yet.
            holder.itemTag.setText(R.string.no_tags);
            holder.itemTag.setTag(null);
            holder.itemTag.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        if (mTags != null) {
            return mTags.size();
        }
        return 0;
    }

    public void setTags(List<Tag> tags) {
        mTags = tags;
        notifyDataSetChanged();
    }

    public void setTagFilter(List<Long> tagIds) {
        Debug.d(this, "setTagFilter(tagIds("+tagIds.size()+"))");
        mSelectedTagIds = tagIds;
        notifyDataSetChanged();
    }
}
