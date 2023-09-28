package com.screen.record;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ScreenRecordService extends Service {

    private int resultCode;
    private Intent resultData = null;

    private MediaProjection mediaProjection = null;
    private MediaRecorder mediaRecorder = null;
    private VirtualDisplay virtualDisplay = null;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    private Context context = null;

    public static void start(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, ScreenRecordService.class));
        } else {
            context.startService(new Intent(context, ScreenRecordService.class));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling startService(Intent),
     * providing the arguments it supplied and a unique integer token representing the start request.
     * Do not call this method directly.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            createNotificationChannel();
            resultCode = intent.getIntExtra("resultCode", -1);
            resultData = intent.getParcelableExtra("resultData");
            mScreenWidth = intent.getIntExtra("mScreenWidth", 0);
            mScreenHeight = intent.getIntExtra("mScreenHeight", 0);
            mScreenDensity = intent.getIntExtra("mScreenDensity", 0);

            mediaProjection = createMediaProjection();
            mediaRecorder = createMediaRecorder();
            virtualDisplay = createVirtualDisplay();
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * START_NOT_STICKY:
         * Constant to return from onStartCommand(Intent, int, int): if this service's process is
         * killed while it is started (after returning from onStartCommand(Intent, int, int)),
         * and there are no new start intents to deliver to it, then take the service out of the
         * started state and don't recreate until a future explicit call to Context.startService(Intent).
         * The service will not receive a onStartCommand(Intent, int, int) call with a null Intent
         * because it will not be re-started if there are no pending Intents to deliver.
         */
        return Service.START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class); //点击后跳转的界面，可以设置跳转数据

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("is running......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);

    }

    //createMediaProjection
    public MediaProjection createMediaProjection() {
        /**
         * Use with getSystemService(Class) to retrieve a MediaProjectionManager instance for
         * managing media projection sessions.
         */
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(resultCode, resultData);
        /**
         * Retrieve the MediaProjection obtained from a succesful screen capture request.
         * Will be null if the result from the startActivityForResult() is anything other than RESULT_OK.
         */
    }

    private MediaRecorder createMediaRecorder() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YY-MM-DD-HHMMSS");
        String filePathName = Environment.getExternalStorageDirectory() + "/video" + simpleDateFormat.format(new Date()) + ".mp4";
        //Used to record audio and video. The recording control is based on a simple state machine.
        MediaRecorder mediaRecorder = new MediaRecorder();
        //Set the video source to be used for recording.
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //Set the format of the output produced during recording.
        //3GPP media file format
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //Sets the video encoding bit rate for recording.
        //param:the video encoding bit rate in bits per second.
        mediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
        //Sets the video encoder to be used for recording.
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //Sets the width and height of the video to be captured.
        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
        //Sets the frame rate of the video to be captured.
        mediaRecorder.setVideoFrameRate(60);
        try {
            //Pass in the file object to be written.
            mediaRecorder.setOutputFile(filePathName);
            //Prepares the recorder to begin capturing and encoding data.
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaRecorder;
    }

    private VirtualDisplay createVirtualDisplay() {
        /**
         * name	String: The name of the virtual display, must be non-empty.This value must never be null.
         width	int: The width of the virtual display in pixels. Must be greater than 0.
         height	int: The height of the virtual display in pixels. Must be greater than 0.
         dpi	int: The density of the virtual display in dpi. Must be greater than 0.
         flags	int: A combination of virtual display flags. See DisplayManager for the full list of flags.
         surface	Surface: The surface to which the content of the virtual display should be rendered, or null if there is none initially.
         callback	VirtualDisplay.Callback: Callback to call when the virtual display's state changes, or null if none.
         handler	Handler: The Handler on which the callback should be invoked, or null if the callback should be invoked on the calling thread's main Looper.
         */
        /**
         * DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
         * Virtual display flag: Allows content to be mirrored on private displays when no content is being shown.
         */
        return mediaProjection.createVirtualDisplay("mediaProjection", mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}