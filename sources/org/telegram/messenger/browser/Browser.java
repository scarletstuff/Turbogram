package org.telegram.messenger.browser;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.baranak.turbogramf.R;
import java.lang.ref.WeakReference;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.CustomTabsCopyReceiver;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.ShareBroadcastReceiver;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.customtabs.CustomTabsCallback;
import org.telegram.messenger.support.customtabs.CustomTabsClient;
import org.telegram.messenger.support.customtabs.CustomTabsIntent;
import org.telegram.messenger.support.customtabs.CustomTabsIntent$Builder;
import org.telegram.messenger.support.customtabs.CustomTabsServiceConnection;
import org.telegram.messenger.support.customtabs.CustomTabsSession;
import org.telegram.messenger.support.customtabsclient.shared.CustomTabsHelper;
import org.telegram.messenger.support.customtabsclient.shared.ServiceConnection;
import org.telegram.messenger.support.customtabsclient.shared.ServiceConnectionCallback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC$TL_messages_getWebPagePreview;
import org.telegram.tgnet.TLRPC$TL_webPage;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

public class Browser {
    private static WeakReference<Activity> currentCustomTabsActivity;
    private static CustomTabsClient customTabsClient;
    private static WeakReference<CustomTabsSession> customTabsCurrentSession;
    private static String customTabsPackageToBind;
    private static CustomTabsServiceConnection customTabsServiceConnection;
    private static CustomTabsSession customTabsSession;

    /* renamed from: org.telegram.messenger.browser.Browser$1 */
    static class C08791 implements ServiceConnectionCallback {
        C08791() {
        }

