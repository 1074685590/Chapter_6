package com.ryg.chapter_6;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;

import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private String              mDownloadUrl;//APK的下载路径
    private NotificationManager mNotificationManager;
    private Notification        mNotification;


    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            notifyMsg("温馨提醒", "文件下载失败", 0);
            stopSelf();
        }
        mDownloadUrl = intent.getStringExtra("apkUrl");//获取下载APK的链接
        downloadFile(mDownloadUrl);//下载APK
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyMsg(String title, String content, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);//为了向下兼容，这里采用了v7包下的NotificationCompat来构造
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(title);
        if (progress > 0 && progress < 100) {
            //下载进行中
            builder.setProgress(100, progress, false);
        } else {
            builder.setProgress(0, 0, false);
        }
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(content);
        if (progress >= 100) {
            //下载完成
            builder.setContentIntent(getInstallIntent());
        }
        mNotification = builder.build();
        mNotificationManager.notify(0, mNotification);
    }

    /**
     * 安装apk文件
     *
     * @return
     */
    private PendingIntent getInstallIntent() {
        File downloadFile = new File(Environment.getExternalStorageDirectory(), "download");
        File file = new File(downloadFile, getNameFromUrl(mDownloadUrl));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    /**
     * 下载apk文件
     */
    private void downloadFile(String downloadUrl) {
        DownloadUtil.get().download(downloadUrl, "download", new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                notifyMsg("温馨提醒", "文件下载已完成", 100);
                Log.d(TAG, "onDownloadSuccess: 线程名称"+Thread.currentThread().getName());
//                showDialog();
                stopSelf();
            }

            @Override
            public void onDownloading(int progress) {
                if (progress % 10 == 0) {
                    //避免频繁刷新View，这里设置每下载10%提醒更新一次进度
                    notifyMsg("温馨提醒", "文件正在下载..", progress);
                }
            }

            @Override
            public void onDownloadFailed() {
                notifyMsg("温馨提醒", "文件下载失败", 0);
                stopSelf();
            }
        });

    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setMessage("应用已经更新完成,请点击更新？");
        alertDialog.setPositiveButton("下次",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

        alertDialog.setNegativeButton("安装",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        File downloadFile = new File(Environment.getExternalStorageDirectory(), "download");
                        File file = new File(downloadFile, getNameFromUrl(mDownloadUrl));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                });

        AlertDialog ad = alertDialog.create();
        //在dialog show 方法之前添加这个代码，表示该dialog是系统的dialog。
        ad.getWindow().setType(TYPE_SYSTEM_ALERT);
        ad.setCanceledOnTouchOutside(false);//点击外面区域不会让dialog消失
        ad.show();
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}