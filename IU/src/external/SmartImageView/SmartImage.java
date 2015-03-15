
package external.SmartImageView;

import android.content.Context;
import android.graphics.Bitmap;

public interface SmartImage {
    public Bitmap getBitmap(Context context);

    public String getUrl();
}
