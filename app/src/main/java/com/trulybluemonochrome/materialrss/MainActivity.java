package com.trulybluemonochrome.materialrss;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;



public class MainActivity extends Activity {

    private CharSequence mTitle;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;

    private DrawerLayout mDrawerLayout;
    private MyExpandableListAdapter mMenuAdapter;
    private MySQLiteOpenHelper mHlpr;
    private SQLiteDatabase mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.openDrawer(GravityCompat.START);
        ExpandableListView expandableList = (ExpandableListView) findViewById(R.id.navigationmenu);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        mHlpr = new MySQLiteOpenHelper(getApplicationContext());
        mydb = mHlpr.getReadableDatabase();


        Cursor cursor = mydb.query("folder", new String[] {"_id", "category"}, null, null, null, null, "_id DESC");

        mMenuAdapter = new MyExpandableListAdapter(
                this, cursor,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { "category" },
                new int[] { android.R.id.text1 },
                android.R.layout.simple_expandable_list_item_1,
                new String[] { "title" },
                new int[] { android.R.id.text1 });
        startManagingCursor(cursor);

        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                Log.d("DEBUG", "submenu item clicked");
                Bundle bundle = new Bundle();
                Cursor cursor =mydb.query("feeds", new String[] {"_id", "category", "title", "url"},  "_id = ?", new String[]{ String.valueOf(l) }, null, null, "_id DESC");
                cursor.moveToFirst();
                bundle.putString("URL", cursor.getString(cursor.getColumnIndex("url")));
                PlaceholderFragment fragment = new PlaceholderFragment();
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();

                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        mQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mQueue, new LruImageCache());
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
            Cursor cur = mydb.query("feeds", new String[] {"_id", "category", "title", "url"},  "category = ?", new String[]{ groupCursor.getString(groupCursor.getColumnIndex("category")) }, null, null, "_id DESC");;
            return cur;
        }

    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
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


    public RequestQueue getRequestQueue() {
        return mQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


}




