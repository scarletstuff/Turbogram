package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.baranak.turbogramf.R;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$InputFile;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$TL_chatPhoto;
import org.telegram.tgnet.TLRPC$TL_secureFile;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.ImageUpdater.ImageUpdaterDelegate;
import org.telegram.ui.Components.LayoutHelper;

public class ChangeChatNameActivity extends BaseFragment implements ImageUpdaterDelegate {
    private static final int done_button = 1;
    private TLRPC$FileLocation avatar;
    private AvatarDrawable avatarDrawable;
    private BackupImageView avatarImage;
    private int chatId;
    private boolean createAfterUpload;
    private TLRPC$Chat currentChat;
    private View doneButton;
    private boolean donePressed;
    private View headerLabelView;
    private ImageUpdater imageUpdater;
    private EditTextBoldCursor nameTextView;
    private AlertDialog progressDialog;
    private TLRPC$InputFile uploadedAvatar;

    /* renamed from: org.telegram.ui.ChangeChatNameActivity$1 */
    class C11631 extends ActionBarMenuOnItemClick {

        /* renamed from: org.telegram.ui.ChangeChatNameActivity$1$1 */
        class C11621 implements OnClickListener {
            C11621() {
            }

            public void onClick(DialogInterface dialog, int which) {
                ChangeChatNameActivity.this.createAfterUpload = false;
                ChangeChatNameActivity.this.progressDialog = null;
                ChangeChatNameActivity.this.donePressed = false;
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }

