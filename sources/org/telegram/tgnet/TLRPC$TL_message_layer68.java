package org.telegram.tgnet;

import android.text.TextUtils;

public class TLRPC$TL_message_layer68 extends TLRPC$TL_message {
    public static int constructor = -1063525281;

    public void readParams(AbstractSerializedData stream, boolean exception) {
        boolean z;
        this.flags = stream.readInt32(exception);
        if ((this.flags & 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.unread = z;
        if ((this.flags & 2) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.out = z;
        if ((this.flags & 16) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mentioned = z;
        if ((this.flags & 32) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.media_unread = z;
        if ((this.flags & 8192) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.silent = z;
        if ((this.flags & 16384) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.post = z;
        if ((this.flags & 1073741824) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.with_my_score = z;
        this.id = stream.readInt32(exception);
        if ((this.flags & 256) != 0) {
            this.from_id = stream.readInt32(exception);
        }
        this.to_id = TLRPC$Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
        if (this.from_id == 0) {
            if (this.to_id.user_id != 0) {
                this.from_id = this.to_id.user_id;
            } else {
                this.from_id = -this.to_id.channel_id;
            }
        }
        if ((this.flags & 4) != 0) {
            this.fwd_from = TLRPC$MessageFwdHeader.TLdeserialize(stream, stream.readInt32(exception), exception);
        }
        if ((this.flags & 2048) != 0) {
            this.via_bot_id = stream.readInt32(exception);
        }
        if ((this.flags & 8) != 0) {
            this.reply_to_msg_id = stream.readInt32(exception);
        }
        this.date = stream.readInt32(exception);
        this.message = stream.readString(exception);
        if ((this.flags & 512) != 0) {
            this.media = TLRPC$MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (!(this.media == null || TextUtils.isEmpty(this.media.captionLegacy))) {
                this.message = this.media.captionLegacy;
            }
        } else {
            this.media = new TLRPC$TL_messageMediaEmpty();
        }
        if ((this.flags & 64) != 0) {
            this.reply_markup = TLRPC$ReplyMarkup.TLdeserialize(stream, stream.readInt32(exception), exception);
        }
        if ((this.flags & 128) != 0) {
            if (stream.readInt32(exception) == 481674261) {
                int count = stream.readInt32(exception);
                int a = 0;
                while (a < count) {
                    TLRPC$MessageEntity object = TLRPC$MessageEntity.TLdeserialize(stream, stream.readInt32(exception), exception);
                    if (object != null) {
                        this.entities.add(object);
                        a++;
                    } else {
                        return;
                    }
                }
            } else if (exception) {
                throw new RuntimeException(String.format("wrong Vector magic, got %x", new Object[]{Integer.valueOf(magic)}));
            } else {
                return;
            }
        }
        if ((this.flags & 1024) != 0) {
            this.views = stream.readInt32(exception);
        }
        if ((this.flags & 32768) != 0) {
            this.edit_date = stream.readInt32(exception);
        }
    }

    public void serializeToStream(AbstractSerializedData stream) {
        int i;
        stream.writeInt32(constructor);
        this.flags = this.unread ? this.flags | 1 : this.flags & -2;
        this.flags = this.out ? this.flags | 2 : this.flags & -3;
        this.flags = this.mentioned ? this.flags | 16 : this.flags & -17;
        this.flags = this.media_unread ? this.flags | 32 : this.flags & -33;
        this.flags = this.silent ? this.flags | 8192 : this.flags & -8193;
        this.flags = this.post ? this.flags | 16384 : this.flags & -16385;
        if (this.with_my_score) {
            i = this.flags | 1073741824;
        } else {
            i = this.flags & -1073741825;
        }
        this.flags = i;
        stream.writeInt32(this.flags);
        stream.writeInt32(this.id);
        if ((this.flags & 256) != 0) {
            stream.writeInt32(this.from_id);
        }
        this.to_id.serializeToStream(stream);
        if ((this.flags & 4) != 0) {
            this.fwd_from.serializeToStream(stream);
        }
        if ((this.flags & 2048) != 0) {
            stream.writeInt32(this.via_bot_id);
        }
        if ((this.flags & 8) != 0) {
            stream.writeInt32(this.reply_to_msg_id);
        }
        stream.writeInt32(this.date);
        stream.writeString(this.message);
        if ((this.flags & 512) != 0) {
            this.media.serializeToStream(stream);
        }
        if ((this.flags & 64) != 0) {
            this.reply_markup.serializeToStream(stream);
        }
        if ((this.flags & 128) != 0) {
            stream.writeInt32(481674261);
            int count = this.entities.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                ((TLRPC$MessageEntity) this.entities.get(a)).serializeToStream(stream);
            }
        }
        if ((this.flags & 1024) != 0) {
            stream.writeInt32(this.views);
        }
        if ((this.flags & 32768) != 0) {
            stream.writeInt32(this.edit_date);
        }
        writeAttachPath(stream);
    }
}
