package com.example.pintube.ui.Search

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.pintube.R
import com.example.pintube.data.local.dao.SearchDAO
import com.example.pintube.data.local.database.YoutubeDatabase
import com.example.pintube.data.remote.api.retrofit.YouTubeApi
import com.example.pintube.data.repository.ApiRepositoryImpl
import com.example.pintube.data.repository.LocalSearchRepositoryImpl
import com.example.pintube.databinding.ActivitySearchBinding
import com.example.pintube.domain.entitiy.SearchEntity
import com.example.pintube.domain.repository.ApiRepository
import com.example.pintube.domain.repository.LocalSearchRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private val apiRepository: ApiRepository = ApiRepositoryImpl(YouTubeApi.youtubeNetwork)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showXButton()

        val recyclerView = binding.rvSearchList
        recyclerView.layoutManager = LinearLayoutManager(this)

        val searchHistoryList = SharedPrefManager.getSearchHistory(this).toMutableList()
        val adapter = SearchListAdapter(searchHistoryList)

        recyclerView.adapter = adapter


        binding.ivSearchFragmentSearch.setOnClickListener {
            val query = binding.etSearchFragmentBar.text.toString()
            if (query.isNotBlank()) {
                SharedPrefManager.saveSearchHistory(this, query)
                val searchHistorySet = SharedPrefManager.getSearchHistory(this)
                adapter.notifyDataSetChanged()
                Log.d("SearchActivity", "Search history: $searchHistorySet")
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchVideo(query)
        }
        binding.ivSearchFragmentBackArrow.setOnClickListener {
            finish()
        }

        binding.removeAll.setOnClickListener {
            if(searchHistoryList.isEmpty()) {
                Toast.makeText(this, "검색기록이 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setIcon(R.drawable.pintube_logo)
                .setTitle("검색기록 전체삭제")
                .setMessage("모든 검색기록을 삭제 하시겠습니까? \n 삭제 시 다시 되돌릴 수 없습니다.")
                .setPositiveButton("삭제"){ dialog, _ ->
                    searchHistoryList.clear()
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.etSearchFragmentBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                showXButton()
                noShowXButton()
            }
        })

        binding.removeQuery.setOnClickListener {
            binding.etSearchFragmentBar.setText("")
        }



        adapter.itemClick = object : SearchListAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {

            }
            override fun onRemoveClick(position: Int) {
                val itemRemove = searchHistoryList[position]
                searchHistoryList.remove(itemRemove)

                adapter.notifyDataSetChanged()

            }
        }

    }

    private fun showXButton() {
        val text = binding.etSearchFragmentBar.text.toString()
        if(text.length >= 1) {
            binding.removeQuery.visibility = View.VISIBLE
        }
    }
    private fun noShowXButton() {
        val text = binding.etSearchFragmentBar.text.toString()
        if(text.length == 0) {
            binding.removeQuery.visibility = View.GONE
        }
    }

    private fun searchVideo(query : String) {
        lifecycleScope.launch {
            val searchResults = apiRepository.searchResult(query)
            val searchResultFragment = SearchResultFragment.newInstance(searchResults)
            setFragment(searchResultFragment)
        }
    }

    private fun setFragment(frag: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.search_frameLayout, frag)
            setReorderingAllowed(true)
            addToBackStack("")
        }
    }
}