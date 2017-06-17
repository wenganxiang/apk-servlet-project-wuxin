package wuxin.enroll.prediction.login;

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
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.fragments.RegisterResultFragment;

public class RegisterResultActivity extends FragmentActivity {
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
        title.setText("×¢²á³É¹¦");
        
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        RegisterResultFragment mRegisterResult = new RegisterResultFragment();
        transaction.replace(R.id.id_ly_container, mRegisterResult);
        transaction.commit();
    }
}