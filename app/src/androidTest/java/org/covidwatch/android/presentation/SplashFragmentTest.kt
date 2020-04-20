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

    }

    @Test
    fun test_LogoVisible() {

        // Check Logo exists
        onView(withId(R.id.imageView)).check(matches(isCompletelyDisplayed()))

    }

    @Test
    fun test_TitleVisible() {

        // Check title text is displayed and matches app name
        onView(withId(R.id.textView)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.textView)).check(matches(withText(R.string.app_name)))

    }

    @Test
    fun test_subtitleVisible() {

        // Check subtitle text is displayed and matches "Help your community stay safe, anonymously."
        onView(withId(R.id.textView2)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.textView2)).check(matches(withText("Help your community stay safe, anonymously.")))

    }

    @Test
    fun test_startButtonVisible() {

        // Check start button is displayed and matches text "Start"
        onView(withId(R.id.start_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.start_button)).check(matches(withText("Start")))

    }

    // Not yet built out
//    @Test
//    fun test_uiDisplayOrderMaintained() {
//
//        // Check that elements are displayed in correct order
//        onView(withId(R.id.imageView)).check(isCompletelyAbove())
//        onView(withId(R.id.textView))
//        onView(withId(R.id.textView2))
//        onView(withId(R.id.start_button))
//
//    }
//
//    @Test
//    fun test_ElementsCentered() {
//
//        // Check that elements are centered
//        onView(withId(R.id.imageView))
//        onView(withId(R.id.textView))
//        onView(withId(R.id.textView2))
//        onView(withId(R.id.start_button)).check(matches(withText("Start")))
//
//    }

}
