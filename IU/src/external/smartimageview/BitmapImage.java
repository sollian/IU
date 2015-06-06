
package external.smartimageview;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

public class BitmapImage implements SmartImage {
    private Bitmap mBitmap;

    public BitmapImage(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public Bitmap getBitmap(Context context, Map<String, String> header) {
        return mBitmap;
    }

    @Override
    public String getUrl() {
        return null;
    }
}
