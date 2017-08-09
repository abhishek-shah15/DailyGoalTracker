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

package com.creatoro.goals.io;

import android.support.annotation.NonNull;

import com.activeandroid.ActiveAndroid;
import com.creatoro.goals.models.Frequency;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.models.ModelFactory;
import com.creatoro.goals.utils.DateUtils;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import javax.inject.Inject;

/**
 * Class that imports data from HabitBull CSV files.
 */
public class HabitBullCSVImporter extends AbstractImporter
{
    private ModelFactory modelFactory;

    @Inject
    public HabitBullCSVImporter(@NonNull HabitList habits,
                                @NonNull ModelFactory modelFactory)
    {
        super(habits);
        this.modelFactory = modelFactory;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();

        return line.startsWith("HabitName,HabitDescription,HabitCategory");
    }

    @Override
    public void importHabitsFromFile(@NonNull final File file) throws IOException
    {
        ActiveAndroid.beginTransaction();
        try
        {
            parseFile(file);
            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    private void parseFile(@NonNull File file) throws IOException
    {
        CSVReader reader = new CSVReader(new FileReader(file));
        HashMap<String, Habit> map = new HashMap<>();

        for(String line[] : reader)
        {
            String name = line[0];
            if(name.equals("HabitName")) continue;

            String description = line[1];
            String dateString[] = line[3].split("-");
            int year = Integer.parseInt(dateString[0]);
            int month = Integer.parseInt(dateString[1]);
            int day = Integer.parseInt(dateString[2]);

            Calendar date = DateUtils.getStartOfTodayCalendar();
            date.set(year, month - 1, day);

            long timestamp = date.getTimeInMillis();

            int value = Integer.parseInt(line[4]);
            if(value != 1) continue;

            Habit h = map.get(name);

            if(h == null)
            {
                h = modelFactory.buildHabit();
                h.setName(name);
                h.setDescription(description);
                h.setFrequency(Frequency.DAILY);
                habits.add(h);
                map.put(name, h);
            }

            if(!h.getRepetitions().containsTimestamp(timestamp))
                h.getRepetitions().toggleTimestamp(timestamp);
        }
    }
}
