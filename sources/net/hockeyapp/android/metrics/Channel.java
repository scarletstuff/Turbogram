package net.hockeyapp.android.metrics;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.hockeyapp.android.metrics.model.Base;
import net.hockeyapp.android.metrics.model.Data;
import net.hockeyapp.android.metrics.model.Domain;
import net.hockeyapp.android.metrics.model.Envelope;
import net.hockeyapp.android.metrics.model.TelemetryData;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

class Channel {
    protected static final int MAX_BATCH_COUNT = 50;
    protected static final int MAX_BATCH_COUNT_DEBUG = 5;
    protected static final int MAX_BATCH_INTERVAL = 15000;
    protected static final int MAX_BATCH_INTERVAL_DEBUG = 3000;
    private static final String TAG = "HockeyApp-Metrics";
    private final Persistence mPersistence;
    protected final List<String> mQueue = new LinkedList();
    private SynchronizeChannelTask mSynchronizeTask;
    protected final TelemetryContext mTelemetryContext;
    private final Timer mTimer;

    private class SynchronizeChannelTask extends TimerTask {
        SynchronizeChannelTask() {
        }

        public void run() {
            Channel.this.synchronize();
        }
    }

    static int getMaxBatchCount() {
        return Util.isDebuggerConnected() ? 5 : 50;
    }

    static int getMaxBatchInterval() {
        return Util.isDebuggerConnected() ? MAX_BATCH_INTERVAL_DEBUG : 15000;
    }

    public Channel(TelemetryContext telemetryContext, Persistence persistence) {
        this.mTelemetryContext = telemetryContext;
        this.mPersistence = persistence;
        this.mTimer = new Timer("HockeyApp User Metrics Sender Queue", true);
    }

    protected synchronized void enqueue(String serializedItem) {
        if (serializedItem != null) {
            if (!this.mQueue.add(serializedItem)) {
                HockeyLog.verbose(TAG, "Unable to add item to queue");
            } else if (this.mQueue.size() >= getMaxBatchCount()) {
                synchronize();
            } else if (this.mQueue.size() == 1) {
                scheduleSynchronizeTask();
            }
        }
    }

    protected void synchronize() {
        if (this.mSynchronizeTask != null) {
            this.mSynchronizeTask.cancel();
        }
        String[] data = null;
        synchronized (this) {
            if (!this.mQueue.isEmpty()) {
                data = new String[this.mQueue.size()];
                this.mQueue.toArray(data);
                this.mQueue.clear();
            }
        }
        if (this.mPersistence != null && data != null) {
            this.mPersistence.persist(data);
        }
    }

    protected Envelope createEnvelope(Data<Domain> data) {
        Envelope envelope = new Envelope();
        envelope.setData(data);
        Domain baseData = data.getBaseData();
        if (baseData instanceof TelemetryData) {
            envelope.setName(((TelemetryData) baseData).getEnvelopeName());
        }
        this.mTelemetryContext.updateScreenResolution();
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setIKey(this.mTelemetryContext.getInstrumentationKey());
        Map<String, String> tags = this.mTelemetryContext.getContextTags();
        if (tags != null) {
            envelope.setTags(tags);
        }
        return envelope;
    }

    protected void scheduleSynchronizeTask() {
        this.mSynchronizeTask = new SynchronizeChannelTask();
        this.mTimer.schedule(this.mSynchronizeTask, (long) getMaxBatchInterval());
    }

    public void enqueueData(Base data) {
        if (data instanceof Data) {
            Envelope envelope = null;
            try {
                envelope = createEnvelope((Data) data);
            } catch (ClassCastException e) {
                HockeyLog.debug(TAG, "Telemetry not enqueued, could not create envelope, must be of type ITelemetry");
            }
            if (envelope != null) {
                enqueue(serializeEnvelope(envelope));
                HockeyLog.debug(TAG, "enqueued telemetry: " + envelope.getName());
                return;
            }
            return;
        }
        HockeyLog.debug(TAG, "Telemetry not enqueued, must be of type ITelemetry");
    }

    protected String serializeEnvelope(Envelope envelope) {
        if (envelope != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                envelope.serialize(stringWriter);
                return stringWriter.toString();
            } catch (IOException e) {
                HockeyLog.debug(TAG, "Failed to save data with exception: " + e.toString());
                return null;
            }
        }
        HockeyLog.debug(TAG, "Envelope wasn't empty but failed to serialize anything, returning null");
        return null;
    }
}
