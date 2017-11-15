package com.example.fangy.videoplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fangy on 2017/11/13.
 */

public class MPD {
    long mediaPresentationDurationUs;
    List<Representation> representation;

    MPD() {
        representation = new ArrayList<Representation>();
    }

    public long getMediaPresentationDurationUs() {
        return mediaPresentationDurationUs;
    }

    public List<Representation> getRepresentation() {
        return representation;
    }

    @Override
    public String toString() {
        return "MPD{" +
                "mediaPresentationDurationUs=" + mediaPresentationDurationUs +
                //", representations=" + representations +
                '}';
    }
}
