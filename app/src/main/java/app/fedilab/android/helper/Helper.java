/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */


package app.fedilab.android.helper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import com.google.android.material.navigation.NavigationView;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.tabs.TabLayout;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import app.fedilab.android.activities.MutedInstanceActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Application;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Card;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Filters;
import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.client.Entities.Mention;
import app.fedilab.android.client.Entities.RemoteInstance;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.Tag;
import app.fedilab.android.client.Entities.TagTimeline;
import app.fedilab.android.client.Entities.Version;
import app.fedilab.android.client.Tls12SocketFactory;
import app.fedilab.android.fragments.DisplayMutedInstanceFragment;
import app.fedilab.android.sqlite.DomainBlockDAO;
import app.fedilab.android.sqlite.StatusCacheDAO;
import app.fedilab.android.sqlite.TimelineCacheDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.BuildConfig;
import app.fedilab.android.R;
import app.fedilab.android.activities.BaseMainActivity;
import app.fedilab.android.activities.HashTagActivity;
import app.fedilab.android.activities.LoginActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.MainApplication;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.activities.WebviewActivity;
import app.fedilab.android.asynctasks.RemoveAccountAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import info.guardianproject.netcipher.client.StrongBuilder;
import info.guardianproject.netcipher.client.StrongConnectionBuilder;
import info.guardianproject.netcipher.client.StrongOkHttpClientBuilder;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.koushikdutta.async.util.StreamUtility.copyStream;
import static app.fedilab.android.activities.BaseMainActivity.filters;


/**
 * Created by Thomas on 23/04/2017.
 * - Constants are defined here.
 * - Reusable methods are implemented in this section
 */

@SuppressWarnings("WeakerAccess")
public class Helper {


    @SuppressWarnings({"unused", "WeakerAccess"})
    public static  final String TAG = "mastodon_etalab";
    public static final String CLIENT_NAME_VALUE = "Fedilab";
    public static final String OAUTH_SCOPES = "read write follow";
    public static final String OAUTH_SCOPES_ADMIN = "read write follow admin:read admin:write admin";
    public static final String OAUTH_SCOPES_PIXELFED = "write follow";
    public static final String OAUTH_SCOPES_PEERTUBE = "user";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";


    public static final String PREF_KEY_ID = "userID";
    public static final String PREF_IS_MODERATOR = "is_moderator";
    public static final String PREF_IS_ADMINISTRATOR = "is_administrator";
    public static final String PREF_INSTANCE = "instance";
    public static final String REDIRECT_CONTENT = "urn:ietf:wg:oauth:2.0:oob";
    public static final String REDIRECT_CONTENT_WEB = "mastalab://backtomastalab";
    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 84;
    public static final int REQ_CODE_SPEECH_INPUT = 132;

    //Thekinrar's API: https://instances.social/api/doc/
    public static final String THEKINRAR_SECRET_TOKEN = "jGj9gW3z9ptyIpB8CMGhAlTlslcemMV6AgoiImfw3vPP98birAJTHOWiu5ZWfCkLvcaLsFZw9e3Pb7TIwkbIyrj3z6S7r2oE6uy6EFHvls3YtapP8QKNZ980p9RfzTb4";
    public static final String YANDEX_KEY = "trnsl.1.1.20170703T074828Z.a95168c920f61b17.699437a40bbfbddc4cd57f345a75c83f0f30c420";

    //Some definitions
    public static final String CLIENT_NAME = "client_name";
    public static final String APP_PREFS = "app_prefs";
    public static final String ID = "id";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String REDIRECT_URIS = "redirect_uris";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String SCOPE = "scope";
    public static final String SCOPES = "scopes";
    public static final String WEBSITE = "website";
    public static final String WEBSITE_VALUE = "https://fedilab.app";
    public static final String SHOW_BATTERY_SAVER_MESSAGE = "show_battery_saver_message";
    public static final String LAST_NOTIFICATION_MAX_ID = "last_notification_max_id";
    public static final String LAST_HOMETIMELINE_MAX_ID = "last_hometimeline_max_id";
    public static final String BOOKMARK_ID = "bookmark_id";
    public static final String BOOKMARK_DATE = "bookmark_date";
    public static final String HOME_LAST_READ = "home_last_read";
    public static final String LAST_READ_TOOT_ID = "last_read_toot_id";
    public static final String LAST_READ_TOOT_DATE = "last_read_toot_date";
    public static final String LAST_HOMETIMELINE_NOTIFICATION_MAX_ID = "last_hometimeline_notification_max_id";
    public static final String SHOULD_CONTINUE_STREAMING = "should_continue_streaming";
    public static final String SHOULD_CONTINUE_STREAMING_HOME = "should_continue_streaming_home";
    public static final String SHOULD_CONTINUE_STREAMING_FEDERATED = "should_continue_streaming_federated";
    public static final String SHOULD_CONTINUE_STREAMING_LOCAL = "should_continue_streaming_local";
    public static final String SEARCH_KEYWORD = "search_keyword";
    public static final String SEARCH_URL = "search_url";
    public static final String CLIP_BOARD = "clipboard";
    public static final String INSTANCE_NAME = "instance_name";
    public static final String LAST_DATE_LIST_REFRESH = "last_date_list_refresh";
    public static final String LAST_DATE_LIST_NAME_REFRESH = "last_date_list_name_refresh";
    public static final String LAST_LIST = "last_list";
    public static final String LAST_LIST_NAME = "last_list_name";


    //Notifications
    public static final int NOTIFICATION_INTENT = 1;
    public static final int HOME_TIMELINE_INTENT = 2;
    public static final int BACK_TO_SETTINGS = 3;
    public static final int CHANGE_USER_INTENT = 4;
    public static final int ADD_USER_INTENT = 5;
    public static final int BACKUP_INTENT = 6;
    public static final int SEARCH_TAG = 7;
    public static final int SEARCH_INSTANCE = 8;
    public static final int SEARCH_REMOTE = 9;
    public static final int RELOAD_MYVIDEOS = 10;
    public static final int REFRESH_TIMELINE = 11;
    //Settings
    public static final int TOOTS_PER_PAGE = 20;
    public static final int ACCOUNTS_PER_PAGE = 20;
    public static final int NOTIFICATIONS_PER_PAGE = 15;
    public static final String SET_ATTACHMENT_ACTION = "set_attachment_action";
    public static final String SET_THEME = "set_theme";
    public static final String SET_TIME_FROM = "set_time_from";
    public static final String SET_TIME_TO = "set_time_to";
    public static final String SET_AUTO_STORE = "set_auto_store";
    public static final String SET_POPUP_PUSH = "set_popup_push";
    public static final String SET_NSFW_TIMEOUT = "set_nsfw_timeout";
    public static final String SET_MEDIA_URLS = "set_media_urls";
    public static final String SET_TEXT_SIZE = "set_text_size";
    public static final String SET_ICON_SIZE = "set_icon_size";
    public static final String SET_TRANSLATOR = "set_translator";
    public static final String SET_LED_COLOUR = "set_led_colour";
    public static final String SET_SHOW_BOOSTS = "set_show_boost";
    public static final String SET_SHOW_REPLIES = "set_show_replies";
    public static final String SET_VIDEO_NSFW = "set_video_nsfw";
    public static final String INSTANCE_VERSION = "instance_version";
    public static final String NEWS_ACCOUNT_ID = "news_account_id";
    public static final String SET_LIVE_NOTIFICATIONS = "set_live_notifications";
    public static final String SET_DISABLE_GIF = "set_disable_gif";
    public static final String SET_DISABLE_ANIMATED_EMOJI = "set_disable_animated_emoji";
    public static final String SET_CAPITALIZE = "set_capitalize";
    public static final String SET_WYSIWYG = "set_wysiwyg";
    public static final String SET_PICTURE_RESIZE = "set_picture_resize";
    public static final String SET_FORWARD_TAGS_IN_REPLY = "set_forward_tags_in_reply";
    public static final String SET_FULL_PREVIEW = "set_full_preview";
    public static final String SET_COMPACT_MODE = "set_compact_mode";
    public static final String SET_CONSOLE_MODE = "set_console_mode";
    public static final String SET_SHARE_DETAILS = "set_share_details";
    public static final String SET_CUSTOM_SHARING = "set_custom_sharing";
    public static final String SET_CUSTOM_SHARING_URL = "set_custom_sharing_url";
    public static final String SET_NOTIF_SOUND = "set_notif_sound";
    public static final String SET_ENABLE_TIME_SLOT = "set_enable_time_slot";
    public static final String SET_KEEP_BACKGROUND_PROCESS = "set_keep_background_process";
    public static final String SET_DISPLAY_EMOJI = "set_display_emoji";
    public static final String SET_DISPLAY_CARD = "set_display_card";
    public static final String SET_DISPLAY_VIDEO_PREVIEWS= "set_display_video_previews";
    public static final String SET_DEFAULT_LOCALE_NEW = "set_default_locale_new";
    public static final String SET_NOTIFICATION_ACTION = "set_notification_action";
    public static final String SET_DISPLAY_CONTENT_AFTER_FM = "set_display_content_after_fm";
    public static final String SET_FEATURED_TAGS ="set_featured_tags";
    public static final String SET_DISPLAY_NEWS_FROM_FEDILAB ="set_display_news_from_fedilab";
    public static final String SET_SEND_CRASH_REPORTS ="set_send_crash_reports";
    public static final String SET_FEATURED_TAG_ACTION ="set_featured_tag_action";
    public static final String SET_HIDE_DELETE_BUTTON_ON_TAB = "set_hide_delete_notification_on_tab";
    public static final String SET_RETRIEVE_METADATA_IF_URL_FROM_EXTERAL = "set_retrieve_metadata_share_from_extras";
    public static final String SET_BLUR_SENSITIVE = "set_blur_sensitive";
    public static final String SET_LONG_PRESS_MEDIA = "set_long_press_media";
    public static final String SET_DISPLAY_TIMELINE_IN_LIST = "set_display_timeline_in_list";
    public static final String SET_ONION_SCHEME = "set_onion_scheme";
    public static final String SET_REMEMBER_POSITION_HOME = "set_remember_position";
    public static final String SET_DISPLAY_ADMIN_MENU = "set_display_admin_menu";
    public static final String SET_DISPLAY_ADMIN_STATUSES = "set_display_admin_statuses";
    public static final String SET_DISPLAY_FEDILAB_FEATURES_BUTTON = "set_display_fedilab_features_button";

    public static final int S_NO = 0;
    static final int S_512KO = 1;
    public static final int S_1MO = 2;
    public static final int S_2MO = 3;
    public static final int S_4MO = 4;
    public static final int S_6MO = 5;
    public static final int S_8MO = 6;

    public static final int ATTACHMENT_ALWAYS = 1;
    public static final int ATTACHMENT_WIFI = 2;
    public static final int ATTACHMENT_ASK = 3;


    public static final String SET_VIDEO_MODE = "set_video_mode";
    public static final int VIDEO_MODE_TORRENT = 0;
    public static final int VIDEO_MODE_WEBVIEW = 1;
    public static final int VIDEO_MODE_DIRECT = 2;


    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_BLACK = 3;

    public static final int LED_COLOUR = 0;

    public static final int TRANS_YANDEX = 0;
    public static final int TRANS_DEEPL = 1;
    public static final int TRANS_NONE = 2;

    public static final int ACTION_SILENT = 0;
    public static final int ACTION_ACTIVE = 1;

    public static final String SET_TRANS_FORCED = "set_trans_forced";
    public static final String SET_NOTIFY = "set_notify";
    public static final String SET_NOTIF_FOLLOW = "set_notif_follow";
    public static final String SET_NOTIF_ADD = "set_notif_follow_add";
    public static final String SET_NOTIF_MENTION = "set_notif_follow_mention";
    public static final String SET_NOTIF_SHARE = "set_notif_follow_share";
    public static final String SET_NOTIF_POLL = "set_notif_follow_poll";
    public static final String SET_NOTIF_FOLLOW_FILTER = "set_notif_follow_filter";
    public static final String SET_NOTIF_ADD_FILTER = "set_notif_follow_add_filter";
    public static final String SET_NOTIF_MENTION_FILTER = "set_notif_follow_mention_filter";
    public static final String SET_NOTIF_SHARE_FILTER = "set_notif_follow_share_filter";
    public static final String SET_NOTIF_POLL_FILTER = "set_notif_follow_poll_filter";
    public static final String SET_FILTER_REGEX_HOME = "set_filter_regex_home";
    public static final String SET_FILTER_REGEX_LOCAL = "set_filter_regex_local";
    public static final String SET_FILTER_REGEX_PUBLIC = "set_filter_regex_public";

    public static final String SET_NOTIF_VALIDATION = "set_share_validation";
    public static final String SET_NOTIF_VALIDATION_FAV = "set_share_validation_fav";
    public static final String SET_WIFI_ONLY = "set_wifi_only";
    public static final String SET_NOTIF_SILENT = "set_notif_silent";
    public static final String SET_EXPAND_CW = "set_expand_cw";
    public static final String SET_EXPAND_MEDIA = "set_expand_media";
    public static final String SET_PHOTO_EDITOR = "set_photo_editor";
    public static final String MAX_UPLOAD_IMG_RETRY_TIMES = "max_upload_img_retry_times";
    public static final String SET_DISPLAY_FOLLOW_INSTANCE = "set_display_follow_instance";
    public static final String SET_DISPLAY_NEW_BADGE = "set_display_new_badge";
    public static final String SET_DISPLAY_BOT_ICON = "set_display_bot_icon";
    public static final String SET_DISPLAY_CONFIRM = "set_display_confirm";
    public static final String SET_QUICK_REPLY = "set_quick_reply";
    public static final String SET_EMBEDDED_BROWSER = "set_embedded_browser";
    public static final String SET_CUSTOM_TABS = "set_custom_tabs";
    public static final String SET_JAVASCRIPT = "set_javascript";
    public static final String SET_COOKIES = "set_cookies";
    public static final String SET_FOLDER_RECORD = "set_folder_record";
    public static final String SET_TOOT_VISIBILITY = "set_toot_visibility";
    public static final String SET_DISPLAY_DIRECT = "set_display_direct";
    public static final String SET_DISPLAY_LOCAL = "set_display_local";
    public static final String SET_DISPLAY_GLOBAL = "set_display_global";
    public static final String SET_DISPLAY_ART = "set_display_art";
    public static final String SET_DISPLAY_PEERTUBE = "set_display_peertube";
    public static final String SET_AUTOMATICALLY_SPLIT_TOOTS = "set_automatically_split_toots";
    public static final String SET_AUTOMATICALLY_SPLIT_TOOTS_SIZE = "set_automatically_split_toots_size";
    public static final String SET_TRUNCATE_TOOTS_SIZE = "set_truncate_toots_size";
    public static final String SET_ART_WITH_NSFW = "set_art_with_nsfw";
    public static final String SET_SECURITY_PROVIDER = "set_security_provider";
    //End points
    public static final String EP_AUTHORIZE = "/oauth/authorize";



