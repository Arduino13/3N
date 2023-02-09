package com.example.vocab.test

import androidx.viewbinding.ViewBinding
import com.example.vocab.thirdParty.Translator
import kotlin.reflect.KClass

class TestFragmentFactory : TestFactoryInterface {
    override val classes: List<KClass<out GenericTest<out ViewBinding>>> = listOf(
        TestSpeechFragment::class,
        TestIsCorrectFragment::class,
        TestChooseFourFragment::class,
        TestSentenceFragment::class,
        TestDialogFragment::class
    )

    override val reversableClasses: List<KClass<out GenericTest<out ViewBinding>>> = listOf(
        TestDialogFragment::class,
        TestIsCorrectFragment::class,
        TestChooseFourFragment::class
        //TestSpeechFragment::class
    )

    override val supportedLanguages: Map<KClass<out GenericTest<out ViewBinding>>, List<String>?> = mapOf(
        TestSpeechFragment::class to listOf("en"),
        TestIsCorrectFragment::class to null,
        TestChooseFourFragment::class to null,
        TestSentenceFragment::class to null,
        TestDialogFragment::class to null
    )

    override fun getObject(
        Class: KClass<out GenericTest<out ViewBinding>>,
        keyToListen: String,
        onResult: (result: GenericTest.Result) -> Unit
    ): GenericTest<*> {
        when (Class) {
            TestIsCorrectFragment::class -> return TestIsCorrectFragment().setArguments(keyToListen, onResult)
            TestChooseFourFragment::class -> return TestChooseFourFragment().setArguments(keyToListen, onResult)
            TestSentenceFragment::class -> return TestSentenceFragment().setArguments(keyToListen, onResult)
            TestDialogFragment::class -> return TestDialogFragment().setArguments(keyToListen, onResult)
            TestSpeechFragment::class -> return TestSpeechFragment().setArguments(keyToListen, onResult)
        }

        throw Exception("unexpected type of class")
    }
}