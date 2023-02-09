package com.example.vocab.homework

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.vocab.DateUtils
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.HomeWork
import com.example.vocab.databinding.FragmentHomeworkCellBinding
import com.example.vocab.gui.BaseItem
import java.util.*

/**
 * Gui component for displaying status of homework
 *
 * @property itemID used as id of homework
 * @property layout to set round corners
 * @property touchListener callback class with method that is called when user click on homework
 * @property resources to access strings namespace
 */
data class BaseHomework(val homework: HomeWork): BaseItem<FragmentHomeworkCellBinding>{
    class TouchAdapter(private val func: ()->Unit){
        fun onClick(){
            func()
        }
    }

    override val layoutId = R.layout.fragment_homework_cell
    override val itemID = homework.id

    var layout: Drawable? = null
    var touchListener: TouchAdapter? = null
    var resources: Resources? = null

    override fun initViewBinding(view: View): FragmentHomeworkCellBinding {
        return FragmentHomeworkCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentHomeworkCellBinding) {
        resources?.let {
            touchListener?.let {
                binding.clickListener = touchListener
            }

            binding.homeworkName.text = homework.name
            binding.homeworkStartDate.text = String.format(
                it.getString(R.string.homework_from_date),
                DateUtils.fromDate2StringShort(homework.date)
            )
            binding.homeworkEndDate.text = String.format(
                it.getString(R.string.homework_to_date),
                DateUtils.fromDate2StringShort(homework.dateCompleted)
            )
            binding.completed.text = String.format(
                it.getString(R.string.homework_completed),
                if(homework.completed) it.getString(R.string.homework_yes) else it.getString(R.string.homework_no)
            )
            binding.completed.setTextColor(if(!homework.completed) ResourcesCompat.getColor(it, R.color.red, null)
                                            else ResourcesCompat.getColor(it, R.color.green, null))

            if(Tools.getScreenWidth(it) < 1000){
                binding.homeworkStartDate.visibility = View.GONE
            }

            val difference = (homework.dateCompleted.time - Date().time)
            binding.homeworkDaysUntil.text = String.format(
                it.getString(R.string.homework_until_date),
                ((if(difference>=0) difference else 0) / 1000 / 60 / 60 / 24).toString()
            )

            layout?.let {
                binding.root.background = layout
            }
        }
    }
}