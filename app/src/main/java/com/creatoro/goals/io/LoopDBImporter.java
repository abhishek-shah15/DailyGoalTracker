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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.utils.DatabaseUtils;
import com.creatoro.goals.utils.FileUtils;

import com.creatoro.goals.BuildConfig;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

/**
 * Class that imports data from database files exported by Loop Habit Tracker.
 */
public class LoopDBImporter extends AbstractImporter
{
    @NonNull
    private Context context;

    @Inject
    public LoopDBImporter(@NonNull @com.creatoro.goals.AppContext Context context,
                          @NonNull HabitList habits)
    {
        super(habits);
        this.context = context;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if (!isSQLite3File(file)) return false;

        SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null,
            SQLiteDatabase.OPEN_READONLY);

        boolean canHandle = true;

        Cursor c = db.rawQuery(
            "select count(*) from SQLITE_MASTER where name=? or name=?",
            new String[]{ "Checkmarks", "Repetitions" });

        if (!c.moveToFirst() || c.getInt(0) != 2)
        {
            Log.w("LoopDBImporter", "Cannot handle file: tables not found");
            canHandle = false;
        }

        if (db.getVersion() > BuildConfig.databaseVersion)
        {
            Log.w("LoopDBImporter", String.format(
                "Cannot handle file: incompatible version: %d > %d",
                db.getVersion(), BuildConfig.databaseVersion));
            canHandle = false;
        }

        c.close();
        db.close();
        return canHandle;
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException
    {
        ActiveAndroid.dispose();
        File originalDB = com.creatoro.goals.utils.DatabaseUtils.getDatabaseFile(context);
        FileUtils.copy(file, originalDB);
        DatabaseUtils.initializeActiveAndroid(context);
    }
}
