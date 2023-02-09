package com.example.vocab.gui

import android.content.Context
import android.content.res.Resources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vocab.Tools

/**
 * Helper static class for changing layout of list accordingly to device orientation
 */
class ResponsibleList {
    companion object{
        /**
         * Changes layout of [list] from default to grid on wide screen
         */
        fun makeResponsible(list: RecyclerView, resources: Resources, context: Context){
            val screenSize = Tools.getScreenSizeOfDevice(resources)
            val columnsNum = Tools.getScreenWidth(resources)/270

            var layoutManagerList = if(screenSize < 6) LinearLayoutManager(context) else GridLayoutManager(context, columnsNum)
            if(screenSize > 6){
                val GridManager = layoutManagerList as GridLayoutManager
                GridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) {
                            columnsNum
                        } else 1
                    }
                }

                layoutManagerList = GridManager
            }
            else{
                list.setPadding(Tools.dp2Pixels(15, resources),0, Tools.dp2Pixels(15, resources),0)
            }

            val itemSpacing = if(screenSize > 6) Tools.dp2Pixels(8, resources) else Tools.dp2Pixels(15, resources)
            list.addItemDecoration(LinearSpacingDecoration(itemSpacing,itemSpacing))

            list.layoutManager = layoutManagerList
        }
    }
}