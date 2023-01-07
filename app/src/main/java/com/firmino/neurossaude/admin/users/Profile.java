package com.firmino.neurossaude.admin.users;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    public final String username;
    public final String image;
    public final String email;

    public final List<Progress> weeks = new ArrayList<>();


    public Profile(String username, String image, String email) {
        this.username = username;
        this.image = image;
        this.email = email;
    }

    public List<Progress> getProgressWeeks(){
        return weeks;
    }

    public int getProgress(int weekCount){
        int progress=0;
        for(Progress p : weeks){
            progress += p.getProgressPercent();
        }
        return progress /weekCount;
    }

    public int getTimeInMillis(){
        int time=0;
        for(Progress p : weeks){
            time += p.millisOnAudio1Media;
            time += p.millisOnAudio2Media;
            time += p.millisOnAudio3Media;
            time += p.millisOnAudio4Media;
            time += p.millisOnAudio5Media;
            time += p.millisOnAudio6Media;
            time += p.millisOnTextMedia;
            time += p.millisOnVideoMedia;
        }
        return time;
    }

    public void addWeekProgress(Progress progress) {
        weeks.add(progress);
    }
}

