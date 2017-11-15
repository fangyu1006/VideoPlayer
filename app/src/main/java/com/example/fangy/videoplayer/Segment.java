package com.example.fangy.videoplayer;

/**
 * Created by fangy on 2017/11/13.
 */

class Segment {
    String media;

    Segment() {
    }

    Segment(String media) {
        this.media = media;
    }

    public String getMedia() {
        return media;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "media='" + media + '\'' +
                '}';
    }
}
