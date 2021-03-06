package saulmm.coordinatorexamples.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.wsl.library.banner.DdBannerIndicator;
import com.wsl.library.design.DdHomeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-11-17.
 */

public class TestActivity4 extends AppCompatActivity{

    @BindView(R.id.dd_banner_viewpager)
    ViewPager bannerPager;
    @BindView(R.id.dd_banner_indicator)
    DdBannerIndicator bannerIndicator;

    @BindView(R.id.rv_content)
    RecyclerView rvContent;

    @BindView(R.id.cl)
    CoordinatorLayout cl;

    @BindView(R.id.bar)
    DdHomeLayout homeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity4);
        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {
        List<String> urls = new ArrayList<>();
        urls.add("http://img5.imgtn.bdimg.com/it/u=1746418361,2823897370&fm=21&gp=0.jpg");
        urls.add("http://img5.imgtn.bdimg.com/it/u=1479621666,13296461&fm=21&gp=0.jpg");
        urls.add("http://img5.imgtn.bdimg.com/it/u=3614602665,2140950684&fm=21&gp=0.jpg");
        urls.add("http://img1.imgtn.bdimg.com/it/u=2562925395,761784532&fm=21&gp=0.jpg");
        DdBannerSimpleAdapter adapter = new DdBannerSimpleAdapter(this, urls);

        bannerPager.setAdapter(adapter);
        bannerIndicator.setupViewpager(bannerPager);

        rvContent.setHasFixedSize(true);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setItemAnimator(new DefaultItemAnimator());
        rvContent.setAdapter(new TestAdapter(20));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            Log.d("test", "onWindowFocusChanged cl height: " + cl.getMeasuredHeight());
            Log.d("test", "onWindowFocusChanged bar height: " + homeLayout.getMeasuredHeight());
            Log.d("test", "onWindowFocusChanged status height: " + homeLayout.getTopInset());
            Log.d("test", "onWindowFocusChanged rv height: " + rvContent.getMeasuredHeight());


        }
    }
}
