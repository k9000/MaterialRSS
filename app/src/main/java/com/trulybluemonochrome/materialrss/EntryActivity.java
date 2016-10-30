package com.trulybluemonochrome.materialrss;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class EntryActivity extends Activity implements View.OnClickListener {

    private String mTitle;
    private String mUri;
    private int mPosition;
    private boolean mPass = false;
    private boolean noti = true;

    private RequestQueue mQueue;

    int selectColor = 0xff00aeef;

    private int mflag;

    private MySQLiteOpenHelper mHlpr;
    private SQLiteDatabase mydb;

    //ArrayList<RssFeed> items;

    // UI references.
    private EditText mTitleView;
    private EditText mUriView;
    private View mLoginStatusView;
    private TextView mPageTitle;
    private Spinner mSpinner;
    private Button button;
    private ListView mListview;
    private ArrayAdapter mAdapter;
    private int mColumnIndex;

    //private ImageButton imageButton;
    //private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);
        mQueue = Volley.newRequestQueue(this, new HurlStack(){
            @Override
            public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
                    throws IOException, AuthFailureError {
                Map newHeaders = new HashMap();
                newHeaders.putAll(additionalHeaders);
                newHeaders.put("User-Agent", "Desktop");
                HttpResponse response = super.performRequest(request, newHeaders);
                return response;
            }
        });

        // レイアウトID登録
        mLoginStatusView = findViewById(R.id.login_status);
        mPageTitle = (TextView) findViewById(R.id.textView1);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mListview = (ListView) findViewById(R.id.listView1);
        if (mAdapter == null) {
            // ImageLoaderをもっているアダプタを設定
            mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_activated_1);
        }

        mListview.setAdapter(mAdapter);
        button = (Button) findViewById(R.id.regist_button);
        button.setOnClickListener(this);
        ((Button) findViewById(R.id.cancel_button)).setOnClickListener(this);
        mTitleView = (EditText) findViewById(R.id.title);
        mUriView = (EditText) findViewById(R.id.uri);
        //imageButton = (ImageButton) findViewById(R.id.imageButton1);
        //checkBox = (CheckBox) findViewById(R.id.checkBox1);


        mHlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = mHlpr.getWritableDatabase();
        Cursor cursor = mydb.query("folder", new String[] {"_id", "category"}, null, null, null, null, "_id DESC");
        mColumnIndex = cursor.getColumnIndex("category");
        String[] from = {"category"};
        int[] to = {android.R.id.text1};
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(this,android.R.layout.simple_spinner_item,cursor,from,to);
        //ドロップダウンリストのレイアウトを設定します。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);



        //Bundle args = new Bundle();
        if (getIntent().getDataString() != null) {// RSS_Linkクリックから
            mflag = 2;// タイトル取得を目指す
            mUri = getIntent().getDataString();
            mUriView.setText(mUri);
            doRequest(getIntent().getDataString());
            //args.putString(ItemDetailFragment.ARG_ITEM_ID, getIntent()
            //.getDataString());

        } else if (getIntent().getStringExtra(Intent.EXTRA_TEXT) != null) {// ページ共有から
            mflag = 1;// URI取得を目指す
            doRequest(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
            //args.putString(ItemDetailFragment.ARG_ITEM_ID, getIntent()
            // .getExtras().getString(Intent.EXTRA_TEXT));
/*
        } else if (getIntent().getBooleanExtra("EDIT", false)) {// 編集クリックから
            mflag = 3;// 記事一覧取得を目指す
            // mItem = getIntent().getParcelableExtra("Parcelable");
            mPosition = getIntent().getExtras().getInt("POSITION");
            mTitle = items.get(mPosition).getTitle();
            mUri = items.get(mPosition).getUrl();
            noti = items.get(mPosition).getNoti();
            button.setText(R.string.edit);
            mPageTitle.setText(R.string.rss_feed_edit);
            args.putString(ItemDetailFragment.ARG_ITEM_ID, mUri);
            selectColor = items.get(mPosition).getTag();
*/
        }
    }
