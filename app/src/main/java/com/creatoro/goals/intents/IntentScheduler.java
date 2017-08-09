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

package com.creatoro.goals.intents;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.creatoro.goals.AppContext;
import com.creatoro.goals.AppScope;

import javax.inject.Inject;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.content.Context.ALARM_SERVICE;

@AppScope
public class IntentScheduler
{
    private final AlarmManager manager;

    @Inject
    public IntentScheduler(@AppContext Context context)
    {
        manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    public void schedule(@NonNull Long timestamp, PendingIntent intent)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, timestamp, intent);
            return;
        }

        if (Build.VERSION.SDK_INT >= 19)
        {
            manager.setExact(RTC_WAKEUP, timestamp, intent);
            return;
        }

        manager.set(RTC_WAKEUP, timestamp, intent);
    }
}
