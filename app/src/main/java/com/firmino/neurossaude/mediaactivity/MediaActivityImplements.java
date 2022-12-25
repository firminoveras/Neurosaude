package com.firmino.neurossaude.mediaactivity;

import android.content.res.Configuration;
import android.net.Uri;

public interface MediaActivityImplements {
    void loadMediaComplete(Uri downloadLink);
    int getProgress();
    long getPosition();
    void setControlsVisibleComplete(boolean visible);
    void onFinishMediaActivity();
    void onScreenOrientationChange(Configuration newConfig);

}