        public void onServiceConnected(CustomTabsClient client) {
            Browser.customTabsClient = client;
            if (SharedConfig.customTabs && Browser.customTabsClient != null) {
                try {
                    Browser.customTabsClient.warmup(0);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }

        public void onServiceDisconnected() {
            Browser.customTabsClient = null;
        }
    }

    private static class NavigationCallback extends CustomTabsCallback {
        private NavigationCallback() {
        }

        public void onNavigationEvent(int navigationEvent, Bundle extras) {
        }
    }

    private static CustomTabsSession getCurrentSession() {
        return customTabsCurrentSession == null ? null : (CustomTabsSession) customTabsCurrentSession.get();
    }

    private static void setCurrentSession(CustomTabsSession session) {
        customTabsCurrentSession = new WeakReference(session);
    }

    private static CustomTabsSession getSession() {
        if (customTabsClient == null) {
            customTabsSession = null;
        } else if (customTabsSession == null) {
            customTabsSession = customTabsClient.newSession(new NavigationCallback());
            setCurrentSession(customTabsSession);
        }
        return customTabsSession;
    }

    public static void bindCustomTabsService(Activity activity) {
        Activity currentActivity = null;
        if (currentCustomTabsActivity != null) {
            currentActivity = (Activity) currentCustomTabsActivity.get();
        }
        if (!(currentActivity == null || currentActivity == activity)) {
            unbindCustomTabsService(currentActivity);
        }
        if (customTabsClient == null) {
            currentCustomTabsActivity = new WeakReference(activity);
            try {
                if (TextUtils.isEmpty(customTabsPackageToBind)) {
                    customTabsPackageToBind = CustomTabsHelper.getPackageNameToUse(activity);
                    if (customTabsPackageToBind == null) {
                        return;
                    }
                }
                customTabsServiceConnection = new ServiceConnection(new C08791());
                if (!CustomTabsClient.bindCustomTabsService(activity, customTabsPackageToBind, customTabsServiceConnection)) {
                    customTabsServiceConnection = null;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static void unbindCustomTabsService(Activity activity) {
        if (customTabsServiceConnection != null) {
            if ((currentCustomTabsActivity == null ? null : (Activity) currentCustomTabsActivity.get()) == activity) {
                currentCustomTabsActivity.clear();
            }
            try {
                activity.unbindService(customTabsServiceConnection);
            } catch (Exception e) {
            }
            customTabsClient = null;
            customTabsSession = null;
        }
    }

    public static void openUrl(Context context, String url) {
        if (url != null) {
            openUrl(context, Uri.parse(url), true);
        }
    }

    public static void openUrl(Context context, Uri uri) {
        openUrl(context, uri, true);
    }

    public static void openUrl(Context context, String url, boolean allowCustom) {
        if (context != null && url != null) {
            openUrl(context, Uri.parse(url), allowCustom);
        }
    }

    public static void openUrl(Context context, Uri uri, boolean allowCustom) {
        openUrl(context, uri, allowCustom, true);
    }

    public static void openUrl(Context context, String url, boolean allowCustom, boolean tryTelegraph) {
        openUrl(context, Uri.parse(url), allowCustom, tryTelegraph);
    }

    public static void openUrl(Context context, Uri uri, boolean allowCustom, boolean tryTelegraph) {
        if (context != null && uri != null) {
            Intent intent;
            int currentAccount = UserConfig.selectedAccount;
            boolean[] forceBrowser = new boolean[]{false};
            boolean internalUri = isInternalUri(uri, forceBrowser);
            if (tryTelegraph) {
                try {
                    if (uri.getHost().toLowerCase().equals("telegra.ph") || uri.toString().toLowerCase().contains("telegram.org/faq")) {
                        AlertDialog[] progressDialog = new AlertDialog[]{new AlertDialog(context, 1)};
                        TLObject req = new TLRPC$TL_messages_getWebPagePreview();
                        req.message = uri.toString();
                        AndroidUtilities.runOnUIThread(new Browser$$Lambda$1(progressDialog, ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, new Browser$$Lambda$0(progressDialog, currentAccount, uri, context, allowCustom))), 1000);
                        return;
                    }
                } catch (Exception e) {
                }
            }
            try {
                String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "";
                if (allowCustom && SharedConfig.customTabs && !internalUri) {
                    if (!scheme.equals("tel")) {
                        int a;
                        String[] browserPackageNames = null;
                        try {
                            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.VIEW", Uri.parse("http://www.google.com")), 0);
                            if (!(list == null || list.isEmpty())) {
                                browserPackageNames = new String[list.size()];
                                for (a = 0; a < list.size(); a++) {
                                    browserPackageNames[a] = ((ResolveInfo) list.get(a)).activityInfo.packageName;
                                    if (BuildVars.LOGS_ENABLED) {
                                        FileLog.d("default browser name = " + browserPackageNames[a]);
                                    }
                                }
                            }
                        } catch (Exception e2) {
                        }
                        List<ResolveInfo> allActivities = null;
                        try {
                            allActivities = context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.VIEW", uri), 0);
                            if (browserPackageNames != null) {
                                a = 0;
                                while (a < allActivities.size()) {
                                    for (String equals : browserPackageNames) {
                                        if (equals.equals(((ResolveInfo) allActivities.get(a)).activityInfo.packageName)) {
                                            allActivities.remove(a);
                                            a--;
                                            break;
                                        }
                                    }
                                    a++;
                                }
                            } else {
                                a = 0;
                                while (a < allActivities.size()) {
                                    if (((ResolveInfo) allActivities.get(a)).activityInfo.packageName.toLowerCase().contains("browser") || ((ResolveInfo) allActivities.get(a)).activityInfo.packageName.toLowerCase().contains("chrome")) {
                                        allActivities.remove(a);
                                        a--;
                                    }
                                    a++;
                                }
                            }
                            if (BuildVars.LOGS_ENABLED) {
                                for (a = 0; a < allActivities.size(); a++) {
                                    FileLog.d("device has " + ((ResolveInfo) allActivities.get(a)).activityInfo.packageName + " to open " + uri.toString());
                                }
                            }
                        } catch (Exception e3) {
                        }
                        if (forceBrowser[0] || allActivities == null || allActivities.isEmpty()) {
                            intent = new Intent(ApplicationLoader.applicationContext, ShareBroadcastReceiver.class);
                            intent.setAction("android.intent.action.SEND");
                            PendingIntent copy = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 0, new Intent(ApplicationLoader.applicationContext, CustomTabsCopyReceiver.class), 134217728);
                            CustomTabsIntent$Builder builder = new CustomTabsIntent$Builder(getSession());
                            builder.addMenuItem(LocaleController.getString("CopyLink", R.string.CopyLink), copy);
                            builder.setToolbarColor(Theme.getColor(Theme.key_actionBarDefault));
                            builder.setShowTitle(true);
                            builder.setActionButton(BitmapFactory.decodeResource(context.getResources(), R.drawable.abc_ic_menu_share_mtrl_alpha), LocaleController.getString("ShareFile", R.string.ShareFile), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 0, intent, 0), false);
                            CustomTabsIntent intent2 = builder.build();
                            intent2.setUseNewTask();
                            intent2.launchUrl(context, uri);
                            return;
                        }
                    }
                }
            } catch (Exception e4) {
                FileLog.e(e4);
            }
            try {
                intent = new Intent("android.intent.action.VIEW", uri);
                if (internalUri) {
                    intent.setComponent(new ComponentName(context.getPackageName(), LaunchActivity.class.getName()));
                }
                intent.putExtra("create_new_tab", true);
                intent.putExtra("com.android.browser.application_id", context.getPackageName());
                context.startActivity(intent);
            } catch (Exception e42) {
                FileLog.e(e42);
            }
        }
    }

    static final /* synthetic */ void lambda$null$0$Browser(AlertDialog[] progressDialog, TLObject response, int currentAccount, Uri uri, Context context, boolean allowCustom) {
        try {
            progressDialog[0].dismiss();
        } catch (Throwable th) {
        }
        progressDialog[0] = null;
        boolean ok = false;
        if (response instanceof TLRPC$TL_messageMediaWebPage) {
            TLRPC$TL_messageMediaWebPage webPage = (TLRPC$TL_messageMediaWebPage) response;
            if ((webPage.webpage instanceof TLRPC$TL_webPage) && webPage.webpage.cached_page != null) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.openArticle, webPage.webpage, uri.toString());
                ok = true;
            }
        }
        if (!ok) {
            openUrl(context, uri, allowCustom, false);
        }
    }

