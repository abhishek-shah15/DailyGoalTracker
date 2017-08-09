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

package com.creatoro.goals.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.creatoro.goals.models.sqlite.InvalidDatabaseVersionException;
import com.creatoro.goals.models.sqlite.records.CheckmarkRecord;
import com.creatoro.goals.models.sqlite.records.HabitRecord;
import com.creatoro.goals.models.sqlite.records.RepetitionRecord;
import com.creatoro.goals.models.sqlite.records.ScoreRecord;
import com.creatoro.goals.models.sqlite.records.StreakRecord;
import com.creatoro.goals.sync.Event;

import com.creatoro.goals.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Random;

public abstract class DatabaseUtils
{
    public static void executeAsTransaction(Callback callback)
    {
        ActiveAndroid.beginTransaction();
        try
        {
            callback.execute();
            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    @NonNull
    public static File getDatabaseFile(Context context)
    {
        String databaseFilename = getDatabaseFilename();
        String root = context.getFilesDir().getPath();

        String format = "%s/../databases/%s";
        String filename = String.format(format, root, databaseFilename);

        return new File(filename);
    }

    @NonNull
    public static String getDatabaseFilename()
    {
        String databaseFilename = BuildConfig.databaseFilename;
        if (com.creatoro.goals.HabitsApplication.isTestMode()) databaseFilename = "test.db";
        return databaseFilename;
    }

    public static String getRandomId()
    {
        return new BigInteger(260, new Random()).toString(32).substring(0, 32);
    }

    @SuppressWarnings("unchecked")
    public static void initializeActiveAndroid(Context context)
    {
        Configuration dbConfig = new Configuration.Builder(context)
            .setDatabaseName(getDatabaseFilename())
            .setDatabaseVersion(BuildConfig.databaseVersion)
            .addModelClasses(CheckmarkRecord.class, HabitRecord.class,
                RepetitionRecord.class, ScoreRecord.class, StreakRecord.class,
                Event.class)
            .create();

        try
        {
            ActiveAndroid.initialize(dbConfig);
        }
        catch (RuntimeException e)
        {
            if(e.getMessage().contains("downgrade"))
                throw new InvalidDatabaseVersionException();
            else throw e;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String saveDatabaseCopy(Context context, File dir) throws IOException
    {
        SimpleDateFormat dateFormat = DateFormats.getBackupDateFormat();
        String date = dateFormat.format(DateUtils.getLocalTime());
        String format = "%s/Daily Goals Tracker Backup %s.db";
        String filename = String.format(format, dir.getAbsolutePath(), date);

        File db = getDatabaseFile(context);
        File dbCopy = new File(filename);
        FileUtils.copy(db, dbCopy);

        return dbCopy.getAbsolutePath();
    }

    public interface Callback
    {
        void execute();
    }
}
