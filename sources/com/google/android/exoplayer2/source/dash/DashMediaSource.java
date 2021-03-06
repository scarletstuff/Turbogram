package com.google.android.exoplayer2.source.dash;

import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.exoplayer2.C0246C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Timeline.Period;
import com.google.android.exoplayer2.Timeline.Window;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.CompositeSequenceableLoaderFactory;
import com.google.android.exoplayer2.source.DefaultCompositeSequenceableLoaderFactory;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource.MediaPeriodId;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.MediaSourceEventListener.EventDispatcher;
import com.google.android.exoplayer2.source.ads.AdsMediaSource.MediaSourceFactory;
import com.google.android.exoplayer2.source.dash.PlayerEmsgHandler.PlayerEmsgCallback;
import com.google.android.exoplayer2.source.dash.manifest.AdaptationSet;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.Representation;
import com.google.android.exoplayer2.source.dash.manifest.UtcTimingElement;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.Loader;
import com.google.android.exoplayer2.upstream.Loader.Callback;
import com.google.android.exoplayer2.upstream.Loader.LoadErrorAction;
import com.google.android.exoplayer2.upstream.LoaderErrorThrower;
import com.google.android.exoplayer2.upstream.LoaderErrorThrower.Dummy;
import com.google.android.exoplayer2.upstream.ParsingLoadable;
import com.google.android.exoplayer2.upstream.ParsingLoadable.Parser;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DashMediaSource extends BaseMediaSource {
    @Deprecated
    public static final long DEFAULT_LIVE_PRESENTATION_DELAY_FIXED_MS = 30000;
    public static final long DEFAULT_LIVE_PRESENTATION_DELAY_MS = 30000;
    @Deprecated
    public static final long DEFAULT_LIVE_PRESENTATION_DELAY_PREFER_MANIFEST_MS = -1;
    public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT = 3;
    private static final long MIN_LIVE_DEFAULT_START_POSITION_US = 5000000;
    private static final int NOTIFY_MANIFEST_INTERVAL_MS = 5000;
    private static final String TAG = "DashMediaSource";
    private final com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory;
    private final CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory;
    private DataSource dataSource;
    private boolean dynamicMediaPresentationEnded;
    private long elapsedRealtimeOffsetMs;
    private long expiredManifestPublishTimeUs;
    private int firstPeriodId;
    private Handler handler;
    private Uri initialManifestUri;
    private final long livePresentationDelayMs;
    private final boolean livePresentationDelayOverridesManifest;
    private Loader loader;
    private DashManifest manifest;
    private final ManifestCallback manifestCallback;
    private final com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory;
    private final EventDispatcher manifestEventDispatcher;
    private IOException manifestFatalError;
    private long manifestLoadEndTimestampMs;
    private final LoaderErrorThrower manifestLoadErrorThrower;
    private boolean manifestLoadPending;
    private long manifestLoadStartTimestampMs;
    private final Parser<? extends DashManifest> manifestParser;
    private Uri manifestUri;
    private final Object manifestUriLock;
    @Nullable
    private TransferListener mediaTransferListener;
    private final int minLoadableRetryCount;
    private final SparseArray<DashMediaPeriod> periodsById;
    private final PlayerEmsgCallback playerEmsgCallback;
    private final Runnable refreshManifestRunnable;
    private final boolean sideloadedManifest;
    private final Runnable simulateManifestRefreshRunnable;
    private int staleManifestReloadAttempt;
    @Nullable
    private final Object tag;

    /* renamed from: com.google.android.exoplayer2.source.dash.DashMediaSource$1 */
    class C03331 implements Runnable {
        C03331() {
        }

        public void run() {
            DashMediaSource.this.startLoadingManifest();
        }
    }

    /* renamed from: com.google.android.exoplayer2.source.dash.DashMediaSource$2 */
    class C03342 implements Runnable {
        C03342() {
        }

        public void run() {
            DashMediaSource.this.processManifest(false);
        }
    }

    private static final class DashTimeline extends Timeline {
        private final int firstPeriodId;
        private final DashManifest manifest;
        private final long offsetInFirstPeriodUs;
        private final long presentationStartTimeMs;
        private final long windowDefaultStartPositionUs;
        private final long windowDurationUs;
        private final long windowStartTimeMs;
        @Nullable
        private final Object windowTag;

        public DashTimeline(long presentationStartTimeMs, long windowStartTimeMs, int firstPeriodId, long offsetInFirstPeriodUs, long windowDurationUs, long windowDefaultStartPositionUs, DashManifest manifest, @Nullable Object windowTag) {
            this.presentationStartTimeMs = presentationStartTimeMs;
            this.windowStartTimeMs = windowStartTimeMs;
            this.firstPeriodId = firstPeriodId;
            this.offsetInFirstPeriodUs = offsetInFirstPeriodUs;
            this.windowDurationUs = windowDurationUs;
            this.windowDefaultStartPositionUs = windowDefaultStartPositionUs;
            this.manifest = manifest;
            this.windowTag = windowTag;
        }

        public int getPeriodCount() {
            return this.manifest.getPeriodCount();
        }

        public Period getPeriod(int periodIndex, Period period, boolean setIdentifiers) {
            String id;
            Integer uid = null;
            Assertions.checkIndex(periodIndex, 0, getPeriodCount());
            if (setIdentifiers) {
                id = this.manifest.getPeriod(periodIndex).id;
            } else {
                id = null;
            }
            if (setIdentifiers) {
                uid = Integer.valueOf(this.firstPeriodId + periodIndex);
            }
            return period.set(id, uid, 0, this.manifest.getPeriodDurationUs(periodIndex), C0246C.msToUs(this.manifest.getPeriod(periodIndex).startMs - this.manifest.getPeriod(0).startMs) - this.offsetInFirstPeriodUs);
        }

        public int getWindowCount() {
            return 1;
        }

        public Window getWindow(int windowIndex, Window window, boolean setTag, long defaultPositionProjectionUs) {
            Assertions.checkIndex(windowIndex, 0, 1);
            return window.set(setTag ? this.windowTag : null, this.presentationStartTimeMs, this.windowStartTimeMs, true, this.manifest.dynamic, getAdjustedWindowDefaultStartPositionUs(defaultPositionProjectionUs), this.windowDurationUs, 0, getPeriodCount() - 1, this.offsetInFirstPeriodUs);
        }

        public int getIndexOfPeriod(Object uid) {
            if (!(uid instanceof Integer)) {
                return -1;
            }
            int periodIndex = ((Integer) uid).intValue() - this.firstPeriodId;
            if (periodIndex < 0 || periodIndex >= getPeriodCount()) {
                periodIndex = -1;
            }
            return periodIndex;
        }

        private long getAdjustedWindowDefaultStartPositionUs(long defaultPositionProjectionUs) {
            long windowDefaultStartPositionUs = this.windowDefaultStartPositionUs;
            if (this.manifest.dynamic) {
                long j;
                if (defaultPositionProjectionUs > 0) {
                    windowDefaultStartPositionUs += defaultPositionProjectionUs;
                    if (windowDefaultStartPositionUs > this.windowDurationUs) {
                        j = windowDefaultStartPositionUs;
                        return C0246C.TIME_UNSET;
                    }
                }
                int periodIndex = 0;
                long defaultStartPositionInPeriodUs = this.offsetInFirstPeriodUs + windowDefaultStartPositionUs;
                long periodDurationUs = this.manifest.getPeriodDurationUs(0);
                while (periodIndex < this.manifest.getPeriodCount() - 1 && defaultStartPositionInPeriodUs >= periodDurationUs) {
                    defaultStartPositionInPeriodUs -= periodDurationUs;
                    periodIndex++;
                    periodDurationUs = this.manifest.getPeriodDurationUs(periodIndex);
                }
                com.google.android.exoplayer2.source.dash.manifest.Period period = this.manifest.getPeriod(periodIndex);
                int videoAdaptationSetIndex = period.getAdaptationSetIndex(2);
                if (videoAdaptationSetIndex == -1) {
                    j = windowDefaultStartPositionUs;
                    return windowDefaultStartPositionUs;
                }
                DashSegmentIndex snapIndex = ((Representation) ((AdaptationSet) period.adaptationSets.get(videoAdaptationSetIndex)).representations.get(0)).getIndex();
                if (snapIndex == null || snapIndex.getSegmentCount(periodDurationUs) == 0) {
                    j = windowDefaultStartPositionUs;
                    return windowDefaultStartPositionUs;
                }
                j = windowDefaultStartPositionUs;
                return (snapIndex.getTimeUs(snapIndex.getSegmentNum(defaultStartPositionInPeriodUs, periodDurationUs)) + windowDefaultStartPositionUs) - defaultStartPositionInPeriodUs;
            }
            return windowDefaultStartPositionUs;
        }

        public Object getUidOfPeriod(int periodIndex) {
            Assertions.checkIndex(periodIndex, 0, getPeriodCount());
            return Integer.valueOf(this.firstPeriodId + periodIndex);
        }
    }

    private final class DefaultPlayerEmsgCallback implements PlayerEmsgCallback {
        private DefaultPlayerEmsgCallback() {
        }

        public void onDashManifestRefreshRequested() {
            DashMediaSource.this.onDashManifestRefreshRequested();
        }

        public void onDashManifestPublishTimeExpired(long expiredManifestPublishTimeUs) {
            DashMediaSource.this.onDashManifestPublishTimeExpired(expiredManifestPublishTimeUs);
        }

        public void onDashLiveMediaPresentationEndSignalEncountered() {
            DashMediaSource.this.onDashLiveMediaPresentationEndSignalEncountered();
        }
    }

    public static final class Factory implements MediaSourceFactory {
        private final com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory;
        private CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory;
        private boolean isCreateCalled;
        private long livePresentationDelayMs;
        private boolean livePresentationDelayOverridesManifest;
        @Nullable
        private final com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory;
        @Nullable
        private Parser<? extends DashManifest> manifestParser;
        private int minLoadableRetryCount;
        @Nullable
        private Object tag;

        public Factory(com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory) {
            this(new com.google.android.exoplayer2.source.dash.DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory);
        }

        public Factory(com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, @Nullable com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory) {
            this.chunkSourceFactory = (com.google.android.exoplayer2.source.dash.DashChunkSource.Factory) Assertions.checkNotNull(chunkSourceFactory);
            this.manifestDataSourceFactory = manifestDataSourceFactory;
            this.minLoadableRetryCount = 3;
            this.livePresentationDelayMs = 30000;
            this.compositeSequenceableLoaderFactory = new DefaultCompositeSequenceableLoaderFactory();
        }

        public Factory setTag(Object tag) {
            Assertions.checkState(!this.isCreateCalled);
            this.tag = tag;
            return this;
        }

        public Factory setMinLoadableRetryCount(int minLoadableRetryCount) {
            Assertions.checkState(!this.isCreateCalled);
            this.minLoadableRetryCount = minLoadableRetryCount;
            return this;
        }

        @Deprecated
        public Factory setLivePresentationDelayMs(long livePresentationDelayMs) {
            if (livePresentationDelayMs == -1) {
                return setLivePresentationDelayMs(30000, false);
            }
            return setLivePresentationDelayMs(livePresentationDelayMs, true);
        }

        public Factory setLivePresentationDelayMs(long livePresentationDelayMs, boolean overridesManifest) {
            Assertions.checkState(!this.isCreateCalled);
            this.livePresentationDelayMs = livePresentationDelayMs;
            this.livePresentationDelayOverridesManifest = overridesManifest;
            return this;
        }

        public Factory setManifestParser(Parser<? extends DashManifest> manifestParser) {
            Assertions.checkState(!this.isCreateCalled);
            this.manifestParser = (Parser) Assertions.checkNotNull(manifestParser);
            return this;
        }

        public Factory setCompositeSequenceableLoaderFactory(CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory) {
            Assertions.checkState(!this.isCreateCalled);
            this.compositeSequenceableLoaderFactory = (CompositeSequenceableLoaderFactory) Assertions.checkNotNull(compositeSequenceableLoaderFactory);
            return this;
        }

        public DashMediaSource createMediaSource(DashManifest manifest) {
            Assertions.checkArgument(!manifest.dynamic);
            this.isCreateCalled = true;
            return new DashMediaSource(manifest, null, null, null, this.chunkSourceFactory, this.compositeSequenceableLoaderFactory, this.minLoadableRetryCount, this.livePresentationDelayMs, this.livePresentationDelayOverridesManifest, this.tag);
        }

        @Deprecated
        public DashMediaSource createMediaSource(DashManifest manifest, @Nullable Handler eventHandler, @Nullable MediaSourceEventListener eventListener) {
            DashMediaSource mediaSource = createMediaSource(manifest);
            if (!(eventHandler == null || eventListener == null)) {
                mediaSource.addEventListener(eventHandler, eventListener);
            }
            return mediaSource;
        }

        public DashMediaSource createMediaSource(Uri manifestUri) {
            this.isCreateCalled = true;
            if (this.manifestParser == null) {
                this.manifestParser = new DashManifestParser();
            }
            return new DashMediaSource(null, (Uri) Assertions.checkNotNull(manifestUri), this.manifestDataSourceFactory, this.manifestParser, this.chunkSourceFactory, this.compositeSequenceableLoaderFactory, this.minLoadableRetryCount, this.livePresentationDelayMs, this.livePresentationDelayOverridesManifest, this.tag);
        }

        @Deprecated
        public DashMediaSource createMediaSource(Uri manifestUri, @Nullable Handler eventHandler, @Nullable MediaSourceEventListener eventListener) {
            DashMediaSource mediaSource = createMediaSource(manifestUri);
            if (!(eventHandler == null || eventListener == null)) {
                mediaSource.addEventListener(eventHandler, eventListener);
            }
            return mediaSource;
        }

        public int[] getSupportedTypes() {
            return new int[]{0};
        }
    }

    static final class Iso8601Parser implements Parser<Long> {
        private static final Pattern TIMESTAMP_WITH_TIMEZONE_PATTERN = Pattern.compile("(.+?)(Z|((\\+|-|−)(\\d\\d)(:?(\\d\\d))?))");

        Iso8601Parser() {
        }

        public Long parse(Uri uri, InputStream inputStream) throws IOException {
            String firstLine = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))).readLine();
            try {
                Matcher matcher = TIMESTAMP_WITH_TIMEZONE_PATTERN.matcher(firstLine);
                if (matcher.matches()) {
                    String timestampWithoutTimezone = matcher.group(1);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    long timestampMs = format.parse(timestampWithoutTimezone).getTime();
                    if (!"Z".equals(matcher.group(2))) {
                        long sign = "+".equals(matcher.group(4)) ? 1 : -1;
                        long hours = Long.parseLong(matcher.group(5));
                        String minutesString = matcher.group(7);
                        timestampMs -= sign * ((((60 * hours) + (TextUtils.isEmpty(minutesString) ? 0 : Long.parseLong(minutesString))) * 60) * 1000);
                    }
                    return Long.valueOf(timestampMs);
                }
                throw new ParserException("Couldn't parse timestamp: " + firstLine);
            } catch (Throwable e) {
                throw new ParserException(e);
            }
        }
    }

    private final class ManifestCallback implements Callback<ParsingLoadable<DashManifest>> {
        private ManifestCallback() {
        }

        public void onLoadCompleted(ParsingLoadable<DashManifest> loadable, long elapsedRealtimeMs, long loadDurationMs) {
            DashMediaSource.this.onManifestLoadCompleted(loadable, elapsedRealtimeMs, loadDurationMs);
        }

        public void onLoadCanceled(ParsingLoadable<DashManifest> loadable, long elapsedRealtimeMs, long loadDurationMs, boolean released) {
            DashMediaSource.this.onLoadCanceled(loadable, elapsedRealtimeMs, loadDurationMs);
        }

        public LoadErrorAction onLoadError(ParsingLoadable<DashManifest> loadable, long elapsedRealtimeMs, long loadDurationMs, IOException error, int errorCount) {
            return DashMediaSource.this.onManifestLoadError(loadable, elapsedRealtimeMs, loadDurationMs, error);
        }
    }

    final class ManifestLoadErrorThrower implements LoaderErrorThrower {
        ManifestLoadErrorThrower() {
        }

        public void maybeThrowError() throws IOException {
            DashMediaSource.this.loader.maybeThrowError();
            maybeThrowManifestError();
        }

        public void maybeThrowError(int minRetryCount) throws IOException {
            DashMediaSource.this.loader.maybeThrowError(minRetryCount);
            maybeThrowManifestError();
        }

        private void maybeThrowManifestError() throws IOException {
            if (DashMediaSource.this.manifestFatalError != null) {
                throw DashMediaSource.this.manifestFatalError;
            }
        }
    }

    private static final class PeriodSeekInfo {
        public final long availableEndTimeUs;
        public final long availableStartTimeUs;
        public final boolean isIndexExplicit;

        public static PeriodSeekInfo createPeriodSeekInfo(com.google.android.exoplayer2.source.dash.manifest.Period period, long durationUs) {
            int i;
            int adaptationSetCount = period.adaptationSets.size();
            long availableStartTimeUs = 0;
            long availableEndTimeUs = Long.MAX_VALUE;
            boolean isIndexExplicit = false;
            boolean seenEmptyIndex = false;
            boolean haveAudioVideoAdaptationSets = false;
            for (i = 0; i < adaptationSetCount; i++) {
                int type = ((AdaptationSet) period.adaptationSets.get(i)).type;
                if (type == 1 || type == 2) {
                    haveAudioVideoAdaptationSets = true;
                    break;
                }
            }
            for (i = 0; i < adaptationSetCount; i++) {
                AdaptationSet adaptationSet = (AdaptationSet) period.adaptationSets.get(i);
                if (!haveAudioVideoAdaptationSets || adaptationSet.type != 3) {
                    DashSegmentIndex index = ((Representation) adaptationSet.representations.get(0)).getIndex();
                    if (index == null) {
                        return new PeriodSeekInfo(true, 0, durationUs);
                    }
                    isIndexExplicit |= index.isExplicit();
                    int segmentCount = index.getSegmentCount(durationUs);
                    if (segmentCount == 0) {
                        seenEmptyIndex = true;
                        availableStartTimeUs = 0;
                        availableEndTimeUs = 0;
                    } else if (!seenEmptyIndex) {
                        long firstSegmentNum = index.getFirstSegmentNum();
                        availableStartTimeUs = Math.max(availableStartTimeUs, index.getTimeUs(firstSegmentNum));
                        if (segmentCount != -1) {
                            long lastSegmentNum = (((long) segmentCount) + firstSegmentNum) - 1;
                            availableEndTimeUs = Math.min(availableEndTimeUs, index.getTimeUs(lastSegmentNum) + index.getDurationUs(lastSegmentNum, durationUs));
                        }
                    }
                }
            }
            return new PeriodSeekInfo(isIndexExplicit, availableStartTimeUs, availableEndTimeUs);
        }

        private PeriodSeekInfo(boolean isIndexExplicit, long availableStartTimeUs, long availableEndTimeUs) {
            this.isIndexExplicit = isIndexExplicit;
            this.availableStartTimeUs = availableStartTimeUs;
            this.availableEndTimeUs = availableEndTimeUs;
        }
    }

    private final class UtcTimestampCallback implements Callback<ParsingLoadable<Long>> {
        private UtcTimestampCallback() {
        }

        public void onLoadCompleted(ParsingLoadable<Long> loadable, long elapsedRealtimeMs, long loadDurationMs) {
            DashMediaSource.this.onUtcTimestampLoadCompleted(loadable, elapsedRealtimeMs, loadDurationMs);
        }

        public void onLoadCanceled(ParsingLoadable<Long> loadable, long elapsedRealtimeMs, long loadDurationMs, boolean released) {
            DashMediaSource.this.onLoadCanceled(loadable, elapsedRealtimeMs, loadDurationMs);
        }

        public LoadErrorAction onLoadError(ParsingLoadable<Long> loadable, long elapsedRealtimeMs, long loadDurationMs, IOException error, int errorCount) {
            return DashMediaSource.this.onUtcTimestampLoadError(loadable, elapsedRealtimeMs, loadDurationMs, error);
        }
    }

    private static final class XsDateTimeParser implements Parser<Long> {
        private XsDateTimeParser() {
        }

        public Long parse(Uri uri, InputStream inputStream) throws IOException {
            return Long.valueOf(Util.parseXsDateTime(new BufferedReader(new InputStreamReader(inputStream)).readLine()));
        }
    }

    static {
        ExoPlayerLibraryInfo.registerModule("goog.exo.dash");
    }

    @Deprecated
    public DashMediaSource(DashManifest manifest, com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, Handler eventHandler, MediaSourceEventListener eventListener) {
        this(manifest, chunkSourceFactory, 3, eventHandler, eventListener);
    }

    @Deprecated
    public DashMediaSource(DashManifest manifest, com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, int minLoadableRetryCount, Handler eventHandler, MediaSourceEventListener eventListener) {
        this(manifest, null, null, null, chunkSourceFactory, new DefaultCompositeSequenceableLoaderFactory(), minLoadableRetryCount, 30000, false, null);
        if (eventHandler != null && eventListener != null) {
            addEventListener(eventHandler, eventListener);
        }
    }

    @Deprecated
    public DashMediaSource(Uri manifestUri, com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory, com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, Handler eventHandler, MediaSourceEventListener eventListener) {
        this(manifestUri, manifestDataSourceFactory, chunkSourceFactory, 3, -1, eventHandler, eventListener);
    }

    @Deprecated
    public DashMediaSource(Uri manifestUri, com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory, com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, int minLoadableRetryCount, long livePresentationDelayMs, Handler eventHandler, MediaSourceEventListener eventListener) {
        this(manifestUri, manifestDataSourceFactory, new DashManifestParser(), chunkSourceFactory, minLoadableRetryCount, livePresentationDelayMs, eventHandler, eventListener);
    }

    @Deprecated
    public DashMediaSource(Uri manifestUri, com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory, Parser<? extends DashManifest> manifestParser, com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, int minLoadableRetryCount, long livePresentationDelayMs, Handler eventHandler, MediaSourceEventListener eventListener) {
        long j;
        boolean z;
        CompositeSequenceableLoaderFactory defaultCompositeSequenceableLoaderFactory = new DefaultCompositeSequenceableLoaderFactory();
        if (livePresentationDelayMs == -1) {
            j = 30000;
        } else {
            j = livePresentationDelayMs;
        }
        if (livePresentationDelayMs != -1) {
            z = true;
        } else {
            z = false;
        }
        this(null, manifestUri, manifestDataSourceFactory, manifestParser, chunkSourceFactory, defaultCompositeSequenceableLoaderFactory, minLoadableRetryCount, j, z, null);
        if (eventHandler != null && eventListener != null) {
            addEventListener(eventHandler, eventListener);
        }
    }

    private DashMediaSource(DashManifest manifest, Uri manifestUri, com.google.android.exoplayer2.upstream.DataSource.Factory manifestDataSourceFactory, Parser<? extends DashManifest> manifestParser, com.google.android.exoplayer2.source.dash.DashChunkSource.Factory chunkSourceFactory, CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory, int minLoadableRetryCount, long livePresentationDelayMs, boolean livePresentationDelayOverridesManifest, @Nullable Object tag) {
        this.initialManifestUri = manifestUri;
        this.manifest = manifest;
        this.manifestUri = manifestUri;
        this.manifestDataSourceFactory = manifestDataSourceFactory;
        this.manifestParser = manifestParser;
        this.chunkSourceFactory = chunkSourceFactory;
        this.minLoadableRetryCount = minLoadableRetryCount;
        this.livePresentationDelayMs = livePresentationDelayMs;
        this.livePresentationDelayOverridesManifest = livePresentationDelayOverridesManifest;
        this.compositeSequenceableLoaderFactory = compositeSequenceableLoaderFactory;
        this.tag = tag;
        this.sideloadedManifest = manifest != null;
        this.manifestEventDispatcher = createEventDispatcher(null);
        this.manifestUriLock = new Object();
        this.periodsById = new SparseArray();
        this.playerEmsgCallback = new DefaultPlayerEmsgCallback();
        this.expiredManifestPublishTimeUs = C0246C.TIME_UNSET;
        if (this.sideloadedManifest) {
            Assertions.checkState(!manifest.dynamic);
            this.manifestCallback = null;
            this.refreshManifestRunnable = null;
            this.simulateManifestRefreshRunnable = null;
            this.manifestLoadErrorThrower = new Dummy();
            return;
        }
        this.manifestCallback = new ManifestCallback();
        this.manifestLoadErrorThrower = new ManifestLoadErrorThrower();
        this.refreshManifestRunnable = new C03331();
        this.simulateManifestRefreshRunnable = new C03342();
    }

    public void replaceManifestUri(Uri manifestUri) {
        synchronized (this.manifestUriLock) {
            this.manifestUri = manifestUri;
            this.initialManifestUri = manifestUri;
        }
    }

    public void prepareSourceInternal(ExoPlayer player, boolean isTopLevelSource, @Nullable TransferListener mediaTransferListener) {
        this.mediaTransferListener = mediaTransferListener;
        if (this.sideloadedManifest) {
            processManifest(false);
            return;
        }
        this.dataSource = this.manifestDataSourceFactory.createDataSource();
        this.loader = new Loader("Loader:DashMediaSource");
        this.handler = new Handler();
        startLoadingManifest();
    }

    public void maybeThrowSourceInfoRefreshError() throws IOException {
        this.manifestLoadErrorThrower.maybeThrowError();
    }

    public MediaPeriod createPeriod(MediaPeriodId periodId, Allocator allocator) {
        int periodIndex = periodId.periodIndex;
        DashMediaPeriod mediaPeriod = new DashMediaPeriod(this.firstPeriodId + periodIndex, this.manifest, periodIndex, this.chunkSourceFactory, this.mediaTransferListener, this.minLoadableRetryCount, createEventDispatcher(periodId, this.manifest.getPeriod(periodIndex).startMs), this.elapsedRealtimeOffsetMs, this.manifestLoadErrorThrower, allocator, this.compositeSequenceableLoaderFactory, this.playerEmsgCallback);
        this.periodsById.put(mediaPeriod.id, mediaPeriod);
        return mediaPeriod;
    }

    public void releasePeriod(MediaPeriod mediaPeriod) {
        DashMediaPeriod dashMediaPeriod = (DashMediaPeriod) mediaPeriod;
        dashMediaPeriod.release();
        this.periodsById.remove(dashMediaPeriod.id);
    }

    public void releaseSourceInternal() {
        this.manifestLoadPending = false;
        this.dataSource = null;
        if (this.loader != null) {
            this.loader.release();
            this.loader = null;
        }
        this.manifestLoadStartTimestampMs = 0;
        this.manifestLoadEndTimestampMs = 0;
        this.manifest = this.sideloadedManifest ? this.manifest : null;
        this.manifestUri = this.initialManifestUri;
        this.manifestFatalError = null;
        if (this.handler != null) {
            this.handler.removeCallbacksAndMessages(null);
            this.handler = null;
        }
        this.elapsedRealtimeOffsetMs = 0;
        this.staleManifestReloadAttempt = 0;
        this.expiredManifestPublishTimeUs = C0246C.TIME_UNSET;
        this.dynamicMediaPresentationEnded = false;
        this.firstPeriodId = 0;
        this.periodsById.clear();
    }

    void onDashManifestRefreshRequested() {
        this.handler.removeCallbacks(this.simulateManifestRefreshRunnable);
        startLoadingManifest();
    }

    void onDashLiveMediaPresentationEndSignalEncountered() {
        this.dynamicMediaPresentationEnded = true;
    }

    void onDashManifestPublishTimeExpired(long expiredManifestPublishTimeUs) {
        if (this.expiredManifestPublishTimeUs == C0246C.TIME_UNSET || this.expiredManifestPublishTimeUs < expiredManifestPublishTimeUs) {
            this.expiredManifestPublishTimeUs = expiredManifestPublishTimeUs;
        }
    }

    void onManifestLoadCompleted(ParsingLoadable<DashManifest> loadable, long elapsedRealtimeMs, long loadDurationMs) {
        this.manifestEventDispatcher.loadCompleted(loadable.dataSpec, loadable.getUri(), loadable.type, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded());
        DashManifest newManifest = (DashManifest) loadable.getResult();
        int periodCount = this.manifest == null ? 0 : this.manifest.getPeriodCount();
        int removedPeriodCount = 0;
        long newFirstPeriodStartTimeMs = newManifest.getPeriod(0).startMs;
        while (removedPeriodCount < periodCount && this.manifest.getPeriod(removedPeriodCount).startMs < newFirstPeriodStartTimeMs) {
            removedPeriodCount++;
        }
        if (newManifest.dynamic) {
            boolean isManifestStale = false;
            if (periodCount - removedPeriodCount > newManifest.getPeriodCount()) {
                Log.w(TAG, "Loaded out of sync manifest");
                isManifestStale = true;
            } else if (this.dynamicMediaPresentationEnded || (this.expiredManifestPublishTimeUs != C0246C.TIME_UNSET && newManifest.publishTimeMs * 1000 <= this.expiredManifestPublishTimeUs)) {
                Log.w(TAG, "Loaded stale dynamic manifest: " + newManifest.publishTimeMs + ", " + this.dynamicMediaPresentationEnded + ", " + this.expiredManifestPublishTimeUs);
                isManifestStale = true;
            }
            if (isManifestStale) {
                int i = this.staleManifestReloadAttempt;
                this.staleManifestReloadAttempt = i + 1;
                if (i < this.minLoadableRetryCount) {
                    scheduleManifestRefresh(getManifestLoadRetryDelayMillis());
                    return;
                } else {
                    this.manifestFatalError = new DashManifestStaleException();
                    return;
                }
            }
            this.staleManifestReloadAttempt = 0;
        }
        this.manifest = newManifest;
        this.manifestLoadPending &= this.manifest.dynamic;
        this.manifestLoadStartTimestampMs = elapsedRealtimeMs - loadDurationMs;
        this.manifestLoadEndTimestampMs = elapsedRealtimeMs;
        if (this.manifest.location != null) {
            synchronized (this.manifestUriLock) {
                if (loadable.dataSpec.uri == this.manifestUri) {
                    this.manifestUri = this.manifest.location;
                }
            }
        }
        if (periodCount != 0) {
            this.firstPeriodId += removedPeriodCount;
            processManifest(true);
        } else if (this.manifest.utcTiming != null) {
            resolveUtcTimingElement(this.manifest.utcTiming);
        } else {
            processManifest(true);
        }
    }

    LoadErrorAction onManifestLoadError(ParsingLoadable<DashManifest> loadable, long elapsedRealtimeMs, long loadDurationMs, IOException error) {
        boolean isFatal = error instanceof ParserException;
        this.manifestEventDispatcher.loadError(loadable.dataSpec, loadable.getUri(), loadable.type, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded(), error, isFatal);
        return isFatal ? Loader.DONT_RETRY_FATAL : Loader.RETRY;
    }

    void onUtcTimestampLoadCompleted(ParsingLoadable<Long> loadable, long elapsedRealtimeMs, long loadDurationMs) {
        this.manifestEventDispatcher.loadCompleted(loadable.dataSpec, loadable.getUri(), loadable.type, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded());
        onUtcTimestampResolved(((Long) loadable.getResult()).longValue() - elapsedRealtimeMs);
    }

    LoadErrorAction onUtcTimestampLoadError(ParsingLoadable<Long> loadable, long elapsedRealtimeMs, long loadDurationMs, IOException error) {
        this.manifestEventDispatcher.loadError(loadable.dataSpec, loadable.getUri(), loadable.type, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded(), error, true);
        onUtcTimestampResolutionError(error);
        return Loader.DONT_RETRY;
    }

    void onLoadCanceled(ParsingLoadable<?> loadable, long elapsedRealtimeMs, long loadDurationMs) {
        this.manifestEventDispatcher.loadCanceled(loadable.dataSpec, loadable.getUri(), loadable.type, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded());
    }

    private void resolveUtcTimingElement(UtcTimingElement timingElement) {
        String scheme = timingElement.schemeIdUri;
        if (Util.areEqual(scheme, "urn:mpeg:dash:utc:direct:2014") || Util.areEqual(scheme, "urn:mpeg:dash:utc:direct:2012")) {
            resolveUtcTimingElementDirect(timingElement);
        } else if (Util.areEqual(scheme, "urn:mpeg:dash:utc:http-iso:2014") || Util.areEqual(scheme, "urn:mpeg:dash:utc:http-iso:2012")) {
            resolveUtcTimingElementHttp(timingElement, new Iso8601Parser());
        } else if (Util.areEqual(scheme, "urn:mpeg:dash:utc:http-xsdate:2014") || Util.areEqual(scheme, "urn:mpeg:dash:utc:http-xsdate:2012")) {
            resolveUtcTimingElementHttp(timingElement, new XsDateTimeParser());
        } else {
            onUtcTimestampResolutionError(new IOException("Unsupported UTC timing scheme"));
        }
    }

    private void resolveUtcTimingElementDirect(UtcTimingElement timingElement) {
        try {
            onUtcTimestampResolved(Util.parseXsDateTime(timingElement.value) - this.manifestLoadEndTimestampMs);
        } catch (ParserException e) {
            onUtcTimestampResolutionError(e);
        }
    }

    private void resolveUtcTimingElementHttp(UtcTimingElement timingElement, Parser<Long> parser) {
        startLoading(new ParsingLoadable(this.dataSource, Uri.parse(timingElement.value), 5, (Parser) parser), new UtcTimestampCallback(), 1);
    }

    private void onUtcTimestampResolved(long elapsedRealtimeOffsetMs) {
        this.elapsedRealtimeOffsetMs = elapsedRealtimeOffsetMs;
        processManifest(true);
    }

    private void onUtcTimestampResolutionError(IOException error) {
        Log.e(TAG, "Failed to resolve UtcTiming element.", error);
        processManifest(true);
    }

    private void processManifest(boolean scheduleRefresh) {
        int i;
        for (i = 0; i < this.periodsById.size(); i++) {
            int id = this.periodsById.keyAt(i);
            if (id >= this.firstPeriodId) {
                ((DashMediaPeriod) this.periodsById.valueAt(i)).updateManifest(this.manifest, id - this.firstPeriodId);
            }
        }
        boolean windowChangingImplicitly = false;
        int lastPeriodIndex = this.manifest.getPeriodCount() - 1;
        PeriodSeekInfo firstPeriodSeekInfo = PeriodSeekInfo.createPeriodSeekInfo(this.manifest.getPeriod(0), this.manifest.getPeriodDurationUs(0));
        PeriodSeekInfo lastPeriodSeekInfo = PeriodSeekInfo.createPeriodSeekInfo(this.manifest.getPeriod(lastPeriodIndex), this.manifest.getPeriodDurationUs(lastPeriodIndex));
        long currentStartTimeUs = firstPeriodSeekInfo.availableStartTimeUs;
        long currentEndTimeUs = lastPeriodSeekInfo.availableEndTimeUs;
        if (this.manifest.dynamic && !lastPeriodSeekInfo.isIndexExplicit) {
            currentEndTimeUs = Math.min((getNowUnixTimeUs() - C0246C.msToUs(this.manifest.availabilityStartTimeMs)) - C0246C.msToUs(this.manifest.getPeriod(lastPeriodIndex).startMs), currentEndTimeUs);
            if (this.manifest.timeShiftBufferDepthMs != C0246C.TIME_UNSET) {
                long offsetInPeriodUs = currentEndTimeUs - C0246C.msToUs(this.manifest.timeShiftBufferDepthMs);
                int periodIndex = lastPeriodIndex;
                while (offsetInPeriodUs < 0 && periodIndex > 0) {
                    periodIndex--;
                    offsetInPeriodUs += this.manifest.getPeriodDurationUs(periodIndex);
                }
                if (periodIndex == 0) {
                    currentStartTimeUs = Math.max(currentStartTimeUs, offsetInPeriodUs);
                } else {
                    currentStartTimeUs = this.manifest.getPeriodDurationUs(0);
                }
            }
            windowChangingImplicitly = true;
        }
        long windowDurationUs = currentEndTimeUs - currentStartTimeUs;
        for (i = 0; i < this.manifest.getPeriodCount() - 1; i++) {
            windowDurationUs += this.manifest.getPeriodDurationUs(i);
        }
        long windowDefaultStartPositionUs = 0;
        if (this.manifest.dynamic) {
            long presentationDelayForManifestMs = this.livePresentationDelayMs;
            if (!(this.livePresentationDelayOverridesManifest || this.manifest.suggestedPresentationDelayMs == C0246C.TIME_UNSET)) {
                presentationDelayForManifestMs = this.manifest.suggestedPresentationDelayMs;
            }
            windowDefaultStartPositionUs = windowDurationUs - C0246C.msToUs(presentationDelayForManifestMs);
            if (windowDefaultStartPositionUs < MIN_LIVE_DEFAULT_START_POSITION_US) {
                windowDefaultStartPositionUs = Math.min(MIN_LIVE_DEFAULT_START_POSITION_US, windowDurationUs / 2);
            }
        }
        refreshSourceInfo(new DashTimeline(this.manifest.availabilityStartTimeMs, (this.manifest.availabilityStartTimeMs + this.manifest.getPeriod(0).startMs) + C0246C.usToMs(currentStartTimeUs), this.firstPeriodId, currentStartTimeUs, windowDurationUs, windowDefaultStartPositionUs, this.manifest, this.tag), this.manifest);
        if (!this.sideloadedManifest) {
            this.handler.removeCallbacks(this.simulateManifestRefreshRunnable);
            if (windowChangingImplicitly) {
                this.handler.postDelayed(this.simulateManifestRefreshRunnable, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
            }
            if (this.manifestLoadPending) {
                startLoadingManifest();
            } else if (scheduleRefresh && this.manifest.dynamic && this.manifest.minUpdatePeriodMs != C0246C.TIME_UNSET) {
                long minUpdatePeriodMs = this.manifest.minUpdatePeriodMs;
                if (minUpdatePeriodMs == 0) {
                    minUpdatePeriodMs = DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS;
                }
                scheduleManifestRefresh(Math.max(0, (this.manifestLoadStartTimestampMs + minUpdatePeriodMs) - SystemClock.elapsedRealtime()));
            }
        }
    }

    private void scheduleManifestRefresh(long delayUntilNextLoadMs) {
        this.handler.postDelayed(this.refreshManifestRunnable, delayUntilNextLoadMs);
    }

    private void startLoadingManifest() {
        this.handler.removeCallbacks(this.refreshManifestRunnable);
        if (this.loader.isLoading()) {
            this.manifestLoadPending = true;
            return;
        }
        Uri manifestUri;
        synchronized (this.manifestUriLock) {
            manifestUri = this.manifestUri;
        }
        this.manifestLoadPending = false;
        startLoading(new ParsingLoadable(this.dataSource, manifestUri, 4, this.manifestParser), this.manifestCallback, this.minLoadableRetryCount);
    }

    private long getManifestLoadRetryDelayMillis() {
        return (long) Math.min((this.staleManifestReloadAttempt - 1) * 1000, 5000);
    }

    private <T> void startLoading(ParsingLoadable<T> loadable, Callback<ParsingLoadable<T>> callback, int minRetryCount) {
        this.manifestEventDispatcher.loadStarted(loadable.dataSpec, loadable.dataSpec.uri, loadable.type, this.loader.startLoading(loadable, callback, minRetryCount));
    }

    private long getNowUnixTimeUs() {
        if (this.elapsedRealtimeOffsetMs != 0) {
            return C0246C.msToUs(SystemClock.elapsedRealtime() + this.elapsedRealtimeOffsetMs);
        }
        return C0246C.msToUs(System.currentTimeMillis());
    }
}
