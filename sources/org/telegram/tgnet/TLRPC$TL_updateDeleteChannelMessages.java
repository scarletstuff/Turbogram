package org.telegram.tgnet;

import java.util.ArrayList;

public class TLRPC$TL_updateDeleteChannelMessages extends TLRPC$Update {
    public static int constructor = -1015733815;
    public int channel_id;
    public ArrayList<Integer> messages = new ArrayList();
    public int pts;
    public int pts_count;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.channel_id = stream.readInt32(exception);
        if (stream.readInt32(exception) == 481674261) {
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                this.messages.add(Integer.valueOf(stream.readInt32(exception)));
            }
            this.pts = stream.readInt32(exception);
            this.pts_count = stream.readInt32(exception);
        } else if (exception) {
            throw new RuntimeException(String.format("wrong Vector magic, got %x", new Object[]{Integer.valueOf(magic)}));
        }
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        stream.writeInt32(this.channel_id);
        stream.writeInt32(481674261);
        int count = this.messages.size();
        stream.writeInt32(count);
        for (int a = 0; a < count; a++) {
            stream.writeInt32(((Integer) this.messages.get(a)).intValue());
        }
        stream.writeInt32(this.pts);
        stream.writeInt32(this.pts_count);
    }
}
