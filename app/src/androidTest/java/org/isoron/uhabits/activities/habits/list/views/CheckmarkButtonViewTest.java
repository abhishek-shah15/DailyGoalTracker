/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.creatoro.goals.activities.habits.list.views;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import com.creatoro.goals.*;
import com.creatoro.goals.models.*;
import com.creatoro.goals.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;
import java.util.concurrent.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CheckmarkButtonViewTest extends BaseViewTest
{
    public static final String PATH = "habits/list/CheckmarkButtonView/";

    private CountDownLatch latch;

    private CheckmarkButtonView view;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        setSimilarityCutoff(0.03f);

        latch = new CountDownLatch(1);
        view = new CheckmarkButtonView(targetContext);
        view.setValue(Checkmark.UNCHECKED);
        view.setColor(ColorUtils.getAndroidTestColor(7));

        measureView(view, dpToPixels(40), dpToPixels(40));
    }

    @Test
    public void testRender_explicitCheck() throws Exception
    {
        view.setValue(Checkmark.CHECKED_EXPLICITLY);
        assertRendersCheckedExplicitly();
    }

    @Test
    public void testRender_implicitCheck() throws Exception
    {
        view.setValue(Checkmark.CHECKED_IMPLICITLY);
        assertRendersCheckedImplicitly();
    }

    @Test
    public void testRender_unchecked() throws Exception
    {
        view.setValue(Checkmark.UNCHECKED);
        assertRendersUnchecked();
    }

    protected void assertRendersCheckedExplicitly() throws IOException
    {
        assertRenders(view, PATH + "render_explicit_check.png");
    }

    protected void assertRendersCheckedImplicitly() throws IOException
    {
        assertRenders(view, PATH + "render_implicit_check.png");
    }

    protected void assertRendersUnchecked() throws IOException
    {
        assertRenders(view, PATH + "render_unchecked.png");
    }
}