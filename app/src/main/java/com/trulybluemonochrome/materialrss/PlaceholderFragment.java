package com.trulybluemonochrome.materialrss;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;


import com.etsy.android.grid.StaggeredGridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class PlaceholderFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private StaggeredGridView mGridView;
    private ArrayAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mURL;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Now give the find the PullToRefreshLayout and set it up
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red,
                R.color.green, R.color.blue,
                R.color.orange);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mURL = getArguments().getString("URL", null);

        return rootView;
    }


    @Override
    public void onRefresh() {
        // 更新処理を実装する
        // ここでは単純に2秒後にインジケータ非表示
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 更新が終了したらインジケータ非表示
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGridView = (StaggeredGridView) getView().findViewById(R.id.grid_view);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String url = ((RssItem)parent.getItemAtPosition(position)).getUrl();
                //((MainActivity)getActivity()).openChromeCustomTab(url);
                final CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .setToolbarColor(ContextCompat.getColor(getActivity(), R.color.primary))
                        .setStartAnimations(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .build();

// Chromeの起動
                tabsIntent.launchUrl(getActivity(), Uri.parse(url));
            }

        });


        if (mAdapter == null) {
            // ImageLoaderをもっているアダプタを設定
            mAdapter = new CardAdapter(getActivity(), R.id.txt_line1);
        }
        final SQLiteDatabase db = ((MainActivity)getActivity()).getDB();
        final Cursor c = db.query("entry", null, "page = ?", new String[]{ mURL}, null, null, "date DESC");

        final ArrayList<RssItem> rsslist = new ArrayList<RssItem>();
        while(c.moveToNext()){
            RssItem _obj = new RssItem();
            _obj.setTitle(c.getString(c.getColumnIndex("title")));
            _obj.setUrl(c.getString(c.getColumnIndex("url")));
            _obj.setText(c.getString(c.getColumnIndex("text")));
            _obj.setPage(c.getString(c.getColumnIndex("page")));
            _obj.setImage(c.getString(c.getColumnIndex("image")));
            try {
                _obj.setDate(new SimpleDateFormat("YYYY-MM-DD HH:MM:SS").parse(c.getString(c.getColumnIndex("date"))));
            } catch (Exception e){
                e.printStackTrace();
            }
            rsslist.add(_obj);
        }
        c.close();
        //db.close();

        mAdapter.addAll(rsslist);

        mGridView.setAdapter(mAdapter);


        //doRequest(mURL);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //((MainActivity) activity).onSectionAttached(
          //      getArguments().getInt(ARG_SECTION_NUMBER));
    }

}



