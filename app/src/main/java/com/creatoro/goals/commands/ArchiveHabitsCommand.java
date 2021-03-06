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

import java.util.LinkedList;
import java.util.List;

/**
 * Command to archive a list of habits.
 */
public class ArchiveHabitsCommand extends Command
{
    final List<Habit> selected;

    final HabitList habitList;

    public ArchiveHabitsCommand(@NonNull HabitList habitList,
                                @NonNull List<Habit> selected)
    {
        super();
        this.habitList = habitList;
        this.selected = new LinkedList<>(selected);
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setArchived(true);
        habitList.update(selected);
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_archived;
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_unarchived;
    }

    @Override
    public void undo()
    {
        for (Habit h : selected) h.setArchived(false);
        habitList.update(selected);
    }

    @NonNull
    @Override
    public Record toRecord()
    {
        return new Record(this);
    }

    public static class Record
    {
        @NonNull
        public final String id;

        @NonNull
        public final String event = "Archive";

        @NonNull
        public final List<Long> habits;

        public Record(@NonNull ArchiveHabitsCommand command)
        {
            id = command.getId();
            habits = new LinkedList<>();
            for (Habit h : command.selected)
            {
                habits.add(h.getId());
            }
        }

        @NonNull
        public ArchiveHabitsCommand toCommand(@NonNull HabitList habitList)
        {
            List<Habit> selected = new LinkedList<>();
            for (Long id : this.habits) selected.add(habitList.getById(id));

            ArchiveHabitsCommand command;
            command = new ArchiveHabitsCommand(habitList, selected);
            command.setId(id);
            return command;
        }
    }
}