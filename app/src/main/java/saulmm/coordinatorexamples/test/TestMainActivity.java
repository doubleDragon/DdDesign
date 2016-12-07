package saulmm.coordinatorexamples.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-12-6.
 */

public class TestMainActivity extends AppCompatActivity{

    private void intentTo() {
        Intent i = new Intent(this, TestActivity4.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("点我跳转");
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentTo();
            }
        });

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(textView,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(layout);
    }
}
