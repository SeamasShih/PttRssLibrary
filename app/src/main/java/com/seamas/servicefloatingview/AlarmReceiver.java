package com.seamas.servicefloatingview;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AlarmReceiver extends BroadcastReceiver {
    private Context context;
    private Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String s = intent.getStringExtra("msg");

        Log.d("Seamas", "onReceive");

        if (s != null && s.equals("rss_check")) {
            this.context = context;
            this.intent = intent;

            questRss();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);

            Intent i = new Intent(context, AlarmReceiver.class);
            i.setAction("com.seamas.START_PTT");
            i.putExtra("msg", "rss_check");

            PendingIntent pi = PendingIntent.getBroadcast(context, 1, i, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            Log.d("Seamas", "setAlarm");
        }
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

    private void questRss() {
//        String urlString = "https://www.ptt.cc/atom/AllTogether.xml";
        String urlString = "https://www.ptt.cc/atom/Gossiping.xml";

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(6 * 1000);
            new Thread(() -> {
                try {
                    urlConn.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (urlConn.getResponseCode() == 200) {
                        readRssData(urlConn.getInputStream());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                urlConn.disconnect();
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readRssData(InputStream is) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            NodeList articleList = doc.getElementsByTagName(PttRssLabel.ENTRY);
            Element newestArticle = (Element) articleList.item(0);
            Article article = new Article();
            article.setTitle(newestArticle.getElementsByTagName(PttRssLabel.TITLE).item(0).getTextContent());
            article.setAddress(newestArticle.getElementsByTagName(PttRssLabel.ID).item(0).getTextContent());
            String content = newestArticle.getElementsByTagName(PttRssLabel.CONTENT).item(0).getTextContent();
            content = content.substring(5, content.length() - 6);
            article.setContent(content);
            Element author = (Element) newestArticle.getElementsByTagName(PttRssLabel.AUTHOR).item(0);
            article.setAuthor(author.getElementsByTagName(PttRssLabel.NAME).item(0).getTextContent());

            createNotification(article);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNewest(Article article, String s) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(s, Context.MODE_PRIVATE);
        boolean r = !article.getTitle().equals(sharedPreferences.getString("Title", ""));
        if (r)
            sharedPreferences.edit().putString("Title", article.getTitle()).apply();
        return r;
    }

    private void createNotification(Article article) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent it = new Intent(context, MainActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, it, 0);

        String text = null;
        String[] strings = article.getAddress().split("/");
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(PttRssLabel.BBS)) {
                text = strings[i + 1];
            }
        }

        if (!isNewest(article, text))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder;
            builder =
                    new NotificationCompat.Builder(context, "CHANNEL")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(article.getTitle())
                            .setContentText(article.getAuthor() + " - " + text)
                            .setChannelId("CHANNEL")
                            .setGroupSummary(true)
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

            CharSequence name = "FxcCalendar";
            String description = "FxcCalendarNotification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL", name, importance);
            channel.setDescription(description);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, builder.build());
        } else {
            Notification.Builder builder;
            builder =
                    new Notification.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(article.getTitle())
                            .setContentText(article.getAuthor() + " - " + text)
                            .setWhen(System.currentTimeMillis())
                            .setGroupSummary(true)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);
            assert notificationManager != null;
            notificationManager.notify(1, builder.build());
        }
    }
}
