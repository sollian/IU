
package com.aiyou.bbs.PullRefer;

import com.aiyou.bbs.PullRefer.NotificationMgr.NotifyType;
import com.aiyou.bbs.bean.Mailbox;
import com.aiyou.bbs.bean.Refer;
import com.aiyou.bbs.bean.Refer.ReferType;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * 检查是否有新邮件、新回复、新@的服务
 * 
 * @author sollian
 */
public class BBSService extends Service {
    private static final String TAG = Service.class.getSimpleName();

    public static final String KEY_TYPE = "type";
    public static final String KEY_NEW_COUNT = "newCount";

    private static final int MSG_REPLY = 0;
    private static final int MSG_AT = 1;
    private static final int MSG_MAIL = 2;
    private static final int MSG_NO_REPLY = 3;
    private static final int MSG_NO_AT = 4;
    private static final int MSG_NO_MAIL = 5;
    private static final int MSG_ERROR = -1;

    private NotificationMgr mNotificationMgr;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_REPLY) {
                mNotificationMgr.sendNotification(getBaseContext(), NotifyType.REPLY);
            } else if (msg.what == MSG_AT) {
                mNotificationMgr.sendNotification(getBaseContext(), NotifyType.AT);
            } else if (msg.what == MSG_MAIL) {
                mNotificationMgr.sendNotification(getBaseContext(), NotifyType.MAIL);
            } else if (msg.what == MSG_NO_REPLY) {
                mNotificationMgr.cancel(NotifyType.REPLY);
            } else if (msg.what == MSG_NO_AT) {
                mNotificationMgr.cancel(NotifyType.AT);
            } else if (msg.what == MSG_NO_MAIL) {
                mNotificationMgr.cancel(NotifyType.MAIL);
            } else if (msg.what == MSG_ERROR) {
                Logcat.e(TAG, "handleMessage MSG_ERROR");
            }
            return true;
        }
    });

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotificationMgr = NotificationMgr.getInstance();
        checkNewMessage();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void checkNewMessage() {
        // 检查是否是访客身份
        if (BBSManager.GUEST.equals(
                BBSManager.getInstance(getBaseContext()).getUserId())) {
            return;
        }
        // 是否有网络连接
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new ReplyRunnable());
        ThreadUtils.execute(new AtRunnable());
        ThreadUtils.execute(new MailRunnable());
    }

    private class ReplyRunnable implements Runnable {
        @Override
        public void run() {
            // 论坛回复
            String strJson = Refer.getReferInfo(BBSService.this, ReferType.REPLY);
            if (strJson == null) {
                return;
            }
            Refer refer = new Refer(strJson);
            if (refer.new_count > 0) {
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_REPLY);
                    // 设置
                    BBSManager.getInstance(BBSService.this).setBBSNotificationRefer(
                            ReferType.REPLY,
                            refer.new_count);
                    // 发送特定action的广播
                    Intent intent = new Intent();
                    intent.setAction(BBSManager.REFER_RECEIVER_ACTION);
                    intent.putExtra(KEY_NEW_COUNT, refer.new_count);
                    intent.putExtra(KEY_TYPE, NotifyType.REPLY);
                    sendBroadcast(intent);
                }
            } else {
                if (mHandler != null) {
                    BBSManager.getInstance(BBSService.this).setBBSNotificationRefer(
                            ReferType.REPLY,
                            0);
                    mHandler.sendEmptyMessage(MSG_NO_REPLY);
                }
            }
        }
    }

    private class AtRunnable implements Runnable {
        @Override
        public void run() {
            // 论坛@我
            String strJson = Refer.getReferInfo(BBSService.this, ReferType.AT);
            if (strJson == null) {
                return;
            }
            Refer refer = new Refer(strJson);
            if (refer.new_count > 0) {
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_AT);
                    // 设置
                    BBSManager.getInstance(BBSService.this).setBBSNotificationRefer(
                            ReferType.AT,
                            refer.new_count);
                    // 发送特定action的广播
                    Intent intent = new Intent();
                    intent.setAction(BBSManager.REFER_RECEIVER_ACTION);
                    intent.putExtra(KEY_NEW_COUNT, refer.new_count);
                    intent.putExtra(KEY_TYPE, NotifyType.AT);
                    sendBroadcast(intent);
                }
            } else {
                if (null != mHandler) {
                    BBSManager.getInstance(BBSService.this).setBBSNotificationRefer(
                            ReferType.AT,
                            0);
                    mHandler.sendEmptyMessage(MSG_NO_AT);
                }
            }
        }
    }

    private class MailRunnable implements Runnable {
        @Override
        public void run() {
            // 论坛新邮件
            String strJson = Mailbox.getMailBoxInfo(BBSService.this);
            if (strJson == null) {
                return;
            }
            Mailbox mailbox = new Mailbox(strJson);
            if (mailbox.new_mail) {
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_MAIL);
                    // 设置
                    BBSManager.getInstance(BBSService.this).setBBSNotificationMail(true);
                    // 发送特定action的广播
                    Intent intent = new Intent();
                    intent.setAction(BBSManager.REFER_RECEIVER_ACTION);
                    intent.putExtra(KEY_TYPE, NotifyType.MAIL);
                    sendBroadcast(intent);
                }
            } else {
                if (null != mHandler) {
                    BBSManager.getInstance(BBSService.this).setBBSNotificationMail(false);
                    mHandler.sendEmptyMessage(MSG_NO_MAIL);
                }
            }
        }
    }
}
