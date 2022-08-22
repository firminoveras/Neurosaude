package com.firmino.neurossaude;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firmino.neurossaude.databinding.ActivityMainBinding;
import com.firmino.neurossaude.views.WeekView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());


        for (int i = 0; i < binding.mWeekViewList.getChildCount(); i++) {
            ((WeekView) binding.mWeekViewList.getChildAt(i)).setOnButtonsVisibleChangeListener((view) -> {
                for (int j = 0; j < binding.mWeekViewList.getChildCount(); j++) {
                    if (binding.mWeekViewList.getChildAt(j) != view) {
                        ((WeekView) binding.mWeekViewList.getChildAt(j)).setButtonsVisible(false);
                    }
                }
            });
        }


    }

    @Override
    protected void onResume() {
        for (int i = 0; i < binding.mWeekViewList.getChildCount(); i++)
            ((WeekView) binding.mWeekViewList.getChildAt(i)).update();
        super.onResume();
    }
}