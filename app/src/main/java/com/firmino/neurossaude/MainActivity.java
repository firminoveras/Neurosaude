package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.firmino.neurossaude.databinding.ActivityMainBinding;
import com.firmino.neurossaude.user.User;
import com.firmino.neurossaude.views.WeekView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isReady = false;
    private ActivityResultLauncher<Intent> mediaActivityResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ((ImageView) findViewById(R.id.Main_Bar_UserImage)).setImageBitmap(User.image);
        ((TextView) findViewById(R.id.Main_Bar_UserName)).setText(User.username);
        ((ImageView) findViewById(R.id.Main_Bar_Background)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.background_rotatoring_zoom));

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

        mediaActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null) {
                System.out.println("bora ver");
                int progressResult = result.getData().getIntExtra("progress", -1);
                int week = result.getData().getIntExtra("week", -1);
                int audioIndex = result.getData().getIntExtra("audioIndex", -1);
                long positionResult = result.getData().getLongExtra("position", -1);
                switch (result.getData().getIntExtra("mediaType", -1)) {
                    case PlayActivity.MEDIA_TYPE_TEXT:
                        User.setTextValues(week, progressResult, positionResult, this::update);
                        break;
                    case PlayActivity.MEDIA_TYPE_VIDEO:
                        User.setVideoValues(week, progressResult, positionResult, this::update);
                        break;
                    case PlayActivity.MEDIA_TYPE_AUDIO:
                        User.setAudioValues(week, audioIndex, progressResult, positionResult, this::update);
                        break;
                }
            }
        });

        loadWeekViews();

    }

    private void update() {
        if (binding.mWeekViewList.getChildCount()>0) {
            float progress = 0;
            for (int i = 0; i < binding.mWeekViewList.getChildCount(); i++) {
                ((WeekView) binding.mWeekViewList.getChildAt(i)).update();
                progress += ((WeekView) binding.mWeekViewList.getChildAt(i)).getProgress();
            }
            //TODO: todos os upgrades são assincronos
            ((TextView)findViewById(R.id.Main_Bar_Progress)).setText(String.valueOf(progress/binding.mWeekViewList.getChildCount()));
        }
    }

    public void loadWeekViews() {
        isReady = false;
        binding.mWeekViewList.removeAllViews();
        User.getTexts((title, videoTitle, videoText, audioTitle, audioText, textTitle, textText) -> {

            for (int i = 0; i < title.size(); i++) {
                WeekView newWeek = new WeekView(this, i + 1, title.get(i), videoTitle.get(i), videoText.get(i), audioTitle.get(i), audioText.get(i), textTitle.get(i), textText.get(i));
                newWeek.setOnCoinClicked(intent -> mediaActivityResult.launch(intent));
                binding.mWeekViewList.addView(newWeek);
            }

            update();
            isReady = true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }


}