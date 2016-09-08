package saulmm.coordinatorexamples.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.wsl.library.banner.DdBannerIndicator;
import com.wsl.library.design.DdBarLayout;
import com.wsl.library.design.DdCollapsingBarLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-25.
 */

public class TestActivity extends AppCompatActivity{

    @BindView(R.id.cl_parent)
    CoordinatorLayout clParent;

    @BindView(R.id.cb_bar)
    DdCollapsingBarLayout cbBar;

    @BindView(R.id.bar_parent)
    DdBarLayout barParent;

    @BindView(R.id.dd_banner_viewpager)
    ViewPager bannerPager;
    @BindView(R.id.dd_banner_indicator)
    DdBannerIndicator bannerIndicator;

    @BindView(R.id.rv_header)
    RecyclerView rvHeader;
    private TestAdapter headerAdapter;

    @BindView(R.id.rv_content)
    RecyclerView rvContent;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        initViews();
        post();
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

        headerAdapter = new TestAdapter(0);
        rvHeader.setHasFixedSize(false);
        rvHeader.setLayoutManager(new LinearLayoutManager(this));
        rvHeader.setItemAnimator(new DefaultItemAnimator());
        rvHeader.setAdapter(headerAdapter);

        tabLayout.addTab(tabLayout.newTab().setText("tab0"));
        tabLayout.addTab(tabLayout.newTab().setText("tab1"));
        tabLayout.addTab(tabLayout.newTab().setText("tab2"));

        rvContent.setHasFixedSize(true);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setItemAnimator(new DefaultItemAnimator());
        rvContent.setAdapter(new TestAdapter(20));
    }

    private void post() {
        rvHeader.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("post", "ssss");
                headerAdapter.addAll(getDelayData());
                barParent.requestLayout();
//                clParent.dispatchDependentViewsChanged(rvHeader);
            }
        }, 2000);
    }

    private List<String> getDelayData() {
        List<String> list = new ArrayList<>(10);
        for(int i=0; i<10; i++) {
            list.add("delay :" + i);
        }
        return list;
    }
}
