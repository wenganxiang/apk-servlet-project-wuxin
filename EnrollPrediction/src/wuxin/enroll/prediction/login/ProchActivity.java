package wuxin.enroll.prediction.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import wuxin.enroll.prediction.R;

public class ProchActivity extends Activity {
	
	private int tick = 5;
	private TextView tickView;
	private boolean started = false;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0x0001:
            	if(tick>0) {            		
            		sendMessageDelayed(obtainMessage(0x0001), 1000);
            		tickView.setText(tick + " 秒后进入");
            		tick--;
            	} else {
            		ProchActivity.this.finish();
            	}
            	break;
            }
            super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		/*set it to be no title*/ 
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		/*set it to be full screen*/ 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		setContentView(R.layout.activity_porch);
		tickView = (TextView) findViewById(R.id.textView1);
		tickView.setText(tick + " 秒后进入");
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {
			@Override 
			public void onClick(View v) {
				ProchActivity.this.finish();
			}
		});
	}
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus && !started){
			mHandler.obtainMessage(0x0001).sendToTarget();
			started = true;
		}
    }
}
