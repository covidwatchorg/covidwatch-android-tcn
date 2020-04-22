package org.covidwatch.android

import androidx.fragment.app.Fragment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.covidwatch.android.presentation.MainActivity
import org.koin.core.module.Module

abstract class FragmentTestRule<F : Fragment> :
    ActivityTestRule<MainActivity>(MainActivity::class.java, true) {

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as TestApplication
        application.injectModule(getModule())
    }

    protected abstract fun createFragment(): F

    protected abstract fun getModule(): Module
}

fun <F : Fragment> createRule(fragment: F, module: Module): FragmentTestRule<F> =
    object : FragmentTestRule<F>() {
        override fun createFragment(): F = fragment
        override fun getModule(): Module = module
    }
