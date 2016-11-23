package com.trulybluemonochrome.materialrss;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;



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

        expandableList.setAdapter(menuAdapter);
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                mFoldername=cursor.getString(cursor.getColumnIndex("category"));
                adapter.notifyDataSetChanged();
                viewPager.setCurrentItem(i1);
                /*
                final Cursor cursor =mydb.query("feeds", new String[] {"_id", "category", "title", "url"},  "_id = ?", new String[]{ String.valueOf(l) }, null, null, "_id DESC");
                cursor.moveToFirst();
                final String foldername = cursor.getString(cursor.getColumnIndex("category"));
                if (mFoldername==foldername){
                    viewPager.setCurrentItem(i1);
                } else {
                    mFoldername = foldername;
                    adapter.destroyAllItem(viewPager);
                    //adapter.setResource(resources);
                    adapter.notifyDataSetChanged();
                    viewPager.setAdapter(adapter);
                    viewPager.setCurrentItem(i1);
                }*/



                /*
                final Bundle bundle = new Bundle();
                final Cursor cursor =mydb.query("feeds", new String[] {"_id", "category", "title", "url"},  "_id = ?", new String[]{ String.valueOf(l) }, null, null, "_id DESC");
                cursor.moveToFirst();
                bundle.putString("URL", cursor.getString(cursor.getColumnIndex("url")));
                PlaceholderFragment fragment = new PlaceholderFragment();
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();*/
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
/*
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
*/
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





