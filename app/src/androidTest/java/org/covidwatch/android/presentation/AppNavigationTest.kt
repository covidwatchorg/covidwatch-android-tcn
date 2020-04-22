package org.covidwatch.android.presentation

import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import org.covidwatch.android.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class AppNavigationTest {

    // Setup
    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testAppNavigation() {

        // Verify splashFragment is in view
        onView(withId(R.id.splashFragmentInternal))
            .check(matches(isDisplayed()))

        // Nav setupBluetoothFragment
        onView(withId(R.id.start_button)).perform(click())

        // Verify setupBluetoothFragment is in view
        onView(withId(R.id.setupBluetoothFragmentInternal))
            .check(matches(isDisplayed()))

        // Nav homeFragment
        onView(withId(R.id.grant_location_access_button)).perform(click())

        // Verify homeFragment is in view
        onView(withId(R.id.homeFragmentInternal))
            .check(matches(isDisplayed()))

        // Nav testQuestionsFragment
        onView(withId(R.id.tested_button)).perform(click())

        // Verify testQuestionsFragment is in view
        onView(withId(R.id.testQuestionsFragmentInternal))
            .check(matches(isDisplayed()))




    }


}