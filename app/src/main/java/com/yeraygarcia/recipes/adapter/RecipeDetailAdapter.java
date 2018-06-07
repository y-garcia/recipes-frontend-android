package com.yeraygarcia.recipes.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.database.entity.RecipeStep;
import com.yeraygarcia.recipes.database.entity.custom.CustomRecipeIngredient;
import com.yeraygarcia.recipes.database.entity.custom.RecipeDetail;
import com.yeraygarcia.recipes.util.Debug;

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

    private final Context mContext;

    private RecipeDetail mRecipe;
    private List<CustomRecipeIngredient> mIngredients;
    private List<RecipeStep> mSteps;

    // Constructors

    public RecipeDetailAdapter(Context context) {
        Debug.d(this, "RecipeDetailAdapter(context)");

        mContext = context;
    }

    // Internal classes

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView itemTitle;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.textview_title);
        }

    }

    class DurationSourceViewHolder extends RecyclerView.ViewHolder {

        TextView itemDuration;
        TextView itemSource;

        private DurationSourceViewHolder(View itemView) {
            super(itemView);
            itemDuration = itemView.findViewById(R.id.textview_duration);
            itemSource = itemView.findViewById(R.id.textview_source);
            itemSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String urlString = (String) v.getTag();

                    try {
                        URL url = new URL(urlString);
                    } catch (MalformedURLException e) {
                        // urlString is not a proper url, do nothing
                    }

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
            itemQuantity = itemView.findViewById(R.id.textview_quantity);
            itemUnit = itemView.findViewById(R.id.textview_unit);
            itemIngredientName = itemView.findViewById(R.id.textview_ingredient_name);
        }

    }

    class StepsViewHolder extends RecyclerView.ViewHolder {

        TextView itemStepNumber;
        TextView itemStepDescription;

        private StepsViewHolder(View itemView) {
            super(itemView);
            itemStepNumber = itemView.findViewById(R.id.textview_step_number);
            itemStepDescription = itemView.findViewById(R.id.textview_step_description);
        }

    }

    // Overrides

    @Override
    public int getItemViewType(int position) {
        Debug.d(this, "getItemViewType(position=" + String.valueOf(position) + ")");

        if (position == 0) {
            return VIEWTYPE_DURATION_SOURCE;
        } else if (position == 1) {
            return VIEWTYPE_INGREDIENTS_HEADER;
        } else if (position < mIngredients.size() + 2) {
            return VIEWTYPE_INGREDIENT;
        } else if (position == mIngredients.size() + 2) {
            return VIEWTYPE_STEPS_HEADER;
        } else {
            return VIEWTYPE_STEP;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Debug.d(this, "onCreateViewHolder(parent, viewType=" + String.valueOf(viewType) + ")");

        int layout;
        View view;

        switch (viewType) {
            case VIEWTYPE_INGREDIENTS_HEADER:
            case VIEWTYPE_STEPS_HEADER:
                layout = R.layout.list_item_header;
                view = LayoutInflater.from(mContext).inflate(layout, parent, false);
                return new HeaderViewHolder(view);
            case VIEWTYPE_DURATION_SOURCE:
                layout = R.layout.list_item_duration_source;
                view = LayoutInflater.from(mContext).inflate(layout, parent, false);
                return new DurationSourceViewHolder(view);
            case VIEWTYPE_INGREDIENT:
                layout = R.layout.list_item_ingredient;
                view = LayoutInflater.from(mContext).inflate(layout, parent, false);
                return new IngredientsViewHolder(view);
            default:
                layout = R.layout.list_item_step;
                view = LayoutInflater.from(mContext).inflate(layout, parent, false);
                return new StepsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Debug.d(this, "onBindViewHolder(holder, position=" + String.valueOf(position) + ")");

        switch (holder.getItemViewType()) {

            case VIEWTYPE_INGREDIENTS_HEADER:
                ((HeaderViewHolder) holder).itemTitle.setText(R.string.header_ingredients);
                break;
            case VIEWTYPE_STEPS_HEADER:
                ((HeaderViewHolder) holder).itemTitle.setText(R.string.header_steps);
                break;
            case VIEWTYPE_DURATION_SOURCE:
                if (mRecipe != null) {
                    ((DurationSourceViewHolder) holder).itemDuration.setText(formatDuration(mRecipe.getRecipe().getDurationInMinutes()));
                    ((DurationSourceViewHolder) holder).itemSource.setText(formatUrl(mRecipe.getRecipe().getUrl()));
                    ((DurationSourceViewHolder) holder).itemSource.setTag(mRecipe.getRecipe().getUrl());
                }
                break;
            case VIEWTYPE_INGREDIENT:
                if (mIngredients != null && mIngredients.size() > 0) {
                    final CustomRecipeIngredient currentIngredient = mIngredients.get(position - 2);
                    ((IngredientsViewHolder) holder).itemQuantity.setText(formatQuantity(currentIngredient.getQuantity()));
                    ((IngredientsViewHolder) holder).itemUnit.setText(formatUnit(currentIngredient.getQuantity(), currentIngredient.getUnitName(), currentIngredient.getUnitNamePlural()));
                    ((IngredientsViewHolder) holder).itemIngredientName.setText(currentIngredient.getIngredientName());
                } else {
                    // Covers the case of data not being ready yet.
                    ((IngredientsViewHolder) holder).itemIngredientName.setText(R.string.no_ingredients);
                }
                break;
            default:
                if (mSteps != null && mSteps.size() > 0) {
                    final RecipeStep currentStep = mSteps.get(position - mIngredients.size() - 3);
                    ((StepsViewHolder) holder).itemStepNumber.setText(String.format(Locale.getDefault(), "%d", currentStep.getSortOrder()));
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
        if (mRecipe != null) {
            size += 3;
        }
        if (mIngredients != null) {
            size += mIngredients.size();
        }
        if (mSteps != null) {
            size += mSteps.size();
        }

        Debug.d(this, "getItemCount() -> " + size);

        return size;
    }

    // Getters and Setters

    public void setRecipe(RecipeDetail recipe) {
        Debug.d(this, "setRecipe(recipe)");
        mRecipe = recipe;
        notifyDataSetChanged();
    }

    public void setIngredients(List<CustomRecipeIngredient> ingredients) {
        Debug.d(this, "setIngredients(ingredients)");
        mIngredients = ingredients;
        notifyDataSetChanged();
    }

    public void setSteps(List<RecipeStep> steps) {
        Debug.d(this, "setSteps(steps)");
        mSteps = steps;
        notifyDataSetChanged();
    }

    // Methods

    private String formatDuration(long duration) {
        return mContext.getString(R.string.duration_format, String.format(Locale.getDefault(), "%d", duration));
    }

    private String formatUrl(String urlString) {
        String domain;

        try {
            URL url = new URL(urlString);
            domain = url.getHost();
        } catch (MalformedURLException e) {
            // urlString is not a proper url, just return it as is
            return urlString;
        }

        return domain;
    }

    private String formatQuantity(Double quantity) {
        if (quantity == null) {
            return "";
        } else if (quantity == Math.rint(quantity)) {
            return String.format(Locale.getDefault(), "%d", quantity.intValue());
        } else {
            return String.format(Locale.getDefault(), "%1$,.2f", quantity);
        }
    }

    private String formatUnit(Double quantity, String unitName, String unitNamePlural) {
        if (quantity != null && quantity == 1) {
            return unitName;
        } else {
            return unitNamePlural;
        }
    }
}
