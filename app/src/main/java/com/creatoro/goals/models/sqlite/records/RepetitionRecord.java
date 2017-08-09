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

package com.creatoro.goals.models.sqlite.records;

import android.database.Cursor;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.creatoro.goals.models.Repetition;

/**
 * The SQLite database record corresponding to a {@link Repetition}.
 */
@Table(name = "Repetitions")
public class RepetitionRecord extends Model implements SQLiteRecord
{
    @Column(name = "habit")
    public HabitRecord habit;

    @Column(name = "timestamp")
    public Long timestamp;

    @Column(name = "value")
    public int value;

    public static RepetitionRecord get(Long id)
    {
        return RepetitionRecord.load(RepetitionRecord.class, id);
    }

    public void copyFrom(Repetition repetition)
    {
        timestamp = repetition.getTimestamp();
        value = repetition.getValue();
    }

    @Override
    public void copyFrom(Cursor c)
    {
        timestamp = c.getLong(1);
        value = c.getInt(2);
    }

    public Repetition toRepetition()
    {
        return new Repetition(timestamp, value);
    }
}
