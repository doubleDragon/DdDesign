/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package saulmm.coordinatorexamples.wsl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import saulmm.coordinatorexamples.R;

public class TestActivity4 extends AppCompatActivity {

//    @BindView(R.id.app_bar_id)
//    AppBarLayout appBarLayout;
//
//    @BindView(R.id.tabLayout)
//    TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test4);
        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {
//        tabLayout.addTab(tabLayout.newTab().setText("tag0"));
//        tabLayout.addTab(tabLayout.newTab().setText("tag1"));
//        tabLayout.addTab(tabLayout.newTab().setText("tag2"));
//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                Log.d("test", "total: " + appBarLayout.getTotalScrollRange() + "---offset:" + verticalOffset);
//            }
//        });
    }

    public static void start(Context c) {
        c.startActivity(new Intent(c, TestActivity4.class));
    }
}