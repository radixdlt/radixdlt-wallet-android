package com.radixdlt.android.apps.wallet.helper

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

fun navigationIconMatcher(): Matcher<View> = Matchers.allOf(
    ViewMatchers.isAssignableFrom(ImageButton::class.java),
    ViewMatchers.withParent(ViewMatchers.isAssignableFrom(Toolbar::class.java))
)

fun clickOn(matcher: Matcher<View>) {
    onView(matcher).perform(ViewActions.click())
}
