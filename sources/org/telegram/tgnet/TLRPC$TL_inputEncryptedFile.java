package org.telegram.tgnet;

public class TLRPC$TL_inputEncryptedFile extends TLRPC$InputEncryptedFile {
    public static int constructor = 1511503333;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        this.id = stream.readInt64(exception);
        this.access_hash = stream.readInt64(exception);
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        stream.writeInt64(this.id);
        stream.writeInt64(this.access_hash);
    }
}
