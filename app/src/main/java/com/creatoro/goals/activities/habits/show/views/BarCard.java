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

package com.creatoro.goals.activities.habits.show.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.creatoro.goals.HabitsApplication;
import com.creatoro.goals.activities.common.views.BarChart;
import com.creatoro.goals.models.Checkmark;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.tasks.Task;
import com.creatoro.goals.tasks.TaskRunner;
import com.creatoro.goals.utils.ColorUtils;
import com.creatoro.goals.utils.DateUtils;

import com.creatoro.goals.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BarCard extends HabitCard
{
    @BindView(R.id.barChart)
    BarChart chart;

    @BindView(R.id.title)
    TextView title;

    @Nullable
    private TaskRunner taskRunner;

    public BarCard(Context context)
    {
        super(context);
        init();
    }

    public BarCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    protected void refreshData()
    {
        if(taskRunner == null) return;
        taskRunner.execute(new RefreshTask(getHabit()));
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_bar, this);
        ButterKnife.bind(this);

        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            taskRunner = app.getComponent().getTaskRunner();
        }

        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        int color = ColorUtils.getAndroidTestColor(1);
        title.setTextColor(color);
        chart.setColor(color);
        chart.populateWithRandomData();
    }

    private class RefreshTask implements Task
    {
        private final Habit habit;

        public RefreshTask(Habit habit) {this.habit = habit;}

        @Override
        public void doInBackground()
        {
            long today = DateUtils.getStartOfToday();
            List<Checkmark> checkmarks =
                habit.getCheckmarks().getByInterval(0, today);
            chart.setCheckmarks(checkmarks);
        }

        @Override
        public void onPreExecute()
        {
            int color = ColorUtils.getColor(getContext(), habit.getColor());
            title.setTextColor(color);
            chart.setColor(color);
            chart.setTarget(habit.getTargetValue());
        }
    }
}
