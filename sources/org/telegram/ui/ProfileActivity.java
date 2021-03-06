package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import com.baranak.turbogramf.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.gms.measurement.AppMeasurement.Param;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter$NotificationCenterDelegate;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$BotInfo;
import org.telegram.tgnet.TLRPC$ChannelParticipant;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$ChatFull;
import org.telegram.tgnet.TLRPC$ChatParticipant;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$InputFile;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$TL_channelAdminRights;
import org.telegram.tgnet.TLRPC$TL_channelBannedRights;
import org.telegram.tgnet.TLRPC$TL_channelFull;
import org.telegram.tgnet.TLRPC$TL_channelParticipant;
import org.telegram.tgnet.TLRPC$TL_channelParticipantAdmin;
import org.telegram.tgnet.TLRPC$TL_channelParticipantBanned;
import org.telegram.tgnet.TLRPC$TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsBots;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipant;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC$TL_channels_getParticipant;
import org.telegram.tgnet.TLRPC$TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC$TL_chatChannelParticipant;
import org.telegram.tgnet.TLRPC$TL_chatFull;
import org.telegram.tgnet.TLRPC$TL_chatParticipant;
import org.telegram.tgnet.TLRPC$TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC$TL_chatParticipantCreator;
import org.telegram.tgnet.TLRPC$TL_chatParticipants;
import org.telegram.tgnet.TLRPC$TL_chatParticipantsForbidden;
import org.telegram.tgnet.TLRPC$TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC$TL_encryptedChat;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC$TL_secureFile;
import org.telegram.tgnet.TLRPC$TL_userEmpty;
import org.telegram.tgnet.TLRPC$TL_userFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.DialogsActivity.DialogsActivityDelegate;
import org.telegram.ui.PhotoViewer.EmptyPhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PlaceProviderObject;
import turbogram.SpecialContactsActivity;
import turbogram.Utilities.TurboConfig;
import turbogram.Utilities.TurboUtils;

public class ProfileActivity extends BaseFragment implements NotificationCenter$NotificationCenterDelegate, DialogsActivityDelegate {
    private static final int add_contact = 1;
    private static final int add_shortcut = 14;
    private static final int add_to_group = 115;
    private static final int block_contact = 2;
    private static final int call_item = 15;
    private static final int convert_to_supergroup = 13;
    private static final int delete_contact = 5;
    private static final int edit_channel = 12;
    private static final int edit_contact = 4;
    private static final int edit_name = 8;
    private static final int invite_to_group = 9;
    private static final int leave_group = 7;
    private static final int search_members = 16;
    private static final int set_admins = 11;
    private static final int share = 10;
    private static final int share_contact = 3;
    private static final int special_contact = 114;
    private int addMemberRow;
    private SimpleTextView adminTextView;
    private boolean allowProfileAnimation = true;
    private ActionBarMenuItem animatingItem;
    private float animationProgress;
    private AvatarDrawable avatarDrawable;
    private BackupImageView avatarImage;
    private int banFromGroup;
    private TLRPC$BotInfo botInfo;
    private ActionBarMenuItem callItem;
    private int channelInfoRow;
    private int channelNameRow;
    private int chat_id;
    private int convertHelpRow;
    private int convertRow;
    private boolean creatingChat;
    private int creatorID;
    private TLRPC$ChannelParticipant currentChannelParticipant;
    private TLRPC$Chat currentChat;
    private TLRPC$EncryptedChat currentEncryptedChat;
    private long dialog_id;
    private ActionBarMenuItem editItem;
    private int emptyRow;
    private int emptyRowChat;
    private int emptyRowChat2;
    private int extraHeight;
    private int filterType = 0;
    private int filterTypeRow;
    private int filterTypeSectionRow;
    private int groupsInCommonRow;
    private ImageUpdater imageUpdater;
    private TLRPC$ChatFull info;
    private int initialAnimationExtraHeight;
    private boolean isBot;
    private LinearLayoutManager layoutManager;
    private int leaveChannelRow;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int loadMoreMembersRow;
    private boolean loadingUsers;
    private int[] mediaCount = new int[]{-1, -1, -1, -1, -1, -1, -1};
    private int[] mediaMergeCount = new int[]{-1, -1, -1, -1, -1, -1, -1};
    private int membersEndRow;
    private int membersRow;
    private int membersSectionRow;
    private long mergeDialogId;
    private SimpleTextView[] nameTextView = new SimpleTextView[2];
    private int onlineCount = -1;
    private SimpleTextView[] onlineTextView = new SimpleTextView[2];
    private boolean openAnimationInProgress;
    private SparseArray<TLRPC$ChatParticipant> participantsMap = new SparseArray();
    private int phoneRow;
    private boolean playProfileAnimation;
    private PhotoViewerProvider provider = new C20561();
    private boolean recreateMenuAfterAnimation;
    private int rowCount = 0;
    private int sectionRow;
    private int selectedUser;
    private int settingsKeyRow;
    private int settingsNotificationsRow;
    private int settingsTimerRow;
    private int sharedFileRow;
    private int sharedMediaRow;
    private int sharedMusicRow;
    private int sharedPhotoRow;
    private int sharedURLRow;
    private int sharedVideoRow;
    private int sharedVoiceRow;
    private ArrayList<Integer> sortedUsers;
    private int startSecretChatRow;
    private TopView topView;
    private int totalFileCount = -1;
    private int totalFileCountMerge = -1;
    private int totalMediaCount = -1;
    private int totalMediaCountMerge = -1;
    private int totalMusicCount = -1;
    private int totalMusicCountMerge = -1;
    private int totalPhotoCount = -1;
    private int totalPhotoCountMerge = -1;
    private int totalURLCount = -1;
    private int totalURLCountMerge = -1;
    private int totalVideoCount = -1;
    private int totalVideoCountMerge = -1;
    private int totalVoiceCount = -1;
    private int totalVoiceCountMerge = -1;
    private boolean userBlocked;
    private int userInfoDetailedRow;
    private int userInfoRow;
    private int userSectionRow;
    private int user_id;
    private int usernameRow;
    private boolean usersEndReached;
    private ImageView writeButton;
    private AnimatorSet writeButtonAnimation;

    /* renamed from: org.telegram.ui.ProfileActivity$1 */
    class C20561 extends EmptyPhotoViewerProvider {
        C20561() {
        }

