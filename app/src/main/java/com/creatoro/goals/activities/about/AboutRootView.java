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

package com.creatoro.goals.activities.about;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.creatoro.goals.HabitsApplication;
import com.creatoro.goals.activities.BaseRootView;
import com.creatoro.goals.intents.IntentFactory;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.utils.StyledResources;

import com.creatoro.goals.BuildConfig;
import com.creatoro.goals.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutRootView extends BaseRootView
{
    @BindView(R.id.tvVersion)
    TextView tvVersion;

    @BindView(R.id.tvRate)
    TextView tvRate;

    @BindView(R.id.tvFeedback)
    TextView tvFeedback;

    @BindView(R.id.tvSource)
    TextView tvSource;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    int developerCountdown = 10;

    @Nullable
    Preferences prefs;

    private final IntentFactory intents;

    public AboutRootView(Context context, IntentFactory intents)
    {
        super(context);
        this.intents = intents;

        addView(inflate(getContext(), R.layout.about, null));
        ButterKnife.bind(this);

        tvVersion.setText(
            String.format(getResources().getString(R.string.version_n),
                BuildConfig.VERSION_NAME));

        if (context.getApplicationContext() instanceof HabitsApplication)
        {
            HabitsApplication app =
                (HabitsApplication) context.getApplicationContext();
            prefs = app.getComponent().getPreferences();
        }
    }

    @Override
    public boolean getDisplayHomeAsUp()
    {
        return true;
    }

    @NonNull
    @Override
    public Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    public int getToolbarColor()
    {
        StyledResources res = new StyledResources(getContext());
        if (!res.getBoolean(R.attr.useHabitColorAsPrimary))
            return super.getToolbarColor();

        return res.getColor(R.attr.aboutScreenColor);
    }

    @OnClick(R.id.tvFeedback)
    public void onClickFeedback()
    {
        Intent intent = intents.sendFeedback(getContext());
        getContext().startActivity(intent);
    }

    @OnClick(R.id.tvVersion)
    public void onClickIcon()
    {
        developerCountdown--;
        if (developerCountdown <= 0)
        {
            if (prefs == null) return;
            prefs.setDeveloper(true);
            String text = "You are now a developer";
            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.tvRate)
    public void onClickRate()
    {
        Intent intent = intents.rateApp(getContext());
        getContext().startActivity(intent);
    }

    @OnClick(R.id.tvSource)
    public void onClickSource()
    {
        Intent intent = intents.viewSourceCode(getContext());
        getContext().startActivity(intent);
    }

    @OnClick(R.id.tvTranslate)
    public void onClickTranslate()
    {
        Intent intent = intents.helpTranslate(getContext());
        getContext().startActivity(intent);
    }

    @Override
    protected void initToolbar()
    {
        super.initToolbar();
        toolbar.setTitle(getResources().getString(R.string.about));
    }
}
