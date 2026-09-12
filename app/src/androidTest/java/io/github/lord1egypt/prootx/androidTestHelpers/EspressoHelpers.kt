package io.github.lord1egypt.prootx.androidTestHelpers

import android.app.Activity
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso
import androidx.test.espresso.Root
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import androidx.test.uiautomator.UiDevice
import androidx.test.platform.app.InstrumentationRegistry
import android.view.KeyCharacterMap
import android.view.KeyEvent
import java.io.File
import java.util.concurrent.TimeoutException

fun @receiver:IdRes Int.shortWaitForDisplay() {
    waitForDisplay(this, timeout = 1000)
}

fun @receiver:IdRes Int.waitForDisplay() {
    waitForDisplay(this, timeout = 10000)
}

fun @receiver:IdRes Int.longWaitForDisplay() {
    waitForDisplay(this)
}

fun @receiver:IdRes Int.extraLongWaitForDisplay() {
    waitForDisplay(this, timeout = 600_000)
}

fun waitForDisplay(@IdRes id: Int, timeout: Long = 300_000) {
    val test = {
        try {
            Espresso.onView(ViewMatchers.withId(id))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            true
        } catch (err: NoMatchingViewException) {
            // View is not in the hierarchy yet.
            false
        } catch (err: AssertionFailedError) {
            // View exists but is not displayed yet.
            false
        }
    }
    waitForSuccess(test, timeout)
}

/**
 * Espresso replacements for the previously used Barista helpers. Semantics are preserved:
 * list position assertions scroll to the position, "not displayed" accepts both an absent
 * view and a non-visible one, dialog clicks target the dialog positive button, and text
 * entry replaces the field content and closes the soft keyboard.
 */
fun assertDisplayedAtPosition(@IdRes listId: Int, position: Int, @IdRes itemViewId: Int, text: String) {
    Espresso.onView(ViewMatchers.withId(listId))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    Espresso.onView(allOf(ViewMatchers.withId(itemViewId), ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText(text)))
}

fun assertNotDisplayed(@IdRes id: Int) {
    try {
        Espresso.onView(ViewMatchers.withId(id))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        throw AssertionError("View with id $id is displayed but should not be")
    } catch (err: NoMatchingViewException) {
        // Not present in the hierarchy -> not displayed.
    } catch (err: AssertionFailedError) {
        // Present but not displayed.
    }
}

fun clickDialogPositiveButton() {
    Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
}

fun writeTo(@IdRes id: Int, text: String) {
    Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.replaceText(text))
    Espresso.closeSoftKeyboard()
}

fun clickListItem(@IdRes listId: Int, position: Int) {
    Espresso.onView(ViewMatchers.withId(listId))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, ViewActions.click()))
}

fun clickRadioButtonItem(@IdRes radioGroupId: Int, @IdRes radioButtonId: Int) {
    Espresso.onView(allOf(
            ViewMatchers.withId(radioButtonId),
            ViewMatchers.isDescendantOfA(ViewMatchers.withId(radioGroupId))
    )).perform(ViewActions.click())
}

fun waitForFile(file: File, timeout: Long = 300_000) {
    val test = { file.exists() }
    waitForSuccess(test, timeout)
}

fun waitForSuccess(test: () -> Boolean, timeout: Long) {
    val startTime = System.currentTimeMillis()
    while (!test()) {
        val currTime = System.currentTimeMillis()
        if (currTime >= startTime + timeout) throw TimeoutException()
        Thread.sleep(10)
    }
}

fun Int.waitForRefresh(activity: Activity) {
    val view = activity.findViewById<SwipeRefreshLayout>(this)
    while (view.isRefreshing) Thread.sleep(1)
}

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        @Suppress("DEPRECATION") // TYPE_TOAST is deprecated
        if (type == WindowManager.LayoutParams.TYPE_TOAST || type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            if (windowToken == appToken) {
                // Window isn't contained by any other windows
                return true
            }
        }
        return false
    }
}

fun @receiver:StringRes Int.matchText(): ViewInteraction =
        Espresso.onView(ViewMatchers.withText(this))

// TODO this doesn't quite work. will be useful for failure tests
fun @receiver:StringRes Int.notDisplayedInToast(): ViewInteraction =
        this.matchText().inRoot(ToastMatcher()).check(ViewAssertions.doesNotExist())

fun @receiver:StringRes Int.displayedInToast(): ViewInteraction =
        this.matchText().inRoot(ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

fun String.enterAsNativeViewText() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val characterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    val events = characterMap.getEvents(this.toCharArray())
    for (event in events) {
        // There are ACTION_UP and ACTION_DOWN events generated. Only need to press keycode once
        if (event.action == KeyEvent.ACTION_UP) continue
        device.pressKeyCode(event.keyCode)
    }
    device.pressEnter()
    Thread.sleep(500)
}

fun executeScript(script: String, location: File) {
    val scriptName = "script.sh"
    val scriptFile = File(location, scriptName)
    scriptFile.writeText(script)
    "chmod 777 $scriptName".enterAsNativeViewText()
    "bash $scriptName".enterAsNativeViewText()
}