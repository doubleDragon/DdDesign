package saulmm.coordinatorexamples.test;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.wsl.library.banner.DdBannerAbstractAdapter;
import com.wsl.library.banner.DdViewHolder;

import java.util.List;

import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-1.
 */

public class DdBannerSimpleAdapter extends DdBannerAbstractAdapter<String> {

    public DdBannerSimpleAdapter(Context context) {
        super(context);
    }

    public DdBannerSimpleAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.item_banner_simple, parent, false);
    }

    @Override
    protected DdViewHolder onCreateHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected void onBindView(int position, DdViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.imageView.setImageResource(R.drawable.ic_dd_default);
        Picasso.with(getContext())
                .load(getItem(position))
                .placeholder(R.drawable.ic_dd_default)
                .error(R.drawable.ic_dd_default)
                .into(holder.imageView);
    }

    private class ViewHolder extends DdViewHolder implements View.OnClickListener{

        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view;
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getPosition();
            String url = getItem(position);
            Log.d("test", "click position: " + position + "--url: " + url);
        }
    }
}
