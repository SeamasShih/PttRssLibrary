package com.seamas.pttrsslibrary;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AlarmReceiver extends BroadcastReceiver {
    private Context context;
    private Intent intent;
    private PttRssSP pttRssSP;
    private ArrayList<Article> articles = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        String s = intent.getStringExtra("msg");

        if (s != null && s.equals("rss_check")) {
            this.context = context;
            this.intent = intent;
            pttRssSP = PttRssUtils.getPttRssSP(context);

            questRss();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, PttRssUtils.getPttRssSP(context).getTime());

            Intent i = new Intent(context, AlarmReceiver.class);
            i.setAction("com.seamas.START_PTT");
            i.putExtra("msg", "rss_check");

            PendingIntent pi = PendingIntent.getBroadcast(context, 1, i, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
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
        String urlString = "https://www.ptt.cc/atom/" + pttRssSP.getBroad() + ".xml";

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
            for (int i = 0 ; i < articleList.getLength() ; i++) {
                Element newestArticle = (Element) articleList.item(i);
                Article article = new Article();
                article.setTitle(newestArticle.getElementsByTagName(PttRssLabel.TITLE).item(0).getTextContent());
                article.setAddress(newestArticle.getElementsByTagName(PttRssLabel.ID).item(0).getTextContent());
                String content = newestArticle.getElementsByTagName(PttRssLabel.CONTENT).item(0).getTextContent();
                content = content.substring(5, content.length() - 6);
                article.setContent(content);
                Element author = (Element) newestArticle.getElementsByTagName(PttRssLabel.AUTHOR).item(0);
                article.setAuthor(author.getElementsByTagName(PttRssLabel.NAME).item(0).getTextContent());

                articles.add(article);
            }

            createNotification(articles.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNewest(Article article) {
        boolean r = article.getTitle().contains(pttRssSP.getFilter()) && !article.getTitle().equals(pttRssSP.getTitle());
        if (r)
            PttRssUtils.refresh(context, pttRssSP.getBroad(), article.getTitle());
        return r;
    }

    private void createNotification(Article article) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent it = new Intent(context, FloatingService.class);
        it.putExtra("Articles", articles);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);

        String text = null;
        String[] strings = article.getAddress().split("/");
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(PttRssLabel.BBS)) {
                text = strings[i + 1];
            }
        }

        if (!isNewest(article))
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
