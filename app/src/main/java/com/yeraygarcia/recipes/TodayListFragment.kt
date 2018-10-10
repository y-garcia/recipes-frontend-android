package com.yeraygarcia.recipes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yeraygarcia.recipes.adapter.TodayAdapter
import com.yeraygarcia.recipes.util.ShortDivider
import com.yeraygarcia.recipes.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.fragment_today_list.view.*
import timber.log.Timber

class TodayListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Timber.d("onCreateView(inflater, container, savedInstanceState)")
        val rootView = inflater.inflate(R.layout.fragment_today_list, container, false)

        activity?.let { activity ->
            val todayAdapter = TodayAdapter(activity)
            rootView.recyclerViewTodayList.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = todayAdapter
                addItemDecoration(ShortDivider(activity, DividerItemDecoration.VERTICAL, 16))
            }

            val viewModel = ViewModelProviders.of(activity).get(RecipeViewModel::class.java)
            viewModel.recipesInShoppingList.observe(this, Observer {
                todayAdapter.recipes = it ?: emptyList()
            })
        }

        return rootView
    }
}