        public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC$FileLocation fileLocation, int index) {
            PlaceProviderObject placeProviderObject = null;
            int i = 0;
            if (fileLocation != null) {
                TLRPC$FileLocation photoBig = null;
                if (ProfileActivity.this.user_id != 0) {
                    User user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (!(user == null || user.photo == null || user.photo.photo_big == null)) {
                        photoBig = user.photo.photo_big;
                    }
                } else if (ProfileActivity.this.chat_id != 0) {
                    TLRPC$Chat chat = MessagesController.getInstance(ProfileActivity.this.currentAccount).getChat(Integer.valueOf(ProfileActivity.this.chat_id));
                    if (!(chat == null || chat.photo == null || chat.photo.photo_big == null)) {
                        photoBig = chat.photo.photo_big;
                    }
                }
                if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                    int[] coords = new int[2];
                    ProfileActivity.this.avatarImage.getLocationInWindow(coords);
                    placeProviderObject = new PlaceProviderObject();
                    placeProviderObject.viewX = coords[0];
                    int i2 = coords[1];
                    if (VERSION.SDK_INT < 21) {
                        i = AndroidUtilities.statusBarHeight;
                    }
                    placeProviderObject.viewY = i2 - i;
                    placeProviderObject.parentView = ProfileActivity.this.avatarImage;
                    placeProviderObject.imageReceiver = ProfileActivity.this.avatarImage.getImageReceiver();
                    if (ProfileActivity.this.user_id != 0) {
                        placeProviderObject.dialogId = ProfileActivity.this.user_id;
                    } else if (ProfileActivity.this.chat_id != 0) {
                        placeProviderObject.dialogId = -ProfileActivity.this.chat_id;
                    }
                    placeProviderObject.thumb = placeProviderObject.imageReceiver.getBitmapSafe();
                    placeProviderObject.size = -1;
                    placeProviderObject.radius = ProfileActivity.this.avatarImage.getImageReceiver().getRoundRadius();
                    placeProviderObject.scale = ProfileActivity.this.avatarImage.getScaleX();
                }
            }
            return placeProviderObject;
        }

        public void willHidePhotoViewer() {
            ProfileActivity.this.avatarImage.getImageReceiver().setVisible(true, true);
        }
    }

    /* renamed from: org.telegram.ui.ProfileActivity$3 */
    class C20583 extends ActionBarMenuOnItemClick {
        C20583() {
        }

        public void onItemClick(int id) {
            if (ProfileActivity.this.getParentActivity() != null) {
                if (id == -1) {
                    ProfileActivity.this.finishFragment();
                } else if (id == 2) {
                    if (MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id)) == null) {
                        return;
                    }
                    if (!ProfileActivity.this.isBot) {
                        builder = new Builder(ProfileActivity.this.getParentActivity());
                        if (ProfileActivity.this.userBlocked) {
                            builder.setMessage(LocaleController.getString("AreYouSureUnblockContact", R.string.AreYouSureUnblockContact));
                        } else {
                            builder.setMessage(LocaleController.getString("AreYouSureBlockContact", R.string.AreYouSureBlockContact));
                        }
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new ProfileActivity$3$$Lambda$0(this));
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        ProfileActivity.this.showDialog(builder.create());
                    } else if (ProfileActivity.this.userBlocked) {
                        MessagesController.getInstance(ProfileActivity.this.currentAccount).unblockUser(ProfileActivity.this.user_id);
                        SendMessagesHelper.getInstance(ProfileActivity.this.currentAccount).sendMessage("/start", (long) ProfileActivity.this.user_id, null, null, false, null, null, null);
                        ProfileActivity.this.finishFragment();
                    } else {
                        MessagesController.getInstance(ProfileActivity.this.currentAccount).blockUser(ProfileActivity.this.user_id);
                    }
                } else if (id == 1) {
                    user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    args = new Bundle();
                    args.putInt("user_id", user.id);
                    args.putBoolean("addContact", true);
                    ProfileActivity.this.presentFragment(new ContactAddActivity(args));
                } else if (id == 3) {
                    args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putString("selectAlertString", LocaleController.getString("SendContactTo", R.string.SendContactTo));
                    args.putString("selectAlertStringGroup", LocaleController.getString("SendContactToGroup", R.string.SendContactToGroup));
                    r0 = new DialogsActivity(args);
                    r0.setDelegate(ProfileActivity.this);
                    ProfileActivity.this.presentFragment(r0);
                } else if (id == 4) {
                    args = new Bundle();
                    args.putInt("user_id", ProfileActivity.this.user_id);
                    ProfileActivity.this.presentFragment(new ContactAddActivity(args));
                } else if (id == 5) {
                    user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null && ProfileActivity.this.getParentActivity() != null) {
                        builder = new Builder(ProfileActivity.this.getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureDeleteContact", R.string.AreYouSureDeleteContact));
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new ProfileActivity$3$$Lambda$1(this, user));
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        ProfileActivity.this.showDialog(builder.create());
                    }
                } else if (id == 7) {
                    ProfileActivity.this.leaveChatPressed();
                } else if (id == 8) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    ProfileActivity.this.presentFragment(new ChangeChatNameActivity(args));
                } else if (id == 12) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    r0 = new ChannelEditActivity(args);
                    r0.setInfo(ProfileActivity.this.info);
                    ProfileActivity.this.presentFragment(r0);
                } else if (id == 9) {
                    user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null) {
                        args = new Bundle();
                        args.putBoolean("onlySelect", true);
                        args.putInt("dialogsType", 2);
                        args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", R.string.AddToTheGroupTitle, new Object[]{UserObject.getUserName(user), "%1$s"}));
                        r0 = new DialogsActivity(args);
                        r0.setDelegate(new ProfileActivity$3$$Lambda$2(this, user));
                        ProfileActivity.this.presentFragment(r0);
                    }
                } else if (id == 10) {
                    try {
                        if (MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id)) != null) {
                            Intent intent = new Intent("android.intent.action.SEND");
                            intent.setType("text/plain");
                            TLRPC$TL_userFull userFull = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.botInfo.user_id);
                            if (ProfileActivity.this.botInfo == null || userFull == null || TextUtils.isEmpty(userFull.about)) {
                                intent = intent;
                                intent.putExtra("android.intent.extra.TEXT", String.format("https://" + MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/%s", new Object[]{user.username}));
                            } else {
                                intent = intent;
                                intent.putExtra("android.intent.extra.TEXT", String.format("%s https://" + MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/%s", new Object[]{userFull.about, user.username}));
                            }
                            ProfileActivity.this.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("BotShare", R.string.BotShare)), 500);
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                } else if (id == 11) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    r0 = new SetAdminsActivity(args);
                    r0.setChatInfo(ProfileActivity.this.info);
                    ProfileActivity.this.presentFragment(r0);
                } else if (id == 13) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    ProfileActivity.this.presentFragment(new ConvertGroupActivity(args));
                } else if (id == 14) {
                    try {
                        long did;
                        if (ProfileActivity.this.currentEncryptedChat != null) {
                            did = ((long) ProfileActivity.this.currentEncryptedChat.id) << 32;
                        } else if (ProfileActivity.this.user_id != 0) {
                            did = (long) ProfileActivity.this.user_id;
                        } else if (ProfileActivity.this.chat_id != 0) {
                            did = (long) (-ProfileActivity.this.chat_id);
                        } else {
                            return;
                        }
                        DataQuery.getInstance(ProfileActivity.this.currentAccount).installShortcut(did);
                    } catch (Exception e2) {
                        FileLog.e(e2);
                    }
                } else if (id == 15) {
                    user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null) {
                        VoIPHelper.startCall(user, ProfileActivity.this.getParentActivity(), MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(user.id));
                    }
                } else if (id == 16) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    if (ChatObject.isChannel(ProfileActivity.this.currentChat)) {
                        args.putInt(Param.TYPE, 2);
                        args.putBoolean("open_search", true);
                        ProfileActivity.this.presentFragment(new ChannelUsersActivity(args));
                        return;
                    }
                    ChatUsersActivity chatUsersActivity = new ChatUsersActivity(args);
                    chatUsersActivity.setInfo(ProfileActivity.this.info);
                    ProfileActivity.this.presentFragment(chatUsersActivity);
                } else if (id == ProfileActivity.special_contact) {
                    if (TurboConfig.containValue("specific_c" + ProfileActivity.this.user_id)) {
                        TurboConfig.removeValue("specific_c" + ProfileActivity.this.user_id);
                    } else {
                        TurboConfig.setIntValue("specific_c" + ProfileActivity.this.user_id, ProfileActivity.this.user_id);
                    }
                    TurboUtils.showToast(ProfileActivity.this.getParentActivity(), LocaleController.getString("Done", R.string.Done), 1);
                    ProfileActivity.this.presentFragment(new SpecialContactsActivity());
                } else if (id == ProfileActivity.add_to_group) {
                    ProfileActivity.this.addToGroups();
                }
            }
        }

        final /* synthetic */ void lambda$onItemClick$0$ProfileActivity$3(DialogInterface dialogInterface, int i) {
            if (ProfileActivity.this.userBlocked) {
                MessagesController.getInstance(ProfileActivity.this.currentAccount).unblockUser(ProfileActivity.this.user_id);
            } else {
                MessagesController.getInstance(ProfileActivity.this.currentAccount).blockUser(ProfileActivity.this.user_id);
            }
        }

        final /* synthetic */ void lambda$onItemClick$1$ProfileActivity$3(User user, DialogInterface dialogInterface, int i) {
            ArrayList<User> arrayList = new ArrayList();
            arrayList.add(user);
            ContactsController.getInstance(ProfileActivity.this.currentAccount).deleteContact(arrayList);
        }

        final /* synthetic */ void lambda$onItemClick$2$ProfileActivity$3(User user, DialogsActivity fragment1, ArrayList dids, CharSequence message, boolean param) {
            long did = ((Long) dids.get(0)).longValue();
            Bundle args1 = new Bundle();
            args1.putBoolean("scrollToTopOnResume", true);
            args1.putInt("chat_id", -((int) did));
            if (MessagesController.getInstance(ProfileActivity.this.currentAccount).checkCanOpenChat(args1, fragment1)) {
                NotificationCenter.getInstance(ProfileActivity.this.currentAccount).removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                NotificationCenter.getInstance(ProfileActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                MessagesController.getInstance(ProfileActivity.this.currentAccount).addUserToChat(-((int) did), user, null, 0, null, ProfileActivity.this);
                ProfileActivity.this.presentFragment(new ChatActivity(args1), true);
                ProfileActivity.this.removeSelfFromStack();
            }
        }
    }

    /* renamed from: org.telegram.ui.ProfileActivity$8 */
    class C20638 extends ViewOutlineProvider {
        C20638() {
        }

        @SuppressLint({"NewApi"})
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
        }
    }

    /* renamed from: org.telegram.ui.ProfileActivity$9 */
    class C20649 extends OnScrollListener {
        C20649() {
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            ProfileActivity.this.checkListViewScroll();
            if (ProfileActivity.this.participantsMap != null && ProfileActivity.this.loadMoreMembersRow != -1 && ProfileActivity.this.layoutManager.findLastVisibleItemPosition() > ProfileActivity.this.loadMoreMembersRow - 8) {
                ProfileActivity.this.getChannelParticipants(false);
            }
        }
    }

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            CombinedDrawable combinedDrawable;
            switch (viewType) {
                case 0:
                    view = new EmptyCell(this.mContext);
                    break;
                case 1:
                    view = new DividerCell(this.mContext);
                    view.setPadding(AndroidUtilities.dp(72.0f), 0, 0, 0);
                    break;
                case 2:
                    view = new TextDetailCell(this.mContext);
                    break;
                case 3:
                    view = new TextCell(this.mContext);
                    break;
                case 4:
                    view = new UserCell(this.mContext, 61, 0, true);
                    break;
                case 5:
                    view = new ShadowSectionCell(this.mContext);
                    combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), Theme.getThemedDrawable(this.mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    combinedDrawable.setFullsize(true);
                    view.setBackgroundDrawable(combinedDrawable);
                    break;
                case 6:
                    view = new TextInfoPrivacyCell(this.mContext);
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) view;
                    combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), Theme.getThemedDrawable(this.mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    combinedDrawable.setFullsize(true);
                    cell.setBackgroundDrawable(combinedDrawable);
                    cell.setText(AndroidUtilities.replaceTags(LocaleController.formatString("ConvertGroupInfo", R.string.ConvertGroupInfo, new Object[]{LocaleController.formatPluralString("Members", MessagesController.getInstance(ProfileActivity.this.currentAccount).maxMegagroupCount)})));
                    break;
                case 7:
                    view = new LoadingCell(this.mContext);
                    break;
                case 8:
                    view = new AboutLinkCell(this.mContext);
                    ((AboutLinkCell) view).setDelegate(new ProfileActivity$ListAdapter$$Lambda$0(this));
                    break;
            }
            view.setLayoutParams(new LayoutParams(-1, -2));
            return new Holder(view);
        }

        final /* synthetic */ void lambda$onCreateViewHolder$0$ProfileActivity$ListAdapter(String url) {
            if (url.startsWith("@")) {
                MessagesController.getInstance(ProfileActivity.this.currentAccount).openByUserName(url.substring(1), ProfileActivity.this, 0);
            } else if (url.startsWith("#")) {
                DialogsActivity fragment = new DialogsActivity(null);
                fragment.setSearchString(url);
                ProfileActivity.this.presentFragment(fragment);
            } else if (url.startsWith("/") && ProfileActivity.this.parentLayout.fragmentsStack.size() > 1) {
                BaseFragment previousFragment = (BaseFragment) ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2);
                if (previousFragment instanceof ChatActivity) {
                    ProfileActivity.this.finishFragment();
                    ((ChatActivity) previousFragment).chatActivityEnterView.setCommand(null, url, false, false);
                }
            }
        }

        public void onBindViewHolder(ViewHolder holder, int i) {
            String text;
            TLRPC$TL_userFull userFull;
            switch (holder.getItemViewType()) {
                case 0:
                    if (i == ProfileActivity.this.emptyRowChat || i == ProfileActivity.this.emptyRowChat2) {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(8.0f));
                        return;
                    } else {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(36.0f));
                        return;
                    }
                case 2:
                    TextDetailCell textDetailCell = (TextDetailCell) holder.itemView;
                    textDetailCell.setMultiline(false);
                    User user;
                    if (i == ProfileActivity.this.phoneRow) {
                        user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                        if (user.phone == null || user.phone.length() == 0) {
                            text = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                        } else {
                            text = PhoneFormat.getInstance().format("+" + user.phone);
                        }
                        textDetailCell.setTextAndValueAndIcon(text, LocaleController.getString("PhoneMobile", R.string.PhoneMobile), R.drawable.profile_phone, 0);
                        return;
                    } else if (i == ProfileActivity.this.usernameRow) {
                        user = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                        if (user == null || TextUtils.isEmpty(user.username)) {
                            text = "-";
                        } else {
                            text = "@" + user.username;
                        }
                        if (ProfileActivity.this.phoneRow == -1 && ProfileActivity.this.userInfoRow == -1 && ProfileActivity.this.userInfoDetailedRow == -1) {
                            textDetailCell.setTextAndValueAndIcon(text, LocaleController.getString("Username", R.string.Username), R.drawable.profile_info, 11);
                            return;
                        } else {
                            textDetailCell.setTextAndValue(text, LocaleController.getString("Username", R.string.Username));
                            return;
                        }
                    } else if (i == ProfileActivity.this.channelNameRow) {
                        if (ProfileActivity.this.currentChat == null || TextUtils.isEmpty(ProfileActivity.this.currentChat.username)) {
                            text = "-";
                        } else {
                            text = "@" + ProfileActivity.this.currentChat.username;
                        }
                        textDetailCell.setTextAndValue(text, MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/" + ProfileActivity.this.currentChat.username);
                        return;
                    } else if (i == ProfileActivity.this.userInfoDetailedRow) {
                        userFull = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
                        textDetailCell.setMultiline(true);
                        textDetailCell.setTextAndValueAndIcon(userFull != null ? userFull.about : null, LocaleController.getString("UserBio", R.string.UserBio), R.drawable.profile_info, 11);
                        return;
                    } else {
                        return;
                    }
                case 3:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    textCell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    String value;
                    String str;
                    if (i == ProfileActivity.this.sharedMediaRow) {
                        if (ProfileActivity.this.totalMediaCount == -1) {
                            value = LocaleController.getString("Loading", R.string.Loading);
                        } else {
                            str = "%d";
                            Object[] objArr = new Object[1];
                            objArr[0] = Integer.valueOf((ProfileActivity.this.totalMediaCountMerge != -1 ? ProfileActivity.this.totalMediaCountMerge : 0) + ProfileActivity.this.totalMediaCount);
                            value = String.format(str, objArr);
                        }
                        if (ProfileActivity.this.user_id == 0 || UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId() != ProfileActivity.this.user_id) {
                            textCell.setTextAndValue(LocaleController.getString("SharedMedia", R.string.SharedMedia), value);
                            return;
                        } else {
                            textCell.setTextAndValueAndIcon(LocaleController.getString("SharedMedia", R.string.SharedMedia), value, R.drawable.profile_list);
                            return;
                        }
                    } else if (i == ProfileActivity.this.sharedPhotoRow || i == ProfileActivity.this.sharedVideoRow || i == ProfileActivity.this.sharedFileRow || i == ProfileActivity.this.sharedURLRow || i == ProfileActivity.this.sharedMusicRow || i == ProfileActivity.this.sharedVoiceRow) {
                        ProfileActivity.this.setRowTextAndValue(i, textCell);
                        return;
                    } else if (i == ProfileActivity.this.groupsInCommonRow) {
                        userFull = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
                        str = LocaleController.getString("GroupsInCommon", R.string.GroupsInCommon);
                        String str2 = "%d";
                        Object[] objArr2 = new Object[1];
                        objArr2[0] = Integer.valueOf(userFull != null ? userFull.common_chats_count : 0);
                        textCell.setTextAndValue(str, String.format(str2, objArr2));
                        return;
                    } else if (i == ProfileActivity.this.settingsTimerRow) {
                        TLRPC$EncryptedChat encryptedChat = MessagesController.getInstance(ProfileActivity.this.currentAccount).getEncryptedChat(Integer.valueOf((int) (ProfileActivity.this.dialog_id >> 32)));
                        if (encryptedChat.ttl == 0) {
                            value = LocaleController.getString("ShortMessageLifetimeForever", R.string.ShortMessageLifetimeForever);
                        } else {
                            value = LocaleController.formatTTLString(encryptedChat.ttl);
                        }
                        textCell.setTextAndValue(LocaleController.getString("MessageLifetime", R.string.MessageLifetime), value);
                        return;
                    } else if (i == ProfileActivity.this.settingsNotificationsRow) {
                        long did;
                        String val;
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(ProfileActivity.this.currentAccount);
                        if (ProfileActivity.this.dialog_id != 0) {
                            did = ProfileActivity.this.dialog_id;
                        } else if (ProfileActivity.this.user_id != 0) {
                            did = (long) ProfileActivity.this.user_id;
                        } else {
                            did = (long) (-ProfileActivity.this.chat_id);
                        }
                        boolean custom = preferences.getBoolean("custom_" + did, false);
                        boolean hasOverride = preferences.contains("notify2_" + did);
                        int value2 = preferences.getInt("notify2_" + did, 0);
                        int delta = preferences.getInt("notifyuntil_" + did, 0);
                        if (value2 != 3 || delta == Integer.MAX_VALUE) {
                            boolean enabled;
                            if (value2 == 0) {
                                if (hasOverride) {
                                    enabled = true;
                                } else if (((int) did) < 0) {
                                    enabled = preferences.getBoolean("EnableGroup", true);
                                } else {
                                    enabled = preferences.getBoolean("EnableAll", true);
                                }
                            } else if (value2 == 1) {
                                enabled = true;
                            } else if (value2 == 2) {
                                enabled = false;
                            } else {
                                enabled = false;
                            }
                            if (enabled && custom) {
                                val = LocaleController.getString("NotificationsCustom", R.string.NotificationsCustom);
                            } else if (hasOverride) {
                                val = enabled ? LocaleController.getString("NotificationsOn", R.string.NotificationsOn) : LocaleController.getString("NotificationsOff", R.string.NotificationsOff);
                            } else {
                                val = enabled ? LocaleController.getString("NotificationsDefaultOn", R.string.NotificationsDefaultOn) : LocaleController.getString("NotificationsDefaultOff", R.string.NotificationsDefaultOff);
                            }
                        } else {
                            delta -= ConnectionsManager.getInstance(ProfileActivity.this.currentAccount).getCurrentTime();
                            if (delta <= 0) {
                                if (custom) {
                                    val = LocaleController.getString("NotificationsCustom", R.string.NotificationsCustom);
                                } else {
                                    val = LocaleController.getString("NotificationsOn", R.string.NotificationsOn);
                                }
                            } else if (delta < 3600) {
                                val = LocaleController.formatString("WillUnmuteIn", R.string.WillUnmuteIn, new Object[]{LocaleController.formatPluralString("Minutes", delta / 60)});
                            } else if (delta < 86400) {
                                val = LocaleController.formatString("WillUnmuteIn", R.string.WillUnmuteIn, new Object[]{LocaleController.formatPluralString("Hours", (int) Math.ceil((double) ((((float) delta) / 60.0f) / 60.0f)))});
                            } else if (delta < 31536000) {
                                val = LocaleController.formatString("WillUnmuteIn", R.string.WillUnmuteIn, new Object[]{LocaleController.formatPluralString("Days", (int) Math.ceil((double) (((((float) delta) / 60.0f) / 60.0f) / 24.0f)))});
                            } else {
                                val = null;
                            }
                        }
                        if (val != null) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString("Notifications", R.string.Notifications), val, R.drawable.profile_list);
                            return;
                        } else {
                            textCell.setTextAndValueAndIcon(LocaleController.getString("Notifications", R.string.Notifications), LocaleController.getString("NotificationsOff", R.string.NotificationsOff), R.drawable.profile_list);
                            return;
                        }
                    } else if (i == ProfileActivity.this.startSecretChatRow) {
                        textCell.setText(LocaleController.getString("StartEncryptedChat", R.string.StartEncryptedChat));
                        textCell.setTag(Theme.key_windowBackgroundWhiteGreenText2);
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText2));
                        return;
                    } else if (i == ProfileActivity.this.settingsKeyRow) {
                        Drawable identiconDrawable = new IdenticonDrawable();
                        identiconDrawable.setEncryptedChat(MessagesController.getInstance(ProfileActivity.this.currentAccount).getEncryptedChat(Integer.valueOf((int) (ProfileActivity.this.dialog_id >> 32))));
                        textCell.setTextAndValueDrawable(LocaleController.getString("EncryptionKey", R.string.EncryptionKey), identiconDrawable);
                        return;
                    } else if (i == ProfileActivity.this.leaveChannelRow) {
                        textCell.setTag(Theme.key_windowBackgroundWhiteRedText5);
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText5));
                        textCell.setText(LocaleController.getString("LeaveChannel", R.string.LeaveChannel));
                        return;
                    } else if (i == ProfileActivity.this.convertRow) {
                        textCell.setText(LocaleController.getString("UpgradeGroup", R.string.UpgradeGroup));
                        textCell.setTag(Theme.key_windowBackgroundWhiteGreenText2);
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText2));
                        return;
                    } else if (i == ProfileActivity.this.addMemberRow) {
                        if (ProfileActivity.this.chat_id > 0) {
                            textCell.setText(LocaleController.getString("AddMember", R.string.AddMember));
                            return;
                        } else {
                            textCell.setText(LocaleController.getString("AddRecipient", R.string.AddRecipient));
                            return;
                        }
                    } else if (i == ProfileActivity.this.membersRow) {
                        if (ProfileActivity.this.info != null) {
                            if (!ChatObject.isChannel(ProfileActivity.this.currentChat) || ProfileActivity.this.currentChat.megagroup) {
                                textCell.setTextAndValue(LocaleController.getString("ChannelMembers", R.string.ChannelMembers), String.format("%d", new Object[]{Integer.valueOf(ProfileActivity.this.info.participants_count)}));
                                return;
                            } else {
                                textCell.setTextAndValue(LocaleController.getString("ChannelSubscribers", R.string.ChannelSubscribers), String.format("%d", new Object[]{Integer.valueOf(ProfileActivity.this.info.participants_count)}));
                                return;
                            }
                        } else if (!ChatObject.isChannel(ProfileActivity.this.currentChat) || ProfileActivity.this.currentChat.megagroup) {
                            textCell.setText(LocaleController.getString("ChannelMembers", R.string.ChannelMembers));
                            return;
                        } else {
                            textCell.setText(LocaleController.getString("ChannelSubscribers", R.string.ChannelSubscribers));
                            return;
                        }
                    } else if (i == ProfileActivity.this.filterTypeRow) {
                        String status = null;
                        if (ProfileActivity.this.filterType == 0) {
                            status = LocaleController.getString("All", R.string.All);
                        } else if (ProfileActivity.this.filterType == 1) {
                            status = LocaleController.getString("Administrators", R.string.Administrators);
                        } else if (ProfileActivity.this.filterType == 2) {
                            status = LocaleController.getString("Bots", R.string.Bots);
                        }
                        textCell.setTextAndValue(LocaleController.getString("MembersFilter", R.string.MembersFilter), status);
                        return;
                    } else {
                        return;
                    }
                case 4:
                    TLRPC$ChatParticipant part;
                    UserCell userCell = (UserCell) holder.itemView;
                    if (ProfileActivity.this.sortedUsers.isEmpty()) {
                        part = (TLRPC$ChatParticipant) ProfileActivity.this.info.participants.participants.get((i - ProfileActivity.this.emptyRowChat2) - 1);
                    } else {
                        part = (TLRPC$ChatParticipant) ProfileActivity.this.info.participants.participants.get(((Integer) ProfileActivity.this.sortedUsers.get((i - ProfileActivity.this.emptyRowChat2) - 1)).intValue());
                    }
                    if (part != null) {
                        if (part instanceof TLRPC$TL_chatChannelParticipant) {
                            TLRPC$ChannelParticipant channelParticipant = ((TLRPC$TL_chatChannelParticipant) part).channelParticipant;
                            if (channelParticipant instanceof TLRPC$TL_channelParticipantCreator) {
                                userCell.setIsAdmin(1);
                            } else if (channelParticipant instanceof TLRPC$TL_channelParticipantAdmin) {
                                userCell.setIsAdmin(2);
                            } else {
                                userCell.setIsAdmin(0);
                            }
                        } else if (part instanceof TLRPC$TL_chatParticipantCreator) {
                            userCell.setIsAdmin(1);
                        } else if (ProfileActivity.this.currentChat.admins_enabled && (part instanceof TLRPC$TL_chatParticipantAdmin)) {
                            userCell.setIsAdmin(2);
                        } else {
                            userCell.setIsAdmin(0);
                        }
                        userCell.setData(MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(part.user_id)), null, null, 0);
                        return;
                    }
                    return;
                case 8:
                    AboutLinkCell aboutLinkCell = holder.itemView;
                    if (i == ProfileActivity.this.userInfoRow) {
                        userFull = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
                        aboutLinkCell.setTextAndIcon(userFull != null ? userFull.about : null, R.drawable.profile_info, ProfileActivity.this.isBot);
                        return;
                    } else if (i == ProfileActivity.this.channelInfoRow) {
                        text = ProfileActivity.this.info.about;
                        while (text.contains("\n\n\n")) {
                            text = text.replace("\n\n\n", "\n\n");
                        }
                        aboutLinkCell.setTextAndIcon(text, R.drawable.profile_info, true);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }

        public boolean isEnabled(ViewHolder holder) {
            int i = holder.getAdapterPosition();
            if (ProfileActivity.this.user_id != 0) {
                if (i == ProfileActivity.this.phoneRow || i == ProfileActivity.this.settingsTimerRow || i == ProfileActivity.this.settingsKeyRow || i == ProfileActivity.this.settingsNotificationsRow || i == ProfileActivity.this.sharedMediaRow || i == ProfileActivity.this.startSecretChatRow || i == ProfileActivity.this.usernameRow || i == ProfileActivity.this.userInfoRow || i == ProfileActivity.this.groupsInCommonRow || i == ProfileActivity.this.userInfoDetailedRow || i == ProfileActivity.this.sharedPhotoRow || i == ProfileActivity.this.sharedVideoRow || i == ProfileActivity.this.sharedFileRow || i == ProfileActivity.this.sharedURLRow || i == ProfileActivity.this.sharedMusicRow || i == ProfileActivity.this.sharedVoiceRow) {
                    return true;
                }
                return false;
            } else if (ProfileActivity.this.chat_id == 0) {
                return false;
            } else {
                if (i == ProfileActivity.this.convertRow || i == ProfileActivity.this.settingsNotificationsRow || i == ProfileActivity.this.sharedMediaRow || ((i > ProfileActivity.this.emptyRowChat2 && i < ProfileActivity.this.membersEndRow) || i == ProfileActivity.this.addMemberRow || i == ProfileActivity.this.channelNameRow || i == ProfileActivity.this.leaveChannelRow || i == ProfileActivity.this.channelInfoRow || i == ProfileActivity.this.membersRow || i == ProfileActivity.this.filterTypeRow || i == ProfileActivity.this.sharedPhotoRow || i == ProfileActivity.this.sharedVideoRow || i == ProfileActivity.this.sharedFileRow || i == ProfileActivity.this.sharedURLRow || i == ProfileActivity.this.sharedMusicRow || i == ProfileActivity.this.sharedVoiceRow)) {
                    return true;
                }
                return false;
            }
        }

        public int getItemCount() {
            return ProfileActivity.this.rowCount;
        }

        public int getItemViewType(int i) {
            if (i == ProfileActivity.this.emptyRow || i == ProfileActivity.this.emptyRowChat || i == ProfileActivity.this.emptyRowChat2) {
                return 0;
            }
            if (i == ProfileActivity.this.sectionRow || i == ProfileActivity.this.userSectionRow) {
                return 1;
            }
            if (i == ProfileActivity.this.phoneRow || i == ProfileActivity.this.usernameRow || i == ProfileActivity.this.channelNameRow || i == ProfileActivity.this.userInfoDetailedRow) {
                return 2;
            }
            if (i == ProfileActivity.this.leaveChannelRow || i == ProfileActivity.this.sharedMediaRow || i == ProfileActivity.this.settingsTimerRow || i == ProfileActivity.this.settingsNotificationsRow || i == ProfileActivity.this.startSecretChatRow || i == ProfileActivity.this.settingsKeyRow || i == ProfileActivity.this.convertRow || i == ProfileActivity.this.addMemberRow || i == ProfileActivity.this.groupsInCommonRow || i == ProfileActivity.this.membersRow || i == ProfileActivity.this.sharedPhotoRow || i == ProfileActivity.this.sharedVideoRow || i == ProfileActivity.this.sharedFileRow || i == ProfileActivity.this.sharedURLRow || i == ProfileActivity.this.sharedMusicRow || i == ProfileActivity.this.sharedVoiceRow || i == ProfileActivity.this.filterTypeRow) {
                return 3;
            }
            if (i > ProfileActivity.this.emptyRowChat2 && i < ProfileActivity.this.membersEndRow) {
                return 4;
            }
            if (i == ProfileActivity.this.membersSectionRow || i == ProfileActivity.this.filterTypeSectionRow) {
                return 5;
            }
            if (i == ProfileActivity.this.convertHelpRow) {
                return 6;
            }
            if (i == ProfileActivity.this.loadMoreMembersRow) {
                return 7;
            }
            if (i == ProfileActivity.this.userInfoRow || i == ProfileActivity.this.channelInfoRow) {
                return 8;
            }
            return 0;
        }
    }

    private class TopView extends View {
        private int currentColor;
        private Paint paint = new Paint();

        public TopView(Context context) {
            super(context);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), ((ProfileActivity.this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight()) + AndroidUtilities.dp(91.0f));
        }

        public void setBackgroundColor(int color) {
            if (color != this.currentColor) {
                this.paint.setColor(color);
                invalidate();
            }
        }

        protected void onDraw(Canvas canvas) {
            int height = getMeasuredHeight() - AndroidUtilities.dp(91.0f);
            canvas.drawRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) (ProfileActivity.this.extraHeight + height), this.paint);
            if (ProfileActivity.this.parentLayout != null) {
                ProfileActivity.this.parentLayout.drawHeaderShadow(canvas, ProfileActivity.this.extraHeight + height);
            }
        }
    }

    public ProfileActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        boolean z = false;
        this.user_id = this.arguments.getInt("user_id", 0);
        this.chat_id = this.arguments.getInt("chat_id", 0);
        this.banFromGroup = this.arguments.getInt("ban_chat_id", 0);
        if (this.user_id != 0) {
            this.dialog_id = this.arguments.getLong("dialog_id", 0);
            if (this.dialog_id != 0) {
                this.currentEncryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf((int) (this.dialog_id >> 32)));
            }
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
            if (user == null) {
                return false;
            }
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatCreated);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.blockedUsersDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.botInfoDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.userInfoDidLoaded);
            if (this.currentEncryptedChat != null) {
                NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didReceivedNewMessages);
            }
            if (MessagesController.getInstance(this.currentAccount).blockedUsers.indexOfKey(this.user_id) >= 0) {
                z = true;
            }
            this.userBlocked = z;
            if (user.bot) {
                this.isBot = true;
                DataQuery.getInstance(this.currentAccount).loadBotInfo(user.id, true, this.classGuid);
            }
            MessagesController.getInstance(this.currentAccount).loadFullUser(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id)), this.classGuid, true);
            this.participantsMap = null;
        } else if (this.chat_id == 0) {
            return false;
        } else {
            this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
            if (this.currentChat == null) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new ProfileActivity$$Lambda$0(this, countDownLatch));
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (this.currentChat == null) {
                    return false;
                }
                MessagesController.getInstance(this.currentAccount).putChat(this.currentChat, true);
            }
            if (this.currentChat.megagroup) {
                getChannelParticipants(true);
            } else {
                this.participantsMap = null;
            }
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
            this.sortedUsers = new ArrayList();
            updateOnlineCount();
            this.imageUpdater = new ImageUpdater();
            this.imageUpdater.delegate = new ProfileActivity$$Lambda$1(this);
            this.imageUpdater.parentFragment = this;
            if (ChatObject.isChannel(this.currentChat)) {
                MessagesController.getInstance(this.currentAccount).loadFullChat(this.chat_id, this.classGuid, true);
            }
        }
        if (TurboConfig.separateMedia) {
            getMediaCount();
        } else {
            int a = 0;
            while (a < 7) {
                if (this.dialog_id != 0) {
                    if (((int) this.dialog_id) != 0 || a != 3) {
                        DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, a, this.classGuid, true);
                    }
                } else if (this.user_id != 0) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, a, this.classGuid, true);
                } else if (this.chat_id > 0) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), a, this.classGuid, true);
                    if (this.mergeDialogId != 0) {
                        DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, a, this.classGuid, true);
                    }
                }
                a++;
            }
        }
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mediaCountDidLoaded);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
        updateRowsIds();
        return true;
    }

    final /* synthetic */ void lambda$onFragmentCreate$0$ProfileActivity(CountDownLatch countDownLatch) {
        this.currentChat = MessagesStorage.getInstance(this.currentAccount).getChat(this.chat_id);
        countDownLatch.countDown();
    }

    final /* synthetic */ void lambda$onFragmentCreate$1$ProfileActivity(TLRPC$InputFile file, TLRPC$PhotoSize small, TLRPC$PhotoSize big, TLRPC$TL_secureFile secureFile) {
        if (this.chat_id != 0) {
            MessagesController.getInstance(this.currentAccount).changeChatAvatar(this.chat_id, file);
        }
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaCountDidLoaded);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
        if (this.user_id != 0) {
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatCreated);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.botInfoDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.userInfoDidLoaded);
            MessagesController.getInstance(this.currentAccount).cancelLoadFullUser(this.user_id);
            if (this.currentEncryptedChat != null) {
                NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didReceivedNewMessages);
            }
        } else if (this.chat_id != 0) {
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
            this.imageUpdater.clear();
        }
    }

    protected ActionBar createActionBar(Context context) {
        boolean z;
        ActionBar actionBar = new ActionBar(context) {
            public boolean onTouchEvent(MotionEvent event) {
                return super.onTouchEvent(event);
            }
        };
        int i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
        actionBar.setItemsBackgroundColor(AvatarDrawable.getButtonColorForId(i), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        if (VERSION.SDK_INT < 21 || AndroidUtilities.isTablet()) {
            z = false;
        } else {
            z = true;
        }
        actionBar.setOccupyStatusBar(z);
        return actionBar;
    }

    public View createView(Context context) {
        int i;
        Theme.createProfileResources(context);
        this.hasOwnBackground = true;
        this.extraHeight = AndroidUtilities.dp(88.0f);
        this.actionBar.setActionBarMenuOnItemClick(new C20583());
        createActionBarMenu();
        this.listAdapter = new ListAdapter(context);
        this.avatarDrawable = new AvatarDrawable();
        this.avatarDrawable.setProfile(true);
        this.fragmentView = new FrameLayout(context) {
            public boolean hasOverlappingRendering() {
                return false;
            }

            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                ProfileActivity.this.checkListViewScroll();
            }
        };
        FrameLayout frameLayout = this.fragmentView;
        this.listView = new RecyclerListView(context) {
            public boolean hasOverlappingRendering() {
                return false;
            }
        };
        this.listView.setTag(Integer.valueOf(6));
        this.listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setItemAnimator(null);
        this.listView.setLayoutAnimation(null);
        this.listView.setClipToPadding(false);
        this.layoutManager = new LinearLayoutManager(context) {
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        this.layoutManager.setOrientation(1);
        this.listView.setLayoutManager(this.layoutManager);
        RecyclerListView recyclerListView = this.listView;
        if (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) {
            i = 5;
        } else {
            i = this.chat_id;
        }
        recyclerListView.setGlowColor(AvatarDrawable.getProfileBackColorForId(i));
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new ProfileActivity$$Lambda$2(this));
        this.listView.setOnItemLongClickListener(new ProfileActivity$$Lambda$3(this));
        if (this.banFromGroup != 0) {
            if (this.currentChannelParticipant == null) {
                TLObject req = new TLRPC$TL_channels_getParticipant();
                req.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.banFromGroup);
                req.user_id = MessagesController.getInstance(this.currentAccount).getInputUser(this.user_id);
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new ProfileActivity$$Lambda$4(this));
            }
            FrameLayout frameLayout1 = new FrameLayout(context) {
                protected void onDraw(Canvas canvas) {
                    int bottom = Theme.chat_composeShadowDrawable.getIntrinsicHeight();
                    Theme.chat_composeShadowDrawable.setBounds(0, 0, getMeasuredWidth(), bottom);
                    Theme.chat_composeShadowDrawable.draw(canvas);
                    canvas.drawRect(0.0f, (float) bottom, (float) getMeasuredWidth(), (float) getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
                }
            };
            frameLayout1.setWillNotDraw(false);
            frameLayout.addView(frameLayout1, LayoutHelper.createFrame(-1, 51, 83));
            frameLayout1.setOnClickListener(new ProfileActivity$$Lambda$5(this));
            View textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
            textView.setTextSize(1, 15.0f);
            textView.setGravity(17);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setText(LocaleController.getString("BanFromTheGroup", R.string.BanFromTheGroup));
            frameLayout1.addView(textView, LayoutHelper.createFrame(-2, -2.0f, 17, 0.0f, 1.0f, 0.0f, 0.0f));
            this.listView.setPadding(0, AndroidUtilities.dp(88.0f), 0, AndroidUtilities.dp(48.0f));
            this.listView.setBottomGlowOffset(AndroidUtilities.dp(48.0f));
        } else {
            this.listView.setPadding(0, AndroidUtilities.dp(88.0f), 0, 0);
        }
        this.topView = new TopView(context);
        TopView topView = this.topView;
        if (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) {
            i = 5;
        } else {
            i = this.chat_id;
        }
        topView.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(i));
        frameLayout.addView(this.topView);
        frameLayout.addView(this.actionBar);
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarImage.setPivotX(0.0f);
        this.avatarImage.setPivotY(0.0f);
        frameLayout.addView(this.avatarImage, LayoutHelper.createFrame(42, 42.0f, 51, 64.0f, 0.0f, 0.0f, 0.0f));
        this.avatarImage.setOnClickListener(new ProfileActivity$$Lambda$6(this));
        int a = 0;
        while (a < 2) {
            if (this.playProfileAnimation || a != 0) {
                this.nameTextView[a] = new SimpleTextView(context);
                if (a == 1) {
                    this.nameTextView[a].setTextColor(Theme.getColor(Theme.key_profile_title));
                } else {
                    this.nameTextView[a].setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
                }
                this.nameTextView[a].setTextSize(18);
                this.nameTextView[a].setGravity(3);
                this.nameTextView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                this.nameTextView[a].setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
                this.nameTextView[a].setPivotX(0.0f);
                this.nameTextView[a].setPivotY(0.0f);
                this.nameTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
                frameLayout.addView(this.nameTextView[a], LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, a == 0 ? 48.0f : 0.0f, 0.0f));
                this.onlineTextView[a] = new SimpleTextView(context);
                SimpleTextView simpleTextView = this.onlineTextView[a];
                i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
                simpleTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(i));
                this.onlineTextView[a].setTextSize(14);
                this.onlineTextView[a].setGravity(3);
                this.onlineTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
                this.onlineTextView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                frameLayout.addView(this.onlineTextView[a], LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, a == 0 ? 48.0f : 8.0f, 0.0f));
                this.adminTextView = new SimpleTextView(context);
                simpleTextView = this.adminTextView;
                i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
                simpleTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(i));
                this.adminTextView.setTextSize(14);
                this.adminTextView.setGravity(3);
                this.adminTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                frameLayout.addView(this.adminTextView, LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, 48.0f, 0.0f));
            }
            a++;
        }
        if (this.user_id != 0 || (this.chat_id >= 0 && (!ChatObject.isLeftFromChat(this.currentChat) || ChatObject.isChannel(this.currentChat)))) {
            this.writeButton = new ImageView(context);
            Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0f), Theme.getColor(Theme.key_profile_actionBackground), Theme.getColor(Theme.key_profile_actionPressedBackground));
            if (VERSION.SDK_INT < 21) {
                Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
                shadowDrawable.setColorFilter(new PorterDuffColorFilter(-16777216, Mode.MULTIPLY));
                Drawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
                combinedDrawable.setIconSize(AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                drawable = combinedDrawable;
            }
            this.writeButton.setBackgroundDrawable(drawable);
            this.writeButton.setScaleType(ScaleType.CENTER);
            this.writeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_profile_actionIcon), Mode.MULTIPLY));
            if (this.user_id != 0) {
                this.writeButton.setImageResource(R.drawable.floating_message);
                this.writeButton.setPadding(0, AndroidUtilities.dp(3.0f), 0, 0);
            } else if (this.chat_id != 0) {
                boolean isChannel = ChatObject.isChannel(this.currentChat);
                if ((!isChannel || ChatObject.canEditInfo(this.currentChat)) && (isChannel || this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                    this.writeButton.setImageResource(R.drawable.floating_camera);
                } else {
                    this.writeButton.setImageResource(R.drawable.floating_message);
                    this.writeButton.setPadding(0, AndroidUtilities.dp(3.0f), 0, 0);
                }
            }
            frameLayout.addView(this.writeButton, LayoutHelper.createFrame(VERSION.SDK_INT >= 21 ? 56 : 60, VERSION.SDK_INT >= 21 ? 56.0f : 60.0f, 53, 0.0f, 0.0f, 16.0f, 0.0f));
            if (VERSION.SDK_INT >= 21) {
                StateListAnimator animator = new StateListAnimator();
                animator.addState(new int[]{16842919}, ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[]{(float) AndroidUtilities.dp(2.0f), (float) AndroidUtilities.dp(4.0f)}).setDuration(200));
                animator.addState(new int[0], ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[]{(float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(2.0f)}).setDuration(200));
                this.writeButton.setStateListAnimator(animator);
                this.writeButton.setOutlineProvider(new C20638());
            }
            this.writeButton.setOnClickListener(new ProfileActivity$$Lambda$7(this));
        }
        needLayout();
        this.listView.setOnScrollListener(new C20649());
        return this.fragmentView;
    }

    final /* synthetic */ void lambda$createView$6$ProfileActivity(View view, int position) {
        if (getParentActivity() != null) {
            Builder builder;
            if (position == this.filterTypeRow) {
                builder = new Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("MembersFilter", R.string.MembersFilter));
                builder.setItems(new CharSequence[]{LocaleController.getString("All", R.string.All), LocaleController.getString("Administrators", R.string.Administrators), LocaleController.getString("Bots", R.string.Bots)}, new ProfileActivity$$Lambda$24(this, position));
                showDialog(builder.create());
            } else if (position == this.sharedMediaRow || position == this.sharedPhotoRow || position == this.sharedVideoRow) {
                args = new Bundle();
                if (this.user_id != 0) {
                    args.putLong("dialog_id", this.dialog_id != 0 ? this.dialog_id : (long) this.user_id);
                } else {
                    args.putLong("dialog_id", (long) (-this.chat_id));
                }
                int[] media = new int[7];
                int a = 0;
                while (a < media.length) {
                    media[a] = this.mediaCount[a];
                    if (this.mediaCount[a] >= 0 && this.mediaMergeCount[a] >= 0) {
                        media[a] = this.mediaCount[a] + this.mediaMergeCount[a];
                    } else if (this.mediaCount[a] >= 0) {
                        media[a] = this.mediaCount[a];
                    } else if (this.mediaMergeCount[a] >= 0) {
                        media[a] = this.mediaMergeCount[a];
                    } else {
                        media[a] = -1;
                    }
                    a++;
                }
                args.putInt("selected_mode", 0);
                MediaActivity fragment = new MediaActivity(args, media);
                fragment.setChatInfo(this.info);
                presentFragment(fragment);
            } else if (position == this.sharedFileRow) {
                selectedMode(1);
            } else if (position == this.sharedURLRow) {
                selectedMode(3);
            } else if (position == this.sharedMusicRow) {
                selectedMode(4);
            } else if (position == this.sharedVoiceRow) {
                selectedMode(2);
            } else if (position == this.groupsInCommonRow) {
                presentFragment(new CommonGroupsActivity(this.user_id));
            } else if (position == this.settingsKeyRow) {
                args = new Bundle();
                args.putInt("chat_id", (int) (this.dialog_id >> 32));
                presentFragment(new IdenticonActivity(args));
            } else if (position == this.settingsTimerRow) {
                showDialog(AlertsCreator.createTTLAlert(getParentActivity(), this.currentEncryptedChat).create());
            } else if (position == this.settingsNotificationsRow) {
                long did;
                if (this.dialog_id != 0) {
                    did = this.dialog_id;
                } else if (this.user_id != 0) {
                    did = (long) this.user_id;
                } else {
                    did = (long) (-this.chat_id);
                }
                AlertsCreator.showCustomNotificationsDialog(this, did, this.currentAccount, new ProfileActivity$$Lambda$25(this));
            } else if (position == this.startSecretChatRow) {
                builder = new Builder(getParentActivity());
                builder.setMessage(LocaleController.getString("AreYouSureSecretChat", R.string.AreYouSureSecretChat));
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new ProfileActivity$$Lambda$26(this));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            } else if (position > this.emptyRowChat2 && position < this.membersEndRow) {
                int user_id;
                if (this.sortedUsers.isEmpty()) {
                    user_id = ((TLRPC$ChatParticipant) this.info.participants.participants.get((position - this.emptyRowChat2) - 1)).user_id;
                } else {
                    user_id = ((TLRPC$ChatParticipant) this.info.participants.participants.get(((Integer) this.sortedUsers.get((position - this.emptyRowChat2) - 1)).intValue())).user_id;
                }
                if (user_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                    args = new Bundle();
                    args.putInt("user_id", user_id);
                    presentFragment(new ProfileActivity(args));
                }
            } else if (position == this.addMemberRow) {
                openAddMember();
            } else if (position == this.channelNameRow) {
                try {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    if (this.info.about == null || this.info.about.length() <= 0) {
                        intent.putExtra("android.intent.extra.TEXT", this.currentChat.title + "\nhttps://" + MessagesController.getInstance(this.currentAccount).linkPrefix + "/" + this.currentChat.username);
                    } else {
                        intent.putExtra("android.intent.extra.TEXT", this.currentChat.title + "\n" + this.info.about + "\nhttps://" + MessagesController.getInstance(this.currentAccount).linkPrefix + "/" + this.currentChat.username);
                    }
                    getParentActivity().startActivityForResult(Intent.createChooser(intent, LocaleController.getString("BotShare", R.string.BotShare)), 500);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else if (position == this.leaveChannelRow) {
                leaveChatPressed();
            } else if (position == this.membersRow) {
                args = new Bundle();
                args.putInt("chat_id", this.chat_id);
                args.putInt(Param.TYPE, 2);
                presentFragment(new ChannelUsersActivity(args));
            } else if (position == this.convertRow) {
                builder = new Builder(getParentActivity());
                builder.setMessage(LocaleController.getString("ConvertGroupAlert", R.string.ConvertGroupAlert));
                builder.setTitle(LocaleController.getString("ConvertGroupAlertWarning", R.string.ConvertGroupAlertWarning));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new ProfileActivity$$Lambda$27(this));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            } else {
                processOnClickOrPress(position);
            }
        }
    }

    final /* synthetic */ void lambda$null$2$ProfileActivity(int position, DialogInterface dialogInterface, int which) {
        if (which != this.filterType) {
            this.filterType = which;
            getChannelParticipants(true);
            if (this.listAdapter != null) {
                this.listAdapter.notifyItemChanged(position);
            }
        }
    }

    final /* synthetic */ void lambda$null$3$ProfileActivity(int param) {
        this.listAdapter.notifyItemChanged(this.settingsNotificationsRow);
    }

    final /* synthetic */ void lambda$null$4$ProfileActivity(DialogInterface dialogInterface, int i) {
        this.creatingChat = true;
        SecretChatHelper.getInstance(this.currentAccount).startSecretChat(getParentActivity(), MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id)));
    }

    final /* synthetic */ void lambda$null$5$ProfileActivity(DialogInterface dialogInterface, int i) {
        MessagesController.getInstance(this.currentAccount).convertToMegaGroup(getParentActivity(), this.chat_id);
    }

    final /* synthetic */ boolean lambda$createView$9$ProfileActivity(View view, int position) {
        if (position <= this.emptyRowChat2 || position >= this.membersEndRow) {
            return processOnClickOrPress(position);
        }
        if (getParentActivity() == null) {
            return false;
        }
        TLRPC$ChatParticipant user;
        TLRPC$ChannelParticipant channelParticipant;
        boolean allowKick = false;
        boolean allowSetAdmin = false;
        boolean canEditAdmin = false;
        if (this.sortedUsers.isEmpty()) {
            user = (TLRPC$ChatParticipant) this.info.participants.participants.get((position - this.emptyRowChat2) - 1);
        } else {
            user = (TLRPC$ChatParticipant) this.info.participants.participants.get(((Integer) this.sortedUsers.get((position - this.emptyRowChat2) - 1)).intValue());
        }
        this.selectedUser = user.user_id;
        if (ChatObject.isChannel(this.currentChat)) {
            channelParticipant = ((TLRPC$TL_chatChannelParticipant) user).channelParticipant;
            if (user.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                return false;
            }
            User u = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(user.user_id));
            allowSetAdmin = (channelParticipant instanceof TLRPC$TL_channelParticipant) || (channelParticipant instanceof TLRPC$TL_channelParticipantBanned);
            if (((channelParticipant instanceof TLRPC$TL_channelParticipantAdmin) || (channelParticipant instanceof TLRPC$TL_channelParticipantCreator)) && !channelParticipant.can_edit) {
                canEditAdmin = false;
            } else {
                canEditAdmin = true;
            }
        } else {
            channelParticipant = null;
            if (user.user_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                if (this.currentChat.creator) {
                    allowKick = true;
                } else if ((user instanceof TLRPC$TL_chatParticipant) && ((this.currentChat.admin && this.currentChat.admins_enabled) || user.inviter_id == UserConfig.getInstance(this.currentAccount).getClientUserId())) {
                    allowKick = true;
                }
            }
            if (!allowKick) {
                return false;
            }
        }
        Builder builder = new Builder(getParentActivity());
        ArrayList<String> items = new ArrayList();
        ArrayList<Integer> actions = new ArrayList();
        if (this.currentChat.megagroup) {
            if (allowSetAdmin && ChatObject.canAddAdmins(this.currentChat)) {
                items.add(LocaleController.getString("SetAsAdmin", R.string.SetAsAdmin));
                actions.add(Integer.valueOf(0));
            }
            if (ChatObject.canBlockUsers(this.currentChat) && canEditAdmin) {
                items.add(LocaleController.getString("KickFromSupergroup", R.string.KickFromSupergroup));
                actions.add(Integer.valueOf(1));
                items.add(LocaleController.getString("KickFromGroup", R.string.KickFromGroup));
                actions.add(Integer.valueOf(2));
            }
        } else {
            items.add(this.chat_id > 0 ? LocaleController.getString("KickFromGroup", R.string.KickFromGroup) : LocaleController.getString("KickFromBroadcast", R.string.KickFromBroadcast));
            actions.add(Integer.valueOf(2));
        }
        items.add(LocaleController.getString("TurboUserMessages", R.string.TurboUserMessages));
        actions.add(Integer.valueOf(3));
        if (items.isEmpty()) {
            return false;
        }
        builder.setItems((CharSequence[]) items.toArray(new CharSequence[items.size()]), new ProfileActivity$$Lambda$22(this, actions, user, channelParticipant));
        showDialog(builder.create());
        return true;
    }

    final /* synthetic */ void lambda$null$8$ProfileActivity(ArrayList actions, TLRPC$ChatParticipant user, TLRPC$ChannelParticipant channelParticipant, DialogInterface dialogInterface, int i) {
        if (((Integer) actions.get(i)).intValue() == 3) {
            Bundle args = new Bundle();
            args.putInt("chat_id", this.chat_id);
            args.putInt("group_userId", user.user_id);
            presentFragment(new ChatActivity(args));
        } else if (((Integer) actions.get(i)).intValue() == 2) {
            kickUser(this.selectedUser);
        } else {
            ChannelRightsEditActivity fragment = new ChannelRightsEditActivity(user.user_id, this.chat_id, channelParticipant.admin_rights, channelParticipant.banned_rights, ((Integer) actions.get(i)).intValue(), true);
            fragment.setDelegate(new ProfileActivity$$Lambda$23(this, actions, i, user));
            presentFragment(fragment);
        }
    }

    final /* synthetic */ void lambda$null$7$ProfileActivity(ArrayList actions, int i, TLRPC$ChatParticipant user, int rights, TLRPC$TL_channelAdminRights rightsAdmin, TLRPC$TL_channelBannedRights rightsBanned) {
        if (((Integer) actions.get(i)).intValue() == 0) {
            TLRPC$TL_chatChannelParticipant channelParticipant1 = (TLRPC$TL_chatChannelParticipant) user;
            if (rights == 1) {
                channelParticipant1.channelParticipant = new TLRPC$TL_channelParticipantAdmin();
            } else {
                channelParticipant1.channelParticipant = new TLRPC$TL_channelParticipant();
            }
            channelParticipant1.channelParticipant.inviter_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
            channelParticipant1.channelParticipant.user_id = user.user_id;
            channelParticipant1.channelParticipant.date = user.date;
            channelParticipant1.channelParticipant.banned_rights = rightsBanned;
            channelParticipant1.channelParticipant.admin_rights = rightsAdmin;
        } else if (((Integer) actions.get(i)).intValue() == 1 && rights == 0 && this.currentChat.megagroup && this.info != null && this.info.participants != null) {
            int a;
            boolean changed = false;
            for (a = 0; a < this.info.participants.participants.size(); a++) {
                if (((TLRPC$TL_chatChannelParticipant) this.info.participants.participants.get(a)).channelParticipant.user_id == user.user_id) {
                    if (this.info != null) {
                        TLRPC$ChatFull tLRPC$ChatFull = this.info;
                        tLRPC$ChatFull.participants_count--;
                    }
                    this.info.participants.participants.remove(a);
                    changed = true;
                    if (this.info != null && this.info.participants != null) {
                        for (a = 0; a < this.info.participants.participants.size(); a++) {
                            if (((TLRPC$ChatParticipant) this.info.participants.participants.get(a)).user_id == user.user_id) {
                                this.info.participants.participants.remove(a);
                                changed = true;
                                break;
                            }
                        }
                    }
                    if (changed) {
                        updateOnlineCount();
                        updateRowsIds();
                        this.listAdapter.notifyDataSetChanged();
                    }
                }
            }
            for (a = 0; a < this.info.participants.participants.size(); a++) {
                if (((TLRPC$ChatParticipant) this.info.participants.participants.get(a)).user_id == user.user_id) {
                    this.info.participants.participants.remove(a);
                    changed = true;
                    break;
                }
            }
            if (changed) {
                updateOnlineCount();
                updateRowsIds();
                this.listAdapter.notifyDataSetChanged();
            }
        }
    }

    final /* synthetic */ void lambda$createView$11$ProfileActivity(TLObject response, TLRPC$TL_error error) {
        if (response != null) {
            AndroidUtilities.runOnUIThread(new ProfileActivity$$Lambda$21(this, response));
        }
    }

    final /* synthetic */ void lambda$null$10$ProfileActivity(TLObject response) {
        this.currentChannelParticipant = ((TLRPC$TL_channels_channelParticipant) response).participant;
    }

    final /* synthetic */ void lambda$createView$13$ProfileActivity(View v) {
        TLRPC$TL_channelBannedRights tLRPC$TL_channelBannedRights;
        int i = this.user_id;
        int i2 = this.banFromGroup;
        if (this.currentChannelParticipant != null) {
            tLRPC$TL_channelBannedRights = this.currentChannelParticipant.banned_rights;
        } else {
            tLRPC$TL_channelBannedRights = null;
        }
        ChannelRightsEditActivity fragment = new ChannelRightsEditActivity(i, i2, null, tLRPC$TL_channelBannedRights, 1, true);
        fragment.setDelegate(new ProfileActivity$$Lambda$20(this));
        presentFragment(fragment);
    }

    final /* synthetic */ void lambda$null$12$ProfileActivity(int rights, TLRPC$TL_channelAdminRights rightsAdmin, TLRPC$TL_channelBannedRights rightsBanned) {
        removeSelfFromStack();
    }

    final /* synthetic */ void lambda$createView$14$ProfileActivity(View v) {
        if (this.user_id != 0) {
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
            if (user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, this.provider);
            }
        } else if (this.chat_id != 0) {
            TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
            if (chat.photo != null && chat.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                PhotoViewer.getInstance().openPhoto(chat.photo.photo_big, this.provider);
            }
        }
    }

    final /* synthetic */ void lambda$createView$16$ProfileActivity(View v) {
        if (getParentActivity() != null) {
            Bundle args;
            if (this.user_id != 0) {
                if (this.playProfileAnimation && (this.parentLayout.fragmentsStack.get(this.parentLayout.fragmentsStack.size() - 2) instanceof ChatActivity)) {
                    finishFragment();
                    return;
                }
                User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
                if (user != null && !(user instanceof TLRPC$TL_userEmpty)) {
                    args = new Bundle();
                    args.putInt("user_id", this.user_id);
                    if (MessagesController.getInstance(this.currentAccount).checkCanOpenChat(args, this)) {
                        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
                        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        presentFragment(new ChatActivity(args), true);
                    }
                }
            } else if (this.chat_id != 0) {
                boolean isChannel = ChatObject.isChannel(this.currentChat);
                if ((!isChannel || ChatObject.canEditInfo(this.currentChat)) && (isChannel || this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                    Builder builder = new Builder(getParentActivity());
                    TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
                    CharSequence[] items = (chat.photo == null || chat.photo.photo_big == null || (chat.photo instanceof TLRPC$TL_chatPhotoEmpty)) ? new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)} : new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                    builder.setItems(items, new ProfileActivity$$Lambda$19(this));
                    showDialog(builder.create());
                } else if (this.playProfileAnimation && (this.parentLayout.fragmentsStack.get(this.parentLayout.fragmentsStack.size() - 2) instanceof ChatActivity)) {
                    finishFragment();
                } else {
                    args = new Bundle();
                    args.putInt("chat_id", this.currentChat.id);
                    if (MessagesController.getInstance(this.currentAccount).checkCanOpenChat(args, this)) {
                        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
                        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        presentFragment(new ChatActivity(args), true);
                    }
                }
            }
        }
    }

    final /* synthetic */ void lambda$null$15$ProfileActivity(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            this.imageUpdater.openCamera();
        } else if (i == 1) {
            this.imageUpdater.openGallery();
        } else if (i == 2) {
            MessagesController.getInstance(this.currentAccount).changeChatAvatar(this.chat_id, null);
        }
    }

    private boolean processOnClickOrPress(int position) {
        User user;
        Builder builder;
        if (position == this.usernameRow || position == this.channelNameRow) {
            String username;
            if (position == this.usernameRow) {
                user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
                if (user == null || user.username == null) {
                    return false;
                }
                username = user.username;
            } else {
                TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
                if (chat == null || chat.username == null) {
                    return false;
                }
                username = chat.username;
            }
            builder = new Builder(getParentActivity());
            builder.setItems(new CharSequence[]{LocaleController.getString("Copy", R.string.Copy)}, new ProfileActivity$$Lambda$8(this, username));
            showDialog(builder.create());
            return true;
        } else if (position == this.phoneRow) {
            user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
            if (user == null || user.phone == null || user.phone.length() == 0 || getParentActivity() == null) {
                return false;
            }
            builder = new Builder(getParentActivity());
            ArrayList<CharSequence> items = new ArrayList();
            ArrayList<Integer> actions = new ArrayList();
            TLRPC$TL_userFull userFull = MessagesController.getInstance(this.currentAccount).getUserFull(user.id);
            if (TurboConfig.callsEnabled && userFull != null && userFull.phone_calls_available) {
                items.add(LocaleController.getString("CallViaTelegram", R.string.CallViaTelegram));
                actions.add(Integer.valueOf(2));
            }
            items.add(LocaleController.getString("Call", R.string.Call));
            actions.add(Integer.valueOf(0));
            items.add(LocaleController.getString("Copy", R.string.Copy));
            actions.add(Integer.valueOf(1));
            builder.setItems((CharSequence[]) items.toArray(new CharSequence[items.size()]), new ProfileActivity$$Lambda$9(this, actions, user));
            showDialog(builder.create());
            return true;
        } else if (position != this.channelInfoRow && position != this.userInfoRow && position != this.userInfoDetailedRow) {
            return false;
        } else {
            builder = new Builder(getParentActivity());
            builder.setItems(new CharSequence[]{LocaleController.getString("Copy", R.string.Copy)}, new ProfileActivity$$Lambda$10(this, position));
            showDialog(builder.create());
            return true;
        }
    }

    final /* synthetic */ void lambda$processOnClickOrPress$17$ProfileActivity(String username, DialogInterface dialogInterface, int i) {
        if (i == 0) {
            try {
                ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", "@" + username));
                Toast.makeText(getParentActivity(), LocaleController.getString("TextCopied", R.string.TextCopied), 0).show();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    final /* synthetic */ void lambda$processOnClickOrPress$18$ProfileActivity(ArrayList actions, User user, DialogInterface dialogInterface, int i) {
        i = ((Integer) actions.get(i)).intValue();
        if (i == 0) {
            try {
                Intent intent = new Intent("android.intent.action.DIAL", Uri.parse("tel:+" + user.phone));
                intent.addFlags(268435456);
                getParentActivity().startActivityForResult(intent, 500);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (i == 1) {
            try {
                ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", "+" + user.phone));
                Toast.makeText(getParentActivity(), LocaleController.getString("PhoneCopied", R.string.PhoneCopied), 0).show();
            } catch (Exception e2) {
                FileLog.e(e2);
            }
        } else if (i == 2) {
            VoIPHelper.startCall(user, getParentActivity(), MessagesController.getInstance(this.currentAccount).getUserFull(user.id));
        }
    }

    final /* synthetic */ void lambda$processOnClickOrPress$19$ProfileActivity(int position, DialogInterface dialogInterface, int i) {
        try {
            String about;
            if (position == this.channelInfoRow) {
                about = this.info.about;
            } else {
                TLRPC$TL_userFull userFull = MessagesController.getInstance(this.currentAccount).getUserFull(this.user_id);
                about = userFull != null ? userFull.about : null;
            }
            if (!TextUtils.isEmpty(about)) {
                AndroidUtilities.addToClipboard(about);
                TurboUtils.showToast(getParentActivity(), LocaleController.getString("TextCopied", R.string.TextCopied), 0);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void leaveChatPressed() {
        Builder builder = new Builder(getParentActivity());
        if (!ChatObject.isChannel(this.chat_id, this.currentAccount) || this.currentChat.megagroup) {
            builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
        } else {
            builder.setMessage(ChatObject.isChannel(this.chat_id, this.currentAccount) ? LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert) : LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
        }
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new ProfileActivity$$Lambda$11(this));
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    final /* synthetic */ void lambda$leaveChatPressed$20$ProfileActivity(DialogInterface dialogInterface, int i) {
        kickUser(0);
    }

    public void saveSelfArgs(Bundle args) {
        if (this.chat_id != 0 && this.imageUpdater != null && this.imageUpdater.currentPicturePath != null) {
            args.putString("path", this.imageUpdater.currentPicturePath);
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.chat_id != 0) {
            MessagesController.getInstance(this.currentAccount).loadChatInfo(this.chat_id, null, false);
            if (this.imageUpdater != null) {
                this.imageUpdater.currentPicturePath = args.getString("path");
            }
        }
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (this.chat_id != 0) {
            this.imageUpdater.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getChannelParticipants(boolean reload) {
        int i = 0;
        if (!this.loadingUsers && this.participantsMap != null && this.info != null) {
            int delay;
            this.loadingUsers = true;
            if (this.participantsMap.size() == 0 || !reload) {
                delay = 0;
            } else {
                delay = 300;
            }
            TLRPC$TL_channels_getParticipants req = new TLRPC$TL_channels_getParticipants();
            req.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chat_id);
            req.filter = new TLRPC$TL_channelParticipantsRecent();
            if (this.filterType != 0) {
                if (this.filterType == 1) {
                    req.filter = new TLRPC$TL_channelParticipantsAdmins();
                } else if (this.filterType == 2) {
                    req.filter = new TLRPC$TL_channelParticipantsBots();
                }
                req.filter.f783q = "";
            }
            if (!reload) {
                i = this.participantsMap.size();
            }
            req.offset = i;
            req.limit = Callback.DEFAULT_DRAG_ANIMATION_DURATION;
            ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new ProfileActivity$$Lambda$12(this, req, delay)), this.classGuid);
        }
    }

    final /* synthetic */ void lambda$getChannelParticipants$22$ProfileActivity(TLRPC$TL_channels_getParticipants req, int delay, TLObject response, TLRPC$TL_error error) {
        AndroidUtilities.runOnUIThread(new ProfileActivity$$Lambda$18(this, error, response, req), (long) delay);
    }

    final /* synthetic */ void lambda$null$21$ProfileActivity(TLRPC$TL_error error, TLObject response, TLRPC$TL_channels_getParticipants req) {
        if (error == null) {
            TLRPC$TL_channels_channelParticipants res = (TLRPC$TL_channels_channelParticipants) response;
            MessagesController.getInstance(this.currentAccount).putUsers(res.users, false);
            if (res.users.size() < Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                this.usersEndReached = true;
            }
            if (req.offset == 0) {
                this.participantsMap.clear();
                this.info.participants = new TLRPC$TL_chatParticipants();
                MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(res.users, null, true, true);
                MessagesStorage.getInstance(this.currentAccount).updateChannelUsers(this.chat_id, res.participants);
            }
            for (int a = 0; a < res.participants.size(); a++) {
                TLRPC$TL_chatChannelParticipant participant = new TLRPC$TL_chatChannelParticipant();
                participant.channelParticipant = (TLRPC$ChannelParticipant) res.participants.get(a);
                participant.inviter_id = participant.channelParticipant.inviter_id;
                participant.user_id = participant.channelParticipant.user_id;
                participant.date = participant.channelParticipant.date;
                if (this.participantsMap.indexOfKey(participant.user_id) < 0) {
                    this.info.participants.participants.add(participant);
                    this.participantsMap.put(participant.user_id, participant);
                }
            }
        }
        updateOnlineCount();
        this.loadingUsers = false;
        updateRowsIds();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    private void openAddMember() {
        boolean z = true;
        Bundle args = new Bundle();
        args.putBoolean("onlyUsers", true);
        args.putBoolean("destroyAfterSelect", true);
        args.putBoolean("returnAsResult", true);
        String str = "needForwardCount";
        if (ChatObject.isChannel(this.currentChat)) {
            z = false;
        }
        args.putBoolean(str, z);
        if (this.chat_id > 0) {
            if (ChatObject.canAddViaLink(this.currentChat)) {
                args.putInt("chat_id", this.currentChat.id);
            }
            args.putString("selectAlertString", LocaleController.getString("AddToTheGroup", R.string.AddToTheGroup));
        }
        ContactsActivity fragment = new ContactsActivity(args);
        fragment.setDelegate(new ProfileActivity$$Lambda$13(this));
        if (!(this.info == null || this.info.participants == null)) {
            SparseArray<User> users = new SparseArray();
            for (int a = 0; a < this.info.participants.participants.size(); a++) {
                users.put(((TLRPC$ChatParticipant) this.info.participants.participants.get(a)).user_id, null);
            }
            fragment.setIgnoreUsers(users);
        }
        presentFragment(fragment);
    }

    final /* synthetic */ void lambda$openAddMember$23$ProfileActivity(User user, String param, ContactsActivity activity) {
        MessagesController.getInstance(this.currentAccount).addUserToChat(this.chat_id, user, this.info, param != null ? Utilities.parseInt(param).intValue() : 0, null, this);
    }

    private void checkListViewScroll() {
        boolean z = false;
        if (this.listView.getChildCount() > 0 && !this.openAnimationInProgress) {
            View child = this.listView.getChildAt(0);
            Holder holder = (Holder) this.listView.findContainingViewHolder(child);
            int top = child.getTop();
            int newOffset = 0;
            if (top >= 0 && holder != null && holder.getAdapterPosition() == 0) {
                newOffset = top;
            }
            if (this.extraHeight != newOffset) {
                this.extraHeight = newOffset;
                this.topView.invalidate();
                if (this.playProfileAnimation) {
                    if (this.extraHeight != 0) {
                        z = true;
                    }
                    this.allowProfileAnimation = z;
                }
                needLayout();
            }
        }
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (!(this.listView == null || this.openAnimationInProgress)) {
            layoutParams = (FrameLayout.LayoutParams) this.listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                this.listView.setLayoutParams(layoutParams);
            }
        }
        if (this.avatarImage != null) {
            float diff = ((float) this.extraHeight) / ((float) AndroidUtilities.dp(88.0f));
            this.listView.setTopGlowOffset(this.extraHeight);
            if (this.writeButton != null) {
                this.writeButton.setTranslationY((float) ((((this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight()) + this.extraHeight) - AndroidUtilities.dp(29.5f)));
                if (!this.openAnimationInProgress) {
                    boolean setVisible = diff > 0.2f;
                    if (setVisible != (this.writeButton.getTag() == null)) {
                        if (setVisible) {
                            this.writeButton.setTag(null);
                        } else {
                            this.writeButton.setTag(Integer.valueOf(0));
                        }
                        if (this.writeButtonAnimation != null) {
                            AnimatorSet old = this.writeButtonAnimation;
                            this.writeButtonAnimation = null;
                            old.cancel();
                        }
                        this.writeButtonAnimation = new AnimatorSet();
                        AnimatorSet animatorSet;
                        Animator[] animatorArr;
                        if (setVisible) {
                            this.writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                            animatorSet = this.writeButtonAnimation;
                            animatorArr = new Animator[3];
                            animatorArr[0] = ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[]{1.0f});
                            animatorArr[1] = ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[]{1.0f});
                            animatorArr[2] = ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[]{1.0f});
                            animatorSet.playTogether(animatorArr);
                        } else {
                            this.writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                            animatorSet = this.writeButtonAnimation;
                            animatorArr = new Animator[3];
                            animatorArr[0] = ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[]{0.2f});
                            animatorArr[1] = ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[]{0.2f});
                            animatorArr[2] = ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[]{0.0f});
                            animatorSet.playTogether(animatorArr);
                        }
                        this.writeButtonAnimation.setDuration(150);
                        this.writeButtonAnimation.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                if (ProfileActivity.this.writeButtonAnimation != null && ProfileActivity.this.writeButtonAnimation.equals(animation)) {
                                    ProfileActivity.this.writeButtonAnimation = null;
                                }
                            }
                        });
                        this.writeButtonAnimation.start();
                    }
                }
            }
            float avatarY = ((((float) (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0)) + ((((float) ActionBar.getCurrentActionBarHeight()) / 2.0f) * (1.0f + diff))) - (21.0f * AndroidUtilities.density)) + ((27.0f * AndroidUtilities.density) * diff);
            this.avatarImage.setScaleX((42.0f + (18.0f * diff)) / 42.0f);
            this.avatarImage.setScaleY((42.0f + (18.0f * diff)) / 42.0f);
            this.avatarImage.setTranslationX(((float) (-AndroidUtilities.dp(47.0f))) * diff);
            this.avatarImage.setTranslationY((float) Math.ceil((double) avatarY));
            for (int a = 0; a < 2; a++) {
                if (this.nameTextView[a] != null) {
                    this.nameTextView[a].setTranslationX((-21.0f * AndroidUtilities.density) * diff);
                    this.nameTextView[a].setTranslationY((((float) Math.floor((double) avatarY)) + ((float) AndroidUtilities.dp(1.3f))) + (((float) AndroidUtilities.dp(7.0f)) * diff));
                    this.onlineTextView[a].setTranslationX((-21.0f * AndroidUtilities.density) * diff);
                    this.onlineTextView[a].setTranslationY((((float) Math.floor((double) avatarY)) + ((float) AndroidUtilities.dp(24.0f))) + (((float) Math.floor((double) (11.0f * AndroidUtilities.density))) * diff));
                    int profileActionbarNameSize = AndroidUtilities.isTablet() ? 20 : 18;
                    int profileActionbarStatusSize = AndroidUtilities.isTablet() ? 16 : 14;
                    this.adminTextView.setTranslationX((-21.0f * AndroidUtilities.density) * diff);
                    this.adminTextView.setTranslationY((((float) Math.floor((double) avatarY)) + ((float) AndroidUtilities.dp((float) (((profileActionbarStatusSize - 14) + (profileActionbarNameSize - 18)) + 32)))) + (((float) Math.floor((double) (22.0f * AndroidUtilities.density))) * diff));
                    this.nameTextView[a].setScaleX(1.0f + (0.12f * diff));
                    this.nameTextView[a].setScaleY(1.0f + (0.12f * diff));
                    if (a == 1 && !this.openAnimationInProgress) {
                        int width;
                        if (AndroidUtilities.isTablet()) {
                            width = AndroidUtilities.dp(490.0f);
                        } else {
                            width = AndroidUtilities.displaySize.x;
                        }
                        int i = (this.callItem == null && this.editItem == null) ? 0 : 48;
                        width = (int) (((float) (width - AndroidUtilities.dp((((float) (i + 40)) * (1.0f - diff)) + 126.0f))) - this.nameTextView[a].getTranslationX());
                        layoutParams = (FrameLayout.LayoutParams) this.nameTextView[a].getLayoutParams();
                        if (((float) width) < (this.nameTextView[a].getPaint().measureText(this.nameTextView[a].getText().toString()) * this.nameTextView[a].getScaleX()) + ((float) this.nameTextView[a].getSideDrawablesSize())) {
                            layoutParams.width = (int) Math.ceil((double) (((float) width) / this.nameTextView[a].getScaleX()));
                        } else {
                            layoutParams.width = -2;
                        }
                        this.nameTextView[a].setLayoutParams(layoutParams);
                        layoutParams = (FrameLayout.LayoutParams) this.onlineTextView[a].getLayoutParams();
                        layoutParams.rightMargin = (int) Math.ceil((double) ((this.onlineTextView[a].getTranslationX() + ((float) AndroidUtilities.dp(8.0f))) + (((float) AndroidUtilities.dp(40.0f)) * (1.0f - diff))));
                        this.onlineTextView[a].setLayoutParams(layoutParams);
                    }
                }
            }
            if (((double) diff) > 0.85d) {
                this.adminTextView.setVisibility(0);
            } else {
                this.adminTextView.setVisibility(8);
            }
        }
    }

    private void fixLayout() {
        if (this.fragmentView != null) {
            this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (ProfileActivity.this.fragmentView != null) {
                        ProfileActivity.this.checkListViewScroll();
                        ProfileActivity.this.needLayout();
                        ProfileActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        ViewHolder holder;
        TLRPC$Chat newChat;
        int count;
        int a;
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if (this.user_id != 0) {
                if (!((mask & 2) == 0 && (mask & 1) == 0 && (mask & 4) == 0)) {
                    updateProfileData();
                }
                if ((mask & 1024) != 0 && this.listView != null) {
                    holder = (Holder) this.listView.findViewHolderForPosition(this.phoneRow);
                    if (holder != null) {
                        this.listAdapter.onBindViewHolder(holder, this.phoneRow);
                    }
                }
            } else if (this.chat_id != 0) {
                if ((mask & 16384) != 0) {
                    newChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
                    if (newChat != null) {
                        this.currentChat = newChat;
                        createActionBarMenu();
                        updateRowsIds();
                        if (this.listAdapter != null) {
                            this.listAdapter.notifyDataSetChanged();
                        }
                    }
                }
                if (!((mask & 8192) == 0 && (mask & 8) == 0 && (mask & 16) == 0 && (mask & 32) == 0 && (mask & 4) == 0)) {
                    updateOnlineCount();
                    updateProfileData();
                }
                if ((mask & 8192) != 0) {
                    updateRowsIds();
                    if (this.listAdapter != null) {
                        this.listAdapter.notifyDataSetChanged();
                    }
                }
                if (((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) && this.listView != null) {
                    count = this.listView.getChildCount();
                    for (a = 0; a < count; a++) {
                        View child = this.listView.getChildAt(a);
                        if (child instanceof UserCell) {
                            ((UserCell) child).update(mask);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.contactsDidLoaded) {
            createActionBarMenu();
        } else if (id == NotificationCenter.mediaCountDidLoaded) {
            long uid = ((Long) args[0]).longValue();
            long did = this.dialog_id;
            if (did == 0) {
                if (this.user_id != 0) {
                    did = (long) this.user_id;
                } else if (this.chat_id != 0) {
                    did = (long) (-this.chat_id);
                }
            }
            if (uid == did || uid == this.mergeDialogId) {
                int type = ((Integer) args[3]).intValue();
                int mCount = ((Integer) args[1]).intValue();
                if (type == 0) {
                    if (uid == did) {
                        this.totalMediaCount = mCount;
                    } else {
                        this.totalMediaCountMerge = mCount;
                    }
                    if (this.listView != null) {
                        count = this.listView.getChildCount();
                        for (a = 0; a < count; a++) {
                            holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                            if (holder.getAdapterPosition() == this.sharedMediaRow) {
                                this.listAdapter.onBindViewHolder(holder, this.sharedMediaRow);
                                break;
                            }
                        }
                    }
                } else {
                    loadMediaCount(uid, did, mCount, type);
                }
                if (uid == did) {
                    this.mediaCount[type] = mCount;
                } else {
                    this.mediaMergeCount[type] = mCount;
                }
            }
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (this.creatingChat) {
                AndroidUtilities.runOnUIThread(new ProfileActivity$$Lambda$14(this, args));
            }
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            TLRPC$EncryptedChat chat = args[0];
            if (this.currentEncryptedChat != null && chat.id == this.currentEncryptedChat.id) {
                this.currentEncryptedChat = chat;
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoaded) {
            boolean oldValue = this.userBlocked;
            this.userBlocked = MessagesController.getInstance(this.currentAccount).blockedUsers.indexOfKey(this.user_id) >= 0;
            if (oldValue != this.userBlocked) {
                createActionBarMenu();
            }
        } else if (id == NotificationCenter.chatInfoDidLoaded) {
            TLRPC$ChatFull chatFull = args[0];
            if (chatFull.id == this.chat_id) {
                boolean byChannelUsers = ((Boolean) args[2]).booleanValue();
                if ((this.info instanceof TLRPC$TL_channelFull) && chatFull.participants == null && this.info != null) {
                    chatFull.participants = this.info.participants;
                }
                boolean loadChannelParticipants = this.info == null && (chatFull instanceof TLRPC$TL_channelFull);
                this.info = chatFull;
                if (this.mergeDialogId == 0 && this.info.migrated_from_chat_id != 0) {
                    this.mergeDialogId = (long) (-this.info.migrated_from_chat_id);
                    if (TurboConfig.separateMedia) {
                        if (TurboConfig.sharedPhotoTitle) {
                            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 5, this.classGuid, true);
                        }
                        if (TurboConfig.sharedVideoTitle) {
                            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 6, this.classGuid, true);
                        }
                        if (TurboConfig.sharedFileTitle) {
                            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 1, this.classGuid, true);
                        }
                        if (TurboConfig.sharedLinkTitle) {
                            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 3, this.classGuid, true);
                        }
                        if (TurboConfig.sharedMusicTitle) {
                            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 4, this.classGuid, true);
                        }
                        if (TurboConfig.sharedVoiceTitle) {
                            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 2, this.classGuid, true);
                        }
                    } else {
                        DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
                    }
                }
                fetchUsersFromChannelInfo();
                updateOnlineCount();
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                }
                newChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
                if (newChat != null) {
                    this.currentChat = newChat;
                    createActionBarMenu();
                }
                if (!this.currentChat.megagroup) {
                    return;
                }
                if (loadChannelParticipants || !byChannelUsers) {
                    getChannelParticipants(true);
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        } else if (id == NotificationCenter.botInfoDidLoaded) {
            TLRPC$BotInfo info = args[0];
            if (info.user_id == this.user_id) {
                this.botInfo = info;
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.userInfoDidLoaded) {
            if (((Integer) args[0]).intValue() == this.user_id) {
                if (this.openAnimationInProgress || this.callItem != null) {
                    this.recreateMenuAfterAnimation = true;
                } else {
                    createActionBarMenu();
                }
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.didReceivedNewMessages && ((Long) args[0]).longValue() == this.dialog_id) {
            ArrayList<MessageObject> arr = args[1];
            for (a = 0; a < arr.size(); a++) {
                MessageObject obj = (MessageObject) arr.get(a);
                if (this.currentEncryptedChat != null && obj.messageOwner.action != null && (obj.messageOwner.action instanceof TLRPC$TL_messageEncryptedAction) && (obj.messageOwner.action.encryptedAction instanceof TLRPC$TL_decryptedMessageActionSetMessageTTL)) {
                    TLRPC$TL_decryptedMessageActionSetMessageTTL action = obj.messageOwner.action.encryptedAction;
                    if (this.listAdapter != null) {
                        this.listAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    final /* synthetic */ void lambda$didReceivedNotification$24$ProfileActivity(Object[] args) {
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
        TLRPC$EncryptedChat encryptedChat = args[0];
        Bundle args2 = new Bundle();
        args2.putInt("enc_id", encryptedChat.id);
        presentFragment(new ChatActivity(args2), true);
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
        updateProfileData();
        fixLayout();
    }

    public void setPlayProfileAnimation(boolean value) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        if (!AndroidUtilities.isTablet() && preferences.getBoolean("view_animations", true)) {
            this.playProfileAnimation = value;
        }
    }

    protected void onTransitionAnimationStart(boolean isOpen, boolean backward) {
        if (!backward && this.playProfileAnimation && this.allowProfileAnimation) {
            this.openAnimationInProgress = true;
        }
        NotificationCenter.getInstance(this.currentAccount).setAllowedNotificationsDutingAnimation(new int[]{NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoaded});
        NotificationCenter.getInstance(this.currentAccount).setAnimationInProgress(true);
    }

    protected void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (!backward && this.playProfileAnimation && this.allowProfileAnimation) {
            this.openAnimationInProgress = false;
            if (this.recreateMenuAfterAnimation) {
                createActionBarMenu();
            }
        }
        NotificationCenter.getInstance(this.currentAccount).setAnimationInProgress(false);
    }

    public float getAnimationProgress() {
        return this.animationProgress;
    }

    @Keep
    public void setAnimationProgress(float progress) {
        int i;
        int i2;
        this.animationProgress = progress;
        this.listView.setAlpha(progress);
        this.listView.setTranslationX(((float) AndroidUtilities.dp(48.0f)) - (((float) AndroidUtilities.dp(48.0f)) * progress));
        if (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) {
            i = 5;
        } else {
            i = this.chat_id;
        }
        int color = AvatarDrawable.getProfileBackColorForId(i);
        int actionBarColor = Theme.getColor(Theme.key_actionBarDefault);
        int r = Color.red(actionBarColor);
        int g = Color.green(actionBarColor);
        int b = Color.blue(actionBarColor);
        this.topView.setBackgroundColor(Color.rgb(r + ((int) (((float) (Color.red(color) - r)) * progress)), g + ((int) (((float) (Color.green(color) - g)) * progress)), b + ((int) (((float) (Color.blue(color) - b)) * progress))));
        if (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) {
            i = 5;
        } else {
            i = this.chat_id;
        }
        color = AvatarDrawable.getIconColorForId(i);
        int iconColor = Theme.getColor(Theme.key_actionBarDefaultIcon);
        r = Color.red(iconColor);
        g = Color.green(iconColor);
        b = Color.blue(iconColor);
        this.actionBar.setItemsColor(Color.rgb(r + ((int) (((float) (Color.red(color) - r)) * progress)), g + ((int) (((float) (Color.green(color) - g)) * progress)), b + ((int) (((float) (Color.blue(color) - b)) * progress))), false);
        color = Theme.getColor(Theme.key_profile_title);
        int titleColor = Theme.getColor(Theme.key_actionBarDefaultTitle);
        r = Color.red(titleColor);
        g = Color.green(titleColor);
        b = Color.blue(titleColor);
        int a = Color.alpha(titleColor);
        int rD = (int) (((float) (Color.red(color) - r)) * progress);
        int gD = (int) (((float) (Color.green(color) - g)) * progress);
        int bD = (int) (((float) (Color.blue(color) - b)) * progress);
        int aD = (int) (((float) (Color.alpha(color) - a)) * progress);
        for (i2 = 0; i2 < 2; i2++) {
            if (this.nameTextView[i2] != null) {
                this.nameTextView[i2].setTextColor(Color.argb(a + aD, r + rD, g + gD, b + bD));
            }
        }
        if (this.user_id != 0 || (ChatObject.isChannel(this.chat_id, this.currentAccount) && !this.currentChat.megagroup)) {
            i = 5;
        } else {
            i = this.chat_id;
        }
        color = AvatarDrawable.getProfileTextColorForId(i);
        int subtitleColor = Theme.getColor(Theme.key_actionBarDefaultSubtitle);
        r = Color.red(subtitleColor);
        g = Color.green(subtitleColor);
        b = Color.blue(subtitleColor);
        a = Color.alpha(subtitleColor);
        rD = (int) (((float) (Color.red(color) - r)) * progress);
        gD = (int) (((float) (Color.green(color) - g)) * progress);
        bD = (int) (((float) (Color.blue(color) - b)) * progress);
        aD = (int) (((float) (Color.alpha(color) - a)) * progress);
        for (i2 = 0; i2 < 2; i2++) {
            if (this.onlineTextView[i2] != null) {
                this.onlineTextView[i2].setTextColor(Color.argb(a + aD, r + rD, g + gD, b + bD));
            }
        }
        this.extraHeight = (int) (((float) this.initialAnimationExtraHeight) * progress);
        color = AvatarDrawable.getProfileColorForId(this.user_id != 0 ? this.user_id : this.chat_id);
        int color2 = AvatarDrawable.getColorForId(this.user_id != 0 ? this.user_id : this.chat_id);
        if (color != color2) {
            this.avatarDrawable.setColor(Color.rgb(Color.red(color2) + ((int) (((float) (Color.red(color) - Color.red(color2))) * progress)), Color.green(color2) + ((int) (((float) (Color.green(color) - Color.green(color2))) * progress)), Color.blue(color2) + ((int) (((float) (Color.blue(color) - Color.blue(color2))) * progress))));
            this.avatarImage.invalidate();
        }
        needLayout();
    }

    protected AnimatorSet onCustomTransitionAnimation(boolean isOpen, final Runnable callback) {
        if (!this.playProfileAnimation || !this.allowProfileAnimation) {
            return null;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(180);
        this.listView.setLayerType(2, null);
        ActionBarMenu menu = this.actionBar.createMenu();
        if (menu.getItem(10) == null && this.animatingItem == null) {
            this.animatingItem = menu.addItem(10, (int) R.drawable.ic_ab_other);
        }
        ArrayList<Animator> animators;
        int a;
        Object obj;
        String str;
        float[] fArr;
        if (isOpen) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.onlineTextView[1].getLayoutParams();
            layoutParams.rightMargin = (int) ((-21.0f * AndroidUtilities.density) + ((float) AndroidUtilities.dp(8.0f)));
            this.onlineTextView[1].setLayoutParams(layoutParams);
            int width = (int) Math.ceil((double) (((float) (AndroidUtilities.displaySize.x - AndroidUtilities.dp(126.0f))) + (21.0f * AndroidUtilities.density)));
            layoutParams = (FrameLayout.LayoutParams) this.nameTextView[1].getLayoutParams();
            if (((float) width) < (this.nameTextView[1].getPaint().measureText(this.nameTextView[1].getText().toString()) * 1.12f) + ((float) this.nameTextView[1].getSideDrawablesSize())) {
                layoutParams.width = (int) Math.ceil((double) (((float) width) / 1.12f));
            } else {
                layoutParams.width = -2;
            }
            this.nameTextView[1].setLayoutParams(layoutParams);
            this.initialAnimationExtraHeight = AndroidUtilities.dp(88.0f);
            this.fragmentView.setBackgroundColor(0);
            setAnimationProgress(0.0f);
            animators = new ArrayList();
            animators.add(ObjectAnimator.ofFloat(this, "animationProgress", new float[]{0.0f, 1.0f}));
            if (this.writeButton != null) {
                this.writeButton.setScaleX(0.2f);
                this.writeButton.setScaleY(0.2f);
                this.writeButton.setAlpha(0.0f);
                animators.add(ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[]{1.0f}));
                animators.add(ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[]{1.0f}));
                animators.add(ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[]{1.0f}));
            }
            a = 0;
            while (a < 2) {
                this.onlineTextView[a].setAlpha(a == 0 ? 1.0f : 0.0f);
                this.nameTextView[a].setAlpha(a == 0 ? 1.0f : 0.0f);
                obj = this.onlineTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 0.0f : 1.0f;
                animators.add(ObjectAnimator.ofFloat(obj, str, fArr));
                obj = this.nameTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 0.0f : 1.0f;
                animators.add(ObjectAnimator.ofFloat(obj, str, fArr));
                a++;
            }
            if (this.animatingItem != null) {
                this.animatingItem.setAlpha(1.0f);
                animators.add(ObjectAnimator.ofFloat(this.animatingItem, "alpha", new float[]{0.0f}));
            }
            if (this.callItem != null) {
                this.callItem.setAlpha(0.0f);
                animators.add(ObjectAnimator.ofFloat(this.callItem, "alpha", new float[]{1.0f}));
            }
            if (this.editItem != null) {
                this.editItem.setAlpha(0.0f);
                animators.add(ObjectAnimator.ofFloat(this.editItem, "alpha", new float[]{1.0f}));
            }
            animatorSet.playTogether(animators);
        } else {
            this.initialAnimationExtraHeight = this.extraHeight;
            animators = new ArrayList();
            animators.add(ObjectAnimator.ofFloat(this, "animationProgress", new float[]{1.0f, 0.0f}));
            if (this.writeButton != null) {
                animators.add(ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[]{0.2f}));
                animators.add(ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[]{0.2f}));
                animators.add(ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[]{0.0f}));
            }
            a = 0;
            while (a < 2) {
                obj = this.onlineTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 1.0f : 0.0f;
                animators.add(ObjectAnimator.ofFloat(obj, str, fArr));
                obj = this.nameTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 1.0f : 0.0f;
                animators.add(ObjectAnimator.ofFloat(obj, str, fArr));
                a++;
            }
            if (this.animatingItem != null) {
                this.animatingItem.setAlpha(0.0f);
                animators.add(ObjectAnimator.ofFloat(this.animatingItem, "alpha", new float[]{1.0f}));
            }
            if (this.callItem != null) {
                this.callItem.setAlpha(1.0f);
                animators.add(ObjectAnimator.ofFloat(this.callItem, "alpha", new float[]{0.0f}));
            }
            if (this.editItem != null) {
                this.editItem.setAlpha(1.0f);
                animators.add(ObjectAnimator.ofFloat(this.editItem, "alpha", new float[]{0.0f}));
            }
            animatorSet.playTogether(animators);
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ProfileActivity.this.listView.setLayerType(0, null);
                if (ProfileActivity.this.animatingItem != null) {
                    ProfileActivity.this.actionBar.createMenu().clearItems();
                    ProfileActivity.this.animatingItem = null;
                }
                callback.run();
            }
        });
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.getClass();
        AndroidUtilities.runOnUIThread(ProfileActivity$$Lambda$15.get$Lambda(animatorSet), 50);
        return animatorSet;
    }

    private void updateOnlineCount() {
        this.onlineCount = 0;
        int currentTime = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        this.sortedUsers.clear();
        if ((this.info instanceof TLRPC$TL_chatFull) || ((this.info instanceof TLRPC$TL_channelFull) && this.info.participants_count <= Callback.DEFAULT_DRAG_ANIMATION_DURATION && this.info.participants != null)) {
            for (int a = 0; a < this.info.participants.participants.size(); a++) {
                TLRPC$ChatParticipant participant = (TLRPC$ChatParticipant) this.info.participants.participants.get(a);
                User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(participant.user_id));
                if (!(user == null || user.status == null || ((user.status.expires <= currentTime && user.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) || user.status.expires <= 10000))) {
                    this.onlineCount++;
                }
                this.sortedUsers.add(Integer.valueOf(a));
                if (participant instanceof TLRPC$TL_chatParticipantCreator) {
                    this.creatorID = participant.user_id;
                }
            }
            try {
                Collections.sort(this.sortedUsers, new ProfileActivity$$Lambda$16(this));
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (this.listAdapter != null) {
                this.listAdapter.notifyItemRangeChanged(this.emptyRowChat2 + 1, this.sortedUsers.size());
            }
        }
    }

    final /* synthetic */ int lambda$updateOnlineCount$25$ProfileActivity(Integer lhs, Integer rhs) {
        User user1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC$ChatParticipant) this.info.participants.participants.get(rhs.intValue())).user_id));
        User user2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC$ChatParticipant) this.info.participants.participants.get(lhs.intValue())).user_id));
        int status1 = 0;
        int status2 = 0;
        if (!(user1 == null || user1.status == null)) {
            status1 = user1.id == UserConfig.getInstance(this.currentAccount).getClientUserId() ? ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + DefaultLoadControl.DEFAULT_MAX_BUFFER_MS : user1.status.expires;
        }
        if (!(user2 == null || user2.status == null)) {
            status2 = user2.id == UserConfig.getInstance(this.currentAccount).getClientUserId() ? ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + DefaultLoadControl.DEFAULT_MAX_BUFFER_MS : user2.status.expires;
        }
        if (status1 <= 0 || status2 <= 0) {
            if (status1 >= 0 || status2 >= 0) {
                if ((status1 < 0 && status2 > 0) || (status1 == 0 && status2 != 0)) {
                    return -1;
                }
                if ((status2 >= 0 || status1 <= 0) && (status2 != 0 || status1 == 0)) {
                    return 0;
                }
                return 1;
            } else if (status1 > status2) {
                return 1;
            } else {
                if (status1 < status2) {
                    return -1;
                }
                return 0;
            }
        } else if (status1 > status2) {
            return 1;
        } else {
            if (status1 < status2) {
                return -1;
            }
            return 0;
        }
    }

    public void setChatInfo(TLRPC$ChatFull chatInfo) {
        this.info = chatInfo;
        if (!(this.info == null || this.info.migrated_from_chat_id == 0)) {
            this.mergeDialogId = (long) (-this.info.migrated_from_chat_id);
        }
        fetchUsersFromChannelInfo();
    }

    private void fetchUsersFromChannelInfo() {
        if (this.currentChat != null && this.currentChat.megagroup && (this.info instanceof TLRPC$TL_channelFull) && this.info.participants != null) {
            for (int a = 0; a < this.info.participants.participants.size(); a++) {
                TLRPC$ChatParticipant chatParticipant = (TLRPC$ChatParticipant) this.info.participants.participants.get(a);
                this.participantsMap.put(chatParticipant.user_id, chatParticipant);
                if (((TLRPC$TL_chatChannelParticipant) chatParticipant).channelParticipant instanceof TLRPC$TL_channelParticipantCreator) {
                    this.creatorID = chatParticipant.user_id;
                }
            }
        }
    }

    private void kickUser(int uid) {
        if (uid != 0) {
            MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chat_id, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(uid)), this.info);
            return;
        }
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
        if (AndroidUtilities.isTablet()) {
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, Long.valueOf(-((long) this.chat_id)));
        } else {
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
        }
        MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chat_id, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId())), this.info);
        this.playProfileAnimation = false;
        finishFragment();
    }

    public boolean isChat() {
        return this.chat_id != 0;
    }

    private void updateRowsIds() {
        boolean hasUsername = false;
        this.emptyRow = -1;
        this.phoneRow = -1;
        this.userInfoRow = -1;
        this.userInfoDetailedRow = -1;
        this.userSectionRow = -1;
        this.sectionRow = -1;
        this.sharedMediaRow = -1;
        this.filterTypeSectionRow = -1;
        this.filterTypeRow = -1;
        this.sharedPhotoRow = -1;
        this.sharedVideoRow = -1;
        this.sharedFileRow = -1;
        this.sharedURLRow = -1;
        this.sharedMusicRow = -1;
        this.sharedVoiceRow = -1;
        this.settingsNotificationsRow = -1;
        this.usernameRow = -1;
        this.settingsTimerRow = -1;
        this.settingsKeyRow = -1;
        this.startSecretChatRow = -1;
        this.membersEndRow = -1;
        this.emptyRowChat2 = -1;
        this.addMemberRow = -1;
        this.channelInfoRow = -1;
        this.channelNameRow = -1;
        this.convertRow = -1;
        this.convertHelpRow = -1;
        this.emptyRowChat = -1;
        this.membersSectionRow = -1;
        this.membersRow = -1;
        this.leaveChannelRow = -1;
        this.loadMoreMembersRow = -1;
        this.groupsInCommonRow = -1;
        this.rowCount = 0;
        int i;
        if (this.user_id != 0) {
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
            i = this.rowCount;
            this.rowCount = i + 1;
            this.emptyRow = i;
            if (!(this.isBot || TextUtils.isEmpty(user.phone))) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.phoneRow = i;
            }
            TLRPC$TL_userFull userFull = MessagesController.getInstance(this.currentAccount).getUserFull(this.user_id);
            if (!(user == null || TextUtils.isEmpty(user.username))) {
                hasUsername = true;
            }
            if (!(userFull == null || TextUtils.isEmpty(userFull.about))) {
                if (this.phoneRow != -1) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.userSectionRow = i;
                }
                if (hasUsername || this.isBot) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.userInfoRow = i;
                } else {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.userInfoDetailedRow = i;
                }
            }
            if (hasUsername) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.usernameRow = i;
            }
            if (!(this.phoneRow == -1 && this.userInfoRow == -1 && this.userInfoDetailedRow == -1 && this.usernameRow == -1)) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sectionRow = i;
            }
            if (this.user_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsNotificationsRow = i;
            }
            if (TurboConfig.separateMedia) {
                if (TurboConfig.sharedPhotoTitle) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedPhotoRow = i;
                }
                if (TurboConfig.sharedVideoTitle) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedVideoRow = i;
                }
                if (TurboConfig.sharedFileTitle) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedFileRow = i;
                }
                if (TurboConfig.sharedLinkTitle) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedURLRow = i;
                }
                if (TurboConfig.sharedMusicTitle) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedMusicRow = i;
                }
                if (TurboConfig.sharedVoiceTitle) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedVoiceRow = i;
                }
            } else {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sharedMediaRow = i;
            }
            if (this.currentEncryptedChat instanceof TLRPC$TL_encryptedChat) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsTimerRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsKeyRow = i;
            }
            if (!(userFull == null || userFull.common_chats_count == 0)) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.groupsInCommonRow = i;
            }
            if (user != null && !this.isBot && this.currentEncryptedChat == null && user.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.startSecretChatRow = i;
            }
        } else if (this.chat_id == 0) {
        } else {
            if (this.chat_id > 0) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.emptyRow = i;
                if (ChatObject.isChannel(this.currentChat) && (!(this.info == null || this.info.about == null || this.info.about.length() <= 0) || (this.currentChat.username != null && this.currentChat.username.length() > 0))) {
                    if (!(this.info == null || this.info.about == null || this.info.about.length() <= 0)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.channelInfoRow = i;
                    }
                    if (this.currentChat.username != null && this.currentChat.username.length() > 0) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.channelNameRow = i;
                    }
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sectionRow = i;
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsNotificationsRow = i;
                if (TurboConfig.separateMedia) {
                    if (TurboConfig.sharedPhotoTitle) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.sharedPhotoRow = i;
                    }
                    if (TurboConfig.sharedVideoTitle) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.sharedVideoRow = i;
                    }
                    if (TurboConfig.sharedFileTitle) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.sharedFileRow = i;
                    }
                    if (TurboConfig.sharedLinkTitle) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.sharedURLRow = i;
                    }
                    if (TurboConfig.sharedMusicTitle) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.sharedMusicRow = i;
                    }
                    if (TurboConfig.sharedVoiceTitle) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.sharedVoiceRow = i;
                    }
                } else {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sharedMediaRow = i;
                }
                if (ChatObject.isChannel(this.currentChat)) {
                    if (!(this.currentChat.megagroup || this.info == null || (!this.currentChat.creator && !this.info.can_view_participants))) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.membersRow = i;
                    }
                    if (!(this.currentChat.creator || this.currentChat.left || this.currentChat.kicked || this.currentChat.megagroup)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.leaveChannelRow = i;
                    }
                    if (this.currentChat.megagroup && (((this.currentChat.admin_rights != null && this.currentChat.admin_rights.invite_users) || this.currentChat.creator || this.currentChat.democracy) && (this.info == null || this.info.participants_count < MessagesController.getInstance(this.currentAccount).maxMegagroupCount))) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.addMemberRow = i;
                    }
                    if (this.currentChat.megagroup) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.filterTypeSectionRow = i;
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.filterTypeRow = i;
                    }
                    if (this.info != null && this.currentChat.megagroup && this.info.participants != null && !this.info.participants.participants.isEmpty()) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.emptyRowChat = i;
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.membersSectionRow = i;
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.emptyRowChat2 = i;
                        this.rowCount += this.info.participants.participants.size();
                        this.membersEndRow = this.rowCount;
                        if (!this.usersEndReached) {
                            i = this.rowCount;
                            this.rowCount = i + 1;
                            this.loadMoreMembersRow = i;
                            return;
                        }
                        return;
                    }
                    return;
                }
                if (this.info != null) {
                    if (!(this.info.participants instanceof TLRPC$TL_chatParticipantsForbidden) && this.info.participants.participants.size() < MessagesController.getInstance(this.currentAccount).maxGroupCount && (this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.addMemberRow = i;
                    }
                    if (this.currentChat.creator && this.info.participants.participants.size() >= MessagesController.getInstance(this.currentAccount).minGroupConvertSize) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.convertRow = i;
                    }
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.emptyRowChat = i;
                if (this.convertRow != -1) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.convertHelpRow = i;
                } else {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.membersSectionRow = i;
                }
                if (this.info != null && !(this.info.participants instanceof TLRPC$TL_chatParticipantsForbidden)) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.emptyRowChat2 = i;
                    this.rowCount += this.info.participants.participants.size();
                    this.membersEndRow = this.rowCount;
                }
            } else if (!ChatObject.isChannel(this.currentChat) && this.info != null && !(this.info.participants instanceof TLRPC$TL_chatParticipantsForbidden)) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.addMemberRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.emptyRowChat2 = i;
                this.rowCount += this.info.participants.participants.size();
                this.membersEndRow = this.rowCount;
            }
        }
    }

    private void updateProfileData() {
        if (this.avatarImage != null && this.nameTextView != null) {
            String onlineTextOverride;
            int currentConnectionState = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
            if (currentConnectionState == 2) {
                onlineTextOverride = LocaleController.getString("WaitingForNetwork", R.string.WaitingForNetwork);
            } else if (currentConnectionState == 1) {
                onlineTextOverride = LocaleController.getString("Connecting", R.string.Connecting);
            } else if (currentConnectionState == 5) {
                onlineTextOverride = LocaleController.getString("Updating", R.string.Updating);
            } else if (currentConnectionState == 4) {
                onlineTextOverride = LocaleController.getString("ConnectingToProxy", R.string.ConnectingToProxy);
            } else {
                onlineTextOverride = null;
            }
            TLObject photo;
            TLRPC$FileLocation photoBig;
            String newString;
            int a;
            if (this.user_id != 0) {
                String newString2;
                boolean z;
                User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
                photo = null;
                photoBig = null;
                if (user.photo != null) {
                    photo = user.photo.photo_small;
                    photoBig = user.photo.photo_big;
                }
                this.avatarDrawable.setInfo(user);
                this.avatarImage.setImage(photo, "50_50", this.avatarDrawable);
                newString = UserObject.getUserName(user);
                if (user.id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                    newString2 = LocaleController.getString("ChatYourSelf", R.string.ChatYourSelf);
                    newString = LocaleController.getString("ChatYourSelfName", R.string.ChatYourSelfName);
                } else if (user.id == 333000 || user.id == 777000) {
                    newString2 = LocaleController.getString("ServiceNotifications", R.string.ServiceNotifications);
                } else if (this.isBot) {
                    newString2 = LocaleController.getString("Bot", R.string.Bot);
                } else {
                    newString2 = LocaleController.formatUserStatus(this.currentAccount, user);
                }
                for (a = 0; a < 2; a++) {
                    if (this.nameTextView[a] != null) {
                        if (a == 0 && user.id != UserConfig.getInstance(this.currentAccount).getClientUserId() && user.id / 1000 != 777 && user.id / 1000 != 333 && user.phone != null && user.phone.length() != 0 && ContactsController.getInstance(this.currentAccount).contactsDict.get(Integer.valueOf(user.id)) == null && (ContactsController.getInstance(this.currentAccount).contactsDict.size() != 0 || !ContactsController.getInstance(this.currentAccount).isLoadingContacts())) {
                            String phoneString = PhoneFormat.getInstance().format("+" + user.phone);
                            if (!this.nameTextView[a].getText().equals(phoneString)) {
                                this.nameTextView[a].setText(phoneString);
                            }
                        } else if (!this.nameTextView[a].getText().equals(newString)) {
                            this.nameTextView[a].setText(newString);
                        }
                        if (a == 0 && onlineTextOverride != null) {
                            this.onlineTextView[a].setText(onlineTextOverride);
                        } else if (!this.onlineTextView[a].getText().equals(newString2)) {
                            this.onlineTextView[a].setText(newString2);
                        }
                        Drawable leftIcon = this.currentEncryptedChat != null ? Theme.chat_lockIconDrawable : null;
                        Drawable rightIcon = null;
                        if (a == 0) {
                            rightIcon = MessagesController.getInstance(this.currentAccount).isDialogMuted((this.dialog_id > 0 ? 1 : (this.dialog_id == 0 ? 0 : -1)) != 0 ? this.dialog_id : (long) this.user_id) ? Theme.chat_muteIconDrawable : null;
                        } else if (user.verified) {
                            Drawable combinedDrawable = new CombinedDrawable(Theme.profile_verifiedDrawable, Theme.profile_verifiedCheckDrawable);
                        }
                        this.nameTextView[a].setLeftDrawable(leftIcon);
                        this.nameTextView[a].setRightDrawable(rightIcon);
                    }
                }
                ImageReceiver imageReceiver = this.avatarImage.getImageReceiver();
                if (PhotoViewer.isShowingImage(photoBig)) {
                    z = false;
                } else {
                    z = true;
                }
                imageReceiver.setVisible(z, false);
            } else if (this.chat_id != 0) {
                int[] result;
                String shortNumber;
                TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
                if (chat != null) {
                    this.currentChat = chat;
                } else {
                    chat = this.currentChat;
                }
                if (!ChatObject.isChannel(chat)) {
                    int count = chat.participants_count;
                    if (this.info != null) {
                        count = this.info.participants.participants.size();
                    }
                    if (count == 0 || this.onlineCount <= 1) {
                        newString = LocaleController.formatPluralString("Members", count);
                    } else {
                        newString = String.format("%s, %s", new Object[]{LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("OnlineCount", this.onlineCount)});
                    }
                } else if (this.info == null || (!this.currentChat.megagroup && (this.info.participants_count == 0 || this.currentChat.admin || this.info.can_view_participants))) {
                    if (this.currentChat.megagroup) {
                        newString = LocaleController.getString("Loading", R.string.Loading).toLowerCase();
                    } else if ((chat.flags & 64) != 0) {
                        newString = LocaleController.getString("ChannelPublic", R.string.ChannelPublic).toLowerCase();
                    } else {
                        newString = LocaleController.getString("ChannelPrivate", R.string.ChannelPrivate).toLowerCase();
                    }
                } else if (!this.currentChat.megagroup || this.info.participants_count > 200) {
                    result = new int[1];
                    shortNumber = LocaleController.formatShortNumber(this.info.participants_count, result);
                    if (this.currentChat.megagroup) {
                        newString = LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), shortNumber);
                        this.adminTextView.setText(LocaleController.getString("Loading", R.string.Loading));
                    } else {
                        newString = LocaleController.formatPluralString("Subscribers", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), shortNumber);
                    }
                } else {
                    if (this.onlineCount <= 1 || this.info.participants_count == 0) {
                        newString = LocaleController.formatPluralString("Members", this.info.participants_count);
                    } else {
                        newString = String.format("%s, %s", new Object[]{LocaleController.formatPluralString("Members", this.info.participants_count), LocaleController.formatPluralString("OnlineCount", this.onlineCount)});
                    }
                    this.adminTextView.setText(LocaleController.getString("Loading", R.string.Loading));
                }
                if (this.creatorID != 0) {
                    this.adminTextView.setText(LocaleController.getString("ChannelCreator", R.string.ChannelCreator) + ": " + UserObject.getUserName(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.creatorID))));
                }
                a = 0;
                while (a < 2) {
                    if (this.nameTextView[a] != null) {
                        if (!(chat.title == null || this.nameTextView[a].getText().equals(chat.title))) {
                            this.nameTextView[a].setText(chat.title);
                        }
                        this.nameTextView[a].setLeftDrawable(null);
                        if (a == 0) {
                            this.nameTextView[a].setRightDrawable(MessagesController.getInstance(this.currentAccount).isDialogMuted((long) (-this.chat_id)) ? Theme.chat_muteIconDrawable : null);
                        } else if (chat.verified) {
                            this.nameTextView[a].setRightDrawable(new CombinedDrawable(Theme.profile_verifiedDrawable, Theme.profile_verifiedCheckDrawable));
                        } else {
                            this.nameTextView[a].setRightDrawable(null);
                        }
                        if (a == 0 && onlineTextOverride != null) {
                            this.onlineTextView[a].setText(onlineTextOverride);
                        } else if (!this.currentChat.megagroup || this.info == null || this.info.participants_count > 200 || this.onlineCount <= 0) {
                            if (a == 0 && ChatObject.isChannel(this.currentChat) && this.info != null && this.info.participants_count != 0 && (this.currentChat.megagroup || this.currentChat.broadcast)) {
                                result = new int[1];
                                shortNumber = LocaleController.formatShortNumber(this.info.participants_count, result);
                                if (this.currentChat.megagroup) {
                                    this.onlineTextView[a].setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), shortNumber));
                                } else {
                                    this.onlineTextView[a].setText(LocaleController.formatPluralString("Subscribers", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), shortNumber));
                                }
                            } else if (!this.onlineTextView[a].getText().equals(newString)) {
                                this.onlineTextView[a].setText(newString);
                            }
                        } else if (!this.onlineTextView[a].getText().equals(newString)) {
                            this.onlineTextView[a].setText(newString);
                        }
                    }
                    a++;
                }
                photo = null;
                photoBig = null;
                if (chat.photo != null) {
                    photo = chat.photo.photo_small;
                    photoBig = chat.photo.photo_big;
                }
                this.avatarDrawable.setInfo(chat);
                this.avatarImage.setImage(photo, "50_50", this.avatarDrawable);
                this.avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig), false);
            }
            this.adminTextView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    try {
                        if (ProfileActivity.this.creatorID != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId()) {
                            Bundle args = new Bundle();
                            args.putInt("user_id", ProfileActivity.this.creatorID);
                            ProfileActivity.this.presentFragment(new ProfileActivity(args));
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    private void createActionBarMenu() {
        ActionBarMenu menu = this.actionBar.createMenu();
        menu.clearItems();
        this.animatingItem = null;
        ActionBarMenuItem item = null;
        if (this.user_id != 0) {
            if (UserConfig.getInstance(this.currentAccount).getClientUserId() != this.user_id) {
                TLRPC$TL_userFull userFull = MessagesController.getInstance(this.currentAccount).getUserFull(this.user_id);
                if (TurboConfig.callsEnabled && userFull != null && userFull.phone_calls_available) {
                    this.callItem = menu.addItem(15, (int) R.drawable.ic_call_white_24dp);
                }
                if (ContactsController.getInstance(this.currentAccount).contactsDict.get(Integer.valueOf(this.user_id)) == null) {
                    User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
                    if (user != null) {
                        item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                        if (this.isBot) {
                            if (!user.bot_nochats) {
                                item.addSubItem(9, LocaleController.getString("BotInvite", R.string.BotInvite));
                            }
                            item.addSubItem(10, LocaleController.getString("BotShare", R.string.BotShare));
                        }
                        if (user.phone != null && user.phone.length() != 0) {
                            item.addSubItem(1, LocaleController.getString("AddContact", R.string.AddContact));
                            item.addSubItem(3, LocaleController.getString("ShareContact", R.string.ShareContact));
                            item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BlockContact", R.string.BlockContact) : LocaleController.getString("Unblock", R.string.Unblock));
                        } else if (this.isBot) {
                            item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BotStop", R.string.BotStop) : LocaleController.getString("BotRestart", R.string.BotRestart));
                        } else {
                            item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BlockContact", R.string.BlockContact) : LocaleController.getString("Unblock", R.string.Unblock));
                        }
                        item.addSubItem(add_to_group, LocaleController.getString("AddToGroupAndChannel", R.string.AddToGroupAndChannel));
                    } else {
                        return;
                    }
                }
                item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                item.addSubItem(3, LocaleController.getString("ShareContact", R.string.ShareContact));
                item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BlockContact", R.string.BlockContact) : LocaleController.getString("Unblock", R.string.Unblock));
                item.addSubItem(4, LocaleController.getString("EditContact", R.string.EditContact));
                item.addSubItem(5, LocaleController.getString("DeleteContact", R.string.DeleteContact));
                if (TurboConfig.containValue("specific_c" + this.user_id)) {
                    item.addSubItem(special_contact, LocaleController.getString("DeleteFromSpecifics", R.string.DeleteFromSpecifics));
                } else {
                    item.addSubItem(special_contact, LocaleController.getString("AddToSpecifics", R.string.AddToSpecifics));
                }
                item.addSubItem(add_to_group, LocaleController.getString("AddToGroupAndChannel", R.string.AddToGroupAndChannel));
            } else {
                item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                item.addSubItem(3, LocaleController.getString("ShareContact", R.string.ShareContact));
            }
        } else if (this.chat_id != 0) {
            if (this.chat_id > 0) {
                TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
                if (this.writeButton != null) {
                    boolean isChannel = ChatObject.isChannel(this.currentChat);
                    if ((!isChannel || ChatObject.canChangeChatInfo(this.currentChat)) && (isChannel || this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                        this.writeButton.setImageResource(R.drawable.floating_camera);
                        this.writeButton.setPadding(0, 0, 0, 0);
                    } else {
                        this.writeButton.setImageResource(R.drawable.floating_message);
                        this.writeButton.setPadding(0, AndroidUtilities.dp(3.0f), 0, 0);
                    }
                }
                if (ChatObject.isChannel(chat)) {
                    if (ChatObject.hasAdminRights(chat)) {
                        this.editItem = menu.addItem(12, (int) R.drawable.menu_settings);
                        if (null == null) {
                            item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                        }
                        if (chat.megagroup) {
                            item.addSubItem(12, LocaleController.getString("ManageGroupMenu", R.string.ManageGroupMenu));
                        } else {
                            item.addSubItem(12, LocaleController.getString("ManageChannelMenu", R.string.ManageChannelMenu));
                        }
                    }
                    if (chat.megagroup) {
                        if (item == null) {
                            item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                        }
                        item.addSubItem(16, LocaleController.getString("SearchMembers", R.string.SearchMembers));
                        if (!(chat.creator || chat.left || chat.kicked)) {
                            item.addSubItem(7, LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu));
                        }
                    }
                } else {
                    if (!chat.admins_enabled || chat.creator || chat.admin) {
                        this.editItem = menu.addItem(8, (int) R.drawable.group_edit_profile);
                    }
                    item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                    if (chat.creator && this.chat_id > 0) {
                        item.addSubItem(11, LocaleController.getString("SetAdmins", R.string.SetAdmins));
                    }
                    if (!chat.admins_enabled || chat.creator || chat.admin) {
                        item.addSubItem(8, LocaleController.getString("ChannelEdit", R.string.ChannelEdit));
                    }
                    item.addSubItem(16, LocaleController.getString("SearchMembers", R.string.SearchMembers));
                    if (chat.creator && (this.info == null || this.info.participants.participants.size() > 0)) {
                        item.addSubItem(13, LocaleController.getString("ConvertGroupMenu", R.string.ConvertGroupMenu));
                    }
                    item.addSubItem(7, LocaleController.getString("DeleteAndExit", R.string.DeleteAndExit));
                }
            } else {
                item = menu.addItem(10, (int) R.drawable.ic_ab_other);
                item.addSubItem(8, LocaleController.getString("EditName", R.string.EditName));
            }
        }
        if (item == null) {
            item = menu.addItem(10, (int) R.drawable.ic_ab_other);
        }
        item.addSubItem(14, LocaleController.getString("AddShortcut", R.string.AddShortcut));
    }

    protected void onDialogDismiss(Dialog dialog) {
        if (this.listView != null) {
            this.listView.invalidateViews();
        }
    }

    public void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param) {
        long did = ((Long) dids.get(0)).longValue();
        Bundle args = new Bundle();
        args.putBoolean("scrollToTopOnResume", true);
        int lower_part = (int) did;
        if (lower_part == 0) {
            args.putInt("enc_id", (int) (did >> 32));
        } else if (lower_part > 0) {
            args.putInt("user_id", lower_part);
        } else if (lower_part < 0) {
            args.putInt("chat_id", -lower_part);
        }
        if (MessagesController.getInstance(this.currentAccount).checkCanOpenChat(args, fragment)) {
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            presentFragment(new ChatActivity(args), true);
            removeSelfFromStack();
            SendMessagesHelper.getInstance(this.currentAccount).sendMessage(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id)), did, null, null, null);
        }
    }

    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) {
            User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
            if (user != null) {
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    VoIPHelper.permissionDenied(getParentActivity(), null);
                } else {
                    VoIPHelper.startCall(user, getParentActivity(), MessagesController.getInstance(this.currentAccount).getUserFull(user.id));
                }
            }
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescriptionDelegate cellDelegate = new ProfileActivity$$Lambda$17(this);
        ThemeDescription[] themeDescriptionArr = new ThemeDescription[94];
        themeDescriptionArr[0] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite);
        themeDescriptionArr[1] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground);
        themeDescriptionArr[2] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem);
        themeDescriptionArr[3] = new ThemeDescription(this.nameTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_profile_title);
        themeDescriptionArr[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue);
        themeDescriptionArr[5] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue);
        themeDescriptionArr[6] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue);
        themeDescriptionArr[7] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue);
        themeDescriptionArr[8] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue);
        themeDescriptionArr[9] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileBlue);
        themeDescriptionArr[10] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarRed);
        themeDescriptionArr[11] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarRed);
        themeDescriptionArr[12] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarRed);
        themeDescriptionArr[13] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorRed);
        themeDescriptionArr[14] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileRed);
        themeDescriptionArr[15] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconRed);
        themeDescriptionArr[16] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarOrange);
        themeDescriptionArr[17] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarOrange);
        themeDescriptionArr[18] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarOrange);
        themeDescriptionArr[19] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorOrange);
        themeDescriptionArr[20] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileOrange);
        themeDescriptionArr[21] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconOrange);
        themeDescriptionArr[22] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarViolet);
        themeDescriptionArr[23] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarViolet);
        themeDescriptionArr[24] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarViolet);
        themeDescriptionArr[25] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorViolet);
        themeDescriptionArr[26] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileViolet);
        themeDescriptionArr[27] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconViolet);
        themeDescriptionArr[28] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarGreen);
        themeDescriptionArr[29] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarGreen);
        themeDescriptionArr[30] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarGreen);
        themeDescriptionArr[31] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorGreen);
        themeDescriptionArr[32] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileGreen);
        themeDescriptionArr[33] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconGreen);
        themeDescriptionArr[34] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarCyan);
        themeDescriptionArr[35] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarCyan);
        themeDescriptionArr[36] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarCyan);
        themeDescriptionArr[37] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorCyan);
        themeDescriptionArr[38] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileCyan);
        themeDescriptionArr[39] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconCyan);
        themeDescriptionArr[40] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarPink);
        themeDescriptionArr[41] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarPink);
        themeDescriptionArr[42] = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarPink);
        themeDescriptionArr[43] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorPink);
        themeDescriptionArr[44] = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfilePink);
        themeDescriptionArr[45] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconPink);
        themeDescriptionArr[46] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector);
        themeDescriptionArr[47] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        themeDescriptionArr[48] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        themeDescriptionArr[49] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text);
        themeDescriptionArr[50] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfileRed);
        themeDescriptionArr[51] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfileOrange);
        themeDescriptionArr[52] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfileViolet);
        themeDescriptionArr[53] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfileGreen);
        themeDescriptionArr[54] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfileCyan);
        themeDescriptionArr[55] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfileBlue);
        themeDescriptionArr[56] = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[]{this.avatarDrawable}, null, Theme.key_avatar_backgroundInProfilePink);
        themeDescriptionArr[57] = new ThemeDescription(this.writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_profile_actionIcon);
        themeDescriptionArr[58] = new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_profile_actionBackground);
        themeDescriptionArr[59] = new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_profile_actionPressedBackground);
        themeDescriptionArr[60] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[61] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGreenText2);
        themeDescriptionArr[62] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText5);
        themeDescriptionArr[63] = new ThemeDescription(this.listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText);
        themeDescriptionArr[64] = new ThemeDescription(this.listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[65] = new ThemeDescription(this.listView, 0, new Class[]{TextDetailCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[66] = new ThemeDescription(this.listView, 0, new Class[]{TextDetailCell.class}, new String[]{"valueImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[67] = new ThemeDescription(this.listView, 0, new Class[]{TextDetailCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[68] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{UserCell.class}, new String[]{"adminImage"}, null, null, null, Theme.key_profile_creatorIcon);
        themeDescriptionArr[69] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{UserCell.class}, new String[]{"adminImage"}, null, null, null, Theme.key_profile_adminIcon);
        themeDescriptionArr[70] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[71] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[72] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteGrayText);
        themeDescriptionArr[73] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlueText);
        themeDescriptionArr[74] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text);
        themeDescriptionArr[75] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed);
        themeDescriptionArr[76] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange);
        themeDescriptionArr[77] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet);
        themeDescriptionArr[78] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen);
        themeDescriptionArr[79] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan);
        themeDescriptionArr[80] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue);
        themeDescriptionArr[81] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink);
        themeDescriptionArr[82] = new ThemeDescription(this.listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle);
        themeDescriptionArr[83] = new ThemeDescription(this.listView, 0, new Class[]{AboutLinkCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[84] = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[85] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteLinkText);
        themeDescriptionArr[86] = new ThemeDescription(this.listView, 0, new Class[]{AboutLinkCell.class}, Theme.linkSelectionPaint, null, null, Theme.key_windowBackgroundWhiteLinkSelection);
        themeDescriptionArr[87] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        themeDescriptionArr[88] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray);
        themeDescriptionArr[89] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        themeDescriptionArr[90] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGray);
        themeDescriptionArr[91] = new ThemeDescription(this.listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4);
        themeDescriptionArr[92] = new ThemeDescription(this.nameTextView[1], 0, null, null, new Drawable[]{Theme.profile_verifiedCheckDrawable}, null, Theme.key_profile_verifiedCheck);
        themeDescriptionArr[93] = new ThemeDescription(this.nameTextView[1], 0, null, null, new Drawable[]{Theme.profile_verifiedDrawable}, null, Theme.key_profile_verifiedBackground);
        return themeDescriptionArr;
    }

    final /* synthetic */ void lambda$getThemeDescriptions$26$ProfileActivity() {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(0);
                }
            }
        }
    }

    private void getMediaCount() {
        if (this.dialog_id != 0) {
            if (TurboConfig.sharedPhotoTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 5, this.classGuid, true);
            }
            if (TurboConfig.sharedVideoTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 6, this.classGuid, true);
            }
            if (TurboConfig.sharedFileTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 1, this.classGuid, true);
            }
            if (TurboConfig.sharedLinkTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 3, this.classGuid, true);
            }
            if (TurboConfig.sharedMusicTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 4, this.classGuid, true);
            }
            if (TurboConfig.sharedVoiceTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 2, this.classGuid, true);
            }
        } else if (this.user_id != 0) {
            if (TurboConfig.sharedPhotoTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, 5, this.classGuid, true);
            }
            if (TurboConfig.sharedVideoTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, 6, this.classGuid, true);
            }
            if (TurboConfig.sharedFileTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, 1, this.classGuid, true);
            }
            if (TurboConfig.sharedLinkTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, 3, this.classGuid, true);
            }
            if (TurboConfig.sharedMusicTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, 4, this.classGuid, true);
            }
            if (TurboConfig.sharedVoiceTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) this.user_id, 2, this.classGuid, true);
            }
        } else if (this.chat_id > 0) {
            if (TurboConfig.sharedPhotoTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), 5, this.classGuid, true);
            }
            if (TurboConfig.sharedVideoTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), 6, this.classGuid, true);
            }
            if (TurboConfig.sharedFileTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), 1, this.classGuid, true);
            }
            if (TurboConfig.sharedLinkTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), 3, this.classGuid, true);
            }
            if (TurboConfig.sharedMusicTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), 4, this.classGuid, true);
            }
            if (TurboConfig.sharedVoiceTitle) {
                DataQuery.getInstance(this.currentAccount).getMediaCount((long) (-this.chat_id), 2, this.classGuid, true);
            }
            if (this.mergeDialogId != 0) {
                if (TurboConfig.sharedPhotoTitle) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 5, this.classGuid, true);
                }
                if (TurboConfig.sharedVideoTitle) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 6, this.classGuid, true);
                }
                if (TurboConfig.sharedFileTitle) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 1, this.classGuid, true);
                }
                if (TurboConfig.sharedLinkTitle) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 3, this.classGuid, true);
                }
                if (TurboConfig.sharedMusicTitle) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 4, this.classGuid, true);
                }
                if (TurboConfig.sharedVoiceTitle) {
                    DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 2, this.classGuid, true);
                }
            }
        }
    }

    private void loadMediaCount(long uid, long did, int mCount, int type) {
        int count;
        int a;
        Holder holder;
        if (type == 5) {
            if (uid == did) {
                this.totalPhotoCount = mCount;
            } else {
                this.totalPhotoCountMerge = mCount;
            }
            if (this.listView != null) {
                count = this.listView.getChildCount();
                for (a = 0; a < count; a++) {
                    holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                    if (holder.getAdapterPosition() == this.sharedPhotoRow) {
                        this.listAdapter.onBindViewHolder(holder, this.sharedPhotoRow);
                        return;
                    }
                }
            }
        } else if (type == 6) {
            if (uid == did) {
                this.totalVideoCount = mCount;
            } else {
                this.totalVideoCountMerge = mCount;
            }
            if (this.listView != null) {
                count = this.listView.getChildCount();
                for (a = 0; a < count; a++) {
                    holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                    if (holder.getAdapterPosition() == this.sharedVideoRow) {
                        this.listAdapter.onBindViewHolder(holder, this.sharedVideoRow);
                        return;
                    }
                }
            }
        } else if (type == 1) {
            if (uid == did) {
                this.totalFileCount = mCount;
            } else {
                this.totalFileCountMerge = mCount;
            }
            if (this.listView != null) {
                count = this.listView.getChildCount();
                for (a = 0; a < count; a++) {
                    holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                    if (holder.getAdapterPosition() == this.sharedFileRow) {
                        this.listAdapter.onBindViewHolder(holder, this.sharedFileRow);
                        return;
                    }
                }
            }
        } else if (type == 3) {
            if (uid == did) {
                this.totalURLCount = mCount;
            } else {
                this.totalURLCountMerge = mCount;
            }
            if (this.listView != null) {
                count = this.listView.getChildCount();
                for (a = 0; a < count; a++) {
                    holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                    if (holder.getAdapterPosition() == this.sharedURLRow) {
                        this.listAdapter.onBindViewHolder(holder, this.sharedURLRow);
                        return;
                    }
                }
            }
        } else if (type == 4) {
            if (uid == did) {
                this.totalMusicCount = mCount;
            } else {
                this.totalMusicCountMerge = mCount;
            }
            if (this.listView != null) {
                count = this.listView.getChildCount();
                for (a = 0; a < count; a++) {
                    holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                    if (holder.getAdapterPosition() == this.sharedMusicRow) {
                        this.listAdapter.onBindViewHolder(holder, this.sharedMusicRow);
                        return;
                    }
                }
            }
        } else if (type == 2) {
            if (uid == did) {
                this.totalVoiceCount = mCount;
            } else {
                this.totalVoiceCountMerge = mCount;
            }
            if (this.listView != null) {
                count = this.listView.getChildCount();
                for (a = 0; a < count; a++) {
                    holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                    if (holder.getAdapterPosition() == this.sharedVoiceRow) {
                        this.listAdapter.onBindViewHolder(holder, this.sharedVoiceRow);
                        return;
                    }
                }
            }
        }
    }

    private void setRowTextAndValue(int i, TextCell textCell) {
        String value;
        String str;
        Object[] objArr;
        if (i == this.sharedPhotoRow) {
            if (this.totalPhotoCount == -1) {
                value = LocaleController.getString("Loading", R.string.Loading);
            } else {
                str = "%d";
                objArr = new Object[1];
                objArr[0] = Integer.valueOf((this.totalPhotoCountMerge != -1 ? this.totalPhotoCountMerge : 0) + this.totalPhotoCount);
                value = String.format(str, objArr);
            }
            textCell.setTextAndValue(LocaleController.getString("SharedPhotoTitle", R.string.SharedPhotoTitle), value);
        } else if (i == this.sharedVideoRow) {
            if (this.totalVideoCount == -1) {
                value = LocaleController.getString("Loading", R.string.Loading);
            } else {
                str = "%d";
                objArr = new Object[1];
                objArr[0] = Integer.valueOf((this.totalVideoCountMerge != -1 ? this.totalVideoCountMerge : 0) + this.totalVideoCount);
                value = String.format(str, objArr);
            }
            textCell.setTextAndValue(LocaleController.getString("SharedVideoTitle", R.string.SharedVideoTitle), value);
        } else if (i == this.sharedFileRow) {
            if (this.totalFileCount == -1) {
                value = LocaleController.getString("Loading", R.string.Loading);
            } else {
                str = "%d";
                objArr = new Object[1];
                objArr[0] = Integer.valueOf((this.totalFileCountMerge != -1 ? this.totalFileCountMerge : 0) + this.totalFileCount);
                value = String.format(str, objArr);
            }
            textCell.setTextAndValue(LocaleController.getString("SharedFileTitle", R.string.SharedFileTitle), value);
        } else if (i == this.sharedURLRow) {
            if (this.totalURLCount == -1) {
                value = LocaleController.getString("Loading", R.string.Loading);
            } else {
                str = "%d";
                objArr = new Object[1];
                objArr[0] = Integer.valueOf((this.totalURLCountMerge != -1 ? this.totalURLCountMerge : 0) + this.totalURLCount);
                value = String.format(str, objArr);
            }
            textCell.setTextAndValue(LocaleController.getString("SharedLinkTitle", R.string.SharedLinkTitle), value);
        } else if (i == this.sharedMusicRow) {
            if (this.totalMusicCount == -1) {
                value = LocaleController.getString("Loading", R.string.Loading);
            } else {
                str = "%d";
                objArr = new Object[1];
                objArr[0] = Integer.valueOf((this.totalMusicCountMerge != -1 ? this.totalMusicCountMerge : 0) + this.totalMusicCount);
                value = String.format(str, objArr);
            }
            textCell.setTextAndValue(LocaleController.getString("SharedMusicTitle", R.string.SharedMusicTitle), value);
        } else if (i == this.sharedVoiceRow) {
            if (this.totalVoiceCount == -1) {
                value = LocaleController.getString("Loading", R.string.Loading);
            } else {
                str = "%d";
                objArr = new Object[1];
                objArr[0] = Integer.valueOf((this.totalVoiceCountMerge != -1 ? this.totalVoiceCountMerge : 0) + this.totalVoiceCount);
                value = String.format(str, objArr);
            }
            textCell.setTextAndValue(LocaleController.getString("SharedVoiceTitle", R.string.SharedVoiceTitle), value);
        }
    }

    private int getParticipantType(TLRPC$ChatParticipant part) {
        if (part == null) {
            return 0;
        }
        if (part instanceof TLRPC$TL_chatChannelParticipant) {
            TLRPC$ChannelParticipant channelParticipant = ((TLRPC$TL_chatChannelParticipant) part).channelParticipant;
            if (channelParticipant instanceof TLRPC$TL_channelParticipantCreator) {
                return 1;
            }
            if (channelParticipant instanceof TLRPC$TL_channelParticipantAdmin) {
                return 2;
            }
            return 0;
        } else if (part instanceof TLRPC$TL_chatParticipantCreator) {
            return 1;
        } else {
            if (part instanceof TLRPC$TL_chatParticipantAdmin) {
                return 2;
            }
            return 0;
        }
    }

    private void addToGroups() {
        if (this.user_id != 0) {
            final User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
            if (user != null) {
                Bundle args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putBoolean("addToGroup", true);
                args.putInt("dialogsType", 0);
                DialogsActivity fragment = new DialogsActivity(args);
                fragment.setDelegate(new DialogsActivityDelegate() {
                    public void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param) {
                        long did = ((Long) dids.get(0)).longValue();
                        if (MessagesController.getInstance(ProfileActivity.this.currentAccount).getChat(Integer.valueOf((int) (-did))) != null) {
                            Builder builder = new Builder(ProfileActivity.this.getParentActivity());
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new ProfileActivity$14$$Lambda$0(this, did, user));
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            builder.setMessage(LocaleController.formatString("AddToTheGroupTitle", R.string.AddToTheGroupTitle, new Object[]{UserObject.getUserName(user), chat.title}));
                            ProfileActivity.this.showDialog(builder.create());
                        }
                    }

                    final /* synthetic */ void lambda$didSelectDialogs$0$ProfileActivity$14(long did, User user, DialogInterface dialogInterface, int i) {
                        MessagesController.getInstance(ProfileActivity.this.currentAccount).addUserToChat((int) (-did), user, null, 0, null, ProfileActivity.this);
                        ProfileActivity.this.finishFragment();
                    }
                });
                presentFragment(fragment);
            }
        }
    }

    private void selectedMode(int selectedMode) {
        int i = 0;
        int i2;
        if (selectedMode == 1) {
            i2 = this.totalFileCount;
            if (this.totalFileCountMerge != -1) {
                i = this.totalFileCountMerge;
            }
            if (i + i2 == 0) {
                selectedMode = 0;
            }
        } else if (selectedMode == 3) {
            i2 = this.totalURLCount;
            if (this.totalURLCountMerge != -1) {
                i = this.totalURLCountMerge;
            }
            if (i + i2 == 0) {
                selectedMode = 0;
            }
        } else if (selectedMode == 4) {
            i2 = this.totalMusicCount;
            if (this.totalMusicCountMerge != -1) {
                i = this.totalMusicCountMerge;
            }
            if (i + i2 == 0) {
                selectedMode = 0;
            }
        } else if (selectedMode == 2) {
            i2 = this.totalVoiceCount;
            if (this.totalVoiceCountMerge != -1) {
                i = this.totalVoiceCountMerge;
            }
            if (i + i2 == 0) {
                selectedMode = 0;
            }
        }
        Bundle args = new Bundle();
        if (this.user_id != 0) {
            args.putLong("dialog_id", this.dialog_id != 0 ? this.dialog_id : (long) this.user_id);
        } else {
            args.putLong("dialog_id", (long) (-this.chat_id));
        }
        int[] media = new int[7];
        int a = 0;
        while (a < media.length) {
            media[a] = this.mediaCount[a];
            if (this.mediaCount[a] >= 0 && this.mediaMergeCount[a] >= 0) {
                media[a] = this.mediaCount[a] + this.mediaMergeCount[a];
            } else if (this.mediaCount[a] >= 0) {
                media[a] = this.mediaCount[a];
            } else if (this.mediaMergeCount[a] >= 0) {
                media[a] = this.mediaMergeCount[a];
            } else {
                media[a] = -1;
            }
            a++;
        }
        args.putInt("selected_mode", selectedMode);
        MediaActivity fragment = new MediaActivity(args, media);
        fragment.setChatInfo(this.info);
        presentFragment(fragment);
    }
}