/*
        if (getIntent().getExtras().getString("ADD") != null) {// 設定の追加ボタンから
            button.setText(R.string.check);
        } else {
            showProgress(true);
            getLoaderManager().initLoader(mflag, args, this);
        }
    }

    /**
     * プログレスバーのON/OFF
     *
     * @param show
     */
    private void showProgress(final boolean show) {
        mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.regist_button:
                if (mPass) {// 認証クリア
                    ContentValues values = new ContentValues();
                    values.put("category", ((Cursor)mSpinner.getSelectedItem()).getString(mColumnIndex));
                    values.put("title", mTitleView.getText().toString());
                    values.put("url", mUriView.getText().toString());
                    mydb.insert("feeds", null, values);

                    //Cursor cursor = mydb.query("feeds", new String[] {"_id", "category", "title", "url"}, null, null, null, null, "_id DESC");
                    //startManagingCursor(cursor);
                    // 終了
                    this.finish();
                }
                break;
            case R.id.cancel_button:
                this.finish();
                break;
        }
    }

    String mResult;
    public void doRequest(String url) {
        mQueue.add(new XMLRequest(url,
                new Response.Listener<InputStream>() {

                    @Override
                    public void onResponse(InputStream in) {
                        //MyData data =
                        try {
                            switch (mflag){
                                case 1:
                                    mResult = parseHtml(in);
                                    mUriView.setText(mResult);
                                    mflag++;
                                    doRequest(mResult);
                                    break;
                                case 2:
                                    final  String title = parseRSS(in);
                                    mTitleView.setText(title);
                                    in.close();
                                    mflag++;
                                    doRequest(mResult);
                                    break;
                                case 3:
                                    ArrayList<RssItem> rsslist = parseXml(in);
                                    mAdapter.addAll(rsslist);
                                    mAdapter.sort(new Comparator<RssItem>() {
                                        @Override
                                        public int compare(RssItem lhs, RssItem rhs) {
                                            if (lhs.getDate() == null)
                                                return 1;
                                            else if (rhs.getDate() == null)
                                                return -1;
                                            else if (lhs.getDate().before(rhs.getDate()))
                                                return 1;
                                            else
                                                return -1;
                                        }
                                    });
                                    //mAdapter.addAll(rsslist);
                                    //mListview.setAdapter(mAdapter);
                                    mPass = true;
                                    in.close();

                                    break;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        //if (mListener != null) {
                        //    mListener.onParseXml(data);
                        //}
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // error
            }
        }));
    }


    public static final ArrayList<RssItem> parseXml(final InputStream is
    ) throws IOException,
            XmlPullParserException {
        final ArrayList<RssItem> list = new ArrayList<RssItem>();
        final XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            RssItem currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if (tag.equals("item") || tag.equals("entry")) {
                            currentItem = new RssItem();
                            //currentItem.setTag(color);
                            //currentItem.setPage(page);
                        } else if (currentItem != null) {
                            if (tag.equals("title")) {
                                currentItem.setTitle(parser.nextText().replaceAll(
                                        "(&#....;|&....;|&...;)", ""));// タグ除去;
                                Log.d("title", currentItem.getTitle());
                            } else if (tag.equals("pubDate")) {
                                currentItem.setDate(new SimpleDateFormat(
                                        "EEE, dd MMM yyyy HH:mm:ss Z",
                                        Locale.ENGLISH).parse(parser.nextText()));
                            } else if (tag.equals("date")
                                    || tag.equals("published")) {
                                currentItem.setDate(new SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss").parse(parser
                                        .nextText()));
                            } else if (tag.equals("link")) {
                                final String link = parser.nextText();
                                if (link != "") {
                                    currentItem.setUrl(link);
                                } else {
                                    final String rel = parser.getAttributeValue(
                                            null, "rel");
                                    final String herf = parser.getAttributeValue(
                                            null, "href");
                                    if (rel.equals("alternate")) {
                                        currentItem.setUrl(herf);
                                    }
                                }
                            } else if (tag.equals("description")
                                    || tag.equals("summary")) {
                                String buf = parser.nextText();
                                currentItem.setImage(StripImageTags(buf));
                                //Log.d("image",currentItem.getImage());
                                currentItem
                                        .setText(buf
                                                .replaceAll(
                                                        "(<.+?>|\r\n|\n\r|\n|\r|&#....;|&....;|&...;|&..;)",
                                                        ""));// タグと改行除去
                            } else if (tag.equals("encoded")) {
                                currentItem.setImage(StripImageTags(parser
                                        .nextText()));
                                //Log.d("image",currentItem.getImage());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();
                        if ((tag.equals("item") || tag.equals("entry"))
                                /*&& removePR(currentItem)*/) {
                            list.add(currentItem);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        list.trimToSize();
        return list;
    }

    private static final String StripImageTags(String str) {
        final Pattern o = Pattern.compile("<img.*?(jpg|png|images).*?>");
        final Pattern p = Pattern.compile("http.*?(jpg|png)");
        final Pattern q = Pattern.compile("//.*?(jpg|png)");
        final Pattern r = Pattern.compile("//.*?images.*?\"");
        String matchstr = null;
        final Matcher mo = o.matcher(str);
        if (mo.find()) {
            str = mo.group();
            final Matcher mp = p.matcher(str);
            final Matcher mq = q.matcher(str);
            final Matcher mr = r.matcher(str);
            if (mp.find()) {
                matchstr = mp.group();
            } else if (mq.find()) {
                matchstr = "http:" + mq.group();
            } else if (mr.find()) {
                matchstr = "http:" + mr.group();
                matchstr = matchstr.substring(0, matchstr.length() - 1);
            } else {
                matchstr = null;
            }
            return matchstr;
        }
        return null;

    }

    public static final String parseHtml(final InputStream is)
            throws IOException, XmlPullParserException {

        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature(Xml.FEATURE_RELAXED, true);
        factory.setNamespaceAware(true);
        final XmlPullParser parser = factory.newPullParser();

        //final ArrayList<RssItem> list = new ArrayList<RssItem>();
        try {
            // URL接続
            final BufferedReader urlIn = new BufferedReader(
                    new InputStreamReader(is));
            // HTMLソースの取得
            final StringBuilder strb = new StringBuilder();
            strb.append((urlIn.readLine()).replaceAll("doctype", "DOCTYPE"));
            while (urlIn.ready()) {
                strb.append(urlIn.readLine());
            }
            is.close();
            while (urlIn.ready()) {
                strb.append(urlIn.readLine());
            }
            urlIn.close();
            //conn.disconnect();

            parser.setInput(new StringReader(strb.toString()));
            int eventType = parser.getEventType();
            RssItem currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if (tag.equals("head")) {
                            currentItem = new RssItem();
                        } else if (currentItem != null) {
                            if (tag.equals("link")) {
                                String rel = parser.getAttributeValue(null, "rel");
                                String type = parser
                                        .getAttributeValue(null, "type");
                                String herf = parser
                                        .getAttributeValue(null, "href");
                                if (rel.equals("alternate")
                                        && (type.equals("application/rss+xml") || type
                                        .equals("application/atom+xml"))) {
                                    return "http:" + herf.replaceAll("http:", "");
                                    //currentItem.setUrl(herf);
                                    //list.add(currentItem);
                                    //return list;
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();
                        if (tag.equals("head")) {
                            return null;
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static final String parseRSS(final InputStream is)
            throws IOException, XmlPullParserException {
        //final ArrayList<RssItem> list = new ArrayList<RssItem>();
        final XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            RssItem currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if (tag.equals("channel") || tag.equals("feed")) {
                            currentItem = new RssItem();
                        } else if (currentItem != null) {
                            if (tag.equals("title")) {
                                return parser.nextText();
                                //currentItem.setTitle(parser.nextText());
                                //list.add(currentItem);
                                //return list;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();
                        if (tag.equals("head")) {
                            return null;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mQueue == null) {
            mQueue.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQueue == null) {
            mQueue.start();
        }
    }




}