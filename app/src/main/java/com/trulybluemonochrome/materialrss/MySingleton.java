package com.trulybluemonochrome.materialrss;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;
    public int imageViewWidth;
    private static final String DEFAULT_CACHE_DIR = "volley";

    private MySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
                    final int cacheSize = maxMemory / 8;       // 最大メモリに依存した実装
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(cacheSize);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = newRequestQueue(mCtx.getApplicationContext(),128 * 1024 * 1024, new HurlStack(){
                @Override
                public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
                        throws IOException, AuthFailureError {
                    final Map newHeaders = new HashMap();
                    newHeaders.putAll(additionalHeaders);
                    newHeaders.put("User-Agent", "Desktop");
                    final HttpResponse response = super.performRequest(request, newHeaders);
                    return response;
                }
            });
            //mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public static RequestQueue newRequestQueue(Context context, int cacheSize, HttpStack stack) {
        final File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        final Network network = new BasicNetwork(stack);
        final RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir, cacheSize), network);
        queue.start();
        return queue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}
