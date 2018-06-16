package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeraygarcia.recipes.adapter.ShoppingListAdapter;
import com.yeraygarcia.recipes.database.entity.custom.UiShoppingListItem;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.viewmodel.ShoppingListViewModel;

public class ShoppingListFragment extends Fragment {

    private FragmentActivity mParentActivity;

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

        ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(mParentActivity);
        RecyclerView shoppingListRecyclerView = rootView.findViewById(R.id.recyclerview_shopping_list);
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        shoppingListRecyclerView.setAdapter(shoppingListAdapter);

        ShoppingListViewModel viewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
        viewModel.getShoppingListItems().observe(this, shoppingListAdapter::setShoppingListItems);

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
                viewModel.removeFromShoppingList(shoppingListItem);
            }
        }).attachToRecyclerView(shoppingListRecyclerView);

        return rootView;
    }
}
