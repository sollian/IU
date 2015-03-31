
package external.SmartImageView;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class SmartImageTask implements Runnable {
    private static final int BITMAP_READY = 0;

    private boolean cancelled = false;
    private OnCompleteHandler onCompleteHandler;
    private SmartImage image;
    private Context context;
    private Map<String, String> mHeader = null;

    public static class OnCompleteHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            onComplete(bitmap);
        }

        public void onComplete(Bitmap bitmap) {
        };
    }

    public SmartImageTask(Context context, SmartImage image) {
        this(context, image, null);
    }
    
    public SmartImageTask(Context context, SmartImage image, Map<String, String> header) {
        this.image = image;
        this.context = context;
        this.mHeader = header;
    }

    @Override
    public void run() {
        if (image != null) {
            complete(image.getBitmap(context, mHeader));
            context = null;
        }
    }

    public void setOnCompleteHandler(OnCompleteHandler handler) {
        this.onCompleteHandler = handler;
    }

    public void cancel() {
        cancelled = true;
    }

    private void complete(Bitmap bitmap) {
        if (onCompleteHandler != null && !cancelled) {
            onCompleteHandler.sendMessage(onCompleteHandler.obtainMessage(
                    BITMAP_READY, bitmap));
        }
    }

    public interface OnCompleteListener {
        public void onComplete();
    }
}