    //Proxy
    public static final String SET_PROXY_ENABLED = "set_proxy_enabled";
    public static final String SET_PROXY_TYPE = "set_proxy_type";
    public static final String SET_PROXY_HOST = "set_proxy_host";
    public static final String SET_PROXY_PORT = "set_proxy_port";
    public static final String SET_PROXY_LOGIN = "set_proxy_login";
    public static final String SET_PROXY_PASSWORD = "set_proxy_password";
    //Refresh job
    public static final int MINUTES_BETWEEN_NOTIFICATIONS_REFRESH = 15;
    public static final int MINUTES_BETWEEN_HOME_TIMELINE = 30;
    public static final int SPLIT_TOOT_SIZE = 500;

    //Translate wait time
    public static final String LAST_TRANSLATION_TIME = "last_translation_time";
    public static final int SECONDES_BETWEEN_TRANSLATE = 30;
    //Intent
    public static final String INTENT_ACTION = "intent_action";
    public static final String INTENT_TARGETED_ACCOUNT = "intent_targeted_account";
    public static final String INTENT_BACKUP_FINISH = "intent_backup_finish";
    public static final String INTENT_SEND_MODIFIED_IMAGE = "intent_send_modified_image";
    public static final String INTENT_ADD_UPLOADED_MEDIA = "intent_add_uploaded_media";
    //Receiver
    public static final String RECEIVE_DATA = "receive_data";
    public static final String RECEIVE_ACTION = "receive_action";
    public static final String RECEIVE_HOME_DATA = "receive_home_data";
    public static final String RECEIVE_FEDERATED_DATA = "receive_federated_data";
    public static final String RECEIVE_LOCAL_DATA = "receive_local_data";
    public static final String RECEIVE_HIDE_ITEM = "receive_hide_item";
    //User agent
    //public static final String USER_AGENT = "Fedilab/"+ BuildConfig.VERSION_NAME + " Android/"+ Build.VERSION.RELEASE;
    public static final String USER_AGENT = System.getProperty("http.agent");
    public static final String SET_YANDEX_API_KEY = "set_yandex_api_key";
    public static final String SET_DEEPL_API_KEY = "set_deepl_api_key";
    public static final String VIDEO_ID = "video_id_update";

    private static boolean menuAccountsOpened = false;

    public static boolean canPin;

    private static final Pattern SHORTNAME_PATTERN = Pattern.compile(":( |)([-+\\w]+):");

