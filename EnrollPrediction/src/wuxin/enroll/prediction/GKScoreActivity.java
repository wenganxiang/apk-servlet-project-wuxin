package wuxin.enroll.prediction;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import wuxin.enroll.prediction.fragments.GKScoreFragment;

public class GKScoreActivity extends FragmentActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        
        initView();
    }

    private void initView() {
        RelativeLayout back = (RelativeLayout) findViewById(R.id.topItem1);
        back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
        ImageView backImg = (ImageView) findViewById(R.id.topImg1);
        backImg.setImageResource(R.drawable.backpress);
        TextView title = (TextView) findViewById(R.id.topText2);
        title.setText("¸ß¿¼Ô¤¹À³É¼¨");
        
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        GKScoreFragment mScore = new GKScoreFragment();
        transaction.replace(R.id.id_ly_container, mScore);
        transaction.commit();
    }
}
