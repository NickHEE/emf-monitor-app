package com.example.emf_monitor;


import android.os.SystemClock;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.emf_monitor.EMF_monitor.LoginActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
@LargeTest // Test takes longer than 1000ms to complete
public class UITest {
    private static String TEST_USERNAME;
    private static String TEST_PASSWORD;
    private static String TEST_THRESHOLD;

    private Matcher<View> hasValueEqualTo(final String content) {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Has EditText/TextView the value:  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView) && !(view instanceof EditText)) {
                    return false;
                }
                if (view != null) {
                    String text;
                    if (view instanceof TextView) {
                        text = ((TextView) view).getText().toString();
                        Log.d("hasValueEqualTo", text);
                    } else {
                        text = ((EditText) view).getText().toString();
                        Log.d("hasValueEqualTo", text);
                    }

                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class); // Launch login activity

    @Before
    public void initTestData() {
        TEST_USERNAME = "nick";
        TEST_PASSWORD = "hi";
        TEST_THRESHOLD = "1000.0";
    }

    @Test
    public void loginAndRecord() {

        // login (username)
        onView(withId(R.id.username_field)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.username_field)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
        //       (password)
        onView(withId(R.id.password_field)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        //       (hit login button)
        onView(withId(R.id.login_id)).perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.settingsButton)).perform(click());// go to settings activity
        //          enter new threshold value
        onView(withId(R.id.threshold_field)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.threshold_field)).perform(typeText(TEST_THRESHOLD), closeSoftKeyboard());
        //          click apply button
        onView(withId(R.id.apply_id)).perform(click());
        //          click record button
        onView(withId(R.id.recordButton)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.recordButton)).perform(click());


    }
    @Test
    public void checkSettingsSaved() {
        // logout/login again
        // login (username)
        onView(withId(R.id.username_field)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.username_field)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
        //       (password)
        onView(withId(R.id.password_field)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        //       (hit login button)
        onView(withId(R.id.login_id)).perform(click());
        // Settings Page
        onView(withId(R.id.settingsButton)).perform(click());
        onView(withId(R.id.threshold_field)).check(matches(hasValueEqualTo(TEST_THRESHOLD)));
    }

}

