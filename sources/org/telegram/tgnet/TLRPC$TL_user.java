package org.telegram.tgnet;

import org.telegram.tgnet.TLRPC.User;

public class TLRPC$TL_user extends User {
    public static int constructor = 773059779;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        boolean z;
        boolean z2 = true;
        this.flags = stream.readInt32(exception);
        this.self = (this.flags & 1024) != 0;
        if ((this.flags & 2048) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.contact = z;
        if ((this.flags & 4096) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mutual_contact = z;
        if ((this.flags & 8192) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.deleted = z;
        if ((this.flags & 16384) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.bot = z;
        if ((this.flags & 32768) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.bot_chat_history = z;
        if ((this.flags & 65536) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.bot_nochats = z;
        if ((this.flags & 131072) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.verified = z;
        if ((this.flags & 262144) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.restricted = z;
        if ((this.flags & 1048576) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.min = z;
        if ((this.flags & 2097152) == 0) {
            z2 = false;
        }
        this.bot_inline_geo = z2;
        this.id = stream.readInt32(exception);
        if ((this.flags & 1) != 0) {
            this.access_hash = stream.readInt64(exception);
        }
        if ((this.flags & 2) != 0) {
            this.first_name = stream.readString(exception);
        }
        if ((this.flags & 4) != 0) {
            this.last_name = stream.readString(exception);
        }
        if ((this.flags & 8) != 0) {
            this.username = stream.readString(exception);
        }
        if ((this.flags & 16) != 0) {
            this.phone = stream.readString(exception);
        }
        if ((this.flags & 32) != 0) {
            this.photo = TLRPC$UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
        }
        if ((this.flags & 64) != 0) {
            this.status = TLRPC$UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }
        if ((this.flags & 16384) != 0) {
            this.bot_info_version = stream.readInt32(exception);
        }
        if ((this.flags & 262144) != 0) {
            this.restriction_reason = stream.readString(exception);
        }
        if ((this.flags & 524288) != 0) {
            this.bot_inline_placeholder = stream.readString(exception);
        }
        if ((this.flags & 4194304) != 0) {
            this.lang_code = stream.readString(exception);
        }
    }

    public void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(constructor);
        this.flags = this.self ? this.flags | 1024 : this.flags & -1025;
        this.flags = this.contact ? this.flags | 2048 : this.flags & -2049;
        this.flags = this.mutual_contact ? this.flags | 4096 : this.flags & -4097;
        this.flags = this.deleted ? this.flags | 8192 : this.flags & -8193;
        this.flags = this.bot ? this.flags | 16384 : this.flags & -16385;
        this.flags = this.bot_chat_history ? this.flags | 32768 : this.flags & -32769;
        this.flags = this.bot_nochats ? this.flags | 65536 : this.flags & -65537;
        this.flags = this.verified ? this.flags | 131072 : this.flags & -131073;
        this.flags = this.restricted ? this.flags | 262144 : this.flags & -262145;
        this.flags = this.min ? this.flags | 1048576 : this.flags & -1048577;
        this.flags = this.bot_inline_geo ? this.flags | 2097152 : this.flags & -2097153;
        stream.writeInt32(this.flags);
        stream.writeInt32(this.id);
        if ((this.flags & 1) != 0) {
            stream.writeInt64(this.access_hash);
        }
        if ((this.flags & 2) != 0) {
            stream.writeString(this.first_name);
        }
        if ((this.flags & 4) != 0) {
            stream.writeString(this.last_name);
        }
        if ((this.flags & 8) != 0) {
            stream.writeString(this.username);
        }
        if ((this.flags & 16) != 0) {
            stream.writeString(this.phone);
        }
        if ((this.flags & 32) != 0) {
            this.photo.serializeToStream(stream);
        }
        if ((this.flags & 64) != 0) {
            this.status.serializeToStream(stream);
        }
        if ((this.flags & 16384) != 0) {
            stream.writeInt32(this.bot_info_version);
        }
        if ((this.flags & 262144) != 0) {
            stream.writeString(this.restriction_reason);
        }
        if ((this.flags & 524288) != 0) {
            stream.writeString(this.bot_inline_placeholder);
        }
        if ((this.flags & 4194304) != 0) {
            stream.writeString(this.lang_code);
        }
    }
}
