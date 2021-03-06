package com.google.android.exoplayer2.source.dash.manifest;

import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class Period {
    public final List<AdaptationSet> adaptationSets;
    public final List<EventStream> eventStreams;
    @Nullable
    public final String id;
    public final long startMs;

    public Period(@Nullable String id, long startMs, List<AdaptationSet> adaptationSets) {
        this(id, startMs, adaptationSets, Collections.emptyList());
    }

    public Period(@Nullable String id, long startMs, List<AdaptationSet> adaptationSets, List<EventStream> eventStreams) {
        this.id = id;
        this.startMs = startMs;
        this.adaptationSets = Collections.unmodifiableList(adaptationSets);
        this.eventStreams = Collections.unmodifiableList(eventStreams);
    }

    public int getAdaptationSetIndex(int type) {
        int adaptationCount = this.adaptationSets.size();
        for (int i = 0; i < adaptationCount; i++) {
            if (((AdaptationSet) this.adaptationSets.get(i)).type == type) {
                return i;
            }
        }
        return -1;
    }
}
