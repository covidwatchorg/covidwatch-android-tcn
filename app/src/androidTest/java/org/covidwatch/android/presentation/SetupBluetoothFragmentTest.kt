package org.covidwatch.android.presentation

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
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
class SetupBluetoothFragmentTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun test_isSetupBluetoothVisible() {

        // Google method of launching fragments in isolation:
        // The "fragmentArgs" and "factory" arguments are optional.
//        val fragmentArgs = Bundle().apply {
//            putInt("selectedListItem", 0)
//        }
//        val factory = MyFragmentFactory()
//        val scenario = launchFragmentInContainer<SplashFragment>()

        // Launch BluetoothSetup fragment without args
        val scenario = launchFragmentInContainer<SetupBluetoothFragment>()

        // Check fragment is displayed
        onView(withId(R.id.setupBluetoothFragment)).check(matches(isDisplayed()))

        // Messing with state changes
//        scenario.moveToState(Lifecycle.State.CREATED)
//        scenario.moveToState(Lifecycle.State.STARTED)
//        scenario.moveToState(Lifecycle.State.RESUMED)
//        scenario.moveToState(Lifecycle.State.DESTROYED)

    }

    // Will uncomment once fragment testing is figured out

    //    @Rule
//    @JvmField
//    var mGrantPermissionRule =
//        GrantPermissionRule.grant(
//            "android.permission.ACCESS_FINE_LOCATION"
//        )

    // Create a TestNavHostController
//        val navController = TestNavHostController(
//            ApplicationProvider.getApplicationContext())
//        navController.setGraph(R.navigation.navigation_main)
//        navController.setCurrentDestination(R.id.setupBluetoothFragment)
//        navController.navigate(R.id.setupBluetoothFragment)

//
//    @Test
//    fun test_LogoVisible() {
//
//        // Check Logo exists
//        onView(withId(R.id.imageView)).check(matches(isCompletelyDisplayed()))
//
//    }
//
//    @Test
//    fun test_TitleVisible() {
//
//        // Check title text is displayed and matches app name
//        onView(withId(R.id.textView)).check(matches(isCompletelyDisplayed()))
//        onView(withId(R.id.textView)).check(matches(withText(R.string.app_name)))
//
//    }
//
//    @Test
//    fun test_subtitleVisible() {
//
//        // Check subtitle text is displayed and matches "Help your community stay safe, anonymously."
//        onView(withId(R.id.textView2)).check(matches(isCompletelyDisplayed()))
//        onView(withId(R.id.textView2)).check(matches(withText(R.string.splash_text)))
//
//    }
//
//    @Test
//    fun test_startButtonVisible() {
//
//        // Check start button is displayed and matches text "Start"
//        onView(withId(R.id.start_button)).check(matches(isCompletelyDisplayed()))
//        onView(withId(R.id.start_button)).check(matches(withText(R.string.start)))
//
//    }

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
