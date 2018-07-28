package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.yeraygarcia.recipes.adapter.ShoppingListAdapter;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.ShortDividerItemDecoration;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

public class ShoppingListFragment extends Fragment {

    private FragmentActivity mParentActivity;
    private RecipeViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)");
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        setHasOptionsMenu(true);

        mViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);

        setupAddItemViews(rootView);

        ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(mParentActivity, mViewModel);
        RecyclerView shoppingListRecyclerView = rootView.findViewById(R.id.recyclerview_shopping_list);
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        shoppingListRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16));
        shoppingListRecyclerView.setAdapter(shoppingListAdapter);

        mViewModel.getShoppingListItems().observe(this, shoppingListAdapter::setShoppingListItems);
        mViewModel.getUnits().observe(this, units -> {/* TODO This is a hack :-( */});

        // recognize when a user swipes an item
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                UiShoppingListItem shoppingListItem = shoppingListAdapter.getShoppingListItems().get(position);
                mViewModel.removeFromShoppingList(shoppingListItem);
            }
        }).attachToRecyclerView(shoppingListRecyclerView);


        return rootView;
    }

    private void setupAddItemViews(View rootView) {
        EditText editTextNewItem = rootView.findViewById(R.id.edittext_new_item);
        ImageButton imageButtonAddItem = rootView.findViewById(R.id.imagebutton_add_item);

        imageButtonAddItem.setOnClickListener(view -> {
            String newItem = editTextNewItem.getText().toString();
            mViewModel.addItemToShoppingList(newItem);
            editTextNewItem.setText("");
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Debug.d(this, "onPause()");
        mViewModel.persistDraft();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Debug.d(this, "onCreateOptionsMenu(menu, inflated)");
        inflater.inflate(R.menu.options_menu_shopping_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_completed_button:
                mViewModel.clearCompletedFromShoppingList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
