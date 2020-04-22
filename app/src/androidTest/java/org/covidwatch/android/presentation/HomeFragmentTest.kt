package org.covidwatch.android.presentation

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.covidwatch.android.createRule
import org.covidwatch.android.di.appModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class HomeFragmentTest {

    private val homeFragment = HomeFragment()

    @get:Rule
    val fragmentRule = createRule(homeFragment, appModule)

    @Test
    fun foo() {
        assertEquals(1 + 1, 2)
    }
}