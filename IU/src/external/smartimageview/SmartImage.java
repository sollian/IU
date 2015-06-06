
package external.smartimageview;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

public interface SmartImage {
    public Bitmap getBitmap(Context context, Map<String, String> header);

    public String getUrl();
}
