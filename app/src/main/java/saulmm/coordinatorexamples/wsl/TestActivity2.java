package saulmm.coordinatorexamples.wsl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-19.
 */

public class TestActivity2 extends AppCompatActivity{

    @BindView(R.id.parent)
    View parent;
    @BindView(R.id.text0)
    View text0;
    @BindView(R.id.text1)
    View text1;

    @OnClick(R.id.bt) void onClickBt() {
        ViewCompat.offsetTopAndBottom(text0, 100);
        Log.d("test", "onWindowFocusChanged text0:" + dumpView(text0));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        ButterKnife.bind(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("test", "onWindowFocusChanged :" + hasFocus);
        Log.d("test", "onWindowFocusChanged parent:" + dumpView(parent));
        Log.d("test", "onWindowFocusChanged text0:" + dumpView(text0));
        Log.d("test", "onWindowFocusChanged text1:" + dumpView(text1));
    }

    private String dumpView(View view) {
        StringBuilder sb = new StringBuilder();
        sb.append(view.getId());
        sb.append("[\n");
        sb.append(view.getX()).append(",").append(view.getY()).append("\n");
        sb.append(view.getTranslationX()).append(",").append(view.getTranslationY()).append("\n");
        sb.append("]");
        return sb.toString();
    }
}