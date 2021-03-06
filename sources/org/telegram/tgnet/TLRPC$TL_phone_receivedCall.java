package org.telegram.tgnet;

public class TLRPC$TL_phone_receivedCall extends TLObject {
    public static int constructor = 399855457;
    public TLRPC$TL_inputPhoneCall peer;

    public TLObject deserializeResponse(AbstractSerializedData stream, int constructor, boolean exception) {
        return TLRPC$Bool.TLdeserialize(stream, constructor, exception);
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        this.peer.serializeToStream(stream);
    }
}
