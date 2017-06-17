package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import wuxin.enroll.prediction.FragmentBackListener;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.Tools;

public class RegisterPhoneFragment extends Fragment implements FragmentBackListener{

	private static final int NUMBER_CHECK = 0x1001;
	private static final int SHOW_PROGRESS = 0x1002;
	private static final int HIDE_PROGRESS = 0x1003;
	
	private Button mNext;
	private TextView mURLText;
	private EditText mNumber;
	private CheckBox mAgreen;
	private ProgressDialog mProgressView;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NUMBER_CHECK:
				if (mProgressView != null) {
					mProgressView.hide();
					mProgressView = null;
				}
				String jsStr = (String) msg.obj;
				JSONObject jsObj = null;
				try {
					jsObj = new JSONObject(jsStr);
					String ans = jsObj.getString("ans");
					if (ans != null) {
						if ("unRegister".equals(ans)) {
							String acc = jsObj.getString("account");
							Intent intent = new Intent("wuxin.enroll.prediction.login.RegisterUserInfoActivity");
							intent.putExtra("account", acc);
							startActivity(intent);
						} else if ("dbError".equals(ans)) {
							Toast.makeText(RegisterPhoneFragment.this.getContext(), "访问失败，稍后再试！", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(RegisterPhoneFragment.this.getContext(), "账号已注册，请回到登录页面找回密码！", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(RegisterPhoneFragment.this.getContext(), "访问错误，稍后再试！", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Log.v("wuxin-login", e.toString());
				} catch (Exception e) {
					Log.v("wuxin-login", e.toString());
				}
				break;
			case SHOW_PROGRESS:
				if (mProgressView == null) {
					mProgressView = new ProgressDialog(RegisterPhoneFragment.this.getContext());
				}
				mProgressView.show();
				sendMessageDelayed(obtainMessage(HIDE_PROGRESS), Tools.HTTP_READ_TIMEOUT);
				break;
			case HIDE_PROGRESS:
				if (mProgressView != null) {
					mProgressView.hide();
					mProgressView = null;
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_register_phone, container, false);
		mNumber = (EditText) root.findViewById(R.id.et_phoneNumber);
		mAgreen = (CheckBox) root.findViewById(R.id.ck_agreen);
		mNext = (Button) root.findViewById(R.id.btn_next);
		mNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mAgreen.isChecked()) {
					Toast.makeText(RegisterPhoneFragment.this.getContext(), "请先阅读并同意使用条款！", Toast.LENGTH_SHORT).show();
					return;
				}
				String number = mNumber.getText().toString();
				if (number != null) {
					if (!Tools.isMobile(number)) {
						Toast.makeText(RegisterPhoneFragment.this.getContext(), "请输入有效的11位手机号码！", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				new HttpThread(new DoRegisterCheck()).start();
			}
		});
		mURLText = (TextView) root.findViewById(R.id.tv_url);
		mURLText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		return root;
	}
	
	class DoRegisterCheck implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(Tools.REQUEST_CODE, Tools.REGISTER_CHECK_REQ);
				obj.put("account", mNumber.getText().toString());
				result = obj.toString().getBytes("GBK");
				if (result != null && result.length > 0) {
					mHandler.obtainMessage(SHOW_PROGRESS).sendToTarget();//非UI线程不能更新UI
				}
				return result;
			} catch (JSONException e) {
				Log.v("wuxin-login", e.toString());
				return result;
			} catch (UnsupportedEncodingException e) {
				Log.v("wuxin-login", e.toString());
				return result;
			}
		}

		@Override
		public void afterResponse(byte[] result) {
			try {
				Message msg = new Message();
				msg.what = NUMBER_CHECK;
				msg.obj = new String(result, 0, result.length, "GBK");;
				mHandler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				Log.v("wuxin-login", e.toString());
			}
		}
	}

	@Override
	public void onBackPressed() {
		
	}
	
}
