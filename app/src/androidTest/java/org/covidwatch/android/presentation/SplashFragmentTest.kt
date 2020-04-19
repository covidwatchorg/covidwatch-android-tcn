package org.covidwatch.android.presentation

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.covidwatch.android.R

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class SplashFragmentTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

//    @Rule
//    @JvmField
//    var mGrantPermissionRule =
//        GrantPermissionRule.grant(
//            "android.permission.ACCESS_FINE_LOCATION"
//        )

    @Test
    fun test_isSplashPageVisible() {



        // The "fragmentArgs" and "factory" arguments are optional.
//        val fragmentArgs = Bundle().apply {
//            putInt("selectedListItem", 0)
//        }
//        val factory = MyFragmentFactory()
//        val scenario = launchFragmentInContainer<SplashFragment>()

        // Launch splash fragment page
        val scenario = launchFragmentInContainer<SplashFragment>()

        // Check is displayed
        onView(withId(R.id.splashFragment)).check(matches(isDisplayed()))

        // Check title text exists
        onView(withId(R.id.textView)).check(matches(withText(R.string.app_name)))




    }

}
