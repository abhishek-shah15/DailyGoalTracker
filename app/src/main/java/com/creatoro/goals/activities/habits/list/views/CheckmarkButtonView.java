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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.TextView;

import com.creatoro.goals.activities.habits.list.controllers.CheckmarkButtonController;
import com.creatoro.goals.models.Checkmark;
import com.creatoro.goals.utils.InterfaceUtils;
import com.creatoro.goals.utils.StyledResources;

import com.creatoro.goals.R;

import static com.creatoro.goals.utils.AttributeSetUtils.getIntAttribute;
import static com.creatoro.goals.utils.ColorUtils.getAndroidTestColor;

public class CheckmarkButtonView extends TextView
{
    private int color;

    private int value;

    private StyledResources res;

    public CheckmarkButtonView(@Nullable Context context)
    {
        super(context);
        init();
    }

    public CheckmarkButtonView(@Nullable Context context,
                               @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();

        if (context != null && attrs != null)
        {
            int color = getIntAttribute(context, attrs, "color", 0);
            int value = getIntAttribute(context, attrs, "value", 0);
            setColor(getAndroidTestColor(color));
            setValue(value);
        }
    }

    public void setColor(int color)
    {
        this.color = color;
        updateText();
    }

    public void setController(final CheckmarkButtonController controller)
    {
        setOnClickListener(v -> controller.onClick());
        setOnLongClickListener(v -> controller.onLongClick());
    }

    public void setValue(int value)
    {
        this.value = value;
        updateText();
    }

    public void toggle()
    {
        value = (value == Checkmark.CHECKED_EXPLICITLY ? Checkmark.UNCHECKED :
                     Checkmark.CHECKED_EXPLICITLY);

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        updateText();
    }

    private void init()
    {
        res = new StyledResources(getContext());

        setWillNotDraw(false);

        setMinHeight(
            getResources().getDimensionPixelSize(R.dimen.checkmarkHeight));
        setMinWidth(
            getResources().getDimensionPixelSize(R.dimen.checkmarkWidth));

        setFocusable(false);
        setGravity(Gravity.CENTER);
        setTypeface(InterfaceUtils.getFontAwesome(getContext()));
    }

    private void updateText()
    {
        int lowContrastColor = res.getColor(R.attr.lowContrastTextColor);

        if (value == Checkmark.CHECKED_EXPLICITLY)
        {
            setText(R.string.fa_check);
            setTextColor(color);
        }

        if (value == Checkmark.CHECKED_IMPLICITLY)
        {
            setText(R.string.fa_check);
            setTextColor(lowContrastColor);
        }

        if (value == Checkmark.UNCHECKED)
        {
            setText(R.string.fa_times);
            setTextColor(lowContrastColor);
        }
    }
}
