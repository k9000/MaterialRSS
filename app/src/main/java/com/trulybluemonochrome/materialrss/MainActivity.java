package com.trulybluemonochrome.materialrss;

import android.os.Bundle;
import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.net.Uri;

import android.support.customtabs.CustomTabsIntent;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private CharSequence mTitle;
    private CustomTabsIntent mTabsIntent = new CustomTabsIntent.Builder().build();
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header=navigationView.getHeaderView(0);

        Menu menuNav = navigationView.getMenu();
        menuNav.findItem(R.id.section1).setTitle("Engadget");
        menuNav.findItem(R.id.section2).setTitle("GIZMODE");
        menuNav.findItem(R.id.section3).setTitle("Lifehacker");

        mTabsIntent = new CustomTabsIntent.Builder().build();
        mQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mQueue, new LruImageCache());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Bundle bundle = new Bundle();

                switch (menuItem.getItemId()) {
                    case R.id.section1:
                        bundle.putString("URL", "http://feed.rssad.jp/rss/engadget/rss");
                        break;
                    case R.id.section2:
                        bundle.putString("URL", "http://feeds.gizmodo.jp/rss/gizmodo/index.xml");
                        break;
                    case R.id.section3:
                        bundle.putString("URL", "http://feeds.lifehacker.jp/rss/lifehacker/index.xml");
                        break;
                    default:
                        break;
                }
                /*
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance( 1))
                        .commit();
*/
                PlaceholderFragment fragment = new PlaceholderFragment();
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
                return false;
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

    public void openChromeCustomTab(String url) {
        //String packageName = CustomTabsHelper.getPackageNameToUse(this);
        //tabsIntent.intent.setPackage(packageName);
        mTabsIntent.launchUrl(this, Uri.parse(url));
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




