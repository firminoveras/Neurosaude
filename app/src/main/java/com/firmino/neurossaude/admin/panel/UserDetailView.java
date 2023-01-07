package com.firmino.neurossaude.admin.panel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firmino.neurossaude.R;
import com.firmino.neurossaude.admin.users.Profile;
import com.firmino.neurossaude.admin.users.Progress;
import com.firmino.neurossaude.user.User;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class UserDetailView extends LinearLayout {
    private final Context mContext;
    private Profile mProfile;
    private OnUserDetailsViewClick onUserDetailsViewClick = () -> {

    };

    public UserDetailView(Context context) {
        super(context);
        mContext = context;
        inflate(mContext, R.layout.user_detail_layout, this);
    }

    public UserDetailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        inflate(mContext, R.layout.user_detail_layout, this);
    }

    public void setup(Profile profile) {
        mProfile = profile;
        ((TextView) findViewById(R.id.Details_Email)).setText(profile.email);
        ((TextView) findViewById(R.id.Details_Username)).setText(profile.username);

        AsyncLoadImage(profile.image);
        AsyncLoadProgress(profile.getProgressWeeks());
        findViewById(R.id.Details_Main).setOnClickListener(v -> onUserDetailsViewClick.onUserDetailsViewClick());


    }

    private void AsyncLoadProgress(List<Progress> progresses) {
        User.getWeekViews(mContext, weekViews -> {
            float total = weekViews.size();
            float progress = 0;

            for(Progress p : progresses) {
                progress+=p.getProgressPercent();
            }
            ((ProgressBar)findViewById(R.id.Details_Progress_Bar)).setProgress((int) (progress/total));
            ((TextView)findViewById(R.id.Details_Progress_Text)).setText(String.format(Locale.getDefault(), "%d %%", (int) (progress / total)));
        });
    }


    public boolean isChecked() {
        return ((CheckBox) findViewById(R.id.Details_Checkbox)).isChecked();
    }

    public void setChecked(boolean isChecked) {
        ((CheckBox) findViewById(R.id.Details_Checkbox)).setChecked(isChecked);
    }

    private void AsyncLoadImage(String imageUrl) {
        if (imageUrl != null) {
            new Thread(() -> {
                Bitmap image;
                try {
                    URL newurl = new URL(Uri.parse(imageUrl).toString());
                    image = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                } catch (IOException e) {
                    image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_google_logged);
                    ((ImageView) findViewById(R.id.Details_Image)).setImageBitmap(image);
                }

                Bitmap finalImage = image;
                ((AppCompatActivity) mContext).runOnUiThread(() -> ((ImageView) findViewById(R.id.Details_Image)).setImageBitmap(finalImage));

            }).start();

        } else {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_google_logged);
            ((ImageView) findViewById(R.id.Details_Image)).setImageBitmap(image);
        }

    }

    public void setOnUserDetailsViewClick(OnUserDetailsViewClick onUserDetailsViewClick) {
        this.onUserDetailsViewClick = onUserDetailsViewClick;
    }

    public Profile getProfile() {
        return mProfile;
    }

    interface OnUserDetailsViewClick{
        void onUserDetailsViewClick();
    }
}
