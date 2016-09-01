package saulmm.coordinatorexamples.wsl;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-23.
 */

public class TestAdapter extends RecyclerView.Adapter{

    private List<String> data;

    public TestAdapter() {
        super();
        data = new ArrayList<>();
        for(int i=0; i< 20; i++) {
            data.add("item " + i);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_test, parent, false);
        return new VHItem(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String text = getItem(position);
        VHItem vhItem = (VHItem) holder;
        vhItem.textView.setText(text);
    }

    private String getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class VHItem extends RecyclerView.ViewHolder {

        @BindView(R.id.text)
        TextView textView;

        public VHItem(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
