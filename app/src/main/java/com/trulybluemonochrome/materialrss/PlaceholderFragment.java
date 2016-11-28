package com.trulybluemonochrome.materialrss;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class PlaceholderFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    //private RecyclerView mRecyclerView;
    //private CardAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<String> mURLlist;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        final PlaceholderFragment fragment = new PlaceholderFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Now give the find the PullToRefreshLayout and set it up
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red,
                R.color.green, R.color.blue,
                R.color.orange);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mURLlist = getArguments().getStringArrayList("URL");
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

        final RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);

        final SQLiteDatabase db = ((MainActivity)getActivity()).getDB();
        //final Cursor c = db.query("entry", null, "page = ? or page = ?", foo, null, null, "date DESC");
        final String[] names = mURLlist.toArray(new String[0]); // do whatever is needed first
        final String query = "SELECT * FROM entry"
                + " WHERE page IN (" + makePlaceholders(names.length) + ") ORDER BY date DESC";
        final Cursor c = db.rawQuery(query, names);
        final ArrayList<RssItem> rsslist = new ArrayList<RssItem>();
        try {
            while(c.moveToNext()) {
                RssItem _obj = new RssItem();
                _obj.setTitle(c.getString(c.getColumnIndex("title")));
                _obj.setUrl(c.getString(c.getColumnIndex("url")));
                _obj.setText(c.getString(c.getColumnIndex("text")));
                _obj.setPage(c.getString(c.getColumnIndex("page")));
                _obj.setImage(c.getString(c.getColumnIndex("image")));
                _obj.setDate(new SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.ENGLISH).parse(c.getString(c.getColumnIndex("date"))));
                rsslist.add(_obj);
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            c.close();
        }

        //db.close();

        //mRecyclerView = (RecyclerView) findViewById(android.R.id.list);
        recyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(3, 1);
        recyclerView.setLayoutManager(layoutManager);
        final CardAdapter adapter = new CardAdapter(getActivity().getApplicationContext(),rsslist){
            @Override
            protected void onItemClicked(@NonNull String uri) {
                super.onItemClicked(uri);
                final CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .setToolbarColor(ContextCompat.getColor(getActivity(), R.color.primary))
                        .setStartAnimations(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .build();

                // Chromeの起動
                tabsIntent.launchUrl(getActivity(), Uri.parse(uri));
            }
        };

        recyclerView.setAdapter(adapter);

    }

    private String makePlaceholders(int i){
        String str = "?";
        if (i>0){
            for (int j=0;j<i-1;j++){
                str += ", ?";
            }
        }
        return str;

    }


}