    static final /* synthetic */ void lambda$openUrl$3$Browser(AlertDialog[] progressDialog, int reqId) {
        if (progressDialog[0] != null) {
            try {
                progressDialog[0].setMessage(LocaleController.getString("Loading", R.string.Loading));
                progressDialog[0].setCanceledOnTouchOutside(false);
                progressDialog[0].setCancelable(false);
                progressDialog[0].setButton(-2, LocaleController.getString("Cancel", R.string.Cancel), new Browser$$Lambda$2(reqId));
                progressDialog[0].show();
            } catch (Exception e) {
            }
        }
    }

    static final /* synthetic */ void lambda$null$2$Browser(int reqId, DialogInterface dialog, int which) {
        ConnectionsManager.getInstance(UserConfig.selectedAccount).cancelRequest(reqId, true);
        try {
            dialog.dismiss();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean isInternalUrl(String url, boolean[] forceBrowser) {
        return isInternalUri(Uri.parse(url), forceBrowser);
    }

    public static boolean isPassportUrl(String url) {
        try {
            url = url.toLowerCase();
            if (url.startsWith("tg:passport") || url.startsWith("tg://passport") || url.startsWith("tg:secureid") || (url.contains("resolve") && url.contains("domain=telegrampassport"))) {
                return true;
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public static boolean isInternalUri(Uri uri, boolean[] forceBrowser) {
        String host = uri.getHost();
        host = host != null ? host.toLowerCase() : "";
        if ("tg".equals(uri.getScheme())) {
            return true;
        }
        String path;
        if ("telegram.dog".equals(host)) {
            path = uri.getPath();
            if (path != null && path.length() > 1) {
                path = path.substring(1).toLowerCase();
                if (!path.startsWith("blog") && !path.equals("iv") && !path.startsWith("faq") && !path.equals("apps")) {
                    return true;
                }
                if (forceBrowser != null) {
                    forceBrowser[0] = true;
                }
                return false;
            }
        } else if ("telegram.me".equals(host) || "t.me".equals(host) || "telesco.pe".equals(host)) {
            path = uri.getPath();
            if (path != null && path.length() > 1) {
                if (!path.substring(1).toLowerCase().equals("iv")) {
                    return true;
                }
                if (forceBrowser != null) {
                    forceBrowser[0] = true;
                }
                return false;
            }
        }
        return false;
    }
}
