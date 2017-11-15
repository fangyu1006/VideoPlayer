package com.example.fangy.videoplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fangy on 2017/11/13.
 */

public class Representation {
    String id;
    int bandwidth;
    String basedUrl;
    long segmentDurationUs;
    List<Segment> segments;

    Representation() {
        segments = new ArrayList<Segment>();
    }
    public String getId() {
        return id;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public long getSegmentDurationUs() {
        return segmentDurationUs;
    }

    public String getBasedUrl() {
        return basedUrl;
    }

    public Segment getInitSegment() { return segments.get(0);}

    Segment getLastSegment() {
        return segments.get(segments.size() - 1);
    }

    @Override
    public String toString() {
        return "Representation{" +
                "id=" + id +
                ", bandwidth=" + bandwidth +
                //", initSegment=" + initSegment +
                ", segmentDurationUs=" + segmentDurationUs +
                ", basedUrl=" + basedUrl +
                //", segments=" + segments +
                '}';
    }
}
