package org.telegram.ui.Adapters;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$ChannelParticipant;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$Peer;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsBanned;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsSearch;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC$TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC$TL_contacts_found;
import org.telegram.tgnet.TLRPC$TL_contacts_search;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC.User;
import turbogram.Utilities.TurboUtils;

public class SearchAdapterHelper {
    private boolean allResultsAreGlobal;
    private int channelLastReqId;
    private int channelLastReqId2;
    private int channelReqId = 0;
    private int channelReqId2 = 0;
    private int currentAccount = UserConfig.selectedAccount;
    private SearchAdapterHelperDelegate delegate;
    private ArrayList<TLObject> globalSearch = new ArrayList();
    private SparseArray<TLObject> globalSearchMap = new SparseArray();
    private ArrayList<TLRPC$ChannelParticipant> groupSearch = new ArrayList();
    private ArrayList<TLRPC$ChannelParticipant> groupSearch2 = new ArrayList();
    private ArrayList<HashtagObject> hashtags;
    private HashMap<String, HashtagObject> hashtagsByText;
    private boolean hashtagsLoadedFromDb = false;
    private String lastFoundChannel;
    private String lastFoundChannel2;
    private String lastFoundUsername = null;
    private int lastReqId;
    private ArrayList<TLObject> localSearchResults;
    private ArrayList<TLObject> localServerSearch = new ArrayList();
    private int reqId = 0;

    public interface SearchAdapterHelperDelegate {
        void onDataSetChanged();

