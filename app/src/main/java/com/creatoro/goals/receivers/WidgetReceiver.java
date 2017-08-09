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

package com.creatoro.goals.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.creatoro.goals.AppComponent;
import com.creatoro.goals.HabitsApplication;
import com.creatoro.goals.intents.IntentParser;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.sync.SyncService;

import com.creatoro.goals.receivers.DaggerWidgetReceiver_WidgetComponent;

import dagger.Component;

/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 * <p>
 * All broadcast messages are received and processed by this class.
 */
public class WidgetReceiver extends BroadcastReceiver
{
    public static final String ACTION_ADD_REPETITION =
        "com.creatoro.goals.ACTION_ADD_REPETITION";

    public static final String ACTION_DISMISS_REMINDER =
        "com.creatoro.goals.ACTION_DISMISS_REMINDER";

    public static final String ACTION_REMOVE_REPETITION =
        "com.creatoro.goals.ACTION_REMOVE_REPETITION";

    public static final String ACTION_TOGGLE_REPETITION =
        "com.creatoro.goals.ACTION_TOGGLE_REPETITION";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        HabitsApplication app =
            (HabitsApplication) context.getApplicationContext();

        WidgetComponent component = DaggerWidgetReceiver_WidgetComponent
            .builder()
            .appComponent(app.getComponent())
            .build();

        IntentParser parser = app.getComponent().getIntentParser();
        WidgetController controller = component.getWidgetController();
        Preferences prefs = app.getComponent().getPreferences();

        if(prefs.isSyncFeatureEnabled())
            context.startService(new Intent(context, SyncService.class));

        try
        {
            IntentParser.CheckmarkIntentData data;
            data = parser.parseCheckmarkIntent(intent);

            switch (intent.getAction())
            {
                case ACTION_ADD_REPETITION:
                    controller.onAddRepetition(data.habit, data.timestamp);
                    break;

                case ACTION_TOGGLE_REPETITION:
                    controller.onToggleRepetition(data.habit, data.timestamp);
                    break;

                case ACTION_REMOVE_REPETITION:
                    controller.onRemoveRepetition(data.habit, data.timestamp);
                    break;
            }
        }
        catch (RuntimeException e)
        {
            Log.e("WidgetReceiver", "could not process intent", e);
        }
    }

    @ReceiverScope
    @Component(dependencies = AppComponent.class)
    interface WidgetComponent
    {
        WidgetController getWidgetController();
    }
}
