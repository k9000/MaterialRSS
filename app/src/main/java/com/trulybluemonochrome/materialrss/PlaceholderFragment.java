package com.trulybluemonochrome.materialrss;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.etsy.android.grid.StaggeredGridView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

        mGridView.setAdapter(mAdapter);


        doRequest(mURL);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    public void doRequest(String url) {
        ((MainActivity)getActivity()).getRequestQueue().add(new XMLRequest(url,
                new Response.Listener<InputStream>() {

                    @Override
                    public void onResponse(InputStream in) {
                        //MyData data =
                        try {
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
                            mGridView.setAdapter(mAdapter);
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
}