        C11631() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                ChangeChatNameActivity.this.finishFragment();
            } else if (id == 1 && !ChangeChatNameActivity.this.donePressed) {
                if (ChangeChatNameActivity.this.nameTextView.length() == 0) {
                    Vibrator v = (Vibrator) ChangeChatNameActivity.this.getParentActivity().getSystemService("vibrator");
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(ChangeChatNameActivity.this.nameTextView, 2.0f, 0);
                    return;
                }
                ChangeChatNameActivity.this.donePressed = true;
                if (ChangeChatNameActivity.this.imageUpdater.uploadingImage != null) {
                    ChangeChatNameActivity.this.createAfterUpload = true;
                    ChangeChatNameActivity.this.progressDialog = new AlertDialog(ChangeChatNameActivity.this.getParentActivity(), 1);
                    ChangeChatNameActivity.this.progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
                    ChangeChatNameActivity.this.progressDialog.setCanceledOnTouchOutside(false);
                    ChangeChatNameActivity.this.progressDialog.setCancelable(false);
                    ChangeChatNameActivity.this.progressDialog.setButton(-2, LocaleController.getString("Cancel", R.string.Cancel), new C11621());
                    ChangeChatNameActivity.this.progressDialog.show();
                    return;
                }
                if (ChangeChatNameActivity.this.uploadedAvatar != null) {
                    MessagesController.getInstance(ChangeChatNameActivity.this.currentAccount).changeChatAvatar(ChangeChatNameActivity.this.chatId, ChangeChatNameActivity.this.uploadedAvatar);
                } else if (ChangeChatNameActivity.this.avatar == null && (ChangeChatNameActivity.this.currentChat.photo instanceof TLRPC$TL_chatPhoto)) {
                    MessagesController.getInstance(ChangeChatNameActivity.this.currentAccount).changeChatAvatar(ChangeChatNameActivity.this.chatId, null);
                }
                ChangeChatNameActivity.this.finishFragment();
                if (ChangeChatNameActivity.this.nameTextView.getText().length() != 0) {
                    ChangeChatNameActivity.this.saveName();
                    ChangeChatNameActivity.this.finishFragment();
                }
            }
        }
    }

    /* renamed from: org.telegram.ui.ChangeChatNameActivity$2 */
    class C11642 implements OnTouchListener {
        C11642() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: org.telegram.ui.ChangeChatNameActivity$3 */
    class C11663 implements View.OnClickListener {

        /* renamed from: org.telegram.ui.ChangeChatNameActivity$3$1 */
        class C11651 implements OnClickListener {
            C11651() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    ChangeChatNameActivity.this.imageUpdater.openCamera();
                } else if (i == 1) {
                    ChangeChatNameActivity.this.imageUpdater.openGallery();
                } else if (i == 2) {
                    ChangeChatNameActivity.this.avatar = null;
                    ChangeChatNameActivity.this.uploadedAvatar = null;
                    ChangeChatNameActivity.this.avatarImage.setImage(ChangeChatNameActivity.this.avatar, "50_50", ChangeChatNameActivity.this.avatarDrawable);
                }
            }
        }

        C11663() {
        }

        public void onClick(View view) {
            if (ChangeChatNameActivity.this.getParentActivity() != null) {
                Builder builder = new Builder(ChangeChatNameActivity.this.getParentActivity());
                builder.setItems(ChangeChatNameActivity.this.avatar != null ? new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)} : new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)}, new C11651());
                ChangeChatNameActivity.this.showDialog(builder.create());
            }
        }
    }

    /* renamed from: org.telegram.ui.ChangeChatNameActivity$4 */
    class C11674 implements TextWatcher {
        C11674() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            String obj;
            AvatarDrawable access$1200 = ChangeChatNameActivity.this.avatarDrawable;
            if (ChangeChatNameActivity.this.nameTextView.length() > 0) {
                obj = ChangeChatNameActivity.this.nameTextView.getText().toString();
            } else {
                obj = null;
            }
            access$1200.setInfo(5, obj, null, false);
            ChangeChatNameActivity.this.avatarImage.invalidate();
        }
    }

    /* renamed from: org.telegram.ui.ChangeChatNameActivity$5 */
    class C11695 implements View.OnClickListener {

        /* renamed from: org.telegram.ui.ChangeChatNameActivity$5$1 */
        class C11681 implements OnClickListener {
            C11681() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                NotificationCenter.getInstance(ChangeChatNameActivity.this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
                if (AndroidUtilities.isTablet()) {
                    NotificationCenter.getInstance(ChangeChatNameActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, Long.valueOf(-((long) ChangeChatNameActivity.this.chatId)));
                } else {
                    NotificationCenter.getInstance(ChangeChatNameActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                }
                MessagesController.getInstance(ChangeChatNameActivity.this.currentAccount).deleteUserFromChat(ChangeChatNameActivity.this.chatId, MessagesController.getInstance(ChangeChatNameActivity.this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(ChangeChatNameActivity.this.currentAccount).getClientUserId())), null, true);
                ChangeChatNameActivity.this.finishFragment();
            }
        }

        C11695() {
        }

        public void onClick(View v) {
            Builder builder = new Builder(ChangeChatNameActivity.this.getParentActivity());
            builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new C11681());
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            ChangeChatNameActivity.this.showDialog(builder.create());
        }
    }

    /* renamed from: org.telegram.ui.ChangeChatNameActivity$7 */
    class C11717 implements Runnable {
        C11717() {
        }

        public void run() {
            if (ChangeChatNameActivity.this.nameTextView != null) {
                ChangeChatNameActivity.this.nameTextView.requestFocus();
                AndroidUtilities.showKeyboard(ChangeChatNameActivity.this.nameTextView);
            }
        }
    }

    public ChangeChatNameActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        this.avatarDrawable = new AvatarDrawable();
        this.chatId = getArguments().getInt("chat_id", 0);
        this.imageUpdater = new ImageUpdater();
        this.imageUpdater.parentFragment = this;
        this.imageUpdater.delegate = this;
        return true;
    }

    public View createView(Context context) {
        float f;
        float f2;
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("ChannelEdit", R.string.ChannelEdit));
        this.actionBar.setActionBarMenuOnItemClick(new C11631());
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
        LinearLayout linearLayout = new LinearLayout(context);
        this.fragmentView = linearLayout;
        this.fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        this.fragmentView.setLayoutParams(new LayoutParams(-1, -1));
        ((LinearLayout) this.fragmentView).setOrientation(1);
        this.fragmentView.setOnTouchListener(new C11642());
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(1);
        linearLayout2.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(linearLayout2, LayoutHelper.createLinear(-1, -2));
        FrameLayout frameLayout = new FrameLayout(context);
        linearLayout2.addView(frameLayout, LayoutHelper.createLinear(-1, -2));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(32.0f));
        this.avatarDrawable.setInfo(5, null, null, false);
        this.avatarDrawable.setDrawPhoto(true);
        View view = this.avatarImage;
        int i = (LocaleController.isRTL ? 5 : 3) | 48;
        if (LocaleController.isRTL) {
            f = 0.0f;
        } else {
            f = 16.0f;
        }
        if (LocaleController.isRTL) {
            f2 = 16.0f;
        } else {
            f2 = 0.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(64, 64.0f, i, f, 12.0f, f2, 12.0f));
        this.avatarImage.setOnClickListener(new C11663());
        this.nameTextView = new EditTextBoldCursor(context);
        if (this.currentChat.megagroup) {
            this.nameTextView.setHint(LocaleController.getString("GroupName", R.string.GroupName));
        } else {
            this.nameTextView.setHint(LocaleController.getString("EnterChannelName", R.string.EnterChannelName));
        }
        this.nameTextView.setMaxLines(4);
        this.nameTextView.setText(this.currentChat.title);
        this.nameTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        this.nameTextView.setTextSize(1, 16.0f);
        this.nameTextView.setHint(LocaleController.getString("GroupName", R.string.GroupName));
        this.nameTextView.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        this.nameTextView.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        this.nameTextView.setImeOptions(268435456);
        this.nameTextView.setInputType(16385);
        this.nameTextView.setPadding(0, 0, 0, AndroidUtilities.dp(8.0f));
        this.nameTextView.setFocusable(this.nameTextView.isEnabled());
        this.nameTextView.setFilters(new InputFilter[]{new LengthFilter(100)});
        this.nameTextView.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        this.nameTextView.setCursorSize(AndroidUtilities.dp(20.0f));
        this.nameTextView.setCursorWidth(1.5f);
        this.nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        view = this.nameTextView;
        f = LocaleController.isRTL ? 16.0f : 96.0f;
        if (LocaleController.isRTL) {
            f2 = 96.0f;
        } else {
            f2 = 16.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(-1, -2.0f, 16, f, 0.0f, f2, 0.0f));
        this.nameTextView.addTextChangedListener(new C11674());
        View shadowSectionCell = new ShadowSectionCell(context);
        shadowSectionCell.setSize(20);
        linearLayout.addView(shadowSectionCell, LayoutHelper.createLinear(-1, -2));
        if (this.currentChat.creator) {
            FrameLayout container3 = new FrameLayout(context);
            container3.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout.addView(container3, LayoutHelper.createLinear(-1, -2));
            shadowSectionCell = new TextSettingsCell(context);
            shadowSectionCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText5));
            shadowSectionCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            shadowSectionCell.setText(LocaleController.getString("DeleteMega", R.string.DeleteMega), false);
            container3.addView(shadowSectionCell, LayoutHelper.createFrame(-1, -2.0f));
            shadowSectionCell.setOnClickListener(new C11695());
            TextInfoPrivacyCell infoCell2 = new TextInfoPrivacyCell(context);
            infoCell2.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            infoCell2.setText(LocaleController.getString("MegaDeleteInfo", R.string.MegaDeleteInfo));
            linearLayout.addView(infoCell2, LayoutHelper.createLinear(-1, -2));
        } else {
            shadowSectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        }
        this.nameTextView.setSelection(this.nameTextView.length());
        if (this.currentChat.photo != null) {
            this.avatar = this.currentChat.photo.photo_small;
            this.avatarImage.setImage(this.avatar, "50_50", this.avatarDrawable);
        } else {
            this.avatarImage.setImageDrawable(this.avatarDrawable);
        }
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (!MessagesController.getGlobalMainSettings().getBoolean("view_animations", true)) {
            this.nameTextView.requestFocus();
            AndroidUtilities.showKeyboard(this.nameTextView);
        }
    }

    public void didUploadedPhoto(final TLRPC$InputFile file, final TLRPC$PhotoSize small, TLRPC$PhotoSize big, TLRPC$TL_secureFile secureFile) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                ChangeChatNameActivity.this.uploadedAvatar = file;
                ChangeChatNameActivity.this.avatar = small.location;
                ChangeChatNameActivity.this.avatarImage.setImage(ChangeChatNameActivity.this.avatar, "50_50", ChangeChatNameActivity.this.avatarDrawable);
                if (ChangeChatNameActivity.this.createAfterUpload) {
                    ChangeChatNameActivity.this.donePressed = false;
                    try {
                        if (ChangeChatNameActivity.this.progressDialog != null && ChangeChatNameActivity.this.progressDialog.isShowing()) {
                            ChangeChatNameActivity.this.progressDialog.dismiss();
                            ChangeChatNameActivity.this.progressDialog = null;
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    ChangeChatNameActivity.this.doneButton.performClick();
                }
            }
        });
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        this.imageUpdater.onActivityResult(requestCode, resultCode, data);
    }

    public void saveSelfArgs(Bundle args) {
        if (!(this.imageUpdater == null || this.imageUpdater.currentPicturePath == null)) {
            args.putString("path", this.imageUpdater.currentPicturePath);
        }
        if (this.nameTextView != null) {
            String text = this.nameTextView.getText().toString();
            if (text != null && text.length() != 0) {
                args.putString("nameTextView", text);
            }
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.imageUpdater != null) {
            this.imageUpdater.currentPicturePath = args.getString("path");
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            AndroidUtilities.runOnUIThread(new C11717(), 100);
        }
    }

    private void saveName() {
        MessagesController.getInstance(this.currentAccount).changeChatTitle(this.chatId, this.nameTextView.getText().toString());
    }
}
