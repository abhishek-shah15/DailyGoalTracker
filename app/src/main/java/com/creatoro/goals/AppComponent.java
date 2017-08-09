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

package com.creatoro.goals;

import android.content.Context;

import com.creatoro.goals.activities.habits.list.model.HabitCardListCache;
import com.creatoro.goals.intents.IntentFactory;
import com.creatoro.goals.intents.IntentParser;
import com.creatoro.goals.intents.PendingIntentFactory;
import com.creatoro.goals.io.DirFinder;
import com.creatoro.goals.io.GenericImporter;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.models.ModelFactory;
import com.creatoro.goals.models.sqlite.SQLModelFactory;
import com.creatoro.goals.notifications.NotificationTray;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.preferences.WidgetPreferences;
import com.creatoro.goals.sync.SyncManager;
import com.creatoro.goals.tasks.AndroidTaskRunner;
import com.creatoro.goals.tasks.TaskRunner;
import com.creatoro.goals.utils.ReminderScheduler;
import com.creatoro.goals.widgets.WidgetUpdater;

import com.creatoro.goals.commands.CreateHabitCommandFactory;
import com.creatoro.goals.commands.EditHabitCommandFactory;

import dagger.Component;

@AppScope
@Component(modules = {
    AppModule.class, AndroidTaskRunner.class, SQLModelFactory.class
})
public interface AppComponent
{
    com.creatoro.goals.commands.CommandRunner getCommandRunner();

    @AppContext
    Context getContext();

    CreateHabitCommandFactory getCreateHabitCommandFactory();

    DirFinder getDirFinder();

    EditHabitCommandFactory getEditHabitCommandFactory();

    GenericImporter getGenericImporter();

    HabitCardListCache getHabitCardListCache();

    HabitList getHabitList();

    HabitLogger getHabitsLogger();

    IntentFactory getIntentFactory();

    IntentParser getIntentParser();

    ModelFactory getModelFactory();

    NotificationTray getNotificationTray();

    PendingIntentFactory getPendingIntentFactory();

    Preferences getPreferences();

    ReminderScheduler getReminderScheduler();

    SyncManager getSyncManager();

    TaskRunner getTaskRunner();

    WidgetPreferences getWidgetPreferences();

    WidgetUpdater getWidgetUpdater();
}
