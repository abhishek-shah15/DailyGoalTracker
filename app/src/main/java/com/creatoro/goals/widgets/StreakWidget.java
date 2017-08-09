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

package com.creatoro.goals.widgets;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.creatoro.goals.activities.common.views.StreakChart;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.Streak;
import com.creatoro.goals.utils.ColorUtils;
import com.creatoro.goals.widgets.views.GraphWidgetView;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class StreakWidget extends BaseWidget
{
    @NonNull
    private Habit habit;

    public StreakWidget(@NonNull Context context, int id, @NonNull Habit habit)
    {
        super(context, id);
        this.habit = habit;
    }

    @Override
    public PendingIntent getOnClickPendingIntent(Context context)
    {
        return pendingIntentFactory.showHabit(habit);
    }

    @Override
    public void refreshData(View view)
    {
        GraphWidgetView widgetView = (GraphWidgetView) view;
        StreakChart chart = (StreakChart) widgetView.getDataView();

        int color = ColorUtils.getColor(getContext(), habit.getColor());

        int count = chart.getMaxStreakCount();
        List<Streak> streaks = habit.getStreaks().getBest(count);

        chart.setColor(color);
        chart.setStreaks(streaks);
    }

    @Override
    protected View buildView()
    {
        StreakChart dataView = new StreakChart(getContext());
        GraphWidgetView view = new GraphWidgetView(getContext(), dataView);
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setTitle(habit.getName());
        view.setLayoutParams(params);
        return view;
    }

    @Override
    protected int getDefaultHeight()
    {
        return 200;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 200;
    }
}
