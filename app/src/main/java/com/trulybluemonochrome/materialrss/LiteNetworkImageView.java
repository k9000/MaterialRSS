package com.trulybluemonochrome.materialrss;

import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.NetworkImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.volley.toolbox.ImageLoader;


public class LiteNetworkImageView extends ImageView {
    /**
     * The URL of the network image to load
     */
    private String mUrl;
    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int mDefaultImageId;
    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;
    /**
     * Local copy of the ImageLoader.
     */
    private int mWidth;
    private ImageLoader mImageLoader;

    public LiteNetworkImageView(Context context) {
        this(context, null);
    }

    public LiteNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiteNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
     * <p>
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)} and
     * {@link NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url         The URL that should be loaded into this ImageView.
     * @param imageLoader ImageLoader that will be used to make the request.
     */
    public void setImageUrl(String url, ImageLoader imageLoader, int maxWidth) {
        mUrl = url;
        mImageLoader = imageLoader;
        mWidth = maxWidth;
        //Log.d("image","setImageUrl");
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary();
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage) {
        mDefaultImageId = defaultImage;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     */
    private void loadImageIfNecessary() {
        //final int width = getWidth();
        //Log.d("image",String.valueOf(mWidth));

        //final int height = getHeight();
        // if the view's bounds aren't known yet, hold off on loading the image.

        if (mWidth == 0) {
            setImageBitmap(null);
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            ImageContainer oldContainer = (ImageContainer) getTag();
            if (oldContainer != null) {
                oldContainer.cancelRequest();
                setImageBitmap(null);
            }
            return;
        }
        final ImageContainer oldContainer = (ImageContainer) getTag();
        // if there was an old request in this view, check if it needs to be canceled.
        if (oldContainer != null && oldContainer.getRequestUrl() != null) {
            if (oldContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                oldContainer.cancelRequest();
                setImageBitmap(null);
            }
        }
        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        final ImageContainer newContainer = mImageLoader.get(mUrl,
                ImageLoader.getImageListener(this, mDefaultImageId, mErrorImageId),mWidth,0);
        //Log.d("image",String.valueOf(width)+"*"+String.valueOf(height));
        // update the tag to be the new bitmap container.
        setTag(newContainer);
        // look at the contents of the new container. if there is a bitmap, load it.
        final Bitmap bitmap = newContainer.getBitmap();
        if (bitmap != null) {
            setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        loadImageIfNecessary();
    }

    @Override
    protected void onDetachedFromWindow() {
        ImageContainer oldContainer = (ImageContainer) getTag();
        if (oldContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            oldContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the tag so we can reload the image if necessary.
            setTag(null);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}