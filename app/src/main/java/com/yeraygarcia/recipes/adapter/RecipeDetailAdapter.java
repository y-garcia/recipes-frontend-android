package com.yeraygarcia.recipes.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipe;
import com.yeraygarcia.recipes.database.entity.custom.UiRecipeIngredient;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class RecipeDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEWTYPE_DURATION_SOURCE = 1;
    private static final int VIEWTYPE_INGREDIENTS_HEADER = 2;
    private static final int VIEWTYPE_STEPS_HEADER = 3;
    private static final int VIEWTYPE_INGREDIENT = 4;
    private static final int VIEWTYPE_STEP = 5;

    private Context mContext;
    private RecipeDetailViewModel mViewModel;
    private LayoutInflater mInflater;

    private UiRecipe mUiRecipe;
    private List<UiRecipeIngredient> mIngredients;
    private List<RecipeStep> mSteps;

    private int mSelectedIngredient = RecyclerView.NO_POSITION;
    private int mSelectedStep = RecyclerView.NO_POSITION;

    // Constructors

    public RecipeDetailAdapter(Context context, RecipeDetailViewModel viewModel) {
        Debug.d(this, "RecipeDetailAdapter(context, viewModel)");

        mContext = context;
        mViewModel = viewModel;
        mInflater = LayoutInflater.from(context);
    }

    // Internal classes

    class IngredientsHeaderViewHolder extends RecyclerView.ViewHolder {

        ImageView itemDecrease;
        ImageView itemIncrease;
        TextView itemServings;

        private IngredientsHeaderViewHolder(View itemView) {
            super(itemView);
            itemDecrease = itemView.findViewById(R.id.imageview_decrease_servings);
            itemIncrease = itemView.findViewById(R.id.imageview_increase_servings);
            itemServings = itemView.findViewById(R.id.textview_servings);

            itemDecrease.setOnClickListener(v -> {
                if (mUiRecipe.getRecipe().getPortions() > 1) {
                    mUiRecipe.getRecipe().decreasePortions();
                    mViewModel.update(mUiRecipe.getRecipe());
                }
            });

            itemIncrease.setOnClickListener(v -> {
                mUiRecipe.getRecipe().increasePortions();
                mViewModel.update(mUiRecipe.getRecipe());
            });
        }

    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView itemTitle;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.textview_title);
        }

    }

    class DurationSourceViewHolder extends RecyclerView.ViewHolder {

        ImageView itemDurationIcon;
        ImageView itemSourceIcon;

        TextView itemDuration;
        TextView itemSource;

        private DurationSourceViewHolder(View itemView) {
            super(itemView);
            itemDurationIcon = itemView.findViewById(R.id.imageview_duration_icon);
            itemSourceIcon = itemView.findViewById(R.id.imageview_source_icon);
            itemDuration = itemView.findViewById(R.id.textview_duration);
            itemSource = itemView.findViewById(R.id.textview_source);
            itemSource.setOnClickListener(v -> {
                String urlString = (String) v.getTag();

                if (URLUtil.isValidUrl(urlString)) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                    mContext.startActivity(browserIntent);
                }
            });
        }

    }

    class IngredientsViewHolder extends RecyclerView.ViewHolder {

        TextView itemQuantity;
        TextView itemUnit;
        TextView itemIngredientName;

        private IngredientsViewHolder(View itemView) {
            super(itemView);
            itemQuantity = itemView.findViewById(R.id.textview_ingredient_quantity);
            itemUnit = itemView.findViewById(R.id.textview_ingredient_unit);
            itemIngredientName = itemView.findViewById(R.id.textview_ingredient_name);

            itemView.setOnClickListener(v -> {
                final int currentPosition = getAdapterPosition();

                if (currentPosition == RecyclerView.NO_POSITION) return;

                // updating old position
                notifyItemChanged(mSelectedIngredient);
                // deselect current item if it's already selected and the user clicks again on it
                mSelectedIngredient = (currentPosition == mSelectedIngredient) ? RecyclerView.NO_POSITION : currentPosition;
                // update new position
                notifyItemChanged(mSelectedIngredient
                );
            });
        }

    }

    class StepsViewHolder extends RecyclerView.ViewHolder {

        TextView itemStepNumber;
        TextView itemStepDescription;

        private StepsViewHolder(View itemView) {
            super(itemView);
            itemStepNumber = itemView.findViewById(R.id.textview_step_number);
            itemStepDescription = itemView.findViewById(R.id.textview_step_description);

            itemView.setOnClickListener(v -> {
                final int position = getAdapterPosition();

                // updating old position
                notifyItemChanged(mSelectedStep);
                // deselect current item if it's already selected and the user clicks again on it
                mSelectedStep = (position == mSelectedStep) ? RecyclerView.NO_POSITION : position;
                // update new position
                notifyItemChanged(mSelectedStep);
            });
        }

    }

    // Overrides

    @Override
    public int getItemViewType(int position) {
        //Debug.d(this, "getItemViewType(position=" + String.valueOf(position) + ")");

        // there is always at least one ingredient item to show the "no ingredients" when empty
        final int ingredientsCount = (mIngredients == null) ? 1 : Math.max(mIngredients.size(), 1);

        if (position == 0) {
            return VIEWTYPE_DURATION_SOURCE;
        } else if (position == 1) {
            return VIEWTYPE_INGREDIENTS_HEADER;
        } else if (position < ingredientsCount + 2) {
            return VIEWTYPE_INGREDIENT;
        } else if (position == ingredientsCount + 2) {
            return VIEWTYPE_STEPS_HEADER;
        } else {
            return VIEWTYPE_STEP;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Debug.d(this, "onCreateViewHolder(parent, viewType=" + String.valueOf(viewType) + ")");

        View view;
        switch (viewType) {
            case VIEWTYPE_INGREDIENTS_HEADER:
                view = mInflater.inflate(R.layout.item_header_ingredients, parent, false);
                return new IngredientsHeaderViewHolder(view);

            case VIEWTYPE_STEPS_HEADER:
                view = mInflater.inflate(R.layout.item_header_generic, parent, false);
                return new HeaderViewHolder(view);

            case VIEWTYPE_DURATION_SOURCE:
                view = mInflater.inflate(R.layout.item_duration_source, parent, false);
                return new DurationSourceViewHolder(view);

            case VIEWTYPE_INGREDIENT:
                view = mInflater.inflate(R.layout.item_ingredient, parent, false);
                return new IngredientsViewHolder(view);

            default:
                view = mInflater.inflate(R.layout.item_step, parent, false);
                return new StepsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Debug.d(this, "onBindViewHolder(holder, position=" + String.valueOf(position) + ")");

        switch (holder.getItemViewType()) {

            case VIEWTYPE_INGREDIENTS_HEADER:
                if (mUiRecipe != null) {
                    String portions = String.format(Locale.getDefault(), "%d", mUiRecipe.getRecipe().getPortions());
                    ((IngredientsHeaderViewHolder) holder).itemServings.setText(portions);

                    int decreaseVisibility = mUiRecipe.getRecipe().getPortions() > 1 ? View.VISIBLE : View.GONE;
                    ((IngredientsHeaderViewHolder) holder).itemDecrease.setVisibility(decreaseVisibility);
                }
                break;

            case VIEWTYPE_STEPS_HEADER:
                ((HeaderViewHolder) holder).itemTitle.setText(R.string.header_steps);
                break;

            case VIEWTYPE_DURATION_SOURCE:
                if (mUiRecipe != null) {
                    formatDurationSourceView((DurationSourceViewHolder) holder,
                            mUiRecipe.getRecipe().getFormattedDuration(mContext),
                            mUiRecipe.getRecipe().getUrl());
                }
                break;

            case VIEWTYPE_INGREDIENT:
                IngredientsViewHolder viewHolder = (IngredientsViewHolder) holder;
                holder.itemView.setSelected(mSelectedIngredient == position);
                if (mIngredients != null && mIngredients.size() > 0) {
                    final UiRecipeIngredient currentIngredient = mIngredients.get(position - 2);
                    viewHolder.itemQuantity.setText(currentIngredient.getFormattedQuantity());
                    viewHolder.itemUnit.setText(currentIngredient.getFormattedUnit());
                    viewHolder.itemIngredientName.setText(currentIngredient.getName());
                } else {
                    Debug.d(this, "no ingredients");
                    // Covers the case of data not being ready yet.
                    viewHolder.itemIngredientName.setText(R.string.no_ingredients);
                }
                break;

            default:
                holder.itemView.setSelected(mSelectedStep == position);
                if (mSteps != null && mSteps.size() > 0) {
                    int ingredientsSize = mIngredients == null ? 0 : mIngredients.size();
                    final RecipeStep currentStep = mSteps.get(position - ingredientsSize - 3);
                    ((StepsViewHolder) holder).itemStepNumber.setText(formatSortOrder(currentStep.getSortOrder()));
                    ((StepsViewHolder) holder).itemStepDescription.setText(currentStep.getDescription());
                } else {
                    // Covers the case of data not being ready yet.
                    ((StepsViewHolder) holder).itemStepDescription.setText(R.string.no_steps);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (mUiRecipe != null) {
            size += 3;
        }
        if (mIngredients != null) {
            // there is always at least one ingredient item to show the "no ingredients" when empty
            size += Math.max(mIngredients.size(), 1);
        }
        if (mSteps != null) {
            // there is always at least one step item to show the "no steps" when empty
            size += Math.max(mSteps.size(), 1);
        }

        //Debug.d(this, "getItemCount() -> " + size);

        return size;
    }

    // Getters and Setters

    public int getSelectedIngredient() {
        return mSelectedIngredient;
    }

    public int getSelectedStep() {
        return mSelectedStep;
    }

    public void setSelectedIngredient(int position) {
        this.mSelectedIngredient = position;
    }

    public void setSelectedStep(int position) {
        this.mSelectedStep = position;
    }

    public void setRecipe(UiRecipe recipe) {
        Debug.d(this, "setRecipe(recipe)");
        mUiRecipe = recipe;
        mSteps = recipe.getSteps();
        notifyDataSetChanged();
    }

    public void setIngredients(List<UiRecipeIngredient> ingredients) {
        Debug.d(this, "setIngredients(ingredients(" + ingredients.size() + "))");
        mIngredients = ingredients;
        notifyDataSetChanged();
    }

    // Methods

    private String formatSortOrder(int sortOrder) {
        return String.format(Locale.getDefault(), "%d", sortOrder);
    }

    private void formatDurationSourceView(DurationSourceViewHolder viewHolder, String duration, String url) {

        viewHolder.itemDuration.setText(duration);

        if (url == null) {
            viewHolder.itemSourceIcon.setVisibility(View.INVISIBLE);
            viewHolder.itemSource.setVisibility(View.INVISIBLE);
            viewHolder.itemSource.setTag(null);
        } else {
            viewHolder.itemSourceIcon.setVisibility(View.VISIBLE);
            viewHolder.itemSource.setVisibility(View.VISIBLE);
            viewHolder.itemSource.setTag(url);

            if (URLUtil.isValidUrl(url)) {
                String formattedUrl = formatUrl(url);
                TextViewCompat.setTextAppearance(viewHolder.itemSource, R.style.source_link);
                SpannableString content = new SpannableString(formattedUrl);
                content.setSpan(new UnderlineSpan(), 0, formattedUrl.length(), 0);
                viewHolder.itemSource.setText(content);
            } else {
                TextViewCompat.setTextAppearance(viewHolder.itemSource, R.style.source_text);
                viewHolder.itemSource.setText(url);
            }
        }
    }

    private String formatUrl(String urlString) {

        if (urlString == null) {
            return mContext.getString(R.string.no_source);
        }

        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            // urlString is not a proper url, just return it as is
            return urlString;
        }
    }
}
