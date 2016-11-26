package com.trulybluemonochrome.materialrss;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    private RequestQueue mQueue;
    private ImageLoader mImageLoader;
    private MySQLiteOpenHelper hlpr;
    private SQLiteDatabase mydb;
    private String mFoldername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = hlpr.getReadableDatabase();


        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(GravityCompat.START);
        final ExpandableListView expandableList = (ExpandableListView) findViewById(R.id.navigationmenu);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);


        final Cursor cursor = mydb.query("folder", new String[] {"_id", "category"}, null, null, null, null, "_id DESC");
        cursor.moveToFirst();
        mFoldername = cursor.getString(cursor.getColumnIndex("category"));


        final SectionsPagerAdapter adapter = new SectionsPagerAdapter(
                getFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        viewPager.setAdapter(adapter);


        final MyExpandableListAdapter menuAdapter = new MyExpandableListAdapter(
                this, cursor,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { "category" },
                new int[] { android.R.id.text1 },
                android.R.layout.simple_expandable_list_item_1,
                new String[] { "title" },
                new int[] { android.R.id.text1 });
        //startManagingCursor(cursor);

        //cursor.close();

        expandableList.setAdapter(menuAdapter);
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                mFoldername=cursor.getString(cursor.getColumnIndex("category"));
                adapter.notifyDataSetChanged();
                viewPager.setCurrentItem(i1);
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                Log.d("DEBUG", "heading clicked");
                return false;
            }
        });


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        //navigationView.setNavigationItemSelectedListener(select);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tab);
        tabLayout.setupWithViewPager(viewPager);


        mQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mQueue, new LruImageCache());

        final Cursor feedcursor = mydb.query("feeds", null, null, null, null, null, "_id DESC");
        //feedcursor.moveToFirst();
        while(feedcursor.moveToNext()){
            doRequest(feedcursor.getString(feedcursor.getColumnIndex("url")));
        }
        feedcursor.close();
        //adapter.notifyDataSetChanged();
    }

    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

        public MyExpandableListAdapter(Context context, Cursor cur,
                                int groupLayout, String[] groupFrom, int[] groupTo,
                                int childLayout, String[] childrenFrom, int[] childrenTo) {
            super(context, cur, groupLayout, groupFrom, groupTo,
                    childLayout, childrenFrom, childrenTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            //String folder = mHlpr.getFolderString(groupCursor, Music.ALBUM);
            final Cursor cur = mydb.query("feeds", new String[] {"_id", "category", "title", "url"},  "category = ?", new String[]{ groupCursor.getString(groupCursor.getColumnIndex("category")) }, null, null, "_id DESC");;
            return cur;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void doRequest(final String url) {
        mQueue.add(new XMLRequest(url,
                new Response.Listener<InputStream>() {
                    @Override
                    public void onResponse(InputStream in) {
                        try {
                            parseXml(in, url);
/*

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
                            mGridView.setAdapter(mAdapter);

*/


                            in.close();
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

        //MAINApplication.getRequestQueue().add(request);
    }

    public void parseXml(final InputStream is, String page
    ) throws IOException,
            XmlPullParserException {
        //final ArrayList<RssItem> list = new ArrayList<RssItem>();
        final XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            //RssItem currentItem = null;
            ContentValues values = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if (tag.equals("item") || tag.equals("entry")) {
                            values = new ContentValues();
                            //currentItem.setTag(color);
                            //currentItem.setPage(page);
                            values.put("page",page);
                        } else if (values != null) {
                            if (tag.equals("title")) {
                                values.put("title",(parser.nextText().replaceAll(
                                        "(&#....;|&....;|&...;)", "")));// タグ除去;
                                //Log.d("title", currentItem.getTitle());
                            } else if (tag.equals("pubDate")) {
                                values.put("date",new SimpleDateFormat("YYYY-MM-DD HH:MM:SS").format(new SimpleDateFormat(
                                        "EEE, dd MMM yyyy HH:mm:ss Z",
                                        Locale.ENGLISH).parse(parser.nextText())));
                            } else if (tag.equals("date")
                                    || tag.equals("published")) {
                                values.put("date",new SimpleDateFormat("YYYY-MM-DD HH:MM:SS").format(new SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss").parse(parser
                                        .nextText())));
                            } else if (tag.equals("link")) {
                                final String link = parser.nextText();
                                if (link != "") {
                                    values.put("url",(link));
                                } else {
                                    final String rel = parser.getAttributeValue(
                                            null, "rel");
                                    final String herf = parser.getAttributeValue(
                                            null, "href");
                                    if (rel.equals("alternate")) {
                                        values.put("url",(herf));
                                    }
                                }
                            } else if (tag.equals("description")
                                    || tag.equals("summary")) {
                                String buf = parser.nextText();
                                values.put("image", StripImageTags(buf));
                                //Log.d("image",currentItem.getImage());
                                values.put("text",(buf
                                                .replaceAll(
                                                        "(<.+?>|\r\n|\n\r|\n|\r|&#....;|&....;|&...;|&..;)",
                                                        "")));// タグと改行除去
                            } else if (tag.equals("encoded")) {
                                values.put("image",StripImageTags(parser
                                        .nextText()));
                                //Log.d("image",currentItem.getImage());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();
                        if ((tag.equals("item") || tag.equals("entry"))
                                /*&& removePR(currentItem)*/) {
                            //list.add(currentItem);
                            mydb.replace("entry", null, values);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //list.trimToSize();
        return; //list;
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


    public RequestQueue getRequestQueue() {
        return mQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public SQLiteDatabase getDB() {
        return mydb;
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private  Cursor mCusor;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final Bundle bundle = new Bundle();
            //mCusor = mydb.query("feeds", new String[]{"_id", "category", "title", "url"}, "category = ?", new String[]{mFoldername}, null, null, "_id DESC");
            mCusor.moveToPosition(position);
            bundle.putString("URL", mCusor.getString(mCusor.getColumnIndex("url")));
            final PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            //final Cursor cursor = mydb.query("feeds", new String[]{"_id", "category", "title", "url"}, "category = ?", new String[]{mFoldername}, null, null, "_id DESC");
            mCusor = mydb.query("feeds", new String[]{"_id", "category", "title", "url"}, "category = ?", new String[]{mFoldername}, null, null, "_id DESC");
            return mCusor.getCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //final Cursor cursor = mydb.query("feeds", new String[]{"_id", "category", "title", "url"}, "category = ?", new String[]{mFoldername}, null, null, "_id DESC");
            mCusor.moveToPosition(position);
            return mCusor.getString(mCusor.getColumnIndex("title"));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void destroyAllItem(ViewPager pager) {
            for (int i = 0; i < getCount() - 1; i++) {
                try {
                    Object obj = this.instantiateItem(pager, i);
                    if (obj != null)
                        destroyItem(pager, i, obj);
                } catch (Exception e) {
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);

            if (position <= getCount()) {
                FragmentManager manager = ((Fragment) object).getFragmentManager();
                FragmentTransaction trans = manager.beginTransaction();
                trans.remove((Fragment) object);
                trans.commit();
            }
        }
    }


}





