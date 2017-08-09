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

package com.creatoro.goals.commands;

import android.support.annotation.NonNull;

import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.HabitList;

import com.creatoro.goals.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Command to change the color of a list of habits.
 */
public class ChangeHabitColorCommand extends Command
{
    @NonNull
    final HabitList habitList;

    @NonNull
    final List<Habit> selected;

    @NonNull
    final List<Integer> originalColors;

    @NonNull
    final Integer newColor;

    public ChangeHabitColorCommand(@NonNull HabitList habitList,
                                   @NonNull List<Habit> selected,
                                   @NonNull Integer newColor)
    {
        this.habitList = habitList;
        this.selected = selected;
        this.newColor = newColor;
        this.originalColors = new ArrayList<>(selected.size());
        for (Habit h : selected) originalColors.add(h.getColor());
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setColor(newColor);
        habitList.update(selected);
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_changed;
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_changed;
    }

    @NonNull
    @Override
    public Record toRecord()
    {
        return new Record(this);
    }

    @Override
    public void undo()
    {
        int k = 0;
        for (Habit h : selected) h.setColor(originalColors.get(k++));
        habitList.update(selected);
    }

    public static class Record
    {
        @NonNull
        public String id;

        @NonNull
        public String event = "ChangeColor";

        @NonNull
        public List<Long> habits;

        @NonNull
        public Integer color;

        public Record(ChangeHabitColorCommand command)
        {
            id = command.getId();
            color = command.newColor;
            habits = new LinkedList<>();
            for (Habit h : command.selected)
            {
                if (!h.hasId()) throw new RuntimeException("Habit not saved");
                habits.add(h.getId());
            }
        }

        public ChangeHabitColorCommand toCommand(@NonNull HabitList habitList)
        {
            List<Habit> selected = new LinkedList<>();
            for (Long id : this.habits) selected.add(habitList.getById(id));

            ChangeHabitColorCommand command;
            command = new ChangeHabitColorCommand(habitList, selected, color);
            command.setId(id);
            return command;
        }
    }
}
