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

package com.creatoro.goals.activities.habits.edit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.android.datetimepicker.time.TimePickerDialog;
import com.creatoro.goals.AppComponent;
import com.creatoro.goals.HabitsApplication;
import com.creatoro.goals.R;
import com.creatoro.goals.activities.BaseActivity;
import com.creatoro.goals.activities.common.dialogs.ColorPickerDialog;
import com.creatoro.goals.activities.common.dialogs.ColorPickerDialogFactory;
import com.creatoro.goals.activities.common.dialogs.WeekdayPickerDialog;
import com.creatoro.goals.activities.habits.edit.views.FrequencyPanel;
import com.creatoro.goals.activities.habits.edit.views.NameDescriptionPanel;
import com.creatoro.goals.activities.habits.edit.views.ReminderPanel;
import com.creatoro.goals.activities.habits.edit.views.TargetPanel;
import com.creatoro.goals.commands.CommandRunner;
import com.creatoro.goals.models.Frequency;
import com.creatoro.goals.models.Habit;
import com.creatoro.goals.models.HabitList;
import com.creatoro.goals.models.ModelFactory;
import com.creatoro.goals.models.WeekdayList;
import com.creatoro.goals.preferences.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;

public class EditHabitDialog extends AppCompatDialogFragment
{
    public static final String BUNDLE_HABIT_ID = "habitId";

    public static final String BUNDLE_HABIT_TYPE = "habitType";

    protected Habit originalHabit;

    protected Preferences prefs;

    protected CommandRunner commandRunner;

    protected HabitList habitList;

    protected AppComponent component;

    protected ModelFactory modelFactory;

    @BindView(R.id.namePanel)
    NameDescriptionPanel namePanel;

    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.reminderPanel)
    ReminderPanel reminderPanel;

    @BindView(R.id.frequencyPanel)
    FrequencyPanel frequencyPanel;

    @BindView(R.id.targetPanel)
    TargetPanel targetPanel;

    private ColorPickerDialogFactory colorPickerDialogFactory;

    @Override
    public int getTheme()
    {
        return R.style.DialogWithTitle;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        colorPickerDialogFactory =
            activity.getComponent().getColorPickerDialogFactory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view;
        view = inflater.inflate(R.layout.edit_habit, container, false);

        initDependencies();
        ButterKnife.bind(this, view);
        originalHabit = parseHabitFromArguments();
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (originalHabit != null){
            tvTitle.setText(R.string.edit_habit);
//            getDialog().setTitle(R.string.edit_habit);
        }else {
            tvTitle.setText(R.string.create_habit);
//            getDialog().setTitle(R.string.create_habit);

        }
//        getDialog().setTitleColor("#");
//        if(getTheme()==THEME_DARK){
//            tvTitle.setTextColor(#999da1);
//        }
//        getTitle();
        populateForm();
        setupReminderController();
        setupNameController();

        return view;
    }

//    protected getTitle()
//    {
//        if (originalHabit != null)
////            tvTitle.setTextColor(@color/grey_500);
//             return R.string.edit_habit;
////        tvTitle.setTextColor();
//        else
//            return R.string.create_habit;
//    }

    protected void saveHabit(@NonNull Habit habit)
    {
        if (originalHabit == null)
        {
            commandRunner.execute(component
                .getCreateHabitCommandFactory()
                .create(habitList, habit), null);
        }
        else
        {
            commandRunner.execute(component.getEditHabitCommandFactory().
                create(habitList, originalHabit, habit), originalHabit.getId());
        }
    }

    private int getTypeFromArguments()
    {
        return getArguments().getInt(BUNDLE_HABIT_TYPE);
    }

    private void initDependencies()
    {
        Context appContext = getContext().getApplicationContext();
        HabitsApplication app = (HabitsApplication) appContext;

        component = app.getComponent();
        prefs = component.getPreferences();
        habitList = component.getHabitList();
        commandRunner = component.getCommandRunner();
        modelFactory = component.getModelFactory();
    }

    @OnClick(R.id.buttonDiscard)
    void onButtonDiscardClick()
    {
        dismiss();
    }

    @OnClick(R.id.buttonSave)
    void onSaveButtonClick()
    {
        int type = getTypeFromArguments();

        if (!namePanel.validate()) return;
        if (type == Habit.YES_NO_HABIT && !frequencyPanel.validate()) return;
        if (type == Habit.NUMBER_HABIT && !targetPanel.validate()) return;

        Habit habit = modelFactory.buildHabit();
        habit.setName(namePanel.getName());
        habit.setDescription(namePanel.getDescription());
        habit.setColor(namePanel.getColor());
        habit.setReminder(reminderPanel.getReminder());
        habit.setFrequency(frequencyPanel.getFrequency());
        habit.setUnit(targetPanel.getUnit());
        habit.setTargetValue(targetPanel.getTargetValue());
        habit.setType(type);

        saveHabit(habit);
        dismiss();
    }

    @Nullable
    private Habit parseHabitFromArguments()
    {
        Bundle arguments = getArguments();
        if (arguments == null) return null;

        Long id = (Long) arguments.get(BUNDLE_HABIT_ID);
        if (id == null) return null;

        Habit habit = habitList.getById(id);
        if (habit == null) throw new IllegalStateException();

        return habit;
    }

    private void populateForm()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setFrequency(Frequency.DAILY);
        habit.setColor(prefs.getDefaultHabitColor(habit.getColor()));
        habit.setType(getTypeFromArguments());

        if (originalHabit != null) habit.copyFrom(originalHabit);

        if (habit.isNumerical()) frequencyPanel.setVisibility(GONE);
        else targetPanel.setVisibility(GONE);

        namePanel.populateFrom(habit);
        frequencyPanel.setFrequency(habit.getFrequency());
        targetPanel.setTargetValue(habit.getTargetValue());
        targetPanel.setUnit(habit.getUnit());
        if (habit.hasReminder()) reminderPanel.setReminder(habit.getReminder());
    }

    private void setupNameController()
    {
        namePanel.setController(new NameDescriptionPanel.Controller()
        {
            @Override
            public void onColorPickerClicked(int previousColor)
            {
                ColorPickerDialog picker =
                    colorPickerDialogFactory.create(previousColor);

                picker.setListener(c ->
                {
                    prefs.setDefaultHabitColor(c);
                    namePanel.setColor(c);
                });

                picker.show(getFragmentManager(), "picker");
            }
        });
    }

    private void setupReminderController()
    {
        reminderPanel.setController(new ReminderPanel.Controller()
        {
            @Override
            public void onTimeClicked(int currentHour, int currentMin)
            {
                TimePickerDialog timePicker;
                boolean is24HourMode = DateFormat.is24HourFormat(getContext());
                timePicker =
                    TimePickerDialog.newInstance(reminderPanel, currentHour,
                        currentMin, is24HourMode);
                timePicker.show(getFragmentManager(), "timePicker");
            }

            @Override
            public void onWeekdayClicked(WeekdayList currentDays)
            {
                WeekdayPickerDialog dialog = new WeekdayPickerDialog();
                dialog.setListener(reminderPanel);
                dialog.setSelectedDays(currentDays);
                dialog.show(getFragmentManager(), "weekdayPicker");
            }
        });
    }
}