    public static final Pattern urlPattern = Pattern.compile(
            "(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,10}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))",

            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static final Pattern hashtagPattern = Pattern.compile("(#[\\w_A-zÀ-ÿ]+)");
    public static final Pattern groupPattern = Pattern.compile("(\\![\\w_]+(\\s|$))");
    public static final Pattern twitterPattern = Pattern.compile("((@[\\w]+)@twitter\\.com)");
    private static final Pattern mentionPattern = Pattern.compile("(@[\\w_]+(\\s|$))");
    private static final Pattern mentionLongPattern = Pattern.compile("(@[\\w_-]+@[a-z0-9.\\-]+[.][a-z]{2,10})");
    public static final Pattern xmppPattern = Pattern.compile("xmpp\\:[-a-zA-Z0-9+$&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]");
    //Event Type
    public enum EventStreaming{
        UPDATE,
        NOTIFICATION,
        DELETE,
        NONE
    }

    public enum NotifType{
        FOLLLOW,
        MENTION,
        BOOST,
        FAV,
        POLL,
        BACKUP,
        STORE,
        TOOT
    }



    /**
     * Converts emojis in input to unicode
     * @param input String
     * @param removeIfUnsupported boolean
     * @return String
     */
    public static String shortnameToUnicode(String input, boolean removeIfUnsupported) {
        Matcher matcher = SHORTNAME_PATTERN.matcher(input);

        boolean supported = Build.VERSION.SDK_INT >= 16;
        while (matcher.find()) {
            String unicode = emoji.get(matcher.group(2));
            if (unicode == null) {
                continue;
            }
            if (supported) {
                if (matcher.group(1).equals(" "))
                    input = input.replace(": " + matcher.group(2) + ":", unicode);
                else
                    input = input.replace(":" + matcher.group(2) + ":", unicode);
            } else if (removeIfUnsupported) {
                if (matcher.group(1).equals(" "))
                    input = input.replace(": " + matcher.group(2) + ":", unicode);
                else
                    input = input.replace(":" + matcher.group(2) + ":", "");
            }
        }
        return input;
    }
    //Emoji manager
    private static Map<String, String> emoji = new HashMap<>();

    public static void fillMapEmoji(Context context) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("emoji.csv")));
            String line;
            while( (line = br.readLine()) != null) {
                String str[] = line.split(",");
                String unicode = null;
                if(str.length == 2)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16)}, 0, 1);
                else if(str.length == 3)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16), Integer.parseInt(str[2].replace("0x","").trim(), 16)}, 0, 2);
                else if(str.length == 4)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16), Integer.parseInt(str[2].replace("0x","").trim(), 16), Integer.parseInt(str[3].replace("0x","").trim(), 16)}, 0, 3);
                else if(str.length == 5)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16), Integer.parseInt(str[2].replace("0x","").trim(), 16), Integer.parseInt(str[3].replace("0x","").trim(), 16), Integer.parseInt(str[4].replace("0x","").trim(), 16)}, 0, 4);
                if( unicode != null)
                    emoji.put(str[0],unicode);
            }
            br.close();
        } catch (IOException ignored) {}
    }

    /***
     *  Check if the user is connected to Internet
     * @return boolean
     */
    public static boolean isConnectedToInternet(Context context, String instance) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if( cm == null)
            return true;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ( ni != null && ni.isConnected()) {
            try {
                InetAddress ipAddr = InetAddress.getByName(instance);
                return !ipAddr.toString().equals("");
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns boolean depending if the user is authenticated
     * @param context Context
     * @return boolean
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String prefKeyOauthTokenT = sharedpreferences.getString(PREF_KEY_OAUTH_TOKEN, null);
        return ( prefKeyOauthTokenT != null);
    }

    /**
     * Log out the authenticated user by removing its token
     * @param context Context
     */
    public static void logout(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        editor.putString(Helper.CLIENT_ID, null);
        editor.putString(Helper.CLIENT_SECRET, null);
        editor.putString(Helper.PREF_KEY_ID, null);
        editor.putBoolean(Helper.PREF_IS_MODERATOR, false);
        editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, false);
        editor.putString(Helper.PREF_INSTANCE, null);
        editor.putString(Helper.ID, null);
        editor.apply();
    }

    /**
     * Log out the authenticated user by removing its token
     * @param activity Activity
     */
    public static void logoutCurrentUser(Activity activity) {
        SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        //Current user

        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(activity));
        Account account = new AccountDAO(activity, db).getUniqAccount(userId, instance);
        if( account != null) {
            account.setToken("null");
            new AccountDAO(activity, db).updateAccount(account);
        }
        Account newAccount = new AccountDAO(activity, db).getLastUsedAccount();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if( newAccount == null){
            editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, null);
            editor.putString(Helper.CLIENT_ID, null);
            editor.putString(Helper.CLIENT_SECRET, null);
            editor.putString(Helper.PREF_KEY_ID, null);
            editor.putBoolean(Helper.PREF_IS_MODERATOR, false);
            editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, false);
            editor.putString(Helper.PREF_INSTANCE, null);
            editor.putString(Helper.ID, null);
            editor.apply();
        }else{
            editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, newAccount.getToken());
            editor.putString(Helper.PREF_KEY_ID, newAccount.getId());
            editor.putString(Helper.PREF_INSTANCE, newAccount.getInstance().trim());

            editor.putBoolean(Helper.PREF_IS_MODERATOR, newAccount.isModerator());
            editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, newAccount.isAdmin());
            editor.commit();
            Intent changeAccount = new Intent(activity, MainActivity.class);
            changeAccount.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.finish();
            activity.startActivity(changeAccount);
        }

    }


    /**
     * Convert String date from Mastodon
     * @param context Context
     * @param date String
     * @return Date
     */
    public static Date mstStringToDate(Context context, String date) throws ParseException {
        if (date == null)
            return null;

        String STRING_DATE_FORMAT;
        Locale local = Locale.getDefault();
        if (!date.contains("+")) {
            STRING_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        } else{ //GNU date format
            STRING_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            local = Locale.ENGLISH;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STRING_DATE_FORMAT, local);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        simpleDateFormat.setLenient(true);
        try {
            return simpleDateFormat.parse(date);
        }catch (Exception e){
            String newdate = date.split("\\+")[0].trim();
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", local);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
            simpleDateFormat.setLenient(true);
            return simpleDateFormat.parse(newdate);
        }
    }

    /**
     * Convert String date from Mastodon
     * @param context Context
     * @param date String
     * @return Date
     */
    public static Date opencollectivetStringToDate(Context context, String date) throws ParseException {
        Locale userLocale;
        if (date == null )
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }
        String STRING_DATE_FORMAT;
        STRING_DATE_FORMAT = "yyyy-MM-dd HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STRING_DATE_FORMAT, userLocale);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        simpleDateFormat.setLenient(true);
        return simpleDateFormat.parse(date);
    }

    /**
     * Convert a date in String -> format yyyy-MM-dd HH:mm:ss
     * @param date Date
     * @return String
     */
    public static String dateToString(Date date) {
        if( date == null)
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Convert a date in String -> format yyyy-MM-dd HH:mm:ss
     * @param date Date
     * @return String
     */
    public static String dateToStringPoll(Date date) {
        if( date == null)
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm",Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Convert a date in String -> format yyyy-MM-dd HH:mm:ss
     * @param date Date
     * @return String
     */
    public static String shortDateToString(Date date) {
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        return df.format(date);
    }

    /**
     * Convert a date in String -> format yyyy-MM-dd HH:mm:ss
     * @param context Context
     * @param date Date
     * @return String
     */
    public static String dateFileToString(Context context, Date date) {
        Locale userLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",userLocale);
        return dateFormat.format(date);
    }

    /**
     * Convert String date from db to Date Object
     * @param stringDate date to convert
     * @return Date
     */
    public static Date stringToDate(Context context, String stringDate) {
        if( stringDate == null)
            return null;
        Locale userLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }
        SimpleDateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",userLocale);
        Date date = null;
        try {
            date = dateFormat.parse(stringDate);
        } catch (java.text.ParseException ignored) {

        }
        return date;
    }

    /**
     * Converts a Date date into a date-time string (SHORT format for both)
     * @param context Context
     * @param date to be converted
     * @return String
     */

    public static String shortDateTime(Context context, Date date) {
        Locale userLocale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userLocale);

        return df.format(date);
    }

    /**
     * Makes the tvDate TextView field clickable, and displays the absolute date & time of a toot
     *  for 5 seconds.
     * @param context Context
     * @param tvDate TextView
     * @param date Date
     */

    public static void absoluteDateTimeReveal(final Context context, final TextView tvDate, final Date date) {
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvDate.setText(Helper.dateDiffFull(date));

                new CountDownTimer((5 * 1000), 1000) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        tvDate.setText(Helper.dateDiff(context, date));
                    }
                }.start();
            }
        });
    }

    /**
     * Check if WIFI is opened
     * @param context Context
     * @return boolean
     */
    public static boolean isOnWIFI(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }


    public static String dateDiffFull(Date dateToot){
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());
        try {
            return df.format(dateToot);
        }catch (Exception e){
            return "";
        }
    }

    /***
     * Returns a String depending of the date
     * @param context Context
     * @param dateToot Date
     * @return String
     */
    public static String dateDiff(Context context, Date dateToot){
        Date now = new Date();
        long diff = now.getTime() - dateToot.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;

        String format = DateFormat.getDateInstance(DateFormat.SHORT).format(dateToot);
        if( years > 0 ) {
            return format;
        } else if( months > 0 || days > 7) {
            //Removes the year depending of the locale from DateFormat.SHORT format
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            df.applyPattern(df.toPattern().replaceAll("[^\\p{Alpha}]*y+[^\\p{Alpha}]*", ""));
            return df.format(dateToot);
        }else if( days > 0 )
            return context.getString(R.string.date_day, days);
        else if(hours > 0)
            return context.getResources().getString(R.string.date_hours, (int)hours);
        else if(minutes > 0)
            return context.getResources().getString(R.string.date_minutes, (int)minutes);
        else {
            if (seconds < 0)
                seconds = 0;
            return context.getResources().getString(R.string.date_seconds, (int) seconds);
        }
    }

    /***
     * Toast message depending of the status code and the initial action
     * @param context Context
     * @param statusCode int the status code
     * @param statusAction API.StatusAction the initial action
     */
    public static void manageMessageStatusCode(Context context, int statusCode, API.StatusAction statusAction){
        String message = "";
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean display_confirm = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONFIRM, true);
        if( !display_confirm){
            return;
        }
        if( statusCode >= 200 && statusCode < 400 ){
            if( statusAction == API.StatusAction.BLOCK){
                message = context.getString(R.string.toast_block);
            }else if(statusAction == API.StatusAction.UNBLOCK){
                message = context.getString(R.string.toast_unblock);
            }else if(statusAction == API.StatusAction.REBLOG){
                message = context.getString(R.string.toast_reblog);
            }else if(statusAction == API.StatusAction.UNREBLOG){
                message = context.getString(R.string.toast_unreblog);
            }else if(statusAction == API.StatusAction.MUTE){
                message = context.getString(R.string.toast_mute);
            }else if(statusAction == API.StatusAction.UNMUTE){
                message = context.getString(R.string.toast_unmute);
            }else if(statusAction == API.StatusAction.MUTE_CONVERSATION){
                message = context.getString(R.string.toast_mute_conversation);
            }else if(statusAction == API.StatusAction.UNMUTE_CONVERSATION){
                message = context.getString(R.string.toast_unmute_conversation);
            }else if(statusAction == API.StatusAction.FOLLOW){
                message = context.getString(R.string.toast_follow);
            }else if(statusAction == API.StatusAction.UNFOLLOW){
                message = context.getString(R.string.toast_unfollow);
            }else if(statusAction == API.StatusAction.FAVOURITE){
                message = context.getString(R.string.toast_favourite);
            }else if(statusAction == API.StatusAction.UNFAVOURITE){
                message = context.getString(R.string.toast_unfavourite);
            }else if(statusAction == API.StatusAction.PIN){
                message = context.getString(R.string.toast_pin);
            }else if (statusAction == API.StatusAction.UNPIN){
                message = context.getString(R.string.toast_unpin);
            }else if(statusAction == API.StatusAction.REPORT){
                message = context.getString(R.string.toast_report);
            }else if(statusAction == API.StatusAction.UNSTATUS){
                message = context.getString(R.string.toast_unstatus);
            }else if(statusAction == API.StatusAction.UNENDORSE){
                message = context.getString(R.string.toast_unendorse);
            }else if(statusAction == API.StatusAction.ENDORSE){
                message = context.getString(R.string.toast_endorse);
            }else if(statusAction == API.StatusAction.SHOW_BOOST){
                message = context.getString(R.string.toast_show_boost);
            }else if(statusAction == API.StatusAction.HIDE_BOOST){
                message = context.getString(R.string.toast_hide_boost);
            }else if(statusAction == API.StatusAction.BLOCK_DOMAIN){
                message = context.getString(R.string.toast_block_domain);
            }

        }else {
            message = context.getString(R.string.toast_error);
        }
        if( !message.trim().equals(""))
            Toasty.success(context, message, Toast.LENGTH_LONG).show();
    }






    /**
     * Manage downloads with URLs
     * @param context Context
     * @param url String download url
     */
    public static void manageDownloads(final Context context, final String url){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
        final DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(url.trim()));
        }catch (Exception e){
            Toasty.error(context,context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            return;
        }
        final String fileName = URLUtil.guessFileName(url, null, null);
        builder.setMessage(context.getResources().getString(R.string.download_file, fileName));
        builder.setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.allowScanningByMediaScanner();
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                        assert dm != null;
                        dm.enqueue(request);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        if( alert.getWindow() != null )
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Sends notification with intent
     * @param context Context
     * @param intent Intent associated to the notifcation
     * @param notificationId int id of the notification
     * @param icon Bitmap profile picture
     * @param title String title of the notification
     * @param message String message for the notification
     */
    public static void notify_user(Context context, Intent intent, int notificationId, Bitmap icon, NotifType notifType, String title, String message ) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        // prepare intent which is triggered if the user click on the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        PendingIntent pIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_ONE_SHOT);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // build notification
        String channelId;
        String channelTitle;
        switch(notifType){
            case BOOST:
                channelId = "channel_boost";
                channelTitle = context.getString(R.string.channel_notif_boost);
                break;
            case FAV:
                channelId = "channel_fav";
                channelTitle = context.getString(R.string.channel_notif_fav);
                break;
            case FOLLLOW:
                channelId = "channel_follow";
                channelTitle = context.getString(R.string.channel_notif_follow);
                break;
            case MENTION:
                channelId = "channel_mention";
                channelTitle = context.getString(R.string.channel_notif_mention);
                break;
            case POLL:
                channelId = "channel_poll";
                channelTitle = context.getString(R.string.channel_notif_poll);
                break;
            case BACKUP:
                channelId = "channel_backup";
                channelTitle = context.getString(R.string.channel_notif_backup);
                break;
            case STORE:
                channelId = "channel_store";
                channelTitle = context.getString(R.string.channel_notif_media);
                break;
            case TOOT:
                channelId = "channel_toot";
                channelTitle = context.getString(R.string.channel_notif_toot);
                break;
            default:
                channelId = "channel_boost";
                channelTitle = context.getString(R.string.channel_notif_boost);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.fedilab_notification_icon)
                .setTicker(message)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(message);
        int ledColour = Color.BLUE;
        switch (sharedpreferences.getInt(Helper.SET_LED_COLOUR, Helper.LED_COLOUR)) {
            case 0: // BLUE
                ledColour = Color.BLUE;
                break;
            case 1: // CYAN
                ledColour = Color.CYAN;
                break;
            case 2: // MAGENTA
                ledColour = Color.MAGENTA;
                break;
            case 3: // GREEN
                ledColour = Color.GREEN;
                break;
            case 4: // RED
                ledColour = Color.RED;
                break;
            case 5: // YELLOW
                ledColour = Color.YELLOW;
                break;
            case 6: // WHITE
                ledColour = Color.WHITE;
                break;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel;
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if( sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT,false) ) {
                channel = new NotificationChannel(channelId, channelTitle, NotificationManager.IMPORTANCE_LOW);
                channel.setSound(null, null);
                channel.setVibrationPattern(new long[] { 500, 500, 500});
                channel.enableVibration(true);
                channel.setLightColor(ledColour);
            }else {
                channel = new NotificationChannel(channelId, channelTitle, NotificationManager.IMPORTANCE_HIGH);
                String soundUri = sharedpreferences.getString(Helper.SET_NOTIF_SOUND, ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() +"/"+ R.raw.boop);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(Uri.parse(soundUri), audioAttributes);
            }
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(channel);
        }else{
            if( sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT,false) ) {
                notificationBuilder.setVibrate(new long[] { 500, 500, 500});
            }else {
                String soundUri =sharedpreferences.getString(Helper.SET_NOTIF_SOUND, ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() +"/"+ R.raw.boop);
                notificationBuilder.setSound(Uri.parse(soundUri));
            }
            notificationBuilder.setLights(ledColour, 500, 1000);
        }
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setLargeIcon(icon);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    /**
     * Manage downloads with URLs
     * @param context Context
     * @param url String download url
     */
    public static void manageMoveFileDownload(final Context context, final String preview_url, final String url, Bitmap bitmap, File fileVideo, boolean isSharing){

        final String fileName = URLUtil.guessFileName(url, null, null);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String myDir = sharedpreferences.getString(Helper.SET_FOLDER_RECORD, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        String mime = getMimeType(url);
        try {
            File file;
            if( bitmap != null) {
                file = new File(myDir, fileName);
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                if( mime !=null && (mime.contains("png") || mime.contains(".PNG")))
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                else
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            }else{
                File fileVideoTargeded = new File(myDir, fileName);
                copy(fileVideo, fileVideoTargeded);
                file = fileVideoTargeded;
            }
            Random r = new Random();
            final int notificationIdTmp = r.nextInt(10000);
            // prepare intent which is triggered if the
            // notification is selected
            final Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            intent.setDataAndType(uri, getMimeType(url));

            if( !isSharing)
                Glide.with(context)
                        .asBitmap()
                        .load(preview_url)
                        .listener(new RequestListener<Bitmap>(){

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                notify_user(context, intent, notificationIdTmp, BitmapFactory.decodeResource(context.getResources(),
                                        R.mipmap.ic_launcher),  NotifType.STORE, context.getString(R.string.save_over), context.getString(R.string.download_from, fileName));
                                Toasty.success(context, context.getString(R.string.toast_saved),Toast.LENGTH_LONG).show();
                                return false;
                            }
                        })
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                notify_user(context, intent, notificationIdTmp, resource,  NotifType.STORE, context.getString(R.string.save_over), context.getString(R.string.download_from, fileName));
                                Toasty.success(context, context.getString(R.string.toast_saved),Toast.LENGTH_LONG).show();
                            }
                        });
            else {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                if( bitmap != null)
                    shareIntent.setType("image/*");
                else
                    shareIntent.setType("video/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, R.string.share_with);
                context.startActivity(shareIntent);
            }
        } catch (Exception ignored) {}
    }


    /**
     * Copy a file by transferring bytes from in to out
     * @param src File source file
     * @param dst File targeted file
     * @throws IOException Exception
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            OutputStream out = new FileOutputStream(dst);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }catch (Exception ignored){}finally {
                out.close();
            }
        } catch (Exception ignored){}finally {
            in.close();
        }
    }

    /**
     * Returns the instance of the authenticated user
     * @param context Context
     * @return String domain instance
     */
    public static String getLiveInstance(Context context){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        return sharedpreferences.getString(Helper.PREF_INSTANCE, null);
    }

    public static String getLiveInstanceWithProtocol(Context context) {
        return instanceWithProtocol(context, getLiveInstance(context));
    }

    public static String instanceWithProtocol(Context context, String instance){
        if( instance == null)
            return null;
        if( instance.endsWith(".onion")) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String scheme = sharedpreferences.getString(SET_ONION_SCHEME+instance, "http");
            return scheme + "://" + instance;
        }else {
            return "https://" + instance;
        }
    }



    /**
     * Converts dp to pixel
     * @param dp float - the value in dp to convert
     * @param context Context
     * @return float - the converted value in pixel
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    /**
     * Toggle for the menu (ie: main menu or accounts menu)
     * @param activity Activity
     */
    public static void menuAccounts(final Activity activity){

        final NavigationView navigationView = activity.findViewById(R.id.nav_view);
        SharedPreferences mSharedPreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String currrentUserId = mSharedPreferences.getString(Helper.PREF_KEY_ID, null);
        final ImageView arrow  = navigationView.getHeaderView(0).findViewById(R.id.owner_accounts);
        if( currrentUserId == null)
            return;

        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK){
            changeDrawableColor(activity, R.drawable.ic_person_add,R.color.dark_text);
            changeDrawableColor(activity, R.drawable.ic_person,R.color.dark_text);
            changeDrawableColor(activity, R.drawable.ic_cancel,R.color.dark_text);
        }else {
            changeDrawableColor(activity, R.drawable.ic_person_add,R.color.black);
            changeDrawableColor(activity, R.drawable.ic_person,R.color.black);
            changeDrawableColor(activity, R.drawable.ic_cancel,R.color.black);
        }

        if( !menuAccountsOpened ){
            arrow.setImageResource(R.drawable.ic_arrow_drop_up);
            SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            final List<Account> accounts = new AccountDAO(activity, db).getAllAccount();
            String lastInstance = "";
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_accounts);
            Menu mainMenu = navigationView.getMenu();
            SubMenu currentSubmenu = null;
            if( accounts != null)
            for(final Account account: accounts) {

                if( !currrentUserId.equals(account.getId()) || !getLiveInstance(activity).equals(account.getInstance()) ) {
                    if( !lastInstance.trim().toUpperCase().equals(account.getInstance().trim().toUpperCase())){
                        lastInstance = account.getInstance().toUpperCase();
                        currentSubmenu = mainMenu.addSubMenu(account.getInstance().toUpperCase());
                    }
                    if( currentSubmenu  == null)
                        continue;
                    final MenuItem item = currentSubmenu.add("@" + account.getAcct());
                    item.setIcon(R.drawable.ic_person);
                    String url = account.getAvatar();
                    if( url.startsWith("/") ){
                        url = "https://" + account.getInstance() + account.getAvatar();
                    }
                    if(!url.equals("null"))
                    Glide.with(activity.getApplicationContext())
                            .asBitmap()
                            .load(url)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    item.setIcon(new BitmapDrawable(activity.getResources(), resource));
                                    item.getIcon().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                }
                            });
                    else
                        Glide.with(activity.getApplicationContext())
                                .asBitmap()
                                .load(R.drawable.missing_peertube)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                        item.setIcon(new BitmapDrawable(activity.getResources(), resource));
                                        item.getIcon().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                                    }
                                });

                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if( ! activity.isFinishing() ) {
                                menuAccountsOpened = false;
                                Toasty.info(activity, activity.getString(R.string.toast_account_changed, "@" + account.getAcct() + "@" + account.getInstance()), Toast.LENGTH_LONG).show();
                                changeUser(activity, account.getId(), account.getInstance(), false);
                                arrow.setImageResource(R.drawable.ic_arrow_drop_down);
                                return true;
                            }
                            return false;
                        }
                    });
                    item.setActionView(R.layout.update_account);
                    ImageView deleteButton = item.getActionView().findViewById(R.id.account_remove_button);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                            int style;
                            if (theme == Helper.THEME_DARK) {
                                style = R.style.DialogDark;
                            } else if (theme == Helper.THEME_BLACK){
                                style = R.style.DialogBlack;
                            }else {
                                style = R.style.Dialog;
                            }
                            new AlertDialog.Builder(activity, style)
                                    .setTitle(activity.getString(R.string.delete_account_title))
                                    .setMessage(activity.getString(R.string.delete_account_message, "@" + account.getAcct() + "@" + account.getInstance()))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            new RemoveAccountAsyncTask(activity, account).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            item.setVisible(false);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });

                }
            }
            currentSubmenu = mainMenu.addSubMenu("");
            MenuItem addItem = currentSubmenu.add(R.string.add_account);
            addItem.setIcon(R.drawable.ic_person_add);
            addItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putExtra("addAccount", true);
                    activity.startActivity(intent);
                    return true;
                }
            });
        }else{
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer);
            hideMenuItem(navigationView.getMenu());
            arrow.setImageResource(R.drawable.ic_arrow_drop_down);
            switchLayout(activity);
        }
        menuAccountsOpened = !menuAccountsOpened;

    }

    public static void hideMenuItem(Menu menu){
        if( BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            MenuItem itemCom = menu.findItem(R.id.nav_main_com);
            if( itemCom != null)
                itemCom.setVisible(false);
            MenuItem itemOpt = menu.findItem(R.id.nav_main_opt);
            if( itemOpt != null)
                itemOpt.setVisible(false);
            MenuItem itemPFCom = menu.findItem(R.id.nav_pixelfed_comm);
            if( itemPFCom != null)
                itemPFCom.setVisible(false);

        }else if( BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED){
            MenuItem itemCom = menu.findItem(R.id.nav_main_com);
            if( itemCom != null)
                itemCom.setVisible(false);
            MenuItem itemOpt = menu.findItem(R.id.nav_main_opt);
            if( itemOpt != null)
                itemOpt.setVisible(false);
            MenuItem itemPCom = menu.findItem(R.id.nav_peertube_comm);
            if( itemPCom != null)
                itemPCom.setVisible(false);
        }else if(  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA || BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
            MenuItem itemCom = menu.findItem(R.id.nav_peertube_comm);
            if( itemCom != null)
                itemCom.setVisible(false);
            MenuItem itemPFCom = menu.findItem(R.id.nav_pixelfed_comm);
            if( itemPFCom != null)
                itemPFCom.setVisible(false);

            if( BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ||  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
                MenuItem nav_list = menu.findItem(R.id.nav_list);
                if( nav_list != null)
                    nav_list.setVisible(false);
                MenuItem nav_peertube = menu.findItem(R.id.nav_peertube);
                if( nav_peertube != null)
                    nav_peertube.setVisible(false);
                MenuItem nav_filters = menu.findItem(R.id.nav_filters);
                if( nav_filters != null)
                    nav_filters.setVisible(false);
                MenuItem nav_follow_request = menu.findItem(R.id.nav_follow_request);
                if( nav_follow_request != null)
                    nav_follow_request.setVisible(false);
                MenuItem nav_who_to_follow = menu.findItem(R.id.nav_who_to_follow);
                if( nav_who_to_follow != null)
                    nav_who_to_follow.setVisible(false);
                MenuItem nav_blocked = menu.findItem(R.id.nav_blocked);
                if( nav_blocked != null)
                    nav_blocked.setVisible(false);
                MenuItem nav_muted = menu.findItem(R.id.nav_muted);
                if( nav_muted != null)
                    nav_muted.setVisible(false);
                MenuItem nav_blocked_domains = menu.findItem(R.id.nav_blocked_domains);
                if( nav_blocked_domains != null)
                    nav_blocked_domains.setVisible(false);
                if(BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ){
                    MenuItem nav_group = menu.findItem(R.id.nav_group);
                    if( nav_group != null)
                        nav_group.setVisible(true);
                }

            }
        }
    }

    /**
     * Changes the user in shared preferences
     * @param activity Activity
     * @param userID String - the new user id
     */
    public static void changeUser(Activity activity, String userID, String instance, boolean notificationIntent) {

        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final NavigationView navigationView = activity.findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        MainActivity.lastNotificationId = null;
        MainActivity.lastHomeId = null;
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        hideMenuItem(navigationView.getMenu());

        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(activity,db).getUniqAccount(userID, instance);
        //Can happen when an account has been deleted and there is a click on an old notification
        if( account == null)
            return;

        String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        if( !account.getToken().equals(token)){
            FragmentManager fm = activity.getFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, account.getToken());
        editor.putString(Helper.PREF_KEY_ID, account.getId());
        editor.putString(Helper.PREF_INSTANCE, account.getInstance().trim());
        editor.putBoolean(Helper.PREF_IS_MODERATOR, account.isModerator());
        editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, account.isAdmin());
        editor.commit();
        Intent changeAccount = new Intent(activity, MainActivity.class);
        changeAccount.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if( notificationIntent)
            changeAccount.putExtra(INTENT_ACTION, NOTIFICATION_INTENT);
        activity.finish();
        activity.startActivity(changeAccount);


    }


    @SuppressWarnings("SameParameterValue")
    private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPixelSize) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, (float) roundPixelSize, (float) roundPixelSize, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    public static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o2, Object o1) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Load the profile picture at the place of hamburger icon
     * @param activity Activity The current activity
     * @param url String the url of the profile picture
     */
    public static void loadPictureIcon(final Activity activity, String url, final ImageView imageView){
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(activity) + url;
        }
        loadGiF(activity,url, imageView);
    }


    public static SpannableString makeMentionsClick(final Context context, List<Mention> mentions){

        String cw_mention = "";
        if( mentions == null)
            return new SpannableString(cw_mention);
        for(Mention mention:mentions){
            cw_mention = String.format("@%s %s",mention.getUsername(),cw_mention);
        }
        SpannableString spannableString = new SpannableString(cw_mention);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        for (final Mention mention : mentions) {
            String targetedAccount = "@" + mention.getUsername();
            if (spannableString.toString().contains(targetedAccount)) {

                //Accounts can be mentioned several times so we have to loop
                for(int startPosition = -1 ; (startPosition = spannableString.toString().indexOf(targetedAccount, startPosition + 1)) != -1 ; startPosition++){
                    int endPosition = startPosition + targetedAccount.length();
                    spannableString.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View textView) {
                                    Intent intent = new Intent(context, ShowAccountActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("accountId", mention.getId());
                                    intent.putExtras(b);
                                    context.startActivity(intent);
                                }
                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(false);
                                    if (theme == THEME_DARK)
                                        ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                                    else if (theme == THEME_BLACK)
                                        ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                                    else if (theme == THEME_LIGHT)
                                        ds.setColor(ContextCompat.getColor(context, R.color.mastodonC4));
                                }
                            },
                            startPosition, endPosition,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }

        }
        return spannableString;
    }

    /**
     * Update the header with the new selected account
     * @param activity Activity
     * @param account Account - new account in use
     * @param headerLayout View - the menu header
     */
    public static void updateHeaderAccountInfo(Activity activity, final Account account, final View headerLayout){
        ImageView profilePicture = headerLayout.findViewById(R.id.profilePicture);
        TextView username = headerLayout.findViewById(R.id.username);
        TextView displayedName = headerLayout.findViewById(R.id.displayedName);
        LinearLayout more_option_container = headerLayout.findViewById(R.id.more_option_container);
        LinearLayout more_account_container = headerLayout.findViewById(R.id.more_account_container);
        SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        ImageView icon = new ImageView(activity);


        FloatingActionButton.LayoutParams layoutparmans = new FloatingActionButton.LayoutParams((int)Helper.convertDpToPixel(35,activity),(int)Helper.convertDpToPixel(35,activity));
        FloatingActionButton.LayoutParams layoutparmanImg = new FloatingActionButton.LayoutParams((int)Helper.convertDpToPixel(25,activity),(int)Helper.convertDpToPixel(25,activity));
        MenuFloating actionButton = null;
        if( theme == THEME_LIGHT) {
            icon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_brush));
            actionButton = new MenuFloating.Builder(activity)
                    .setContentView(icon, layoutparmanImg)
                    .setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular))
                    .setLayoutParams(layoutparmans)
                    .setTag("THEME")
                    .intoView(more_option_container)
                    .build();
        }else if( theme == THEME_DARK) {
            icon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_brush_white));
            actionButton = new MenuFloating.Builder(activity)
                    .setContentView(icon, layoutparmanImg)
                    .setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular_dark))
                    .setLayoutParams(layoutparmans)
                    .setTag("THEME")
                    .intoView(more_option_container)
                    .build();
        }else if( theme == THEME_BLACK) {
            icon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_brush_white));
            actionButton = new MenuFloating.Builder(activity)
                    .setContentView(icon, layoutparmanImg)
                    .setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular_black))
                    .setLayoutParams(layoutparmans)
                    .setTag("THEME")
                    .intoView(more_option_container)
                    .build();
        }

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(activity);

        // repeat many times:
        ImageView itemIconLight = new ImageView(activity);
        itemIconLight.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_brush));
        SubActionButton buttonLight = itemBuilder
                .setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular))
                .setContentView(itemIconLight).build();

        ImageView itemDark = new ImageView(activity);
        itemDark.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_brush_white));
        SubActionButton buttonDark = itemBuilder
                .setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular_dark))
                .setContentView(itemDark).build();

        ImageView itemBlack = new ImageView(activity);
        itemBlack.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_brush_white));
        SubActionButton buttonBlack = itemBuilder
                .setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular_black))
                .setContentView(itemBlack).build();

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(activity)
                .addSubActionView(buttonLight)
                .addSubActionView(buttonDark)
                .addSubActionView(buttonBlack)
                .attachTo(actionButton)
                .setStartAngle(0)
                .setEndAngle(90)
                .build();

        if( actionButton != null) {
            actionButton.setFocusableInTouchMode(true);
            actionButton.setFocusable(true);
            actionButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(actionMenu.isOpen())
                        actionMenu.close(true);
                    else
                        actionMenu.open(true);
                    return false;
                }
            });
            actionButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    try {
                        actionMenu.close(true);
                    }catch (Exception ignored){}

                }
            });
        }
        buttonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_THEME, Helper.THEME_LIGHT);
                editor.apply();
                activity.recreate();
            }
        });
        buttonDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_THEME, Helper.THEME_DARK);
                editor.apply();
                activity.recreate();
            }
        });
        buttonBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(true);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_THEME, Helper.THEME_BLACK);
                editor.apply();
                activity.recreate();
            }
        });


        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        final List<Account> accounts = new AccountDAO(activity, db).getAllAccount();

        if( accounts != null && accounts.size() > 1) {

            FloatingActionButton.LayoutParams layoutparmansAcc = new FloatingActionButton.LayoutParams((int) Helper.convertDpToPixel(37, activity), (int) Helper.convertDpToPixel(37, activity));
            FloatingActionButton.LayoutParams layoutparmanImgAcc = new FloatingActionButton.LayoutParams((int) Helper.convertDpToPixel(35, activity), (int) Helper.convertDpToPixel(35, activity));
            MenuFloating actionButtonAcc = null;
            SharedPreferences mSharedPreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String currrentUserId = mSharedPreferences.getString(Helper.PREF_KEY_ID, null);
            for(final Account accountChoice: accounts) {
                if( !accountChoice.getAvatar().startsWith("http"))
                    accountChoice.setAvatar("https://" + accountChoice.getInstance() + accountChoice.getAvatar());
                if (currrentUserId != null && (!currrentUserId.equals(accountChoice.getId()) || !getLiveInstance(activity).equals(accountChoice.getInstance()))) {
                    icon = new ImageView(activity);
                    ImageView finalIcon = icon;
                    Glide.with(activity.getApplicationContext())
                            .asBitmap()
                            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(270)))
                            .listener(new RequestListener<Bitmap>(){

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                    if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
                                        finalIcon.setImageResource(R.drawable.missing);
                                    else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                        finalIcon.setImageResource(R.drawable.missing_peertube);
                                    return false;
                                }
                            })
                            .load(accountChoice.getAvatar())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    finalIcon.setImageBitmap(resource);
                                }
                            });
                    MenuFloating.Builder actionButtonAccBuild = new MenuFloating.Builder(activity);
                    if( theme == THEME_LIGHT) {
                        actionButtonAccBuild.setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular));
                    }else if( theme == THEME_DARK) {
                        actionButtonAccBuild.setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular_dark));
                    }else if( theme == THEME_BLACK) {
                        actionButtonAccBuild.setBackgroundDrawable(activity.getResources().getDrawable( R.drawable.circular_black));
                    }

                    actionButtonAcc = actionButtonAccBuild
                            .setContentView(finalIcon, layoutparmanImgAcc)
                            .setLayoutParams(layoutparmansAcc)
                            .setTag("ACCOUNT")
                            .intoView(more_account_container)
                            .build();

                    break;
                }
            }

            FloatingActionMenu.Builder actionMenuAccBuilder = new FloatingActionMenu.Builder(activity);

            for(final Account accountChoice: accounts) {
                if (currrentUserId != null && !currrentUserId.equals(accountChoice.getId())) {
                    SubActionButton.Builder itemBuilderAcc = new SubActionButton.Builder(activity);
                    if( !accountChoice.getAvatar().startsWith("http"))
                        accountChoice.setAvatar("https://" + accountChoice.getInstance() + accountChoice.getAvatar());
                    ImageView itemIconAcc = new ImageView(activity);
                    Glide.with(activity.getApplicationContext())
                            .asBitmap()
                            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(270)))
                            .load(accountChoice.getAvatar())
                            .listener(new RequestListener<Bitmap>(){

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                    if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
                                        itemIconAcc.setImageResource(R.drawable.missing);
                                    else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                        itemIconAcc.setImageResource(R.drawable.missing_peertube);
                                    return false;
                                }
                            })
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    itemIconAcc.setImageBitmap(resource);
                                }
                            });

                    if( accounts.size() > 2 ) {
                        if(accountChoice.getSocial() != null && accountChoice.getSocial().equals("PEERTUBE"))
                            itemBuilderAcc.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.circular_peertube));
                        else
                            itemBuilderAcc.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.circular_mastodon));
                        SubActionButton subActionButtonAcc = itemBuilderAcc.setContentView(itemIconAcc, layoutparmanImgAcc)
                                .setLayoutParams(layoutparmansAcc)
                                .build();

                        subActionButtonAcc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if( !accountChoice.getToken().equals("null")) {
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, accountChoice.getToken());
                                    editor.putString(Helper.PREF_KEY_ID, accountChoice.getId());
                                    editor.putString(Helper.PREF_INSTANCE, accountChoice.getInstance().trim());
                                    editor.putBoolean(Helper.PREF_IS_MODERATOR, accountChoice.isModerator());
                                    editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, accountChoice.isAdmin());
                                    editor.commit();
                                    if (accountChoice.getSocial() != null && accountChoice.getSocial().equals("PEERTUBE"))
                                        Toasty.info(activity, activity.getString(R.string.toast_account_changed, "@" + accountChoice.getAcct()), Toast.LENGTH_LONG).show();
                                    else
                                        Toasty.info(activity, activity.getString(R.string.toast_account_changed, "@" + accountChoice.getAcct() + "@" + accountChoice.getInstance()), Toast.LENGTH_LONG).show();
                                    Intent changeAccount = new Intent(activity, MainActivity.class);
                                    changeAccount.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    activity.finish();
                                    activity.startActivity(changeAccount);
                                }else{ //The account was logged out
                                    Intent intent = new Intent(activity, LoginActivity.class);
                                    intent.putExtra("instance", accountChoice.getInstance());
                                    intent.putExtra("social", accountChoice.getSocial());
                                    activity.startActivity(intent);
                                }
                            }
                        });
                        actionMenuAccBuilder.addSubActionView(subActionButtonAcc);
                    }
                }
            }

            if( actionButtonAcc != null) {
                FloatingActionMenu actionMenuAcc = actionMenuAccBuilder.attachTo(actionButtonAcc)
                        .setStartAngle(0)
                        .setEndAngle(135)
                        .build();
                if( accounts.size() > 2){
                    actionButtonAcc.setFocusableInTouchMode(true);
                    actionButtonAcc.setFocusable(true);
                    actionButtonAcc.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(actionMenuAcc.isOpen())
                                actionMenuAcc.close(true);
                            else
                                actionMenuAcc.open(true);
                            return false;
                        }
                    });
                    actionButtonAcc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            try {
                                actionMenuAcc.close(true);
                            }catch (Exception ignored){}

                        }
                    });
                }else{
                    actionButtonAcc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for(final Account accountChoice: accounts) {
                                if (!currrentUserId.equals(accountChoice.getId())) {
                                    if( !accountChoice.getToken().equals("null")) {
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, accountChoice.getToken());
                                        editor.putString(Helper.PREF_KEY_ID, accountChoice.getId());
                                        editor.putString(Helper.PREF_INSTANCE, accountChoice.getInstance().trim());
                                        editor.putBoolean(Helper.PREF_IS_MODERATOR, accountChoice.isModerator());
                                        editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, accountChoice.isAdmin());
                                        editor.commit();
                                        Intent changeAccount = new Intent(activity, MainActivity.class);
                                        changeAccount.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        activity.finish();
                                        activity.startActivity(changeAccount);
                                    }else{ //The account was logged out
                                        Intent intent = new Intent(activity, LoginActivity.class);
                                        intent.putExtra("instance", accountChoice.getInstance());
                                        intent.putExtra("social", accountChoice.getSocial());
                                        activity.startActivity(intent);
                                    }
                                }
                            }
                        }
                    });
                }

            }

        }
        if( account == null ) {
            Helper.logout(activity);
            Intent myIntent = new Intent(activity, LoginActivity.class);
            Toasty.error(activity,activity.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            activity.startActivity(myIntent);
            activity.finish(); //User is logged out to get a new token
        }else {
            account.makeAccountNameEmoji(activity, ((BaseMainActivity)activity), account);
            username.setText(String.format("@%s",account.getUsername() + "@" + account.getInstance()));
            displayedName.setText(account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);
            String url = account.getAvatar();
            if( url.startsWith("/") ){
                url = Helper.getLiveInstanceWithProtocol(activity) + account.getAvatar();
            }
            loadGiF(activity, url, profilePicture);
            String urlHeader = account.getHeader();
            if( urlHeader.startsWith("/") ){
                urlHeader = Helper.getLiveInstanceWithProtocol(activity) + account.getHeader();
            }
            ImageView owner_accounts = headerLayout.findViewById(R.id.owner_accounts);
            ImageView header_option_info = headerLayout.findViewById(R.id.header_option_info);
            ImageView header_option_menu = headerLayout.findViewById(R.id.header_option_menu);
            if( theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK){
                changeDrawableColor(activity, owner_accounts,R.color.dark_text);
                changeDrawableColor(activity, header_option_info,R.color.dark_text);
                changeDrawableColor(activity, header_option_menu,R.color.dark_text);
            }else {
                changeDrawableColor(activity, owner_accounts,R.color.light_black);
                changeDrawableColor(activity, header_option_info,R.color.light_black);
                changeDrawableColor(activity, header_option_menu,R.color.light_black);
            }
            if (!urlHeader.contains("missing.png")) {
                Glide.with(activity.getApplicationContext())
                        .asBitmap()
                        .load(urlHeader)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                ImageView backgroundImage = headerLayout.findViewById(R.id.back_ground_image);
                                backgroundImage.setImageBitmap(resource);
                                if( theme == THEME_LIGHT){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        backgroundImage.setImageAlpha(80);
                                    }else {
                                        backgroundImage.setAlpha(80);
                                    }
                                }else{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        backgroundImage.setImageAlpha(60);
                                    }else {
                                        backgroundImage.setAlpha(60);
                                    }
                                }

                            }
                        });
            }
        }
        profilePicture.setOnClickListener(null);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account != null) {
                    Intent intent = new Intent(activity, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                        b.putBoolean("peertubeaccount", true);
                        b.putString("accountId", account.getAcct());
                    }else
                        b.putString("accountId", account.getId());
                    intent.putExtras(b);
                    activity.startActivity(intent);
                }
            }
        });
    }


    /**
     * Retrieves the cache size
     * @param directory File
     * @return long value in Mo
     */
    public static long cacheSize(File directory) {
        long length = 0;
        if( directory == null || directory.length() == 0 )
            return -1;
        for (File file : directory.listFiles()) {
            if (file.isFile()){
                try {
                    length += file.length();
                } catch (NullPointerException e) {
                    return -1;
                }
            }else {
                if( !file.getName().equals("databases") && !file.getName().equals("shared_prefs")) {
                    length += cacheSize(file);
                }
            }
        }
        return length;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                if (!aChildren.equals("databases") && !aChildren.equals("shared_prefs")) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else{
            return dir != null && dir.isFile() && dir.delete();
        }
    }




    /***
     * Check if the account bio contents urls & tags and fills the content with ClickableSpan
     * Click on url => webview or external app
     * Click on tag => HashTagActivity
     * @param context Context
     * @param fullContent String, should be the st
     * @return TextView
     */
    public static SpannableString clickableElementsDescription(final Context context, String fullContent) {


        SpannableString spannableString;
        fullContent = Helper.shortnameToUnicode(fullContent, true);
        SpannableString spannableStringT = new SpannableString(fullContent);
        Pattern aLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        Matcher matcherALink = aLink.matcher(spannableStringT.toString());
        ArrayList<Account> accountsMentionUnknown = new ArrayList<>();
        while (matcherALink.find()){
            String acct = matcherALink.group(3).replace("@","");
            String instance = matcherALink.group(2);
            Account account = new Account();
            account.setAcct(acct);
            account.setInstance(instance);
            accountsMentionUnknown.add(account);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spannableString = new SpannableString(Html.fromHtml(spannableStringT.toString(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        else
            //noinspection deprecation
            spannableString = new SpannableString(Html.fromHtml(spannableStringT.toString()));

        URLSpan[] urls = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
        for(URLSpan span : urls)
            spannableString.removeSpan(span);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        Matcher matcher;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            matcher = Patterns.WEB_URL.matcher(spannableString);
        else
            matcher = urlPattern.matcher(spannableString);
        while (matcher.find()){
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            final String url = spannableString.toString().substring(matchStart, matchEnd);

            if( url.startsWith("http"))
                if( matchEnd <= spannableString.toString().length() && matchEnd >= matchStart)
                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            Helper.openBrowser(context, url);
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            if (theme == THEME_DARK)
                                ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                            else if (theme == THEME_BLACK)
                                ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                            else if (theme == THEME_LIGHT)
                                ds.setColor(ContextCompat.getColor(context, R.color.mastodonC4));
                        }
                    }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        matcher = hashtagPattern.matcher(spannableString);
        while (matcher.find()){
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            final String tag = spannableString.toString().substring(matchStart, matchEnd);
            if( matchEnd <= spannableString.toString().length() && matchEnd >= matchStart)
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Intent intent = new Intent(context, HashTagActivity.class);
                        Bundle b = new Bundle();
                        b.putString("tag", tag.substring(1));
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                        if (theme == THEME_DARK)
                            ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                        else if (theme == THEME_BLACK)
                            ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                        else if (theme == THEME_LIGHT)
                            ds.setColor(ContextCompat.getColor(context, R.color.mastodonC4));
                    }
                }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if( accountsMentionUnknown.size() > 0 ) {
            for(Account account: accountsMentionUnknown){
                String targetedAccount = "@" + account.getAcct();
                if (spannableString.toString().toLowerCase().contains(targetedAccount.toLowerCase())) {
                    //Accounts can be mentioned several times so we have to loop
                    for(int startPosition = -1 ; (startPosition = spannableString.toString().toLowerCase().indexOf(targetedAccount.toLowerCase(), startPosition + 1)) != -1 ; startPosition++){
                        int endPosition = startPosition + targetedAccount.length();
                        if( endPosition <= spannableString.toString().length() && endPosition >= startPosition)
                            spannableString.setSpan(new ClickableSpan() {
                                 @Override
                                 public void onClick(View textView) {
                                     CrossActions.doCrossProfile(context,account);
                                 }
                                 @Override
                                 public void updateDrawState(TextPaint ds) {
                                     super.updateDrawState(ds);
                                     ds.setUnderlineText(false);
                                     if (theme == THEME_DARK)
                                         ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                                     else if (theme == THEME_BLACK)
                                         ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                                     else if (theme == THEME_LIGHT)
                                         ds.setColor(ContextCompat.getColor(context, R.color.mastodonC4));
                                 }
                             },
                            startPosition, endPosition,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        matcher = android.util.Patterns.EMAIL_ADDRESS.matcher(spannableString);
        while (matcher.find()){
            int matchStart = matcher.start(0);
            int matchEnd = matcher.end();
            final String email = spannableString.toString().substring(matchStart, matchEnd);
            if( matchEnd <= spannableString.toString().length() && matchEnd >= matchStart) {
                urls = spannableString.getSpans(matchStart, matchEnd, URLSpan.class);
                for(URLSpan span : urls)
                    spannableString.removeSpan(span);
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        try {
                            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                            emailIntent.setType("plain/text");
                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_email)));
                        }catch (Exception e){
                            Toasty.error(context, context.getString(R.string.toast_no_apps), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                        if (theme == THEME_DARK)
                            ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                        else if (theme == THEME_BLACK)
                            ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                        else if (theme == THEME_LIGHT)
                            ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                    }
                }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

        }
        matcher = Helper.xmppPattern.matcher(spannableString);
        while (matcher.find()){
            int matchStart = matcher.start(0);
            int matchEnd = matcher.end();
            final String url = spannableString.toString().substring(matchStart, matchEnd);
            if( matchEnd <= spannableString.toString().length() && matchEnd >= matchStart) {
                urls = spannableString.getSpans(matchStart, matchEnd, URLSpan.class);
                for(URLSpan span : urls)
                    spannableString.removeSpan(span);
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(intent);
                        }catch (Exception e){
                            Toasty.error(context, context.getString(R.string.toast_no_apps), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                        if (theme == THEME_DARK)
                            ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                        else if (theme == THEME_BLACK)
                            ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                        else if (theme == THEME_LIGHT)
                            ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                    }
                }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

        }
        return spannableString;
    }


    public static WebView initializeWebview(Activity activity, int webviewId){

        WebView webView = activity.findViewById(webviewId);
        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean javascript = sharedpreferences.getBoolean(Helper.SET_JAVASCRIPT, true);

        webView.getSettings().setJavaScriptEnabled(javascript);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean cookies = sharedpreferences.getBoolean(Helper.SET_COOKIES, false);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, cookies);
        }
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        return webView;
    }


    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException ignored) {}
        return "";
    }

    /**
     * change color of a drawable
     * @param drawable int the drawable
     * @param hexaColor example 0xffff00
     */
    public static Drawable changeDrawableColor(Context context, int drawable, int hexaColor){
        Drawable mDrawable = ContextCompat.getDrawable(context, drawable);
        int color = Color.parseColor(context.getString(hexaColor));
        assert mDrawable != null;
        mDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        DrawableCompat.setTint(mDrawable, ContextCompat.getColor(context, hexaColor));
        return mDrawable;
    }
    /**
     * change color of a drawable
     * @param imageView int the ImageView
     * @param hexaColor example 0xffff00
     */
    public static void changeDrawableColor(Context context, ImageView imageView, int hexaColor){
        if( imageView == null)
            return;
        imageView.setColorFilter(context.getResources().getColor(hexaColor));
    }

    /**
     * change color of a drawable
     * @param imageButton int the ImageButton
     * @param hexaColor example 0xffff00
     */
    public static void changeDrawableColor(Context context, ImageButton imageButton, int hexaColor){
        if( imageButton == null)
            return;
        imageButton.setColorFilter(context.getResources().getColor(hexaColor));
    }

    /**
     * change color of a drawable
     * @param button int the button
     * @param hexaColor example 0xffff00
     */
    public static void changeButtonTextColor(Context context, Button button, int hexaColor){
        if( button == null)
            return;
        button.setTextColor(context.getResources().getColor(hexaColor));
    }
    /**
     * Returns the current locale of the device
     * @param context Context
     * @return String locale
     */
    public static String currentLocale(Context context) {
        String locale;
        Locale current;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            current = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            current = context.getResources().getConfiguration().locale;
        }
        locale = current.toString();
        locale = locale.split("_")[0];
        return locale;
    }


    /**
     * Compare date with these in shared pref.
     * @param context Context
     * @param newDate String
     * @param shouldBeGreater boolean if date passed as a parameter should be greater
     * @return boolean
     */
    public static boolean compareDate(Context context, String newDate, boolean shouldBeGreater){
        String dateRef;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if (shouldBeGreater) {
            dateRef = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        }else {
            dateRef = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
        }
        try{
            Locale userLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userLocale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                //noinspection deprecation
                userLocale = context.getResources().getConfiguration().locale;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", userLocale);
            Date newDateD = formatter.parse(newDate);
            Date dateRefD = formatter.parse(dateRef);
            if (shouldBeGreater) {
                return (newDateD.after(dateRefD));
            }else {
                return (newDateD.before(dateRefD));
            }
        } catch (java.text.ParseException e) {
            return false;
        }
    }


    /**
     * Tells if the the service can notify depending of the current hour and minutes
     * @param context Context
     * @return boolean
     */
    public static boolean canNotify(Context context){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
        if( !notify)
            return false;
        boolean enable_time_slot = sharedpreferences.getBoolean(Helper.SET_ENABLE_TIME_SLOT, true);
        if( ! enable_time_slot)
            return true;
        String dateIni = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        String dateEnd = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
        int notification = sharedpreferences.getInt(Helper.SET_NOTIFICATION_ACTION, Helper.ACTION_ACTIVE);
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        String hourS = String.valueOf(hour).length() == 1?"0"+String.valueOf(hour):String.valueOf(hour);
        String minuteS = String.valueOf(minute).length() == 1?"0"+String.valueOf(minute):String.valueOf(minute);
        String currentDate = hourS + ":" + minuteS;
        try{
            Locale userLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userLocale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                //noinspection deprecation
                userLocale = context.getResources().getConfiguration().locale;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", userLocale);
            Date dateIniD = formatter.parse(dateIni);
            Date dateEndD = formatter.parse(dateEnd);
            Date currentDateD = formatter.parse(currentDate);
            boolean canNotify = false;
            if( currentDateD.before(dateEndD) && currentDateD.after(dateIniD) && notification == Helper.ACTION_ACTIVE)
                canNotify = true;
            else if ( currentDateD.after(dateEndD) && currentDateD.before(dateIniD) && notification == Helper.ACTION_SILENT )
                canNotify = true;
            return canNotify;
        } catch (java.text.ParseException e) {
            return true;
        }
    }


    /**
     * Unserialized a
     * @param serializedListTimeline String serialized List
     * @return List
     */
    public static app.fedilab.android.client.Entities.List restoreListtimelineFromString(String serializedListTimeline){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedListTimeline, app.fedilab.android.client.Entities.List.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a List class
     * @param listTimeline List to serialize
     * @return String serialized List
     */
    public static String listTimelineToStringStorage(app.fedilab.android.client.Entities.List listTimeline){
        Gson gson = new Gson();
        try {
            return gson.toJson(listTimeline);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Unserialized a TagTimeline
     * @param serializedTagTimeline String serialized TagTimeline
     * @return TagTimeline
     */
    public static TagTimeline restoreTagTimelineFromString(String serializedTagTimeline){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedTagTimeline, TagTimeline.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a TagTimeline class
     * @param tagTimeline TagTimeline to serialize
     * @return String serialized TagTimeline
     */
    public static String tagTimelineToStringStorage(TagTimeline tagTimeline){
        Gson gson = new Gson();
        try {
            return gson.toJson(tagTimeline);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Unserialized a RemoteInstance
     * @param serializedRemoteInstance String serialized RemoteInstance
     * @return RemoteInstance
     */
    public static RemoteInstance restoreRemoteInstanceFromString(String serializedRemoteInstance){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedRemoteInstance, RemoteInstance.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a RemoteInstance class
     * @param remoteInstance RemoteInstance to serialize
     * @return String serialized RemoteInstance
     */
    public static String remoteInstanceToStringStorage(RemoteInstance remoteInstance){
        Gson gson = new Gson();
        try {
            return gson.toJson(remoteInstance);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Unserialized a Locale
     * @param serializedLocale String serialized locale
     * @return Locale
     */
    public static Locale restoreLocaleFromString(String serializedLocale){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedLocale, Locale.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a Locale class
     * @param locale Locale to serialize
     * @return String serialized Locale
     */
    public static String localeToStringStorage(Locale locale){
        Gson gson = new Gson();
        try {
            return gson.toJson(locale);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a Status class
     * @param status Status to serialize
     * @return String serialized Status
     */
    public static String statusToStringStorage(Status status){
        Gson gson = new Gson();
        try {
            return gson.toJson(status);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a Card
     * @param serializedCard String serialized card
     * @return Card
     */
    public static Card restoreCardFromString(String serializedCard){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedCard, Card.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a Card class
     * @param card Card to serialize
     * @return String serialized Status
     */
    public static String cardToStringStorage(Card card){
        Gson gson = new Gson();
        try {
            return gson.toJson(card);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a Status
     * @param serializedStatus String serialized status
     * @return Status
     */
    public static Status restoreStatusFromString(String serializedStatus){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedStatus, Status.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized a List<String>
     * @param list List<String> to serialize
     * @return String serialized List
     */
    public static String arrayToStringStorage(List<String> list){
        Gson gson = new Gson();
        try {
            return gson.toJson(list);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a List<String>
     * @param serializedArray String serialized array
     * @return List<String> list
     */
    public static List<String> restoreArrayFromString(String serializedArray){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedArray, List.class);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Serialized an Application class
     * @param application Application to serialize
     * @return String serialized Application
     */
    public static String applicationToStringStorage(Application application){
        Gson gson = new Gson();
        try {
            return gson.toJson(application);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized an Application
     * @param serializedApplication String serialized application
     * @return Application
     */
    public static Application restoreApplicationFromString(String serializedApplication){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedApplication, Application.class);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Serialized a Account class
     * @param account Account to serialize
     * @return String serialized Account
     */
    public static String accountToStringStorage(Account account){
        Gson gson = new Gson();
        try {
            return gson.toJson(account);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Unserialized an Account
     * @param serializedAccount String serialized account
     * @return Account
     */
    public static Account restoreAccountFromString(String serializedAccount){
        Gson gson = new Gson();
        try {
            return gson.fromJson(serializedAccount, Account.class);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Serialized a List of Emojis class
     * @param emojis Emojis List to serialize
     * @return String serialized List of Emojis
     */
    public static String emojisToStringStorage(List<Emojis> emojis){
        Gson gson = new Gson();
        try {
            return gson.toJson(emojis);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a list of Emojis
     * @param serializedEmojis String serialized emojis
     * @return List<Emojis>
     */
    public static List<Emojis> restoreEmojisFromString(String serializedEmojis){
        Type listType = new TypeToken<ArrayList<Emojis>>(){}.getType();
        try {
            return new Gson().fromJson(serializedEmojis, listType);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Serialized a List of a Attachment class
     * @param attachments Attachment List to serialize
     * @return String serialized List of Attachment
     */
    public static String attachmentToStringStorage(List<Attachment> attachments){
        Gson gson = new Gson();
        try {
            return gson.toJson(attachments);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a list of Attachment
     * @param serializedAttachment String serialized attachment
     * @return List<Attachment>
     */
    public static ArrayList<Attachment> restoreAttachmentFromString(String serializedAttachment){
        Type listType = new TypeToken<ArrayList<Attachment>>(){}.getType();
        try {
            return new Gson().fromJson(serializedAttachment, listType);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    /**
     * Serialized a List of a Mention class
     * @param mentions Mention List to serialize
     * @return String serialized List of Mention
     */
    public static String mentionToStringStorage(List<Mention> mentions){
        Gson gson = new Gson();
        try {
            return gson.toJson(mentions);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a list of Mention
     * @param serializedMention String serialized mention
     * @return String serialized List of Mention
     */
    public static List<Mention> restoreMentionFromString(String serializedMention){
        Type listType = new TypeToken<ArrayList<Mention>>(){}.getType();
        try {
            return new Gson().fromJson(serializedMention, listType);
        }catch (Exception e){
            return null;
        }
    }


    /**
     * Serialized a List of a Tag class
     * @param tags Tag List to serialize
     * @return String serialized List of Tag
     */
    public static String tagToStringStorage(List<Tag> tags){
        Gson gson = new Gson();
        try {
            return gson.toJson(tags);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Unserialized a list of Tag
     * @param serializedTag String serialized tag
     * @return String serialized List of Tag
     */
    public static List<Tag> restoreTagFromString(String serializedTag){
        Type listType = new TypeToken<ArrayList<Tag>>(){}.getType();
        try {
            return new Gson().fromJson(serializedTag, listType);
        }catch (Exception e){
            return null;
        }
    }



    /**
     * Check if a job id is in array of ids
     * @param jobIds int[]
     * @param id int id to check
     * @return boolean
     */
    public static boolean isJobPresent(int[] jobIds, int id){
        for(int x:jobIds) {
            if (x == id) {return true;}
        }
        return false;
    }



    public static void unCheckAllMenuItems(NavigationView navigationView){
        navigationView.setCheckedItem(R.id.menu_none);
        unCheckAllMenuItemsRec(navigationView.getMenu());
    }

    private static void unCheckAllMenuItemsRec(@NonNull final Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            if(item.hasSubMenu()) {
                unCheckAllMenuItemsRec(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
    }


    /**
     * Changes the menu layout
     * @param activity Activity must be an instance of MainActivity
     */
    public static void switchLayout(Activity activity){
        //Check if the class calling the method is an instance of MainActivity
        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final NavigationView navigationView = activity.findViewById(R.id.nav_view);
        TabLayout tableLayout = activity.findViewById(R.id.tabLayout);
        String userID = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(activity,db).getUniqAccount(userID, instance);
        if( account != null) {
            if (account.isLocked()) {
                if( navigationView.getMenu().findItem(R.id.nav_follow_request) != null)
                    navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
            } else {
                if( navigationView.getMenu().findItem(R.id.nav_follow_request) != null)
                    navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
            }
            MenuItem news = navigationView.getMenu().findItem(R.id.nav_news);
            if( news != null){
                boolean display_news = sharedpreferences.getBoolean(Helper.SET_DISPLAY_NEWS_FROM_FEDILAB, true);
                if( ! display_news ){
                    news.setVisible(false);
                }
            }
            if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_administration);
                if( adminItem != null){
                    adminItem.setVisible(false);
                }
            }else{
                boolean display_admin_menu = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ADMIN_MENU + userID + instance, false);
                if( !display_admin_menu){
                    MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_administration);
                    if( adminItem != null){
                        adminItem.setVisible(false);
                    }
                }
            }

        }
        String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userID + instance, null);
        if (instanceVersion != null && navigationView.getMenu().findItem(R.id.nav_list) != null) {
            Version currentVersion = new Version(instanceVersion);
            Version minVersion = new Version("2.1");
            if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                navigationView.getMenu().findItem(R.id.nav_list).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_list).setVisible(false);
            }
        }
        tableLayout.setVisibility(View.VISIBLE);
    }



    /**
     * Get a bitmap from a view
     * @param view The view to convert
     * @return Bitmap
     */
    public static Bitmap convertTootIntoBitmap(Context context, String name, View view) {

        if( view.getWidth() == 0 || view.getHeight() == 0){
            Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            return null;
        }
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth()+(int)Helper.convertDpToPixel(10, context), view.getHeight()+(int)Helper.convertDpToPixel(30, context), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawBitmap(returnedBitmap, view.getWidth()+(int)Helper.convertDpToPixel(10, context), 0, null);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else {
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            if (theme == Helper.THEME_DARK) {
                canvas.drawColor(ContextCompat.getColor(context, R.color.mastodonC1));

            }else if( theme == Helper.THEME_BLACK){
                canvas.drawColor(ContextCompat.getColor(context, R.color.black));
            }
            else {
                canvas.drawColor(Color.WHITE);
            }
        }

        view.draw(canvas);
        Paint paint = new Paint();
        int mastodonC4 = ContextCompat.getColor(context, R.color.mastodonC4);
        paint.setColor(mastodonC4);
        paint.setStrokeWidth(12);
        paint.setTextSize((int)Helper.convertDpToPixel(14, context));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawText(name +" - #Fedilab", 0, view.getHeight() +(int)Helper.convertDpToPixel(15, context), paint);

        return returnedBitmap;
    }


    @SuppressLint("DefaultLocale")
    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        Locale locale = null;
        try {
            locale = Locale.getDefault();
        }catch (Exception ignored){}
        if(locale != null)
        return String.format(locale, "%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
        else
            return String.format( "%.1f %c",
                    count / Math.pow(1000, exp),
                    "kMGTPE".charAt(exp-1));
    }


    public static Bitmap addBorder(Bitmap resource, Context context) {
        int w = resource.getWidth();
        int h = resource.getHeight();
        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);
        Paint p = new Paint();
        p.setAntiAlias(true);
        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        c.drawBitmap(resource, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ContextCompat.getColor(context, R.color.white));
        p.setStrokeWidth(3);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
        return output;
    }

    public static String secondsToString(int pTime) {

        int hour = pTime/3600;
        int min = (pTime -(hour*3600))/60;
        int sec = pTime -(hour*3600)-(min*60);
        String strHour="0", strMin="0", strSec="0";

        if( hour > 0 )
            strHour = String.format(Locale.getDefault(), "%02d", hour);
        if( min > 0 )
            strMin = String.format(Locale.getDefault(), "%02d", min);
        strSec = String.format(Locale.getDefault(), "%02d", sec);
        if( hour > 0 )
            return String.format(Locale.getDefault(),"%s:%s:%s",strHour, strMin,strSec);
        else
            return String.format(Locale.getDefault(), "%s:%s",strMin,strSec);
    }

    public static void loadGiF(final Context context, String url, final ImageView imageView){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean disableGif = sharedpreferences.getBoolean(SET_DISABLE_GIF, false);

        if (context instanceof FragmentActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && ((FragmentActivity) context).isDestroyed()) {
                return;
            }
        }
        if( url != null && url.startsWith("/")){
            url = Helper.getLiveInstanceWithProtocol(context) + url;
        }
        if( url == null  || url.equals("null")|| url.equals("false") || url.contains("missing.png") || url.contains(".svg")) {
            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || BaseMainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                try {
                    Glide.with(imageView.getContext())
                            .load(R.drawable.missing)
                            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                            .into(imageView);
                } catch (Exception ignored) {
                }
                return;
            }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
                try {
                    Glide.with(imageView.getContext())
                            .load(R.drawable.missing_peertube)
                            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                            .into(imageView);
                } catch (Exception ignored) {
                }
                return;
            }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ||  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
                try {
                    Glide.with(imageView.getContext())
                            .load(R.drawable.gnu_default_avatar)
                            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                            .into(imageView);
                } catch (Exception ignored) {
                }
                return;
            }
        }
        if( !disableGif)
            try {
                Glide.with(imageView.getContext())
                        .load(url)
                        .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                        .into(imageView);
            }catch (Exception ignored){}
        else
            try {
            Glide.with(context)
                    .asBitmap()
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                    .load(url)
                    .into(imageView);
            }catch (Exception ignored){}
    }

    /**
     * Manage URLs to open (built-in or external app)
     * @param context Context
     * @param url String url to open
     */
    public static void openBrowser(Context context, String url){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        if( embedded_browser) {
            Intent intent = new Intent(context, WebviewActivity.class);
            Bundle b = new Bundle();
            String finalUrl = url;
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                finalUrl = "http://" + url;
            b.putString("url", finalUrl);
            intent.putExtras(b);
            context.startActivity(intent);
        }else {
            boolean custom_tabs = sharedpreferences.getBoolean(Helper.SET_CUSTOM_TABS, true);
            if( custom_tabs){
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.setToolbarColor(ContextCompat.getColor(context, R.color.mastodonC1));
                try {
                    customTabsIntent.launchUrl(context, Uri.parse(url));
                }catch (Exception ignored){Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();}
            }else{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                try {
                    context.startActivity(intent);
                }catch (Exception e){
                    Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                }

            }
        }
    }


    public static void installProvider(){

        boolean patch_provider = true;
        try {
            Context ctx = MainApplication.getApp();
            SharedPreferences sharedpreferences = ctx.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            patch_provider = sharedpreferences.getBoolean(Helper.SET_SECURITY_PROVIDER, true);
        }catch (Exception ignored){}
        if( patch_provider)
            try {
                Security.insertProviderAt(Conscrypt.newProvider(),1);
            }catch (Exception ignored){}
    }


    public enum MediaType{
        MEDIA,
        PROFILE
    }

    
    public static ByteArrayInputStream compressImage(Context context, android.net.Uri uriFile, MediaType mediaType){

        ContentResolver cr = context.getContentResolver();
        String mime = cr.getType(uriFile);
        ByteArrayInputStream bs = null;
        if(mime != null && mime.toLowerCase().contains("image")){
            Bitmap takenImage;
            try {
                takenImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uriFile);
            } catch (IOException e) {
                try {
                    Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                }catch (Exception ignored){};
                return null;
            }
            ExifInterface exif = null;
            try (InputStream inputStream = context.getContentResolver().openInputStream(uriFile)) {
                assert inputStream != null;
                exif = new ExifInterface(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Matrix matrix = null;
            if( takenImage != null ){
                int size = takenImage.getByteCount();
                if( exif != null) {
                    int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotationDegree = 0;
                    if (rotation == ExifInterface.ORIENTATION_ROTATE_90) { rotationDegree = 90; }
                    else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {  rotationDegree = 180; }
                    else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {  rotationDegree =  270; }
                    matrix = new Matrix();
                    if (rotation != 0f) {matrix.preRotate(rotationDegree);}
                }

                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                int resizeSet = sharedpreferences.getInt(Helper.SET_PICTURE_RESIZE, Helper.S_4MO);
                if( mediaType == MediaType.PROFILE)
                    resizeSet = Helper.S_1MO;
                double resizeby = size;
                if( resizeSet == Helper.S_512KO){
                    resizeby = 4194304;
                }else if(resizeSet == Helper.S_1MO){
                    resizeby = 8388608;
                }else if(resizeSet == Helper.S_2MO){
                    resizeby = 16777216;
                }else if(resizeSet == Helper.S_4MO){
                    resizeby = 33554432;
                }else if(resizeSet == Helper.S_6MO){
                    resizeby = 50331648;
                }else if(resizeSet == Helper.S_8MO) {
                    resizeby = 67108864;
                }

                double resize = ((double)size)/resizeby;
                if( resize > 1 ){
                    Bitmap newBitmap = Bitmap.createScaledBitmap(takenImage, (int) (takenImage.getWidth() / resize),
                            (int) (takenImage.getHeight() / resize), true);
                    Bitmap adjustedBitmap;
                    if( matrix != null)
                        try {
                            adjustedBitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight(), matrix, true);
                        }catch (Exception e){
                            adjustedBitmap = newBitmap;
                        }
                    else
                        adjustedBitmap = newBitmap;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    if( mime.contains("png") || mime.contains(".PNG"))
                        adjustedBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                    else
                        adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    bs = new ByteArrayInputStream(bitmapdata);
                }else {
                    try {
                        InputStream inputStream = context.getContentResolver().openInputStream(uriFile);
                        byte[] buff = new byte[8 * 1024];
                        int bytesRead;
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        assert inputStream != null;
                        while((bytesRead = inputStream.read(buff)) != -1) {
                            bao.write(buff, 0, bytesRead);
                        }
                        byte[] data = bao.toByteArray();
                        bs  = new ByteArrayInputStream(data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uriFile);
                    byte[] buff = new byte[8 * 1024];
                    int bytesRead;
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    assert inputStream != null;
                    while((bytesRead = inputStream.read(buff)) != -1) {
                        bao.write(buff, 0, bytesRead);
                    }
                    byte[] data = bao.toByteArray();
                    bs  = new ByteArrayInputStream(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uriFile);
                byte[] buff = new byte[8 * 1024];
                int bytesRead;
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                assert inputStream != null;
                while((bytesRead = inputStream.read(buff)) != -1) {
                    bao.write(buff, 0, bytesRead);
                }
                byte[] data = bao.toByteArray();
                bs  = new ByteArrayInputStream(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bs;
    }

    public static String getFileName(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        try {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            returnCursor.close();
            Random r = new Random();
            int suf = r.nextInt(9999 - 1000) + 1000;
            return String.valueOf(suf)+name;
        }catch (Exception e){
            Random r = new Random();
            int suf = r.nextInt(9999 - 1000) + 1000;
            ContentResolver cr = context.getContentResolver();
            String mime = cr.getType(uri);
            if( mime != null && mime.split("/").length > 1)
                return "__" + String.valueOf(suf)+"."+mime.split("/")[1];
            else
                return "__" + String.valueOf(suf)+".jpg";
        }
    }

    public static Bitmap compressImageIfNeeded(Context context, Bitmap bmToCompress){

        int size = bmToCompress.getByteCount();
        double resizeby =  33554432; //4Mo
        double resize = ((double)size)/resizeby;
        if( resize > 1 ) {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bmToCompress, (int) (bmToCompress.getWidth() / resize),
                    (int) (bmToCompress.getHeight() / resize), true);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            return newBitmap;
        }
        return bmToCompress;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public static void largeLog(String content) {
        if (content.length() > 4000) {
            Log.v(Helper.TAG, content.substring(0, 4000));
            largeLog(content.substring(4000));
        } else {
            Log.v(Helper.TAG, content);
        }
    }






    /**
     * Allows to split the toot by dot "." for sentences - adds number at the end automatically
     * @param content String initial content
     * @param maxChars int the max chars per toot (minus 10 to write the page: 1/x, 2/x etc.)
     * @return ArrayList<String> split toot
     */
    public static ArrayList<String> splitToots(String content, int maxChars){
        String[] splitContent = content.split("\\s");
        ArrayList<String> splitToot = new ArrayList<>();
        StringBuilder tempContent = new StringBuilder(splitContent[0]);
        ArrayList<String> mentions = new ArrayList<>();
        Matcher matcher = mentionLongPattern.matcher(content);
        while (matcher.find()) {
            String mentionLong = matcher.group(1);
            mentions.add(mentionLong);
        }
        matcher = mentionPattern.matcher(content);
        while (matcher.find()) {
            String mentionLong = matcher.group(1);
            mentions.add(mentionLong);
        }
        StringBuilder mentionString = new StringBuilder();
        for(String mention: mentions){
            mentionString.append(mention).append(" ");
        }
        int mentionLength = mentionString.length();
        int maxCharsMention = maxChars - mentionLength;
        for(int i= 0 ; i < splitContent.length ; i++){
            if (i < (splitContent.length - 1) && (countLength(tempContent.toString()) + countLength(splitContent[i + 1])) < (maxChars - 10)) {
                tempContent.append(" ").append(splitContent[i + 1]);
            } else {
                splitToot.add(tempContent.toString());
                if (i < (splitContent.length - 1)) {
                    if( maxCharsMention > 0){
                        maxChars = maxCharsMention;
                        tempContent = new StringBuilder(mentionString+splitContent[i + 1]);
                    }else{
                        tempContent = new StringBuilder(splitContent[i + 1]);
                    }
                }
            }
        }
        int i=1;
        ArrayList<String> reply = new ArrayList<>();
        for(String newContent : splitToot){
            if( splitToot.size() > 1 ) {
                reply.add((i - 1), newContent + " - " + i + "/" + splitToot.size());
            }else{
                reply.add((i - 1), newContent);
            }
            i++;
        }
        return reply;
    }



    public static int countLength(String text){
        if( text == null) {
            return 0;
        }
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA ){
            Matcher matcherALink = Patterns.WEB_URL.matcher(text);
            while (matcherALink.find()){
                int matchStart = matcherALink.start();
                int matchEnd = matcherALink.end();
                final String url = text.substring(matcherALink.start(1), matcherALink.end(1));
                if( matchEnd <= text.length() && matchEnd >= matchStart){
                    if( url.length() > 23){
                        text = text.replaceFirst(url,"abcdefghijklmnopkrstuvw");
                    }
                }
            }
        }
        return text.length() - countWithEmoji(text);
    }


    public static int countWithEmoji(String text){
        int emojiCount = 0;
        for (int i = 0; i < text.length(); i++) {
            int type = Character.getType(text.charAt(i));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                emojiCount++;
            }
        }
        return emojiCount/2;
    }

    public static boolean filterToots(Context context, Status status, List<String> timedMute, RetrieveFeedsAsyncTask.Type type){
        String filter;
        if( status == null)
            return true;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( type != null) {
            if (type == RetrieveFeedsAsyncTask.Type.HOME)
                filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_HOME, null);
            else if (type == RetrieveFeedsAsyncTask.Type.LOCAL)
                filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_LOCAL, null);
            else
                filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);
        }else {
            filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);
        }

        String content = status.getContent();
        if( status.getSpoiler_text() != null)
            content += " "+ status.getSpoiler_text();
        if( status.getAccount()  == null)
            return false;
        boolean show_nsfw = sharedpreferences.getBoolean(Helper.SET_ART_WITH_NSFW, false);
        if( type == RetrieveFeedsAsyncTask.Type.ART && !show_nsfw && status.isSensitive())
            return false;

        if(MainActivity.filters != null){
            for(Filters mfilter: filters){
                ArrayList<String> filterContext = mfilter.getContext();
                if(
                        (type == RetrieveFeedsAsyncTask.Type.HOME && filterContext.contains("home")) ||
                                (type == RetrieveFeedsAsyncTask.Type.LOCAL && filterContext.contains("public")) ||
                                (type == RetrieveFeedsAsyncTask.Type.PUBLIC && filterContext.contains("public"))

                        ) {
                    if (mfilter.isWhole_word() && content.contains(mfilter.getPhrase())) {
                        return false;
                    } else {
                        try {
                            if( mfilter.getPhrase().contains("#"))
                                mfilter.setPhrase(mfilter.getPhrase().replaceAll("\\#", "\\#"));
                            Pattern filterPattern = Pattern.compile("(" + mfilter.getPhrase() + ")", Pattern.CASE_INSENSITIVE);
                            Matcher matcher = filterPattern.matcher(content);
                            if (matcher.find())
                                return false;
                        } catch (Exception ignored) { }
                    }
                }
            }
        }
        if(filter != null && filter.length() > 0){
            try {
                Pattern filterPattern = Pattern.compile("(" + filter + ")", Pattern.CASE_INSENSITIVE);
                Matcher matcher = filterPattern.matcher(content);
                if (matcher.find())
                    return false;
            }catch (Exception ignored){ }
        }
        if (type == RetrieveFeedsAsyncTask.Type.HOME) {
            if (status.getReblog() != null && !sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true))
                return false;
            else if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().equals("null") && !sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true)) {
                return false;
            }
        } else {
            if (context instanceof ShowAccountActivity) {
                if (status.getReblog() != null && !((ShowAccountActivity) context).showBoosts())
                    return false;
                else if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().equals("null") && !((ShowAccountActivity) context).showReplies())
                    return false;
            }
        }
        if (timedMute != null && timedMute.size() > 0 && timedMute.contains(status.getAccount().getId()))
            return false;
        if( status.isNotice())
            return false;
        return true;
    }

    public static void colorizeIconMenu(Menu menu, int toolbarIconsColor) {
        final PorterDuffColorFilter colorFilter
                = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.MULTIPLY);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem v = menu.getItem(i);
            v.getIcon().setColorFilter(colorFilter);
        }
    }

    /**
     * Code from "Michal Pawlowski"
     * https://snow.dog/blog/how-to-dynamicaly-change-android-toolbar-icons-color
     * @param toolbarView toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity reference to activity needed to register observers
     */
    public static void colorizeToolbar(Toolbar toolbarView, int toolbarIconsColor, Activity activity) {
        final PorterDuffColorFilter colorFilter
                = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.SRC_ATOP);

        for(int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);

            //Step 1 : Changing the color of back button (or open drawer button).
            if(v instanceof ImageButton) {
                //Action Bar back button
                ((ImageButton)v).getDrawable().setColorFilter(colorFilter);
            }
            if(v instanceof ImageView) {
                //Action Bar back button
                if( v.getId() != R.id.pp_actionBar)
                    ((ImageView)v).setColorFilter(colorFilter);
            }
            if(v instanceof MenuItem) {
                ((MenuItem)v).getIcon().setColorFilter(colorFilter);
            }
            if(v instanceof ActionMenuView) {
                for(int j = 0; j < ((ActionMenuView)v).getChildCount(); j++) {

                    //Step 2: Changing the color of any ActionMenuViews - icons that
                    //are not back button, nor text, nor overflow menu icon.
                    final View innerView = ((ActionMenuView)v).getChildAt(j);

                    if(innerView instanceof ActionMenuItemView) {
                        int drawablesCount = ((ActionMenuItemView)innerView).getCompoundDrawables().length;
                        for(int k = 0; k < drawablesCount; k++) {
                            if(((ActionMenuItemView)innerView).getCompoundDrawables()[k] != null) {
                                final int finalK = k;

                                //Important to set the color filter in seperate thread,
                                //by adding it to the message queue
                                //Won't work otherwise.
                                innerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
                                    }
                                });
                            }
                        }
                    }
                }
            }
            if(v instanceof LinearLayout) {
                for(int j = 0; j < ((LinearLayout)v).getChildCount(); j++) {
                    //Action Bar back button
                    if (((LinearLayout) v).getChildAt(j) instanceof ImageView)
                        ((ImageView)((LinearLayout) v).getChildAt(j)).setColorFilter(colorFilter);
                }
            }
            //Step 3: Changing the color of title and subtitle.
            toolbarView.setTitleTextColor(toolbarIconsColor);
            toolbarView.setSubtitleTextColor(toolbarIconsColor);

            //Step 4: Changing the color of the Overflow Menu icon.
            setOverflowButtonColor(activity, colorFilter);
        }
    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     * @param activity
     * @param colorFilter
     */
    private static void setOverflowButtonColor(final Activity activity, final PorterDuffColorFilter colorFilter) {
        @SuppressLint("PrivateResource")
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                androidx.appcompat.widget.AppCompatImageView overflow=(androidx.appcompat.widget.AppCompatImageView) outViews.get(0);
                overflow.setColorFilter(colorFilter);
                removeOnGlobalLayoutListener(decorView,this);
            }
        });
    }

    private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
        else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }


    public static String[] getLocales(Context context){
        String[] locale = new String[20];
        locale[0] = context.getString(R.string.default_language);
        locale[1] = context.getString(R.string.english);
        locale[2] = context.getString(R.string.french);
        locale[3] = context.getString(R.string.german);
        locale[4] = context.getString(R.string.italian);
        locale[5] = context.getString(R.string.japanese);
        locale[6] = context.getString(R.string.simplified_chinese);
        locale[7] = context.getString(R.string.traditional_chinese);
        locale[8] = context.getString(R.string.basque);
        locale[9] = context.getString(R.string.arabic);
        locale[10] = context.getString(R.string.dutch);
        locale[11] = context.getString(R.string.galician);
        locale[12] = context.getString(R.string.greek);
        locale[13] = context.getString(R.string.portuguese);
        locale[14] = context.getString(R.string.spanish);
        locale[15] = context.getString(R.string.polish);
        locale[16] = context.getString(R.string.serbian);
        locale[17] = context.getString(R.string.ukrainian);
        locale[18] = context.getString(R.string.russian);
        locale[19] = context.getString(R.string.norwegian);
        return locale;
    }


    public static String getLocalesTwoChars(int stringLocaleId){
        switch (stringLocaleId){
            case R.string.default_language:
                return Locale.getDefault().getLanguage();
            case R.string.english:
                return "en";
            case R.string.french:
                return "fr";
            case R.string.arabic:
                return "ar";
            case R.string.kabyle:
                return "kab";
            case R.string.italian:
                return "it";
            case R.string.catalan:
                return "ca";
            case R.string.german:
                return "de";
            case R.string.spanish:
                return "es";
            case R.string.welsh:
                return "cy";
            case R.string.polish:
                return "pl";
            case R.string.traditional_chinese:
                return "zh-TW";
            case R.string.simplified_chinese:
                return "zh-CN";
            case R.string.basque:
                return "eu";
            case R.string.hindi:
                return "hi";
            case R.string.japanese:
                return "ja";
            case R.string.dutch:
                return "nl";
            case R.string.galician:
                return "gl";
            case R.string.greek:
                return "el";
            case R.string.portuguese:
                return "pt";
            case R.string.serbian:
                return "sr";
            case R.string.ukrainian:
                return "uk";
            case R.string.russian:
                return "ru";
            case R.string.norwegian:
                return "no";
            default:
                return Locale.getDefault().getLanguage();
        }
    }


    public static int languageSpinnerPosition(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        String defaultLocaleString = sharedpreferences.getString(Helper.SET_DEFAULT_LOCALE_NEW, "NOT_DEFINED");
        switch (defaultLocaleString){
            case "NOT_DEFINED":
                return 0;
            case "en":
                return 1;
            case "fr":
                return 2;
            case "de":
                return 3;
            case "it":
                return 4;
            case "ja":
                return 5;
            case "zh-TW":
                return 6;
            case "zh-CN":
                return 7;
            case "eu":
                return 8;
            case "ar":
                return 9;
            case "nl":
                return 10;
            case "gl":
                return 11;
            case "el":
                return 12;
            case "pt":
                return 13;
            case "es":
                return 14;
            case "pl":
                return 15;
            case "sr":
                return 16;
            case "uk":
                return 17;
            case "ru":
                return 18;
            case "no":
                return 19;
            default:
                return 0;
        }
    }

    public static boolean containsCaseInsensitive(String s, List<String> l){
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }

    public static boolean isTablet(Context context){
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static void changeMaterialSpinnerColor(Context context, MaterialSpinner materialSpinner){

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == THEME_BLACK) {
            materialSpinner.setBackgroundColor(ContextCompat.getColor(context, R.color.black_3));
            materialSpinner.setArrowColor(ContextCompat.getColor(context, R.color.dark_text));
            materialSpinner.setTextColor(ContextCompat.getColor(context, R.color.dark_text));
        }else if( theme == THEME_DARK){
            materialSpinner.setBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1));
            materialSpinner.setArrowColor(ContextCompat.getColor(context, R.color.dark_text));
            materialSpinner.setTextColor(ContextCompat.getColor(context, R.color.dark_text));
        }else {
            materialSpinner.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            materialSpinner.setArrowColor(ContextCompat.getColor(context, R.color.black));
            materialSpinner.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

    }

    public static class CacheTask extends AsyncTask<Void, Void, Void> {
        private float cacheSize;
        private WeakReference<Context> contextReference;

        public CacheTask(Context context) {
            contextReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            long sizeCache = Helper.cacheSize(contextReference.get().getCacheDir().getParentFile());
            cacheSize = 0;
            if (sizeCache > 0) {
                cacheSize = (float) sizeCache / 1000000.0f;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            int style;
            SharedPreferences sharedpreferences = contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            if (theme == Helper.THEME_DARK) {
                style = R.style.DialogDark;
            } else if (theme == Helper.THEME_BLACK) {
                style = R.style.DialogBlack;
            } else {
                style = R.style.Dialog;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(contextReference.get(), style);
            builder.setTitle(R.string.cache_title);

            final float finalCacheSize = cacheSize;
            builder.setMessage(contextReference.get().getString(R.string.cache_message, String.format("%s %s", String.format(Locale.getDefault(), "%.2f", cacheSize), contextReference.get().getString(R.string.cache_units))))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String path = contextReference.get().getCacheDir().getParentFile().getPath();
                                        File dir = new File(path);
                                        if (dir.isDirectory()) {
                                            Helper.deleteDir(dir);
                                        }
                                        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                        new StatusCacheDAO(contextReference.get(), db).removeDuplicate();
                                        Date date = new Date( System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
                                        String dateString = Helper.dateToString(date);
                                        new TimelineCacheDAO(contextReference.get(), db).removeAfterDate(dateString);
                                    } catch (Exception ignored) {
                                    }
                                }
                            });
                            Toasty.success(contextReference.get(), contextReference.get().getString(R.string.toast_cache_clear, String.format("%s %s", String.format(Locale.getDefault(), "%.2f", finalCacheSize), contextReference.get().getString(R.string.cache_units))), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }



    public static String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(context.getCacheDir() + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * List from ClearUrls
     * https://gitlab.com/KevinRoebert/ClearUrls/blob/master/data/data.min.json#L106
     */
    private static final String[] UTM_PARAMS = {
            "utm_\\w+",
            "ga_source",
            "ga_medium",
            "ga_term",
            "ga_content",
            "ga_campaign",
            "ga_place",
            "yclid",
            "_openstat",
            "fb_action_ids",
            "fb_action_types",
            "fb_source",
            "fb_ref",
            "fbclid",
            "action_object_map",
            "action_type_map",
            "action_ref_map",
            "gs_l",
            "mkt_tok",
            "hmb_campaign",
            "hmb_medium",
            "hmb_source",
            "[\\?|&]ref[\\_]?"

    };

    public static String remove_tracking_param(String original_content){
        if( original_content == null)
            return original_content;
        String cleaned_content = original_content;
        for(String utm: UTM_PARAMS){
            cleaned_content = cleaned_content.replaceAll("&amp;"+utm+"=[0-9a-zA-Z._-]*", "");
            cleaned_content = cleaned_content.replaceAll("&"+utm+"=[0-9a-zA-Z._-]*", "");
            cleaned_content = cleaned_content.replaceAll("\\?"+utm+"=[0-9a-zA-Z._-]*", "?");
        }
        return cleaned_content;
    }


    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Objects.equals(value, entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }


    public static void download(Context context, String file, String url){


        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(3)
                .build();
        Fetch fetch = Fetch.Impl.getInstance(fetchConfiguration);
        FetchListener fetchListener = new FetchListener() {
            @Override
            public void onWaitingNetwork(@NotNull Download download) {
            }
            @Override
            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
            }
            @Override
            public void onResumed(@NotNull Download download) {
            }
            @Override
            public void onRemoved(@NotNull Download download) {
            }
            @Override
            public void onQueued(@NotNull Download download, boolean b) {
            }
            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {
            }
            @Override
            public void onPaused(@NotNull Download download) {
            }
            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @org.jetbrains.annotations.Nullable Throwable throwable) {
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            }
            @Override
            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {
            }
            @Override
            public void onDeleted(@NotNull Download download) {
            }
            @Override
            public void onCompleted(@NotNull Download download) {
                if( download.getFileUri().getPath() != null) {
                    String url = download.getUrl();
                    final String fileName = URLUtil.guessFileName(url, null, null);
                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    String mime = Helper.getMimeType(url);
                    File file = new File(download.getFileUri().getPath());
                    final Intent intent = new Intent();
                    Random r = new Random();
                    final int notificationIdTmp = r.nextInt(10000);
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("file://" + file.getAbsolutePath() );
                    intent.setDataAndType(uri, mime);
                    Helper.notify_user(context, intent, notificationIdTmp, BitmapFactory.decodeResource(context.getResources(),
                            R.mipmap.ic_launcher),  Helper.NotifType.STORE, context.getString(R.string.save_over), context.getString(R.string.download_from, fileName));
                }
            }
            @Override
            public void onCancelled(@NotNull Download download) {
            }
            @Override
            public void onAdded(@NotNull Download download) {
            }
        };

        fetch.addListener(fetchListener);
        final Request request = new Request(url, file);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        fetch.enqueue(request, updatedRequest -> {
            //Request was successfully enqueued for download.
        }, error -> {
        });


    }


    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public static volatile boolean orbotConnected = false;

    public static void initNetCipher(Context context) {
        Context appContext = context.getApplicationContext();

        try {
            StrongOkHttpClientBuilder.forMaxSecurity(appContext).build(new StrongBuilder.Callback<OkHttpClient>() {
                @Override
                public void onConnected(OkHttpClient okHttpClient) {
                    UploadService.HTTP_STACK = new OkHttpStack(getHttpClient(context));
                    orbotConnected = true;
                }

                @Override
                public void onConnectionException(Exception exc) {
                    orbotConnected = false;
                }

                @Override
                public void onTimeout() {
                    orbotConnected = false;
                }

                @Override
                public void onInvalid() {
                    orbotConnected = false;
                }
            });
        } catch (Exception ignored) { }
    }


    public static OkHttpClient getHttpClient(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(null);
        return enableTls12OnPreLollipop(clientBuilder).build();
    }

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception ignored) {}
        }

        return client;
    }


    public static void exportInstanceBlock(Context context, String username){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String backupDBPath = "instance_mute_"+timeStamp+".csv";
        File sd = Environment.getExternalStorageDirectory();
        final String filename = sd + "/" + backupDBPath;


        new AsyncTask<Void, Void, List<String>>() {
            private WeakReference<Context> contextReference = new WeakReference<>(context);
            APIResponse apiResponse;

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected List<String> doInBackground(Void... voids) {
                apiResponse = new API(contextReference.get()).getBlockedDomain(null);
                return apiResponse.getDomains();
            }
            @Override
            protected void onPostExecute( List<String> domains) {
                if( domains == null){
                    return;
                }
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK){
                    style = R.style.DialogBlack;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                LayoutInflater inflater = ((MutedInstanceActivity)context).getLayoutInflater();
                ScrollView scrollView = new ScrollView(context);
                LinearLayout linearLayout_main = new LinearLayout(context);

                linearLayout_main.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout_main.setOrientation(LinearLayout.VERTICAL);

                for(String domain: domains){
                    @SuppressLint("InflateParams") View item = inflater.inflate(R.layout.muted_instance_popup, null);
                    CheckBox checkBox = item.findViewById(R.id.popup_domain);
                    checkBox.setText(domain);
                    linearLayout_main.addView(item);
                }
                scrollView.addView(linearLayout_main);
                dialogBuilder.setView(scrollView);
                HashMap<String, String> domainBlocked = new HashMap<>();
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for( int i = 0; i < linearLayout_main.getChildCount() ; i++){
                            LinearLayout linearLayout = (LinearLayout) linearLayout_main.getChildAt(i);
                            CheckBox checkBox = linearLayout.findViewById(R.id.popup_domain);
                            EditText editText = linearLayout.findViewById(R.id.popup_comment);
                            if( checkBox.isChecked()){
                                String comment = null;
                                String domain = checkBox.getText().toString().trim();
                                if(  editText.getText() != null) {
                                    comment = editText.getText().toString().trim();
                                }
                                domainBlocked.put(domain, comment);
                            }
                        }
                        if( domainBlocked.size() > 0 ){
                            new Thread() {
                                public void run() {
                                    try {

                                        FileWriter fw = new FileWriter(filename);
                                        fw.append("INSTANCE");
                                        fw.append(',');
                                        fw.append("COMMENT");
                                        fw.append('\n');
                                        Iterator it = domainBlocked.entrySet().iterator();
                                        while (it.hasNext()) {
                                            Map.Entry pair = (Map.Entry)it.next();
                                            fw.append((String)pair.getKey());
                                            fw.append(',');
                                            fw.append((String)pair.getValue());
                                            fw.append('\n');
                                            it.remove();
                                        }
                                        fw.close();
                                    } catch (Exception ignored) {
                                    }
                                    final Intent intent = new Intent();
                                    Random r = new Random();
                                    final int notificationIdTmp = r.nextInt(10000);
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    Uri uri = Uri.fromFile(new File(filename));
                                    intent.setDataAndType(uri, "text/csv");
                                    Helper.notify_user(context, intent, notificationIdTmp, BitmapFactory.decodeResource(context.getResources(),
                                            R.mipmap.ic_launcher),  Helper.NotifType.STORE, context.getString(R.string.muted_instance_exported), context.getString(R.string.download_from, backupDBPath));
                                }
                            }.start();
                        }
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
    }



    public static void importInstanceBlock(Context context, HashMap<String, String> instances){


        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
        LayoutInflater inflater = ((MutedInstanceActivity)context).getLayoutInflater();
        ScrollView scrollView = new ScrollView(context);
        LinearLayout linearLayout_main = new LinearLayout(context);

        linearLayout_main.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout_main.setOrientation(LinearLayout.VERTICAL);
        Iterator it = instances.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            @SuppressLint("InflateParams") View item = inflater.inflate(R.layout.muted_instance_popup, null);
            CheckBox checkBox = item.findViewById(R.id.popup_domain);
            checkBox.setText(pair.getKey().toString().trim());
            item.findViewById(R.id.popup_comment).setVisibility(View.GONE);
            if( pair.getValue() != null) {
                TextView textView = item.findViewById(R.id.comment_text);
                textView.setText(pair.getValue().toString());
                textView.setVisibility(View.VISIBLE);
            }
            linearLayout_main.addView(item);
            it.remove();
        }
        scrollView.addView(linearLayout_main);
        dialogBuilder.setView(scrollView);
        HashMap<String, String> domainBlocked = new HashMap<>();
        dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                for( int i = 0; i < linearLayout_main.getChildCount() ; i++){
                    LinearLayout linearLayout = (LinearLayout) linearLayout_main.getChildAt(i);
                    CheckBox checkBox = linearLayout.findViewById(R.id.popup_domain);
                    if( checkBox.isChecked()){
                        String domain = checkBox.getText().toString().trim();
                        domainBlocked.put(domain, null);
                    }
                }
                if( domainBlocked.size() > 0 ){
                    new Thread() {
                        public void run() {
                            Iterator it = domainBlocked.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry)it.next();
                                try {
                                    sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                new PostActionAsyncTask(context, API.StatusAction.BLOCK_DOMAIN, pair.getKey().toString().trim(), ((MutedInstanceActivity)context)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                it.remove();
                            }
                        }
                    }.start();
                }
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        new AsyncTask<Void, Void, List<String>>() {
            private WeakReference<Context> contextReference = new WeakReference<>(context);
            APIResponse apiResponse;

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected List<String> doInBackground(Void... voids) {
                apiResponse = new API(contextReference.get()).getBlockedDomain(null);
                return apiResponse.getDomains();
            }
            @Override
            protected void onPostExecute( List<String> domains) {
                if( domains == null){
                    return;
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );


    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
