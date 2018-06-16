package com.yeraygarcia.recipes;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeraygarcia.recipes.adapter.TodayAdapter;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.ShortDividerItemDecoration;
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel;

public class TodayListFragment extends Fragment {

    private FragmentActivity mParentActivity;

    public TodayListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentActivity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Debug.d(this, "onCreateView(inflater, container, savedInstanceState)");
        View rootView = inflater.inflate(R.layout.fragment_today_list, container, false);

        TodayAdapter mTodayAdapter = new TodayAdapter(mParentActivity);
        RecyclerView recipesRecyclerView = rootView.findViewById(R.id.recyclerview_today_list);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity));
        recipesRecyclerView.setAdapter(mTodayAdapter);
        recipesRecyclerView.addItemDecoration(new ShortDividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL, 16));

        RecipeViewModel mViewModel = ViewModelProviders.of(mParentActivity).get(RecipeViewModel.class);
        mViewModel.getRecipesInShoppingList().observe(this, mTodayAdapter::setRecipes);

        return rootView;
    }
}
