package org.telegram.messenger;

import android.net.Uri;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSource$$CC;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$TL_document;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC$TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC$TL_documentAttributeVideo;

public class FileStreamLoadOperation implements DataSource {
    private long bytesRemaining;
    private CountDownLatch countDownLatch;
    private int currentAccount;
    private int currentOffset;
    private DataSpec dataSpec;
    private TLRPC$Document document;
    private RandomAccessFile file;
    private final TransferListener listener;
    private FileLoadOperation loadOperation;
    private boolean opened;
    private Uri uri;

    public void addTransferListener(TransferListener transferListener) {
        DataSource$$CC.addTransferListener(this, transferListener);
    }

    public Map getResponseHeaders() {
        return DataSource$$CC.getResponseHeaders(this);
    }

    public FileStreamLoadOperation() {
        this(null);
    }

    public FileStreamLoadOperation(TransferListener listener) {
        this.listener = listener;
    }

    public long open(DataSpec dataSpec) throws IOException {
        this.uri = dataSpec.uri;
        this.dataSpec = dataSpec;
        this.currentAccount = Utilities.parseInt(this.uri.getQueryParameter("account")).intValue();
        this.document = new TLRPC$TL_document();
        this.document.access_hash = Utilities.parseLong(this.uri.getQueryParameter("hash")).longValue();
        this.document.id = Utilities.parseLong(this.uri.getQueryParameter(TtmlNode.ATTR_ID)).longValue();
        this.document.size = Utilities.parseInt(this.uri.getQueryParameter("size")).intValue();
        this.document.dc_id = Utilities.parseInt(this.uri.getQueryParameter("dc")).intValue();
        this.document.mime_type = this.uri.getQueryParameter("mime");
        TLRPC$TL_documentAttributeFilename filename = new TLRPC$TL_documentAttributeFilename();
        filename.file_name = this.uri.getQueryParameter("name");
        this.document.attributes.add(filename);
        if (this.document.mime_type.startsWith(MimeTypes.BASE_TYPE_VIDEO)) {
            this.document.attributes.add(new TLRPC$TL_documentAttributeVideo());
        } else if (this.document.mime_type.startsWith(MimeTypes.BASE_TYPE_AUDIO)) {
            this.document.attributes.add(new TLRPC$TL_documentAttributeAudio());
        }
        FileLoader instance = FileLoader.getInstance(this.currentAccount);
        TLRPC$Document tLRPC$Document = this.document;
        int i = (int) dataSpec.position;
        this.currentOffset = i;
        this.loadOperation = instance.loadStreamFile(this, tLRPC$Document, i);
        this.bytesRemaining = dataSpec.length == -1 ? ((long) this.document.size) - dataSpec.position : dataSpec.length;
        if (this.bytesRemaining < 0) {
            throw new EOFException();
        }
        this.opened = true;
        if (this.listener != null) {
            this.listener.onTransferStart(this, dataSpec, false);
        }
        this.file = new RandomAccessFile(this.loadOperation.getCurrentFile(), "r");
        this.file.seek((long) this.currentOffset);
        return this.bytesRemaining;
    }

    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        }
        if (this.bytesRemaining == 0) {
            return -1;
        }
        int availableLength = 0;
        try {
            if (this.bytesRemaining < ((long) readLength)) {
                readLength = (int) this.bytesRemaining;
            }
            while (availableLength == 0) {
                availableLength = this.loadOperation.getDownloadedLengthFromOffset(this.currentOffset, readLength);
                if (availableLength == 0) {
                    if (this.loadOperation.isPaused()) {
                        FileLoader.getInstance(this.currentAccount).loadStreamFile(this, this.document, this.currentOffset);
                    }
                    this.countDownLatch = new CountDownLatch(1);
                    this.countDownLatch.await();
                }
            }
            this.file.readFully(buffer, offset, availableLength);
            this.currentOffset += availableLength;
            this.bytesRemaining -= (long) availableLength;
            if (this.listener == null) {
                return availableLength;
            }
            this.listener.onBytesTransferred(this, this.dataSpec, false, availableLength);
            return availableLength;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public Uri getUri() {
        return this.uri;
    }

    public void close() {
        if (this.loadOperation != null) {
            this.loadOperation.removeStreamListener(this);
        }
        if (this.countDownLatch != null) {
            this.countDownLatch.countDown();
        }
        if (this.file != null) {
            try {
                this.file.close();
            } catch (Exception e) {
                FileLog.e(e);
            }
            this.file = null;
        }
        this.uri = null;
        if (this.opened) {
            this.opened = false;
            if (this.listener != null) {
                this.listener.onTransferEnd(this, this.dataSpec, false);
            }
        }
    }

    protected void newDataAvailable() {
        if (this.countDownLatch != null) {
            this.countDownLatch.countDown();
        }
    }
}
