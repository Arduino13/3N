package com.example.vocab.webPages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.vocab.basic.Student
import com.example.vocab.basic.Syncable
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem

/**
 * Model for storing [BaseArticle] objects
 *
 * Old implementation that need to refactored to use [BaseViewModel]
 *
 * @property data read only list of [BaseArticle] objects
 * @property numberOfPages number of articles to load from each webpage
 */

class WebListModel(private val model: globalModel, private val maxRSS: Int): ViewModel() {
    private val _data = MutableLiveData<List<BaseItem<*>>?>()
    private val _numberOfPages = MutableLiveData<Int>(0)
    private val obsFun = Observer<Syncable>{loadData()}

    init{
        loadData()
        model.data.observeForever(obsFun)
    }

    override fun onCleared() {
        model.data.removeObserver(obsFun)
    }

    val data: LiveData<List<BaseItem<*>>?> = _data
    val numberOfPages:LiveData<Int> = _numberOfPages

    private fun setData(data: List<BaseItem<*>>?, numberOfPages: Int){
        _data.postValue(data)
        _numberOfPages.postValue(numberOfPages)
    }

    private fun loadData(){
        setData(null,0) //triggers progress bar
        val listOfArticles = mutableListOf<BaseItem<*>>()
        var numberOfLoadedPages = 0

        val listOfWebs = (model.data.value as Student).listWebs
        for(w in listOfWebs){
            RSS(maxRSS).loadFeed(w.address) { success, articles ->
                if (success) {
                    listOfArticles += BaseHeader(w.name)
                    for (a in articles!!) {
                        listOfArticles += BaseArticle(a, w.name)
                    }

                    numberOfLoadedPages += 1

                    if (numberOfLoadedPages == listOfWebs.size) {
                        setData(listOfArticles, numberOfLoadedPages)
                    }
                }
            }
        }

        if(listOfWebs.size == 0) setData(listOf(), 0)
    }
}