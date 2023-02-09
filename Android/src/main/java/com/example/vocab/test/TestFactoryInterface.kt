package com.example.vocab.test

import androidx.viewbinding.ViewBinding
import com.example.vocab.basic.Word
import com.example.vocab.thirdParty.Translator
import kotlin.reflect.KClass

/**
 * Interface for test factory, which holds all available tests
 *
 * @property classes list of available test classes
 * @property reversableClasses list of test that can also test translation in reverse order
 */
interface TestFactoryInterface {
    val classes: List<KClass<out GenericTest<out ViewBinding>>>

    val reversableClasses: List<KClass<out GenericTest<out ViewBinding>>>
    val supportedLanguages: Map<KClass<out GenericTest<out ViewBinding>>, List<String>?>

    /**
     * To given class returns initialized test
     * [keyToListen] defines a keyword for sending event when control test button is pressed
     */
    fun getObject(
        Class: KClass<out GenericTest<out ViewBinding>>,
        keyToListen: String,
        onResult: (result: GenericTest.Result)->Unit
    ): GenericTest<*>
}