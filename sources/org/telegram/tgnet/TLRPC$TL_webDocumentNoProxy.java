package org.telegram.tgnet;

public class TLRPC$TL_webDocumentNoProxy extends TLRPC$WebDocument {
    public static int constructor = -104284986;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.url = stream.readString(exception);
        this.size = stream.readInt32(exception);
        this.mime_type = stream.readString(exception);
        if (stream.readInt32(exception) == 481674261) {
            int count = stream.readInt32(exception);
            int a = 0;
            while (a < count) {
                TLRPC$DocumentAttribute object = TLRPC$DocumentAttribute.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object != null) {
                    this.attributes.add(object);
                    a++;
                } else {
                    return;
                }
            }
        } else if (exception) {
            throw new RuntimeException(String.format("wrong Vector magic, got %x", new Object[]{Integer.valueOf(magic)}));
        }
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        stream.writeString(this.url);
        stream.writeInt32(this.size);
        stream.writeString(this.mime_type);
        stream.writeInt32(481674261);
        int count = this.attributes.size();
        stream.writeInt32(count);
        for (int a = 0; a < count; a++) {
            ((TLRPC$DocumentAttribute) this.attributes.get(a)).serializeToStream(stream);
        }
    }
}
