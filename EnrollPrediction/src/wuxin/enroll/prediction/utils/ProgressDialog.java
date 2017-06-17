package wuxin.enroll.prediction.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import wuxin.enroll.prediction.R;

public class ProgressDialog {
	
	private Context mContext;
	private Dialog mProgress;
	
	public ProgressDialog(Context context) {
		mContext = context;
		mProgress = new Dialog(mContext);
		mProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mProgress.setContentView(R.layout.progress_view);
		mProgress.setCancelable(false);
		//WindowManager.LayoutParams lp = mProgress.getWindow().getAttributes();
		mProgress.getWindow().setGravity(Gravity.CENTER);
		//mProgress.getWindow().setAttributes(lp);
	}
	
	public void show() {
		if (mProgress != null && !mProgress.isShowing()) {
			mProgress.show();
		}
	}
	
	public void hide(){
		if(mProgress != null && mProgress.isShowing()) {
			mProgress.dismiss();
		}
		mProgress = null;
	}
	
	public boolean isShowing() {
		if (mProgress != null && !mProgress.isShowing()) {
			return mProgress.isShowing();
		}
		return false;
	}
}
