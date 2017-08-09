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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.util.SQLiteUtils;
import com.creatoro.goals.models.Frequency;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.Reminder;
import com.creatoro.goals.models.WeekdayList;

import java.lang.reflect.Field;

/**
 * The SQLite database record corresponding to a {@link Habit}.
 */
@Table(name = "Habits")
public class HabitRecord extends Model implements SQLiteRecord
{
    public static String SELECT =
        "select id, color, description, freq_den, freq_num, " +
        "name, position, reminder_hour, reminder_min, " +
        "highlight, archived, reminder_days, type, target_type, " +
        "target_value, unit from habits ";

    @Column(name = "name")
    public String name;

    @Column(name = "description")
    public String description;

    @Column(name = "freq_num")
    public Integer freqNum;

    @Column(name = "freq_den")
    public Integer freqDen;

    @Column(name = "color")
    public Integer color;

    @Column(name = "position")
    public Integer position;

    @Nullable
    @Column(name = "reminder_hour")
    public Integer reminderHour;

    @Nullable
    @Column(name = "reminder_min")
    public Integer reminderMin;

    @NonNull
    @Column(name = "reminder_days")
    public Integer reminderDays;

    @Column(name = "highlight")
    public Integer highlight;

    @Column(name = "archived")
    public Integer archived;

    @Column(name = "type")
    public Integer type;

    @Column(name = "target_value")
    public Double targetValue;

    @Column(name = "target_type")
    public Integer targetType;

    @Column(name = "unit")
    public String unit;

    public HabitRecord()
    {
    }

    @Nullable
    public static HabitRecord get(long id)
    {
        return HabitRecord.load(HabitRecord.class, id);
    }

    /**
     * Changes the id of a habit on the database.
     *
     * @param oldId the original id
     * @param newId the new id
     */
    @SuppressLint("DefaultLocale")
    public static void updateId(long oldId, long newId)
    {
        SQLiteUtils.execSql(
            String.format("update Habits set Id = %d where Id = %d", newId,
                oldId));
    }

    /**
     * Deletes the habit and all data associated to it, including checkmarks,
     * repetitions and scores.
     */
    public void cascadeDelete()
    {
        Long id = getId();

        com.creatoro.goals.utils.DatabaseUtils.executeAsTransaction(() -> {
            new Delete()
                .from(CheckmarkRecord.class)
                .where("habit = ?", id)
                .execute();

            new Delete()
                .from(RepetitionRecord.class)
                .where("habit = ?", id)
                .execute();

            new Delete()
                .from(ScoreRecord.class)
                .where("habit = ?", id)
                .execute();

            new Delete()
                .from(StreakRecord.class)
                .where("habit = ?", id)
                .execute();

            delete();
        });
    }

    public void copyFrom(Habit model)
    {
        this.name = model.getName();
        this.description = model.getDescription();
        this.highlight = 0;
        this.color = model.getColor();
        this.archived = model.isArchived() ? 1 : 0;
        this.type = model.getType();
        this.targetType = model.getTargetType();
        this.targetValue = model.getTargetValue();
        this.unit = model.getUnit();

        Frequency freq = model.getFrequency();
        this.freqNum = freq.getNumerator();
        this.freqDen = freq.getDenominator();
        this.reminderDays = 0;
        this.reminderMin = null;
        this.reminderHour = null;

        if (model.hasReminder())
        {
            Reminder reminder = model.getReminder();
            this.reminderHour = reminder.getHour();
            this.reminderMin = reminder.getMinute();
            this.reminderDays = reminder.getDays().toInteger();
        }
    }

    @Override
    public void copyFrom(Cursor c)
    {
        setId(c.getLong(0));
        color = c.getInt(1);
        description = c.getString(2);
        freqDen = c.getInt(3);
        freqNum = c.getInt(4);
        name = c.getString(5);
        position = c.getInt(6);
        reminderHour = c.getInt(7);
        reminderMin = c.getInt(8);
        highlight = c.getInt(9);
        archived = c.getInt(10);
        reminderDays = c.getInt(11);
        type = c.getInt(12);
        targetType = c.getInt(13);
        targetValue = c.getDouble(14);
        unit = c.getString(15);
    }

    public void copyTo(Habit habit)
    {
        habit.setName(this.name);
        habit.setDescription(this.description);
        habit.setFrequency(new Frequency(this.freqNum, this.freqDen));
        habit.setColor(this.color);
        habit.setArchived(this.archived != 0);
        habit.setId(this.getId());
        habit.setType(this.type);
        habit.setTargetType(this.targetType);
        habit.setTargetValue(this.targetValue);
        habit.setUnit(this.unit);

        if (reminderHour != null && reminderMin != null)
        {
            habit.setReminder(new Reminder(reminderHour, reminderMin,
                new WeekdayList(reminderDays)));
        }
    }

    /**
     * Saves the habit on the database, and assigns the specified id to it.
     *
     * @param id the id that the habit should receive
     */
    public void save(long id)
    {
        save();
        updateId(getId(), id);
    }

    private void setId(Long id)
    {
        try
        {
            Field f = (Model.class).getDeclaredField("mId");
            f.setAccessible(true);
            f.set(this, id);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
