package com.example.vocab.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.vocab.Settings
import com.example.vocab.basic.Student
import com.example.vocab.basic.StudentParcelable
import com.example.vocab.database.Database

class TestLaunchUtils {
    companion object {
        /**
         * Helper function to transfer required data to test activity
         */
        fun getIntent(student: Student, context: Context, activity: Activity, withoutDisableList: Boolean = false):Intent {
            val maxNumWords = (Database(context, activity).getSetting(Settings.maxNumWords) as? Int) ?: Settings.maxNumWordsDef
            val reverseWords = (Database(context, activity).getSetting(Settings.reverseTests) as? Boolean) ?: Settings.reverseTestsDef
            val disabledList = (Database(context, activity).getSetting(Settings.disabledWordLists) as? String) ?: ""

            val intent = Intent(activity, TestRunActivity::class.java)
            intent.putExtra("student", StudentParcelable(student))
            intent.putExtra(Settings.maxNumWords, maxNumWords)
            intent.putExtra(Settings.reverseTests, reverseWords)
            if(withoutDisableList){
                intent.putExtra(Settings.disabledWordLists, "")
            }
            else{
                intent.putExtra(Settings.disabledWordLists, disabledList)
            }

            return intent
        }
    }
}