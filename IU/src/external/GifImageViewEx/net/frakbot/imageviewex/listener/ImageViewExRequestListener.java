
package external.GifImageViewEx.net.frakbot.imageviewex.listener;

import external.GifImageViewEx.com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import external.GifImageViewEx.net.frakbot.imageviewex.ImageViewNext;

public abstract class ImageViewExRequestListener implements RequestListener {
    protected ImageViewNext mImageViewNext;

    public ImageViewExRequestListener(ImageViewNext imageViewNext) {
        this.mImageViewNext = imageViewNext;
    }
}
