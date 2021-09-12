package com.cnl.orientation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import static android.view.WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;


public class MainService extends Service {

    private static final String ACTION_KEY = "Orientation";
    private static final String TAG = "MainService";
    private static final String channelId = "channel_service";
    private static final String channelName = "前台服务通知渠道";
    private static final int ID = 0xFFEEDD;
    public static boolean isRunning;

    private View view;
    private WindowManager manager;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        creat();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
            builder.setContent(createRemoteViews());
            builder.setOngoing(true);
            builder.setSmallIcon(android.R.drawable.ic_menu_rotate);
            builder.setWhen(System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(channelId);
            }
            startForeground(ID, builder.build());
            isRunning = true;
        }
        if (intent != null && intent.hasExtra(ACTION_KEY)) {
            params.screenOrientation = intent.getIntExtra(ACTION_KEY, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            manager.updateViewLayout(view, params);
            Log.d(TAG, "screenOrientation: " + params.screenOrientation);
        }
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    private RemoteViews createRemoteViews() {
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(ACTION_KEY, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        PendingIntent landscape = PendingIntent.getService(this, 0x123, intent, 0);
        Intent intent2 = new Intent(this, MainService.class);
        intent2.putExtra(ACTION_KEY, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PendingIntent portrait = PendingIntent.getService(this, 0x456, intent2, 0);

        RemoteViews remote = new RemoteViews(getPackageName(), R.layout.service_main);
        remote.setOnClickPendingIntent(R.id.landscape, landscape);
        remote.setOnClickPendingIntent(R.id.portrait, portrait);
        return remote;
    }

    private void creat() {
        int type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        view = new View(getApplicationContext());
        params = new WindowManager.LayoutParams(
                1, 1, type,
                FLAG_LOCAL_FOCUS_MODE |  //0x‭1080338‬
                        FLAG_SHOW_WHEN_LOCKED |
                        SOFT_INPUT_ADJUST_NOTHING |
                        FLAG_NOT_TOUCHABLE |
                        FLAG_NOT_TOUCH_MODAL |
                        FLAG_NOT_FOCUSABLE, PixelFormat.OPAQUE);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = -1;
        params.y = -1;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        manager.addView(view, params);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
    }
}
