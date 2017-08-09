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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.creatoro.goals.HabitsApplication;
import com.creatoro.goals.activities.habits.list.ListHabitsActivity;
import com.creatoro.goals.activities.habits.list.controllers.CheckmarkButtonController;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.utils.DateUtils;

import com.creatoro.goals.R;
import com.creatoro.goals.activities.habits.list.controllers.CheckmarkButtonControllerFactory;

import java.util.Random;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.creatoro.goals.utils.AttributeSetUtils.getIntAttribute;
import static com.creatoro.goals.utils.ColorUtils.getAndroidTestColor;

public class CheckmarkPanelView extends LinearLayout
    implements Preferences.Listener
{
    private static final int LEFT_TO_RIGHT = 0;

    private static final int RIGHT_TO_LEFT = 1;

    @Nullable
    private Preferences prefs;

    private int values[];

    private int nButtons;

    private int color;

    private Controller controller;

    @NonNull
    private Habit habit;

    private int dataOffset;

    public CheckmarkPanelView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkPanelView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        init();

        if (ctx != null && attrs != null)
        {
            int paletteColor = getIntAttribute(ctx, attrs, "color", 0);
            setColor(getAndroidTestColor(paletteColor));
            setButtonCount(getIntAttribute(ctx, attrs, "button_count", 5));
        }

        if (isInEditMode()) initEditMode();
    }

    public CheckmarkButtonView indexToButton(int i)
    {
        int position = i;

        if (getCheckmarkOrder() == RIGHT_TO_LEFT) position = nButtons - i - 1;

        return (CheckmarkButtonView) getChildAt(position);
    }

    @Override
    public void onCheckmarkOrderChanged()
    {
        setupButtons();
    }

    public void setButtonCount(int newButtonCount)
    {
        if (nButtons != newButtonCount)
        {
            nButtons = newButtonCount;
            addButtons();
        }

        setupButtons();
    }

    public void setColor(int color)
    {
        this.color = color;
        setupButtons();
    }

    public void setController(Controller controller)
    {
        this.controller = controller;
        setupButtons();
    }

    public void setDataOffset(int dataOffset)
    {
        this.dataOffset = dataOffset;
        setupButtons();
    }

    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
        setupButtons();
    }

    public void setValues(int[] values)
    {
        this.values = values;
        setupButtons();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (prefs != null) prefs.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (prefs != null) prefs.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        float buttonWidth = getResources().getDimension(R.dimen.checkmarkWidth);
        float buttonHeight =
            getResources().getDimension(R.dimen.checkmarkHeight);

        float width = buttonWidth * nButtons;

        widthSpec = makeMeasureSpec((int) width, EXACTLY);
        heightSpec = makeMeasureSpec((int) buttonHeight, EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
    }

    private void addButtons()
    {
        removeAllViews();

        for (int i = 0; i < nButtons; i++)
            addView(new CheckmarkButtonView(getContext()));
    }

    private int getCheckmarkOrder()
    {
        if (prefs == null) return LEFT_TO_RIGHT;
        return prefs.shouldReverseCheckmarks() ? RIGHT_TO_LEFT : LEFT_TO_RIGHT;
    }

    private void init()
    {
        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof com.creatoro.goals.HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        setWillNotDraw(false);
        values = new int[0];
    }

    private void initEditMode()
    {
        int values[] = new int[nButtons];

        for (int i = 0; i < nButtons; i++)
            values[i] = Math.min(2, new Random().nextInt(4));

        setValues(values);
    }

    private void setupButtonControllers(long timestamp,
                                        CheckmarkButtonView buttonView)
    {
        if (controller == null) return;
        if (!(getContext() instanceof ListHabitsActivity)) return;

        ListHabitsActivity activity = (ListHabitsActivity) getContext();
        CheckmarkButtonControllerFactory buttonControllerFactory = activity
            .getListHabitsComponent()
            .getCheckmarkButtonControllerFactory();

        com.creatoro.goals.activities.habits.list.controllers.CheckmarkButtonController buttonController =
            buttonControllerFactory.create(habit, timestamp);
        buttonController.setListener(controller);
        buttonController.setView(buttonView);
        buttonView.setController(buttonController);
    }

    private void setupButtons()
    {
        long timestamp = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;
        timestamp -= day * dataOffset;

        for (int i = 0; i < nButtons; i++)
        {
            CheckmarkButtonView buttonView = indexToButton(i);
            if (i + dataOffset >= values.length) break;
            buttonView.setValue(values[i + dataOffset]);
            buttonView.setColor(color);
            setupButtonControllers(timestamp, buttonView);
            timestamp -= day;
        }
    }

    public interface Controller extends CheckmarkButtonController.Listener
    {

    }
}
