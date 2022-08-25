package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firmino.neurossaude.databinding.ActivityMainBinding;
import com.firmino.neurossaude.user.User;
import com.firmino.neurossaude.views.WeekView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        ((ImageView) findViewById(R.id.Main_Bar_UserImage)).setImageBitmap(User.image);
        ((TextView) findViewById(R.id.Main_Bar_UserName)).setText(User.username);
        ((ImageView) findViewById(R.id.Main_Bar_Background)).startAnimation(AnimationUtils.loadAnimation(this,R.anim.background_rotatoring_zoom));

        ViewGroup.LayoutParams lay = binding.MainBar.getLayoutParams();
        lay.height = (int) getResources().getDisplayMetrics().heightPixels;
        binding.MainBar.setLayoutParams(lay);
        findViewById(R.id.Main_ProgressLayout).setVisibility(View.GONE);
        new Handler().postDelayed(() -> runOnUiThread(() -> {
            findViewById(R.id.Main_ProgressLayout).setVisibility(View.VISIBLE);
            ValueAnimator anim = ValueAnimator.ofInt(getResources().getDisplayMetrics().heightPixels, (int) getResources().getDimension(R.dimen.app_bar_height));
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = binding.MainBar.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                binding.MainBar.setLayoutParams(params);
            });
            anim.setDuration(1000);
            anim.start();
        }), 3000);


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

    public void updateAllWeekViews() {
        for (int i = 0; i < binding.mWeekViewList.getChildCount(); i++)
            ((WeekView) binding.mWeekViewList.getChildAt(i)).update();
    }


    @Override
    protected void onResume() {
        updateAllWeekViews();
        super.onResume();
    }
}