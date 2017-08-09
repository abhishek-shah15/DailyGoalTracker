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

package com.creatoro.goals.activities.habits.list;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.creatoro.goals.HabitsApplication;
import com.creatoro.goals.activities.ActivityModule;
import com.creatoro.goals.activities.BaseActivity;
import com.creatoro.goals.activities.ThemeSwitcher;
import com.creatoro.goals.activities.habits.list.model.HabitCardListAdapter;
import com.creatoro.goals.preferences.Preferences;
import com.creatoro.goals.sync.SyncService;
import com.creatoro.goals.utils.MidnightTimer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import com.creatoro.goals.R;

/**
 * Activity that allows the user to see and modify the list of habits.
 */
public class ListHabitsActivity extends BaseActivity
{
    private HabitCardListAdapter adapter;

    private ListHabitsRootView rootView;

    private ListHabitsScreen screen;

    private ListHabitsComponent component;

    private boolean pureBlack;

    private Preferences prefs;

    private MidnightTimer midnightTimer;

    public ListHabitsComponent getListHabitsComponent()
    {
        return component;
    }

    //custom code for admob
    SharedPreferences mPrefs;
    private InterstitialAd interstitialAd;
    final AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
    AdView adView;
    private boolean checkAd;
    private static final int AD_UNIT_ID = R.string.app_interstitial_ID;
    // Custom code ends

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // custom code for admob

        mPrefs = getSharedPreferences("myPref", MODE_PRIVATE);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(AD_UNIT_ID));

        final AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (!checkAd) {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
//						Log.v(LOG_TAG, "not Loaded");
                    }
                    checkAd = true;
                }
            }

            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(adRequestBuilder.build());
            }
        });
        // admob code ends


        HabitsApplication app = (HabitsApplication) getApplicationContext();

        component = DaggerListHabitsComponent
            .builder()
            .appComponent(app.getComponent())
            .activityModule(new ActivityModule(this))
            .build();

        ListHabitsMenu menu = component.getMenu();
        ListHabitsSelectionMenu selectionMenu = component.getSelectionMenu();
        ListHabitsController controller = component.getController();

        adapter = component.getAdapter();
        rootView = component.getRootView();
        screen = component.getScreen();

        prefs = app.getComponent().getPreferences();
        pureBlack = prefs.isPureBlackEnabled();

        screen.setMenu(menu);
        screen.setController(controller);
        screen.setSelectionMenu(selectionMenu);
        rootView.setController(controller, selectionMenu);

        midnightTimer = component.getMidnightTimer();

        if(prefs.isSyncFeatureEnabled())
            startService(new Intent(this, SyncService.class));

        setScreen(screen);
        controller.onStartup();
    }

    @Override
    protected void onPause()
    {
        midnightTimer.onPause();
        screen.onDettached();
        adapter.cancelRefresh();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        adapter.refresh();
        screen.onAttached();
        rootView.postInvalidate();
        midnightTimer.onResume();

        if (prefs.getTheme() == ThemeSwitcher.THEME_DARK &&
            prefs.isPureBlackEnabled() != pureBlack)
        {
            restartWithFade();
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
//		super.onBackPressed();
        final Dialog dialog = new Dialog(ListHabitsActivity.this);
        // Include dialog.xml file
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rank_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        Button nothanks = (Button)dialog.findViewById(R.id.btn_nothanks);
        Button ratenow = (Button)dialog.findViewById(R.id.btn_ratenow);
        Button ratelater = (Button)dialog.findViewById(R.id.btn_ratelater);


        nothanks.setOnClickListener(v -> {

            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean("RateNever", true);
            edit.commit();

            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                // Proceed to the next level.
//					Log.v(LOG_TAG, "not loaded");
            }
            interstitialAd.loadAd(adRequestBuilder.build());
            finish();


        });

        ratenow.setOnClickListener(v -> {

            Uri uri = Uri
                    .parse("https://play.google.com/store/apps/details?id="
                            + getApplicationContext()
                            .getPackageName());

            Intent rateIntent = new Intent(
                    Intent.ACTION_VIEW, uri);
            startActivity(rateIntent);

            Log.v("linkkkkkkkkkkkkk", uri.toString());
            finish();

        });

        ratelater.setOnClickListener(v -> {

            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                // Proceed to the next level.
//					Log.v(LOG_TAG, "not loaded");
            }
            interstitialAd.loadAd(adRequestBuilder.build());
            finish();
        });
        dialog.show();
    }

}
