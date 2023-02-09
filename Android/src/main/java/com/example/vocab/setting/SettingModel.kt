package com.example.vocab.setting

import androidx.lifecycle.ViewModel
import com.example.vocab.Settings
import com.example.vocab.database.Database
import com.example.vocab.thirdParty.Translator

/**
 * Data model for settings tab
 *
 * @property nightMode set application colors to night mode
 * @property maxNumRss maximum number of articles loaded for each page
 * @property language language of translator
 * @property maxNumOfWords maximum number of words for each test
 * @property reverseWords decides if tests will test translation from foreign language or to it
 */
class SettingModel(private val db: Database): ViewModel(){
    var nightMode: Boolean = false
        get(){
            return (db.getSetting(Settings.nightMode) as? Boolean) ?: false
        }
        set(new){
            field = new
            db.saveSetting(
                mapOf(
                    Settings.nightMode to new
                )
            )
        }

    var maxNumRss: Int = 15
        get(){
            return (db.getSetting(Settings.rssNum) as? Int) ?: 15
        }
        set(new){
            var newTemp = new
            if(new > 50) newTemp = 50
            else if(new < 5) newTemp = 5

            field = newTemp
            db.saveSetting(
                mapOf(
                    Settings.rssNum to newTemp
                )
            )
        }

    var language: Translator.languages = Translator.languages.en
        get(){
            return Translator.fromID((db.getSetting(Settings.language) as? Int) ?: Translator.languages.en.language) ?: Translator.languages.en
        }
        set(new){
            field = new
            db.saveSetting(
                mapOf(
                    Settings.language to new.language
                )
            )
        }

    var AppLanguage: Translator.languages = Translator.languages.en
        get(){
            return Translator.fromID((db.getSetting(Settings.AppLanguage) as? Int) ?: Translator.languages.en.language) ?: Translator.languages.en
        }
        set(new){
            field = new
            db.saveSetting(
                mapOf(
                    Settings.AppLanguage to new.language
                )
            )
        }

    var maxNumOfWords: Int = 20
        get(){
            return (db.getSetting(Settings.maxNumWords) as? Int) ?: 20
        }
        set(new){
            var newTemp = new
            if(new > 50) newTemp = 50
            else if(new < 5) newTemp = 5

            field = newTemp
            db.saveSetting(
                mapOf(
                    Settings.maxNumWords to newTemp
                )
            )
        }

    var reverseWords: Boolean = false
        get(){
            return (db.getSetting(Settings.reverseTests) as? Boolean) ?: false
        }
        set(new){
            field = new
            db.saveSetting(
                mapOf(
                    Settings.reverseTests to new
                )
            )
        }
}