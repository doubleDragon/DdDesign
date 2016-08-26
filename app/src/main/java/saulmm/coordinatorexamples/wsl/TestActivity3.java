package saulmm.coordinatorexamples.wsl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-19.
 */

public class TestActivity3 extends AppCompatActivity {

//    @BindView(R.id.recyclerView)
//    RecyclerView recyclerView;
//    @BindView(R.id.tabLayout)
//    TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test3);
        ButterKnife.bind(this);

//        initViews();
    }

    private void initViews() {
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(new TestAdapter());
//
//        tabLayout.addTab(tabLayout.newTab().setText("aaa"));
//        tabLayout.addTab(tabLayout.newTab().setText("bbb"));
//        tabLayout.addTab(tabLayout.newTab().setText("ccc"));
    }
}