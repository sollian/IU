
package com.aiyou.bbs.PullRefer;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.bbs.BBSListActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationMgr {
    public enum NotifyType {
        REPLY(0x1124, "reply"), // 回复我的消息
        AT(0x1125, "at"), // @我的消息
        MAIL(0x1126, "mail");// 新邮件

        private int mId;
        private String mTag;

        private NotifyType(int id, String tag) {
            mId = id;
            mTag = tag;
        }

        public int getId() {
            return mId;
        }

        public String getTag() {
            return mTag;
        }
    }

    private NotificationManager mNotificationMgr;
    private static NotificationMgr mInstance;

    private NotificationMgr() {
        mNotificationMgr = (NotificationManager) AiYouApplication.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationMgr getInstance() {
        if (mInstance == null) {
            mInstance = new NotificationMgr();
        }
        return mInstance;
    }

    @SuppressWarnings("deprecation")
    public void sendNotification(Context context, NotifyType type) {
        Intent intent = new Intent(context, BBSListActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        boolean reply = false, at = false, mail = false;
        switch (type) {
            case AT:
                at = true;
                break;
            case MAIL:
                mail = true;
                break;
            case REPLY:
                reply = true;
                break;
            default:
                break;

        }
        intent.putExtra(NotifyType.REPLY.getTag(), reply);
        intent.putExtra(NotifyType.AT.getTag(), at);
        intent.putExtra(NotifyType.MAIL.getTag(), mail);

        Notification BBSNotification = new Notification();
        BBSNotification.icon = R.drawable.ic_launcher;
        BBSNotification.tickerText = context
                .getString(R.string.app_name);
        BBSNotification.when = System.currentTimeMillis();
        BBSNotification.defaults = Notification.DEFAULT_SOUND;
        BBSNotification.defaults |= Notification.DEFAULT_VIBRATE;
        BBSNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        PendingIntent mobilePi = PendingIntent.getActivity(context, type.getId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (type == NotifyType.REPLY) {
            BBSNotification.setLatestEventInfo(context, "论坛消息",
                    "新回复我的文章", mobilePi);
        } else if (type == NotifyType.AT) {
            BBSNotification.setLatestEventInfo(context, "论坛消息",
                    "新@我的文章", mobilePi);
        } else if (type == NotifyType.MAIL) {
            BBSNotification.setLatestEventInfo(context, "论坛消息", "新邮件",
                    mobilePi);
        }
        mNotificationMgr.notify(type.getId(), BBSNotification);
    }

    public void cancel(NotifyType type) {
        mNotificationMgr.cancel(type.getId());
    }
}
