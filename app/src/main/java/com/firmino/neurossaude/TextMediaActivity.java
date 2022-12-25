package com.firmino.neurossaude;

import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.firmino.neurossaude.mediaactivity.MediaActivity;
import com.firmino.neurossaude.views.MediaControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;

import io.noties.markwon.Markwon;

public class TextMediaActivity extends MediaActivity {

    private Markwon mText;
    private TextView mTextView;
    private MediaControl mMediaControl;
    private String[] textParts;
    private int textActualPage;
    private boolean isHighContrast = false;
    private int textSizeSp = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_media);

        mTextView = findViewById(R.id.Text_TextViewer);
        mMediaControl = findViewById(R.id.Text_MediaControl);

        ((TextView) findViewById(R.id.Text_Week)).setText(extrasTitle);
        ((TextView) findViewById(R.id.Text_Title)).setText(extraSubtitle);
        ((TextView) findViewById(R.id.Text_Description)).setText(extraDesciption);

        textSizeSp = getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).getInt("textSizeSp",14);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);

        mMediaControl.setOnClickMediaControlButton(new MediaControl.OnClickMediaControlButton() {
            @Override
            public void onMediaControl1Clicked(TextView view) {
                setTextPage(textActualPage - 1);
            }

            @Override
            public void onMediaControl2Clicked(TextView view) {
                setTextPage(textActualPage - 1);
            }

            @Override
            public void onMediaControl3Clicked(TextView view) {
                setTextPage(textActualPage + 1);
            }

            @Override
            public void onMediaControl4Clicked(TextView view) {
                setTextPage(textActualPage + 1);
            }

            @Override
            public void onMediaControl5Clicked(TextView view) {
                textSizeSp = 14;
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
                getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putInt("textSizeSp",textSizeSp).apply();
            }

            @Override
            public void onMediaControl6Clicked(TextView view) {
                if (textSizeSp > 8) {
                    textSizeSp -= 1;
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
                    getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putInt("textSizeSp",textSizeSp).apply();
                }
            }

            @Override
            public void onMediaControl7Clicked(TextView view) {
                if (textSizeSp < 32) {
                    textSizeSp += 1;
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
                    getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putInt("textSizeSp",textSizeSp).apply();
                }
            }

            @Override
            public void onMediaControl8Clicked(TextView view) {
                isHighContrast = !isHighContrast;
                mTextView.setTextColor(isHighContrast ? Color.BLACK : Color.WHITE);
            }

            @Override
            public void onMediaControl9Clicked(TextView view) {
                onBackPressed();
            }
        });
    }

    private void setTextPage(int page) {
        if (page >= 0 && page < textParts.length && isLoaded) {
            textActualPage = page;
            mText.setMarkdown(findViewById(R.id.Text_TextViewer), textParts[page]);
            ((TextView) findViewById(R.id.Text_PageActual)).setText(String.valueOf(page + 1));
            ((TextView) findViewById(R.id.Text_PageTotal)).setText(MessageFormat.format("/{0}", textParts.length));
            mMediaControl.setProgress(getProgress() * 10);
            if (getProgress() > extraLastProgress) extraLastProgress = getProgress();
        }
    }

    @Override
    public void loadMediaComplete(Uri downloadLink) {
        try {
            StringBuilder text = new StringBuilder();
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(downloadLink.toString()).openStream()));
            while ((line = in.readLine()) != null) text.append(line).append('\n');
            in.close();
            textParts = text.toString().split("___");
            runOnUiThread(() -> {
                mText = Markwon.create(this);
                mText.setMarkdown(findViewById(R.id.Text_TextViewer), textParts[0]);
                mTextView.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.Text_LoadingText)).setVisibility(View.GONE);
                isLoaded = true;
                setTextPage((int) extraLastPosition);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getProgress() {
        return Math.round(((float) textActualPage / ((float) textParts.length - 1)) * 100);
    }

    @Override
    public long getPosition() {
        return textActualPage;
    }

    @Override
    public void setControlsVisibleComplete(boolean visible) {
        mMediaControl.setControlsVisible(visible);
    }

    @Override
    public void onFinishMediaActivity() {
        mTextView.setVisibility(View.INVISIBLE);
        findViewById(R.id.Text_LoadingText).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.Text_LoadingText)).setText(R.string.saving);
    }

    @Override
    public void onScreenOrientationChange(Configuration newConfig) {
        findViewById(R.id.Text_SpaceLayout).setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
        findViewById(R.id.Text_TitleLayout).setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
    }

}