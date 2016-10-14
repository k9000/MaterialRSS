package com.trulybluemonochrome.materialrss;

import android.graphics.Bitmap;

import java.util.Date;


public class RssItem {
    private String title;
    private String url;
    private String text;
    private int tag;
    private String page;
    private String image;
    private Date date;
    public RssItem(final String title, final String url, final String text,
                   final int tag, final String page, final String image) {
        this.title = title;
        this.url = url;
        this.text = text;
        this.tag = tag;
        this.page = page;
        this.image = image;
    }
    public RssItem() {
    }
    @Override
    public String toString() {
        return title;
    }
    public final String getTitle() {
        return title;
    }
    public final String getUrl() {
        return url;
    }
    public final String getText() {
        return text;
    }
    public final int getTag() {
        return tag;
    }
    public final String getPage() {
        return page;
    }
    public String getImage() {
        return image;
    }
    public final Date getDate() {
        return date;
    }
    public final void setTitle(final String title) {
        this.title = title;
    }
    public final void setUrl(final String url) {
        this.url = url;
    }
    public final void setText(final String text) {
        this.text = text;
    }
    public final void setTag(final int tag) {
        this.tag = tag;
    }
    public final void setPage(final String page) {
        this.page = page;
    }
    public final void setImage(final String stripImageTags) {
        this.image = stripImageTags;
    }
    public final Bitmap getImageData() {
        final Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
        bmp.eraseColor(tag);
        return bmp;
    }
    public void setDate(final Date date) {
        this.date = date;
    }
    public void clear() {
        this.title = null;
        this.url = null;
        this.text = null;
        this.tag = 0;
        this.page = null;
        this.image = null;
    }
}