        void onSetHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap);
    }

    protected static final class DialogSearchResult {
        public int date;
        public CharSequence name;
        public TLObject object;

        protected DialogSearchResult() {
        }
    }

    public static class HashtagObject {
        int date;
        String hashtag;
    }

    public SearchAdapterHelper(boolean global) {
        this.allResultsAreGlobal = global;
    }

    public void queryServerSearch(String query, boolean allowUsername, boolean allowChats, boolean allowBots, boolean allowSelf, int channelId, boolean kicked) {
        if (this.reqId != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
            this.reqId = 0;
        }
        if (this.channelReqId != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.channelReqId, true);
            this.channelReqId = 0;
        }
        if (this.channelReqId2 != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.channelReqId2, true);
            this.channelReqId2 = 0;
        }
        if (query == null) {
            this.groupSearch.clear();
            this.groupSearch2.clear();
            this.globalSearch.clear();
            this.globalSearchMap.clear();
            this.localServerSearch.clear();
            this.lastReqId = 0;
            this.channelLastReqId = 0;
            this.channelLastReqId2 = 0;
            this.delegate.onDataSetChanged();
            return;
        }
        TLRPC$TL_channels_getParticipants req;
        if (query.length() <= 0 || channelId == 0) {
            this.groupSearch.clear();
            this.groupSearch2.clear();
            this.channelLastReqId = 0;
            this.delegate.onDataSetChanged();
        } else {
            req = new TLRPC$TL_channels_getParticipants();
            if (kicked) {
                req.filter = new TLRPC$TL_channelParticipantsBanned();
            } else {
                req.filter = new TLRPC$TL_channelParticipantsSearch();
            }
            req.filter.f783q = query;
            req.limit = 50;
            req.offset = 0;
            req.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(channelId);
            int currentReqId = this.channelLastReqId + 1;
            this.channelLastReqId = currentReqId;
            this.channelReqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new SearchAdapterHelper$$Lambda$0(this, currentReqId, query), 2);
            if (kicked) {
                req = new TLRPC$TL_channels_getParticipants();
                req.filter = new TLRPC$TL_channelParticipantsKicked();
                req.filter.f783q = query;
                req.limit = 50;
                req.offset = 0;
                req.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(channelId);
                int currentReqId2 = this.channelLastReqId2 + 1;
                this.channelLastReqId2 = currentReqId2;
                this.channelReqId2 = ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new SearchAdapterHelper$$Lambda$1(this, currentReqId2, query), 2);
            }
        }
        if (!allowUsername) {
            return;
        }
        if (query.length() > 0) {
            req = new TLRPC$TL_contacts_search();
            req.f796q = query;
            req.limit = 50;
            currentReqId = this.lastReqId + 1;
            this.lastReqId = currentReqId;
            this.reqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new SearchAdapterHelper$$Lambda$2(this, currentReqId, allowChats, allowBots, allowSelf, query), 2);
            return;
        }
        this.globalSearch.clear();
        this.globalSearchMap.clear();
        this.localServerSearch.clear();
        this.lastReqId = 0;
        this.delegate.onDataSetChanged();
    }

    final /* synthetic */ void lambda$queryServerSearch$1$SearchAdapterHelper(int currentReqId, String query, TLObject response, TLRPC$TL_error error) {
        AndroidUtilities.runOnUIThread(new SearchAdapterHelper$$Lambda$10(this, currentReqId, error, response, query));
    }

    final /* synthetic */ void lambda$null$0$SearchAdapterHelper(int currentReqId, TLRPC$TL_error error, TLObject response, String query) {
        if (currentReqId == this.channelLastReqId && error == null) {
            TLRPC$TL_channels_channelParticipants res = (TLRPC$TL_channels_channelParticipants) response;
            this.lastFoundChannel = query.toLowerCase();
            MessagesController.getInstance(this.currentAccount).putUsers(res.users, false);
            this.groupSearch = res.participants;
            this.delegate.onDataSetChanged();
        }
        this.channelReqId = 0;
    }

    final /* synthetic */ void lambda$queryServerSearch$3$SearchAdapterHelper(int currentReqId2, String query, TLObject response, TLRPC$TL_error error) {
        AndroidUtilities.runOnUIThread(new SearchAdapterHelper$$Lambda$9(this, currentReqId2, error, response, query));
    }

    final /* synthetic */ void lambda$null$2$SearchAdapterHelper(int currentReqId2, TLRPC$TL_error error, TLObject response, String query) {
        if (currentReqId2 == this.channelLastReqId2 && error == null) {
            TLRPC$TL_channels_channelParticipants res = (TLRPC$TL_channels_channelParticipants) response;
            this.lastFoundChannel2 = query.toLowerCase();
            MessagesController.getInstance(this.currentAccount).putUsers(res.users, false);
            this.groupSearch2 = res.participants;
            this.delegate.onDataSetChanged();
        }
        this.channelReqId2 = 0;
    }

    final /* synthetic */ void lambda$queryServerSearch$5$SearchAdapterHelper(int currentReqId, boolean allowChats, boolean allowBots, boolean allowSelf, String query, TLObject response, TLRPC$TL_error error) {
        AndroidUtilities.runOnUIThread(new SearchAdapterHelper$$Lambda$8(this, currentReqId, error, response, allowChats, allowBots, allowSelf, query));
    }

    final /* synthetic */ void lambda$null$4$SearchAdapterHelper(int currentReqId, TLRPC$TL_error error, TLObject response, boolean allowChats, boolean allowBots, boolean allowSelf, String query) {
        if (currentReqId == this.lastReqId && error == null) {
            int a;
            TLRPC$Chat chat;
            User user;
            TLRPC$Peer peer;
            TLRPC$TL_contacts_found res = (TLRPC$TL_contacts_found) response;
            this.globalSearch.clear();
            this.globalSearchMap.clear();
            this.localServerSearch.clear();
            MessagesController.getInstance(this.currentAccount).putChats(res.chats, false);
            MessagesController.getInstance(this.currentAccount).putUsers(res.users, false);
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(res.users, res.chats, true, true);
            SparseArray<TLRPC$Chat> chatsMap = new SparseArray();
            SparseArray<User> usersMap = new SparseArray();
            for (a = 0; a < res.chats.size(); a++) {
                chat = (TLRPC$Chat) res.chats.get(a);
                chatsMap.put(chat.id, chat);
            }
            for (a = 0; a < res.users.size(); a++) {
                user = (User) res.users.get(a);
                usersMap.put(user.id, user);
            }
            for (int b = 0; b < 2; b++) {
                ArrayList<TLRPC$Peer> arrayList;
                if (b != 0) {
                    arrayList = res.results;
                    for (a = 0; a < arrayList.size(); a++) {
                        peer = (TLRPC$Peer) arrayList.get(a);
                        user = null;
                        chat = null;
                        if (peer.user_id != 0) {
                            user = (User) usersMap.get(peer.user_id);
                        } else if (peer.chat_id != 0) {
                            chat = (TLRPC$Chat) chatsMap.get(peer.chat_id);
                        } else if (peer.channel_id != 0) {
                            chat = (TLRPC$Chat) chatsMap.get(peer.channel_id);
                        }
                        if (chat == null) {
                            this.globalSearch.add(user);
                            this.globalSearchMap.put(user.id, user);
                        } else {
                            this.globalSearch.add(chat);
                            this.globalSearchMap.put(-chat.id, chat);
                        }
                    }
                } else if (this.allResultsAreGlobal) {
                    arrayList = res.my_results;
                    for (a = 0; a < arrayList.size(); a++) {
                        peer = (TLRPC$Peer) arrayList.get(a);
                        user = null;
                        chat = null;
                        if (peer.user_id != 0) {
                            user = (User) usersMap.get(peer.user_id);
                        } else if (peer.chat_id != 0) {
                            chat = (TLRPC$Chat) chatsMap.get(peer.chat_id);
                        } else if (peer.channel_id != 0) {
                            chat = (TLRPC$Chat) chatsMap.get(peer.channel_id);
                        }
                        if (chat == null) {
                            if (allowChats && !TurboUtils.isHiddenDialog(-chat.id)) {
                                this.globalSearch.add(chat);
                                this.globalSearchMap.put(-chat.id, chat);
                            }
                        } else if (user != null && ((allowBots || !user.bot) && ((allowSelf || !user.self) && !TurboUtils.isHiddenDialog(user.id)))) {
                            this.globalSearch.add(user);
                            this.globalSearchMap.put(user.id, user);
                        }
                    }
                }
            }
            if (!this.allResultsAreGlobal) {
                for (a = 0; a < res.my_results.size(); a++) {
                    peer = (TLRPC$Peer) res.my_results.get(a);
                    user = null;
                    chat = null;
                    if (peer.user_id != 0) {
                        user = (User) usersMap.get(peer.user_id);
                    } else if (peer.chat_id != 0) {
                        chat = (TLRPC$Chat) chatsMap.get(peer.chat_id);
                    } else if (peer.channel_id != 0) {
                        chat = (TLRPC$Chat) chatsMap.get(peer.channel_id);
                    }
                    if (chat != null) {
                        if (!TurboUtils.isHiddenDialog(-chat.id)) {
                            this.localServerSearch.add(chat);
                            this.globalSearchMap.put(-chat.id, chat);
                        }
                    } else if (!(user == null || TurboUtils.isHiddenDialog(user.id))) {
                        this.localServerSearch.add(user);
                        this.globalSearchMap.put(user.id, user);
                    }
                }
            }
            this.lastFoundUsername = query.toLowerCase();
            if (this.localSearchResults != null) {
                mergeResults(this.localSearchResults);
            }
            this.delegate.onDataSetChanged();
        }
        this.reqId = 0;
    }

    public void unloadRecentHashtags() {
        this.hashtagsLoadedFromDb = false;
    }

    public boolean loadRecentHashtags() {
        if (this.hashtagsLoadedFromDb) {
            return true;
        }
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new SearchAdapterHelper$$Lambda$3(this));
        return false;
    }

    final /* synthetic */ void lambda$loadRecentHashtags$8$SearchAdapterHelper() {
        try {
            SQLiteCursor cursor = MessagesStorage.getInstance(this.currentAccount).getDatabase().queryFinalized("SELECT id, date FROM hashtag_recent_v2 WHERE 1", new Object[0]);
            ArrayList<HashtagObject> arrayList = new ArrayList();
            HashMap<String, HashtagObject> hashMap = new HashMap();
            while (cursor.next()) {
                HashtagObject hashtagObject = new HashtagObject();
                hashtagObject.hashtag = cursor.stringValue(0);
                hashtagObject.date = cursor.intValue(1);
                arrayList.add(hashtagObject);
                hashMap.put(hashtagObject.hashtag, hashtagObject);
            }
            cursor.dispose();
            Collections.sort(arrayList, SearchAdapterHelper$$Lambda$6.$instance);
            AndroidUtilities.runOnUIThread(new SearchAdapterHelper$$Lambda$7(this, arrayList, hashMap));
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    static final /* synthetic */ int lambda$null$6$SearchAdapterHelper(HashtagObject lhs, HashtagObject rhs) {
        if (lhs.date < rhs.date) {
            return 1;
        }
        if (lhs.date > rhs.date) {
            return -1;
        }
        return 0;
    }

    final /* synthetic */ void lambda$null$7$SearchAdapterHelper(ArrayList arrayList, HashMap hashMap) {
        setHashtags(arrayList, hashMap);
    }

    public void mergeResults(ArrayList<TLObject> localResults) {
        this.localSearchResults = localResults;
        if (this.globalSearchMap.size() != 0 && localResults != null) {
            int count = localResults.size();
            for (int a = 0; a < count; a++) {
                TLObject obj = (TLObject) localResults.get(a);
                if (obj instanceof User) {
                    User u = (User) this.globalSearchMap.get(((User) obj).id);
                    if (u != null) {
                        this.globalSearch.remove(u);
                        this.localServerSearch.remove(u);
                        this.globalSearchMap.remove(u.id);
                    }
                } else if (obj instanceof TLRPC$Chat) {
                    TLRPC$Chat c = (TLRPC$Chat) this.globalSearchMap.get(-((TLRPC$Chat) obj).id);
                    if (c != null) {
                        this.globalSearch.remove(c);
                        this.localServerSearch.remove(c);
                        this.globalSearchMap.remove(-c.id);
                    }
                }
            }
        }
    }

    public void setDelegate(SearchAdapterHelperDelegate searchAdapterHelperDelegate) {
        this.delegate = searchAdapterHelperDelegate;
    }

    public void addHashtagsFromMessage(CharSequence message) {
        if (message != null) {
            boolean changed = false;
            Matcher matcher = Pattern.compile("(^|\\s)#[\\w@.]+").matcher(message);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (!(message.charAt(start) == '@' || message.charAt(start) == '#')) {
                    start++;
                }
                String hashtag = message.subSequence(start, end).toString();
                if (this.hashtagsByText == null) {
                    this.hashtagsByText = new HashMap();
                    this.hashtags = new ArrayList();
                }
                HashtagObject hashtagObject = (HashtagObject) this.hashtagsByText.get(hashtag);
                if (hashtagObject == null) {
                    hashtagObject = new HashtagObject();
                    hashtagObject.hashtag = hashtag;
                    this.hashtagsByText.put(hashtagObject.hashtag, hashtagObject);
                } else {
                    this.hashtags.remove(hashtagObject);
                }
                hashtagObject.date = (int) (System.currentTimeMillis() / 1000);
                this.hashtags.add(0, hashtagObject);
                changed = true;
            }
            if (changed) {
                putRecentHashtags(this.hashtags);
            }
        }
    }

    private void putRecentHashtags(ArrayList<HashtagObject> arrayList) {
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new SearchAdapterHelper$$Lambda$4(this, arrayList));
    }

    final /* synthetic */ void lambda$putRecentHashtags$9$SearchAdapterHelper(ArrayList arrayList) {
        try {
            MessagesStorage.getInstance(this.currentAccount).getDatabase().beginTransaction();
            SQLitePreparedStatement state = MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("REPLACE INTO hashtag_recent_v2 VALUES(?, ?)");
            int a = 0;
            while (a < arrayList.size() && a != 100) {
                HashtagObject hashtagObject = (HashtagObject) arrayList.get(a);
                state.requery();
                state.bindString(1, hashtagObject.hashtag);
                state.bindInteger(2, hashtagObject.date);
                state.step();
                a++;
            }
            state.dispose();
            MessagesStorage.getInstance(this.currentAccount).getDatabase().commitTransaction();
            if (arrayList.size() >= 100) {
                MessagesStorage.getInstance(this.currentAccount).getDatabase().beginTransaction();
                for (a = 100; a < arrayList.size(); a++) {
                    MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("DELETE FROM hashtag_recent_v2 WHERE id = '" + ((HashtagObject) arrayList.get(a)).hashtag + "'").stepThis().dispose();
                }
                MessagesStorage.getInstance(this.currentAccount).getDatabase().commitTransaction();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public ArrayList<TLObject> getGlobalSearch() {
        return this.globalSearch;
    }

    public ArrayList<TLObject> getLocalServerSearch() {
        return this.localServerSearch;
    }

    public ArrayList<TLRPC$ChannelParticipant> getGroupSearch() {
        return this.groupSearch;
    }

    public ArrayList<TLRPC$ChannelParticipant> getGroupSearch2() {
        return this.groupSearch2;
    }

    public ArrayList<HashtagObject> getHashtags() {
        return this.hashtags;
    }

    public String getLastFoundUsername() {
        return this.lastFoundUsername;
    }

    public String getLastFoundChannel() {
        return this.lastFoundChannel;
    }

    public String getLastFoundChannel2() {
        return this.lastFoundChannel2;
    }

    public void clearRecentHashtags() {
        this.hashtags = new ArrayList();
        this.hashtagsByText = new HashMap();
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new SearchAdapterHelper$$Lambda$5(this));
    }

    final /* synthetic */ void lambda$clearRecentHashtags$10$SearchAdapterHelper() {
        try {
            MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("DELETE FROM hashtag_recent_v2 WHERE 1").stepThis().dispose();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void setHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        this.hashtags = arrayList;
        this.hashtagsByText = hashMap;
        this.hashtagsLoadedFromDb = true;
        this.delegate.onSetHashtags(arrayList, hashMap);
    }
}
