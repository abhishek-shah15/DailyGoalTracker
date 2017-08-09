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

package com.creatoro.goals.activities.habits.list.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.creatoro.goals.activities.habits.list.views.HabitCardView;
import com.creatoro.goals.models.Habit;

public class HabitCardController implements HabitCardView.Controller
{
    @Nullable
    private HabitCardView view;

    @Nullable
    private Listener listener;

    @Override
    public void onEdit(@NonNull Habit habit, long timestamp)
    {
        if(listener != null) listener.onEdit(habit, timestamp);
    }

    @Override
    public void onInvalidEdit()
    {
        if(listener != null) listener.onInvalidEdit();
    }

    @Override
    public void onInvalidToggle()
    {
        if (listener != null) listener.onInvalidToggle();
    }

    @Override
    public void onToggle(@NonNull Habit habit, long timestamp)
    {
        if (view != null) view.triggerRipple(timestamp);
        if (listener != null) listener.onToggle(habit, timestamp);
    }

    public void setListener(@Nullable Listener listener)
    {
        this.listener = listener;
    }

    public void setView(@Nullable HabitCardView view)
    {
        this.view = view;
    }

    public interface Listener extends CheckmarkButtonController.Listener,
                                      NumberButtonController.Listener
    {

    }
}
