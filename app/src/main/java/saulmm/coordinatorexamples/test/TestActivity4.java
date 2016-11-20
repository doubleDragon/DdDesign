package saulmm.coordinatorexamples.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wsl.library.banner.DdBannerIndicator;
import com.wsl.library.design.DdBarLayout;
import com.wsl.library.design.DdCollapsingBarLayout;
import com.wsl.library.design.DdHeaderLayout;
import com.wsl.library.design.DdToolBarView;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-25.
 */

public class TestActivity4 extends AppCompatActivity {

    @BindView(R.id.cl_parent)
    CoordinatorLayout clParent;

    @BindView(R.id.cb_bar)
    DdCollapsingBarLayout cbBar;

    @BindView(R.id.bar_parent)
    DdHeaderLayout barParent;

    @BindView(R.id.dd_banner_viewpager)
    ViewPager bannerPager;
    @BindView(R.id.dd_banner_indicator)
    DdBannerIndicator bannerIndicator;

//    @BindView(R.id.rv_header)
//    RecyclerView rvHeader;
//    private TestAdapter headerAdapter;

//    @BindView(R.id.tv_header0)
//    TextView tvHeader0;
//    @BindView(R.id.tv_header1)
//    TextView tvHeader1;
//    @BindView(R.id.tv_header2)
//    TextView tvHeader2;

    @BindView(R.id.rv_content)
    RecyclerView rvContent;

    @BindView(R.id.rv_content_test)
    RecyclerView rvContentTest;

//    @BindView(R.id.tabLayout)
//    TabLayout tabLayout;

//    @OnClick(R.id.iv_home) void onClickHome() {
//        Log.d("test", "click home");
//    }
//
//    @OnClick(R.id.iv_menu) void onClickMenu() {
//        Log.d("test", "click menu");
//    }

    @BindView(R.id.iv_menu)
    DdToolBarView ivMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test4);
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

//        headerAdapter = new TestAdapter(0);
//        rvHeader.setHasFixedSize(false);
//        rvHeader.setLayoutManager(new LinearLayoutManager(this));
//        rvHeader.setItemAnimator(new DefaultItemAnimator());
//        rvHeader.setAdapter(headerAdapter);

//        tabLayout.addTab(tabLayout.newTab().setText("tab0"));
//        tabLayout.addTab(tabLayout.newTab().setText("tab1"));
//        tabLayout.addTab(tabLayout.newTab().setText("tab2"));

        rvContent.setHasFixedSize(true);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setItemAnimator(new DefaultItemAnimator());
        rvContent.setAdapter(new TestAdapter(10));

        rvContentTest.setHasFixedSize(true);
        rvContentTest.setLayoutManager(new LinearLayoutManager(this));
        rvContentTest.setItemAnimator(new DefaultItemAnimator());
        rvContentTest.setAdapter(new TestAdapter(10));
    }

    private void post() {
        rvContent.postDelayed(new Runnable() {
            @Override
            public void run() {
//                ivMenu.setInnerDrawable(ContextCompat.getDrawable(TestActivity.this, R.drawable.ic_menu_manager));
                ivMenu.setCircleAlpha(0);
            }
        }, 5000);
    }

    private List<String> getDelayData() {
        List<String> list = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            list.add("delay :" + i);
        }
        return list;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        Log.d("test", "onWindowFocusChanged height: " + tvHeader0.getHeight());
    }
}
