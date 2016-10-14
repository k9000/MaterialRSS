package com.trulybluemonochrome.materialrss;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class XMLRequest extends Request<InputStream> {

    private final Listener<InputStream> mListener;

    public XMLRequest(int method, String url,
                              Listener<InputStream> listener,
                              ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    public XMLRequest(String url, Listener<InputStream> listener,
                              ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void deliverResponse(InputStream response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<InputStream> parseNetworkResponse(NetworkResponse response) {
        InputStream is = new ByteArrayInputStream(response.data);
        return Response.success(is, HttpHeaderParser.parseCacheHeaders(response));
    }
}
