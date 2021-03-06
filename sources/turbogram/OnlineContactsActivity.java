package turbogram;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.baranak.turbogramf.R;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter$NotificationCenterDelegate;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$TL_contact;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.SearchAdapter;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SectionsAdapter;
import org.telegram.ui.ContactsActivity.ContactsActivityDelegate;
import turbogram.Utilities.TurboConfig;
import turbogram.Utilities.TurboUtils;

public class OnlineContactsActivity extends BaseFragment implements NotificationCenter$NotificationCenterDelegate {
    private boolean addingToChannel;
    private boolean allowBots = true;
    private boolean allowUsernameSearch = true;
    private int chat_id;
    private boolean checkPermission = true;
    private boolean createSecretChat;
    private boolean creatingChat;
    private ContactsActivityDelegate delegate;
    private boolean destroyAfterSelect;
    private EmptyTextProgressView emptyView;
    private HashMap<Integer, User> ignoreUsers;
    private RecyclerListView listView;
    private OnlineContactsAdapter listViewAdapter;
    private boolean needForwardCount = true;
    private boolean needPhonebook;
    private boolean onlyUsers;
    private AlertDialog permissionDialog;
    private boolean returnAsResult;
    private SearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;
    private String selectAlertString = null;

    /* renamed from: turbogram.OnlineContactsActivity$1 */
    class C23871 extends ActionBarMenuOnItemClick {
        C23871() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                OnlineContactsActivity.this.finishFragment();
            } else if (id == 1) {
                ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).cleanup();
                ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).readContacts();
            }
        }
    }

    /* renamed from: turbogram.OnlineContactsActivity$2 */
    class C23882 implements OnItemClickListener {
        C23882() {
        }

        public void onItemClick(View view, int position) {
            boolean z = false;
            User user;
            Bundle args;
            if (OnlineContactsActivity.this.searching && OnlineContactsActivity.this.searchWas) {
                user = (User) OnlineContactsActivity.this.searchListViewAdapter.getItem(position);
                if (user != null) {
                    if (OnlineContactsActivity.this.searchListViewAdapter.isGlobalSearch(position)) {
                        ArrayList<User> users = new ArrayList();
                        users.add(user);
                        MessagesController.getInstance(OnlineContactsActivity.this.currentAccount).putUsers(users, false);
                        MessagesStorage.getInstance(OnlineContactsActivity.this.currentAccount).putUsersAndChats(users, null, false, true);
                    }
                    if (OnlineContactsActivity.this.returnAsResult) {
                        if (OnlineContactsActivity.this.ignoreUsers == null || !OnlineContactsActivity.this.ignoreUsers.containsKey(Integer.valueOf(user.id))) {
                            OnlineContactsActivity.this.didSelectResult(user, true, null);
                            return;
                        }
                        return;
                    } else if (!OnlineContactsActivity.this.createSecretChat) {
                        args = new Bundle();
                        args.putInt("user_id", user.id);
                        if (MessagesController.getInstance(OnlineContactsActivity.this.currentAccount).checkCanOpenChat(args, OnlineContactsActivity.this) && !TurboConfig.containValue("hide_" + user.id)) {
                            OnlineContactsActivity.this.presentFragment(new ChatActivity(args), true);
                            return;
                        }
                        return;
                    } else if (user.id != UserConfig.getInstance(OnlineContactsActivity.this.currentAccount).getClientUserId()) {
                        OnlineContactsActivity.this.creatingChat = true;
                        SecretChatHelper.getInstance(OnlineContactsActivity.this.currentAccount).startSecretChat(OnlineContactsActivity.this.getParentActivity(), user);
                        return;
                    } else {
                        return;
                    }
                }
                return;
            }
            int section = OnlineContactsActivity.this.listViewAdapter.getSectionForPosition(position);
            int row = OnlineContactsActivity.this.listViewAdapter.getPositionInSectionForPosition(position);
            if (row >= 0 && section >= 0) {
                Object item = OnlineContactsActivity.this.listViewAdapter.getItem(section, row);
                if (item instanceof User) {
                    user = (User) item;
                    if (OnlineContactsActivity.this.returnAsResult) {
                        if (OnlineContactsActivity.this.ignoreUsers == null || !OnlineContactsActivity.this.ignoreUsers.containsKey(Integer.valueOf(user.id))) {
                            OnlineContactsActivity.this.didSelectResult(user, true, null);
                        }
                    } else if (OnlineContactsActivity.this.createSecretChat) {
                        OnlineContactsActivity.this.creatingChat = true;
                        SecretChatHelper.getInstance(OnlineContactsActivity.this.currentAccount).startSecretChat(OnlineContactsActivity.this.getParentActivity(), user);
                    } else {
                        args = new Bundle();
                        args.putInt("user_id", user.id);
                        if (MessagesController.getInstance(OnlineContactsActivity.this.currentAccount).checkCanOpenChat(args, OnlineContactsActivity.this) && !TurboConfig.containValue("hide_" + user.id)) {
                            OnlineContactsActivity onlineContactsActivity = OnlineContactsActivity.this;
                            BaseFragment chatActivity = new ChatActivity(args);
                            if (!TurboConfig.keepContactsOpen) {
                                z = true;
                            }
                            onlineContactsActivity.presentFragment(chatActivity, z);
                        }
                    }
                }
            }
        }
    }

    /* renamed from: turbogram.OnlineContactsActivity$3 */
    class C23893 extends OnScrollListener {
        C23893() {
        }

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == 1 && OnlineContactsActivity.this.searching && OnlineContactsActivity.this.searchWas) {
                AndroidUtilities.hideKeyboard(OnlineContactsActivity.this.getParentActivity().getCurrentFocus());
            }
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    }

    /* renamed from: turbogram.OnlineContactsActivity$6 */
    class C23926 implements ThemeDescriptionDelegate {
        C23926() {
        }

        public void didSetColor() {
            int count = OnlineContactsActivity.this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = OnlineContactsActivity.this.listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(0);
                } else if (child instanceof ProfileSearchCell) {
                    ((ProfileSearchCell) child).update(0);
                }
            }
        }
    }

    class OnlineContactsAdapter extends SectionsAdapter {
        private HashMap<Integer, ?> checkedMap;
        private HashMap<Integer, User> ignoreUsers;
        private boolean isAdmin;
        private Context mContext;
        private boolean needPhonebook;
        private int onlyUsers;
        private boolean scrolling;

        public OnlineContactsAdapter(Context context, int onlyUsersType, boolean arg2, HashMap<Integer, User> arg3, boolean arg4) {
            this.mContext = context;
            this.onlyUsers = onlyUsersType;
            this.needPhonebook = arg2;
            this.ignoreUsers = arg3;
            this.isAdmin = arg4;
        }

        public Object getItem(int section, int position) {
            ArrayList<String> sortedUsersSectionsArray;
            HashMap<String, ArrayList<TLRPC$TL_contact>> usersSectionsDict = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersMutualSectionsDict : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersSectionsDict;
            if (this.onlyUsers == 2) {
                sortedUsersSectionsArray = ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray;
            } else {
                sortedUsersSectionsArray = ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
            }
            ArrayList<TLRPC$TL_contact> arr;
            if (this.onlyUsers == 0 || this.isAdmin) {
                if (section == 0) {
                    return null;
                }
                if (section - 1 < sortedUsersSectionsArray.size()) {
                    arr = (ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                    if (position < arr.size()) {
                        return MessagesController.getInstance(OnlineContactsActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC$TL_contact) arr.get(position)).user_id));
                    }
                    return null;
                } else if (this.needPhonebook) {
                    return ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).phoneBookContacts.get(position);
                } else {
                    return null;
                }
            } else if (section >= sortedUsersSectionsArray.size()) {
                return null;
            } else {
                arr = (ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section));
                if (position < arr.size()) {
                    return MessagesController.getInstance(OnlineContactsActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC$TL_contact) arr.get(position)).user_id));
                }
                return null;
            }
        }

        public boolean isEnabled(int section, int row) {
            HashMap<String, ArrayList<TLRPC$TL_contact>> usersSectionsDict = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersMutualSectionsDict : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersSectionsDict;
            ArrayList<String> sortedUsersSectionsArray = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
            if (this.onlyUsers == 0 || this.isAdmin) {
                if (section == 0) {
                    if (this.needPhonebook || this.isAdmin) {
                        if (row == 1) {
                            return false;
                        }
                        return true;
                    } else if (row == 3) {
                        return false;
                    } else {
                        return true;
                    }
                } else if (section - 1 >= sortedUsersSectionsArray.size() || row < ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1))).size()) {
                    return true;
                } else {
                    return false;
                }
            } else if (row < ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section))).size()) {
                return true;
            } else {
                return false;
            }
        }

        public int getSectionCount() {
            int count = (this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray).size();
            if (this.onlyUsers == 0) {
                count++;
            }
            if (this.isAdmin) {
                count++;
            }
            if (this.needPhonebook) {
                return count + 1;
            }
            return count;
        }

        public int getCountForSection(int section) {
            ArrayList<String> sortedUsersSectionsArray;
            HashMap<String, ArrayList<TLRPC$TL_contact>> usersSectionsDict = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersMutualSectionsDict : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersSectionsDict;
            if (this.onlyUsers == 2) {
                sortedUsersSectionsArray = ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray;
            } else {
                sortedUsersSectionsArray = ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
            }
            int count;
            if (this.onlyUsers == 0 || this.isAdmin) {
                if (section == 0) {
                    if (this.needPhonebook || this.isAdmin) {
                        return 2;
                    }
                    return 4;
                } else if (section - 1 < sortedUsersSectionsArray.size()) {
                    count = ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1))).size();
                    if (section - 1 != sortedUsersSectionsArray.size() - 1 || this.needPhonebook) {
                        return count + 1;
                    }
                    return count;
                }
            } else if (section < sortedUsersSectionsArray.size()) {
                count = ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section))).size();
                if (section != sortedUsersSectionsArray.size() - 1 || this.needPhonebook) {
                    return count + 1;
                }
                return count;
            }
            if (this.needPhonebook) {
                return ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).phoneBookContacts.size();
            }
            return 0;
        }

        public View getSectionHeaderView(int section, View view) {
            if (this.onlyUsers == 2) {
                HashMap<String, ArrayList<TLRPC$TL_contact>> usersSectionsDict = ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersMutualSectionsDict;
            } else {
                HashMap hashMap = ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersSectionsDict;
            }
            ArrayList<String> sortedUsersSectionsArray = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
            if (view == null) {
                view = new LetterSectionCell(this.mContext);
            }
            LetterSectionCell cell = (LetterSectionCell) view;
            if (this.onlyUsers == 0 || this.isAdmin) {
                if (section == 0) {
                    cell.setLetter("");
                } else if (section - 1 < sortedUsersSectionsArray.size()) {
                    cell.setLetter((String) sortedUsersSectionsArray.get(section - 1));
                } else {
                    cell.setLetter("");
                }
            } else if (section < sortedUsersSectionsArray.size()) {
                cell.setLetter((String) sortedUsersSectionsArray.get(section));
            } else {
                cell.setLetter("");
            }
            return view;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            float f = 72.0f;
            switch (viewType) {
                case 0:
                    view = new UserCell(this.mContext, 58, 1, false);
                    break;
                case 1:
                    view = new TextCell(this.mContext);
                    break;
                case 2:
                    view = new GraySectionCell(this.mContext);
                    ((GraySectionCell) view).setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
                    break;
                default:
                    float f2;
                    view = new DividerCell(this.mContext);
                    if (LocaleController.isRTL) {
                        f2 = 28.0f;
                    } else {
                        f2 = 72.0f;
                    }
                    int dp = AndroidUtilities.dp(f2);
                    if (!LocaleController.isRTL) {
                        f = 28.0f;
                    }
                    view.setPadding(dp, 0, AndroidUtilities.dp(f), 0);
                    break;
            }
            return new Holder(view);
        }

        public void onBindViewHolder(int section, int position, ViewHolder holder) {
            switch (holder.getItemViewType()) {
                case 0:
                    UserCell userCell = holder.itemView;
                    HashMap<String, ArrayList<TLRPC$TL_contact>> usersSectionsDict = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersMutualSectionsDict : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersSectionsDict;
                    ArrayList<String> sortedUsersSectionsArray = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
                    int i = (this.onlyUsers == 0 || this.isAdmin) ? 1 : 0;
                    User user = MessagesController.getInstance(OnlineContactsActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC$TL_contact) ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section - i))).get(position)).user_id));
                    userCell.setData(user, null, null, 0);
                    if (this.checkedMap != null) {
                        userCell.setChecked(this.checkedMap.containsKey(Integer.valueOf(user.id)), !this.scrolling);
                    }
                    if (this.ignoreUsers == null) {
                        return;
                    }
                    if (this.ignoreUsers.containsKey(Integer.valueOf(user.id))) {
                        userCell.setAlpha(0.5f);
                        return;
                    } else {
                        userCell.setAlpha(1.0f);
                        return;
                    }
                case 1:
                    TextCell textCell = holder.itemView;
                    if (section != 0) {
                        Contact contact = (Contact) ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).phoneBookContacts.get(position);
                        if (contact.first_name != null && contact.last_name != null) {
                            textCell.setText(contact.first_name + " " + contact.last_name);
                            return;
                        } else if (contact.first_name == null || contact.last_name != null) {
                            textCell.setText(contact.last_name);
                            return;
                        } else {
                            textCell.setText(contact.first_name);
                            return;
                        }
                    } else if (this.needPhonebook) {
                        textCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
                        return;
                    } else if (this.isAdmin) {
                        textCell.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink), R.drawable.menu_invite);
                        return;
                    } else if (position == 0) {
                        textCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
                        return;
                    } else if (position == 1) {
                        textCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
                        return;
                    } else if (position == 2) {
                        textCell.setTextAndIcon(LocaleController.getString("NewChannel", R.string.NewChannel), R.drawable.menu_broadcast);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }

        public int getItemViewType(int section, int position) {
            HashMap<String, ArrayList<TLRPC$TL_contact>> usersSectionsDict = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersMutualSectionsDict : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineUsersSectionsDict;
            ArrayList<String> sortedUsersSectionsArray = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
            if (this.onlyUsers == 0 || this.isAdmin) {
                if (section == 0) {
                    if (((this.needPhonebook || this.isAdmin) && position == 1) || position == 3) {
                        return 2;
                    }
                } else if (section - 1 < sortedUsersSectionsArray.size()) {
                    if (position >= ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1))).size()) {
                        return 3;
                    }
                    return 0;
                }
                return 1;
            } else if (position < ((ArrayList) usersSectionsDict.get(sortedUsersSectionsArray.get(section))).size()) {
                return 0;
            } else {
                return 3;
            }
        }

        public String getLetter(int position) {
            ArrayList<String> sortedUsersSectionsArray = this.onlyUsers == 2 ? ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersMutualSectionsArray : ContactsController.getInstance(OnlineContactsActivity.this.currentAccount).onlineSortedUsersSectionsArray;
            int section = getSectionForPosition(position);
            if (section == -1) {
                section = sortedUsersSectionsArray.size() - 1;
            }
            if (section <= 0 || section > sortedUsersSectionsArray.size()) {
                return null;
            }
            return (String) sortedUsersSectionsArray.get(section - 1);
        }

        public int getPositionForScrollProgress(float progress) {
            return (int) (((float) getItemCount()) * progress);
        }
    }

    public OnlineContactsActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
        if (this.arguments != null) {
            this.onlyUsers = getArguments().getBoolean("onlyUsers", false);
            this.destroyAfterSelect = this.arguments.getBoolean("destroyAfterSelect", false);
            this.returnAsResult = this.arguments.getBoolean("returnAsResult", false);
            this.createSecretChat = this.arguments.getBoolean("createSecretChat", false);
            this.selectAlertString = this.arguments.getString("selectAlertString");
            this.allowUsernameSearch = this.arguments.getBoolean("allowUsernameSearch", true);
            this.needForwardCount = this.arguments.getBoolean("needForwardCount", true);
            this.allowBots = this.arguments.getBoolean("allowBots", true);
            this.addingToChannel = this.arguments.getBoolean("addingToChannel", false);
            this.chat_id = this.arguments.getInt("chat_id", 0);
        } else {
            this.needPhonebook = true;
        }
        ContactsController.getInstance(this.currentAccount).checkInviteText();
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
        this.delegate = null;
    }

    public View createView(Context context) {
        this.searching = false;
        this.searchWas = false;
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("OnlineContacts", R.string.OnlineContacts));
        this.actionBar.setActionBarMenuOnItemClick(new C23871());
        this.actionBar.createMenu().addItem(1, (int) R.drawable.turbo_ab_refresh);
        this.listViewAdapter = new OnlineContactsAdapter(context, 1, false, null, false);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        this.emptyView = new EmptyTextProgressView(context);
        this.emptyView.setShowAtCenter(true);
        this.emptyView.showTextView();
        frameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView = new RecyclerListView(context);
        this.listView.setEmptyView(this.emptyView);
        this.listView.setSectionsType(1);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setFastScrollEnabled();
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        this.listView.setAdapter(this.listViewAdapter);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setOnItemClickListener(new C23882());
        this.listView.setOnScrollListener(new C23893());
        return this.fragmentView;
    }

    private void didSelectResult(final User user, boolean useAlert, String param) {
        if (!useAlert || this.selectAlertString == null) {
            if (this.delegate != null) {
                this.delegate.didSelectContact(user, param, null);
                this.delegate = null;
            }
            finishFragment();
        } else if (getParentActivity() != null) {
            if (user.bot && user.bot_nochats && !this.addingToChannel) {
                try {
                    TurboUtils.showToast(getParentActivity(), LocaleController.getString("BotCantJoinGroups", R.string.BotCantJoinGroups), 0);
                    return;
                } catch (Exception e) {
                    FileLog.e(e);
                    return;
                }
            }
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            String message = LocaleController.formatStringSimple(this.selectAlertString, new Object[]{UserObject.getUserName(user)});
            EditText editText = null;
            if (!user.bot && this.needForwardCount) {
                message = String.format("%s\n\n%s", new Object[]{message, LocaleController.getString("AddToTheGroupForwardCount", R.string.AddToTheGroupForwardCount)});
                editText = new EditText(getParentActivity());
                editText.setTextSize(18.0f);
                editText.setText("50");
                editText.setGravity(17);
                editText.setInputType(2);
                editText.setImeOptions(6);
                editText.setBackgroundDrawable(Theme.createEditTextDrawable(getParentActivity(), true));
                final EditText editTextFinal = editText;
                editText.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    public void afterTextChanged(Editable s) {
                        try {
                            String str = s.toString();
                            if (str.length() != 0) {
                                int value = Utilities.parseInt(str).intValue();
                                if (value < 0) {
                                    editTextFinal.setText("0");
                                    editTextFinal.setSelection(editTextFinal.length());
                                } else if (value > 300) {
                                    editTextFinal.setText("300");
                                    editTextFinal.setSelection(editTextFinal.length());
                                } else if (!str.equals("" + value)) {
                                    editTextFinal.setText("" + value);
                                    editTextFinal.setSelection(editTextFinal.length());
                                }
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                });
                builder.setView(editText);
            }
            builder.setMessage(message);
            final EditText finalEditText = editText;
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    OnlineContactsActivity.this.didSelectResult(user, false, finalEditText != null ? finalEditText.getText().toString() : "0");
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
            if (editText != null) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) editText.getLayoutParams();
                if (layoutParams != null) {
                    if (layoutParams instanceof LayoutParams) {
                        ((LayoutParams) layoutParams).gravity = 1;
                    }
                    int dp = AndroidUtilities.dp(10.0f);
                    layoutParams.leftMargin = dp;
                    layoutParams.rightMargin = dp;
                    editText.setLayoutParams(layoutParams);
                }
                editText.setSelection(editText.getText().length());
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
        if (this.checkPermission && VERSION.SDK_INT >= 23) {
            Context activity = getParentActivity();
            if (activity != null) {
                this.checkPermission = false;
                if (activity.checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
                    return;
                }
                if (activity.shouldShowRequestPermissionRationale("android.permission.READ_CONTACTS")) {
                    Builder builder = new Builder(activity);
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                    Dialog create = builder.create();
                    this.permissionDialog = create;
                    showDialog(create);
                    return;
                }
                askForPermissons();
            }
        }
    }

    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (this.permissionDialog != null && dialog == this.permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @TargetApi(23)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity != null) {
            ArrayList<String> permissons = new ArrayList();
            if (activity.checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
                permissons.add("android.permission.READ_CONTACTS");
                permissons.add("android.permission.WRITE_CONTACTS");
                permissons.add("android.permission.GET_ACCOUNTS");
            }
            activity.requestPermissions((String[]) permissons.toArray(new String[permissons.size()]), 1);
        }
    }

    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            int a = 0;
            while (a < permissions.length) {
                if (grantResults.length > a && grantResults[a] == 0) {
                    String str = permissions[a];
                    Object obj = -1;
                    switch (str.hashCode()) {
                        case 1977429404:
                            if (str.equals("android.permission.READ_CONTACTS")) {
                                obj = null;
                                break;
                            }
                            break;
                    }
                    switch (obj) {
                        case null:
                            ContactsController.getInstance(this.currentAccount).readContacts();
                            break;
                        default:
                            break;
                    }
                }
                a++;
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.actionBar != null) {
            this.actionBar.closeSearchField();
        }
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.contactsDidLoaded) {
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if ((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (this.createSecretChat && this.creatingChat) {
                TLRPC$EncryptedChat encryptedChat = args[0];
                Bundle args2 = new Bundle();
                args2.putInt("enc_id", encryptedChat.id);
                NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                presentFragment(new ChatActivity(args2), true);
            }
        } else if (id == NotificationCenter.closeChats && !this.creatingChat) {
            removeSelfFromStack();
        }
    }

    private void updateVisibleRows(int mask) {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(mask);
                }
            }
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescriptionDelegate сellDelegate = new C23926();
        ThemeDescription[] themeDescriptionArr = new ThemeDescription[36];
        themeDescriptionArr[0] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite);
        themeDescriptionArr[1] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault);
        themeDescriptionArr[2] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault);
        themeDescriptionArr[3] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon);
        themeDescriptionArr[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle);
        themeDescriptionArr[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector);
        themeDescriptionArr[6] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch);
        themeDescriptionArr[7] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder);
        themeDescriptionArr[8] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector);
        themeDescriptionArr[9] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SECTIONS, new Class[]{LetterSectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4);
        themeDescriptionArr[10] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        themeDescriptionArr[11] = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder);
        themeDescriptionArr[12] = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, Theme.key_fastScrollActive);
        themeDescriptionArr[13] = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, Theme.key_fastScrollInactive);
        themeDescriptionArr[14] = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, Theme.key_fastScrollText);
        themeDescriptionArr[15] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[16] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, сellDelegate, Theme.key_windowBackgroundWhiteGrayText);
        themeDescriptionArr[17] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, сellDelegate, Theme.key_windowBackgroundWhiteBlueText);
        themeDescriptionArr[18] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable}, null, Theme.key_avatar_text);
        themeDescriptionArr[19] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundRed);
        themeDescriptionArr[20] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundOrange);
        themeDescriptionArr[21] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundViolet);
        themeDescriptionArr[22] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundGreen);
        themeDescriptionArr[23] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundCyan);
        themeDescriptionArr[24] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue);
        themeDescriptionArr[25] = new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundPink);
        themeDescriptionArr[26] = new ThemeDescription(this.listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[27] = new ThemeDescription(this.listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon);
        themeDescriptionArr[28] = new ThemeDescription(this.listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2);
        themeDescriptionArr[29] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection);
        themeDescriptionArr[30] = new ThemeDescription(this.listView, 0, new Class[]{ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon);
        themeDescriptionArr[31] = new ThemeDescription(this.listView, 0, new Class[]{ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck);
        themeDescriptionArr[32] = new ThemeDescription(this.listView, 0, new Class[]{ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground);
        themeDescriptionArr[33] = new ThemeDescription(this.listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3);
        themeDescriptionArr[34] = new ThemeDescription(this.listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3);
        themeDescriptionArr[35] = new ThemeDescription(this.listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name);
        return themeDescriptionArr;
    }
}
