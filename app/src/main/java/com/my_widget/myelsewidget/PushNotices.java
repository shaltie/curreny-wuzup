package com.my_widget.myelsewidget;

import java.util.concurrent.TimeUnit;

        import android.app.Notification;
        import android.app.NotificationManager;
import android.app.Service;
        import android.content.Intent;
        import android.os.IBinder;
import android.util.Log;

public class PushNotices extends Service {
    NotificationManager nm;

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendNotif();
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotif() {

        try {
            // 1-я часть
            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!");



            // 3-я часть
            //Intent intent = new Intent(this, MainActivity.class);
            //intent.putExtra(MainActivity.FILE_NAME, "somefile");
            //PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // 2-я часть
            // notif.setLatestEventInfo(this, "Notification's title", "Notification's text", pIntent);

            // ставим флаг, чтобы уведомление пропало после нажатия
            //notif.flags |= Notification.FLAG_AUTO_CANCEL;

            // отправляем
            //nm.notify(1, notif);
        } catch (Exception e) {
            Log.e("Notice error", e.toString());
        }

    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
