package org.telegram.ui.Components.voip;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.Settings.System;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baranak.turbogramf.R;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import java.io.File;
import java.util.Collections;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_inputPhoneCall;
import org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC$TL_phone_setCallRating;
import org.telegram.tgnet.TLRPC$TL_updates;
import org.telegram.tgnet.TLRPC$TL_userFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.BetterRatingView.OnRatingChangeListener;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.VoIPActivity;

public class VoIPHelper {
    private static final int VOIP_SUPPORT_ID = 4244000;
    public static long lastCallTime = 0;

    public static void startCall(User user, final Activity activity, TLRPC$TL_userFull userFull) {
        boolean isAirplaneMode = true;
        if (userFull != null && userFull.phone_calls_private) {
            new Builder((Context) activity).setTitle(LocaleController.getString("VoipFailed", R.string.VoipFailed)).setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("CallNotAvailable", R.string.CallNotAvailable, new Object[]{ContactsController.formatName(user.first_name, user.last_name)}))).setPositiveButton(LocaleController.getString("OK", R.string.OK), null).show();
        } else if (ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState() != 3) {
            CharSequence string;
            if (System.getInt(activity.getContentResolver(), "airplane_mode_on", 0) == 0) {
                isAirplaneMode = false;
            }
            Builder title = new Builder((Context) activity).setTitle(isAirplaneMode ? LocaleController.getString("VoipOfflineAirplaneTitle", R.string.VoipOfflineAirplaneTitle) : LocaleController.getString("VoipOfflineTitle", R.string.VoipOfflineTitle));
            if (isAirplaneMode) {
                string = LocaleController.getString("VoipOfflineAirplane", R.string.VoipOfflineAirplane);
            } else {
                string = LocaleController.getString("VoipOffline", R.string.VoipOffline);
            }
            Builder bldr = title.setMessage(string).setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            if (isAirplaneMode) {
                final Intent settingsIntent = new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
                if (settingsIntent.resolveActivity(activity.getPackageManager()) != null) {
                    bldr.setNeutralButton(LocaleController.getString("VoipOfflineOpenSettings", R.string.VoipOfflineOpenSettings), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            activity.startActivity(settingsIntent);
                        }
                    });
                }
            }
            bldr.show();
        } else if (VERSION.SDK_INT < 23 || activity.checkSelfPermission("android.permission.RECORD_AUDIO") == 0) {
            initiateCall(user, activity);
        } else {
            activity.requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 101);
        }
    }

    private static void initiateCall(final User user, final Activity activity) {
        if (activity != null && user != null) {
            if (VoIPService.getSharedInstance() != null) {
                if (VoIPService.getSharedInstance().getUser().id != user.id) {
                    new Builder((Context) activity).setTitle(LocaleController.getString("VoipOngoingAlertTitle", R.string.VoipOngoingAlertTitle)).setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("VoipOngoingAlert", R.string.VoipOngoingAlert, new Object[]{ContactsController.formatName(callUser.first_name, callUser.last_name), ContactsController.formatName(user.first_name, user.last_name)}))).setPositiveButton(LocaleController.getString("OK", R.string.OK), new OnClickListener() {

                        /* renamed from: org.telegram.ui.Components.voip.VoIPHelper$2$1 */
                        class C16891 implements Runnable {
                            C16891() {
                            }

                            public void run() {
                                VoIPHelper.doInitiateCall(user, activity);
                            }
                        }

                        public void onClick(DialogInterface dialog, int which) {
                            if (VoIPService.getSharedInstance() != null) {
                                VoIPService.getSharedInstance().hangUp(new C16891());
                            } else {
                                VoIPHelper.doInitiateCall(user, activity);
                            }
                        }
                    }).setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null).show();
                    return;
                }
                activity.startActivity(new Intent(activity, VoIPActivity.class).addFlags(268435456));
            } else if (VoIPService.callIShouldHavePutIntoIntent == null) {
                doInitiateCall(user, activity);
            }
        }
    }

    private static void doInitiateCall(User user, Activity activity) {
        if (activity != null && user != null && System.currentTimeMillis() - lastCallTime >= AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS) {
            lastCallTime = System.currentTimeMillis();
            Intent intent = new Intent(activity, VoIPService.class);
            intent.putExtra("user_id", user.id);
            intent.putExtra("is_outgoing", true);
            intent.putExtra("start_incall_activity", true);
            intent.putExtra("account", UserConfig.selectedAccount);
            try {
                activity.startService(intent);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    @TargetApi(23)
    public static void permissionDenied(final Activity activity, final Runnable onFinish) {
        if (!activity.shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO")) {
            new Builder((Context) activity).setTitle(LocaleController.getString("AppName", R.string.AppName)).setMessage(LocaleController.getString("VoipNeedMicPermission", R.string.VoipNeedMicPermission)).setPositiveButton(LocaleController.getString("OK", R.string.OK), null).setNegativeButton(LocaleController.getString("Settings", R.string.Settings), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                    activity.startActivity(intent);
                }
            }).show().setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (onFinish != null) {
                        onFinish.run();
                    }
                }
            });
        }
    }

    public static File getLogsDir() {
        File logsDir = new File(ApplicationLoader.applicationContext.getCacheDir(), "voip_logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        return logsDir;
    }

    public static boolean canRateCall(TLRPC$TL_messageActionPhoneCall call) {
        if ((call.reason instanceof TLRPC$TL_phoneCallDiscardReasonBusy) || (call.reason instanceof TLRPC$TL_phoneCallDiscardReasonMissed)) {
            return false;
        }
        for (String hash : MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getStringSet("calls_access_hashes", Collections.EMPTY_SET)) {
            String[] d = hash.split(" ");
            if (d.length >= 2 && d[0].equals(call.call_id + "")) {
                return true;
            }
        }
        return false;
    }

    public static void showRateAlert(Context context, TLRPC$TL_messageActionPhoneCall call) {
        for (String hash : MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getStringSet("calls_access_hashes", Collections.EMPTY_SET)) {
            String[] d = hash.split(" ");
            if (d.length >= 2 && d[0].equals(call.call_id + "")) {
                try {
                    Context context2 = context;
                    showRateAlert(context2, null, call.call_id, Long.parseLong(d[1]), UserConfig.selectedAccount);
                    return;
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    public static void showRateAlert(Context context, Runnable onDismiss, long callID, long accessHash, int account) {
        final File log = getLogFile(callID);
        View linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        int pad = AndroidUtilities.dp(16.0f);
        linearLayout.setPadding(pad, pad, pad, 0);
        linearLayout = new TextView(context);
        linearLayout.setTextSize(2, 16.0f);
        linearLayout.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        linearLayout.setGravity(17);
        linearLayout.setText(LocaleController.getString("VoipRateCallAlert", R.string.VoipRateCallAlert));
        linearLayout.addView(linearLayout);
        linearLayout = new BetterRatingView(context);
        linearLayout.addView(linearLayout, LayoutHelper.createLinear(-2, -2, 1, 0, 16, 0, 0));
        linearLayout = new EditText(context);
        linearLayout.setHint(LocaleController.getString("CallReportHint", R.string.CallReportHint));
        linearLayout.setInputType(147457);
        linearLayout.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        linearLayout.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        linearLayout.setBackgroundDrawable(Theme.createEditTextDrawable(context, true));
        linearLayout.setPadding(0, AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f));
        linearLayout.setTextSize(18.0f);
        linearLayout.setVisibility(8);
        linearLayout.addView(linearLayout, LayoutHelper.createLinear(-1, -2, 8.0f, 8.0f, 8.0f, 0.0f));
        final boolean[] includeLogs = new boolean[]{true};
        linearLayout = new CheckBoxCell(context, 1);
        final View view = linearLayout;
        View.OnClickListener c16935 = new View.OnClickListener() {
            public void onClick(View v) {
                boolean z;
                boolean[] zArr = includeLogs;
                if (includeLogs[0]) {
                    z = false;
                } else {
                    z = true;
                }
                zArr[0] = z;
                view.setChecked(includeLogs[0], true);
            }
        };
        linearLayout.setText(LocaleController.getString("CallReportIncludeLogs", R.string.CallReportIncludeLogs), null, true, false);
        linearLayout.setClipToPadding(false);
        linearLayout.setOnClickListener(c16935);
        linearLayout.addView(linearLayout, LayoutHelper.createLinear(-1, -2, -8.0f, 0.0f, -8.0f, 0.0f));
        linearLayout = new TextView(context);
        linearLayout.setTextSize(2, 14.0f);
        linearLayout.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        linearLayout.setText(LocaleController.getString("CallReportLogsExplain", R.string.CallReportLogsExplain));
        linearLayout.setPadding(AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f), 0);
        linearLayout.setOnClickListener(c16935);
        linearLayout.addView(linearLayout);
        linearLayout.setVisibility(8);
        linearLayout.setVisibility(8);
        if (!log.exists()) {
            includeLogs[0] = false;
        }
        final View view2 = linearLayout;
        final View view3 = linearLayout;
        final long j = accessHash;
        final long j2 = callID;
        final int i = account;
        final Context context2 = context;
        final Runnable runnable = onDismiss;
        AlertDialog alert = new Builder(context).setTitle(LocaleController.getString("CallMessageReportProblem", R.string.CallMessageReportProblem)).setView(linearLayout).setPositiveButton(LocaleController.getString("Send", R.string.Send), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final int currentAccount = UserConfig.selectedAccount;
                final TLRPC$TL_phone_setCallRating req = new TLRPC$TL_phone_setCallRating();
                req.rating = view2.getRating();
                if (req.rating < 5) {
                    req.comment = view3.getText().toString();
                } else {
                    req.comment = "";
                }
                req.peer = new TLRPC$TL_inputPhoneCall();
                req.peer.access_hash = j;
                req.peer.id = j2;
                ConnectionsManager.getInstance(i).sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, TLRPC$TL_error error) {
                        if (response instanceof TLRPC$TL_updates) {
                            MessagesController.getInstance(currentAccount).processUpdates((TLRPC$TL_updates) response, false);
                            if (includeLogs[0] && log.exists() && req.rating < 4) {
                                SendMessagesHelper.prepareSendingDocument(log.getAbsolutePath(), log.getAbsolutePath(), null, "text/plain", 4244000, null, null, null);
                                Toast.makeText(context2, LocaleController.getString("CallReportSent", R.string.CallReportSent), 1).show();
                            }
                        }
                    }
                });
            }
        }).setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        }).create();
        if (BuildVars.DEBUG_VERSION && log.exists()) {
            final Context context3 = context;
            OnClickListener c16978 = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(context3, LaunchActivity.class);
                    intent.setAction("android.intent.action.SEND");
                    intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(log));
                    context3.startActivity(intent);
                }
            };
            alert.setNeutralButton("Send log", c16978);
        }
        final AlertDialog alertDialog = alert;
        alert.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                AndroidUtilities.hideKeyboard(alertDialog.getWindow().getDecorView());
            }
        });
        alert.show();
        final View btn = alert.getButton(-1);
        btn.setEnabled(false);
        view2 = linearLayout;
        final Context context4 = context;
        final File file = log;
        final View view4 = linearLayout;
        final View view5 = linearLayout;
        linearLayout.setOnRatingChangeListener(new OnRatingChangeListener() {
            public void onRatingChanged(int rating) {
                int i;
                int i2 = 0;
                btn.setEnabled(rating > 0);
                view2.setHint(rating < 4 ? LocaleController.getString("CallReportHint", R.string.CallReportHint) : LocaleController.getString("VoipFeedbackCommentHint", R.string.VoipFeedbackCommentHint));
                EditText editText = view2;
                if (rating >= 5 || rating <= 0) {
                    i = 8;
                } else {
                    i = 0;
                }
                editText.setVisibility(i);
                if (view2.getVisibility() == 8) {
                    ((InputMethodManager) context4.getSystemService("input_method")).hideSoftInputFromWindow(view2.getWindowToken(), 0);
                }
                if (file.exists()) {
                    CheckBoxCell checkBoxCell = view4;
                    if (rating < 4) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    checkBoxCell.setVisibility(i);
                    TextView textView = view5;
                    if (rating >= 4) {
                        i2 = 8;
                    }
                    textView.setVisibility(i2);
                }
            }
        });
    }

    private static File getLogFile(long callID) {
        if (BuildVars.DEBUG_VERSION) {
            File debugLogsDir = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "logs");
            for (String log : debugLogsDir.list()) {
                if (log.endsWith("voip" + callID + ".txt")) {
                    return new File(debugLogsDir, log);
                }
            }
        }
        return new File(getLogsDir(), callID + ".log");
    }

    public static void upgradeP2pSetting(int account) {
        SharedPreferences prefs = MessagesController.getMainSettings(account);
        if (prefs.contains("calls_p2p")) {
            Editor e = prefs.edit();
            if (!prefs.getBoolean("calls_p2p", true)) {
                e.putInt("calls_p2p_new", 2);
            }
            e.remove("calls_p2p").commit();
        }
    }

    public static void showCallDebugSettings(Context context) {
        final SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(1);
        TextView warning = new TextView(context);
        warning.setTextSize(1, 15.0f);
        warning.setText("Please only change these settings if you know exactly what they do.");
        warning.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        ll.addView(warning, LayoutHelper.createLinear(-1, -2, 16.0f, 8.0f, 16.0f, 8.0f));
        final TextCheckCell tcpCell = new TextCheckCell(context);
        tcpCell.setTextAndCheck("Force TCP", preferences.getBoolean("dbg_force_tcp_in_calls", false), false);
        tcpCell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean z;
                boolean z2 = true;
                boolean force = preferences.getBoolean("dbg_force_tcp_in_calls", false);
                Editor editor = preferences.edit();
                String str = "dbg_force_tcp_in_calls";
                if (force) {
                    z = false;
                } else {
                    z = true;
                }
                editor.putBoolean(str, z);
                editor.commit();
                TextCheckCell textCheckCell = tcpCell;
                if (force) {
                    z2 = false;
                }
                textCheckCell.setChecked(z2);
            }
        });
        ll.addView(tcpCell);
        if (BuildVars.DEBUG_VERSION && BuildVars.LOGS_ENABLED) {
            final TextCheckCell dumpCell = new TextCheckCell(context);
            dumpCell.setTextAndCheck("Dump detailed stats", preferences.getBoolean("dbg_dump_call_stats", false), false);
            dumpCell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean z;
                    boolean z2 = true;
                    boolean force = preferences.getBoolean("dbg_dump_call_stats", false);
                    Editor editor = preferences.edit();
                    String str = "dbg_dump_call_stats";
                    if (force) {
                        z = false;
                    } else {
                        z = true;
                    }
                    editor.putBoolean(str, z);
                    editor.commit();
                    TextCheckCell textCheckCell = dumpCell;
                    if (force) {
                        z2 = false;
                    }
                    textCheckCell.setChecked(z2);
                }
            });
            ll.addView(dumpCell);
        }
        if (VERSION.SDK_INT >= 26) {
            final TextCheckCell connectionServiceCell = new TextCheckCell(context);
            connectionServiceCell.setTextAndCheck("Enable ConnectionService", preferences.getBoolean("dbg_force_connection_service", false), false);
            connectionServiceCell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean z;
                    boolean z2 = true;
                    boolean force = preferences.getBoolean("dbg_force_connection_service", false);
                    Editor editor = preferences.edit();
                    String str = "dbg_force_connection_service";
                    if (force) {
                        z = false;
                    } else {
                        z = true;
                    }
                    editor.putBoolean(str, z);
                    editor.commit();
                    TextCheckCell textCheckCell = connectionServiceCell;
                    if (force) {
                        z2 = false;
                    }
                    textCheckCell.setChecked(z2);
                }
            });
            ll.addView(connectionServiceCell);
        }
        new Builder(context).setTitle(LocaleController.getString("DebugMenuCallSettings", R.string.DebugMenuCallSettings)).setView(ll).show();
    }
}
