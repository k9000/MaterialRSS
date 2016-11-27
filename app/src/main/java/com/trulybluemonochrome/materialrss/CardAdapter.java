package com.trulybluemonochrome.materialrss;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public LiteNetworkImageView imageView;
        public TextView txtLineOne;
        public TextView txtLineTwo;
        public Button btnGo;
        public View contentview;
        public CardView cardView;
        public ViewHolder(View v) {
            super(v);
            contentview = v;
            imageView = (LiteNetworkImageView)v.findViewById(R.id.image);
            txtLineOne = (TextView) v.findViewById(R.id.txt_line1);
            txtLineTwo = (TextView) v.findViewById(R.id.txt_line2);
            btnGo = (Button) v.findViewById(R.id.btn_go);
            cardView = (CardView) v.findViewById(R.id.card);

        }
    }

    private final LayoutInflater mLayoutInflater;
    private final ArrayList<Integer> mBackgroundColors;
    private final ArrayList<RssItem> mDataSet;
    private Context mContext;

    // タップされたときに呼び出されるメソッドを定義
    protected void onItemClicked(@NonNull String uri) {
    }

    public CardAdapter(final Context context, final ArrayList<RssItem> myDataSet) {
        //super(context, textViewResourceId);
        //mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mDataSet = myDataSet;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        mBackgroundColors = new ArrayList<Integer>();
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue2));
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue6));
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue3));
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue7));
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue4));
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue8));
        mBackgroundColors.add(ContextCompat.getColor(context,R.color.blue5));

    }

    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
        // Create a new view.
        final View v = mLayoutInflater.inflate(R.layout.list_item, viewGroup, false);
        // you can also set view size here. like this
        // ViewGroup.LayoutParams params = v.getLayoutParams();
        // params.height = view_size;
        // v.setLayoutParams(params);
        final CardAdapter.ViewHolder holder = new CardAdapter.ViewHolder(v);
        holder.contentview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                final String uri = mDataSet.get(position).getUrl();
                onItemClicked(uri);
            }
        });
        //Log.d("image","onCreateViewHolder");
        final ViewTreeObserver observer = holder.contentview.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener()
                {
                    @Override
                    public void onGlobalLayout()
                    {
                        MySingleton.getInstance(mContext).imageViewWidth = holder.imageView.getWidth();
                        holder.contentview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // attach data to covertView
        final RssItem item = mDataSet.get(position);

        //holder.imageview.setImageResource(item.img_id);
        final int backgroundIndex = position >= mBackgroundColors.size() ?
                position % mBackgroundColors.size() : position;


        holder.txtLineOne.setText(item.getTitle());//getItem(position) + position);
        final SimpleDateFormat sdf = new SimpleDateFormat("M'/'d k:mm ",
                Locale.JAPAN);
        holder.txtLineTwo.setText(sdf.format(item.getDate()));
        holder.cardView.setCardBackgroundColor(mBackgroundColors.get(backgroundIndex));

        final String imageUrl = item.getImage();

        holder.imageView.setImageUrl(imageUrl, MySingleton.getInstance(mContext).getImageLoader(), MySingleton.getInstance(mContext).imageViewWidth);

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}