package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.widget.EditText;
import android.widget.Toast;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.db.DBOperate;
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.Tools;
import wuxin.enroll.prediction.db.DBHelper;

public class RegisterUserInfoFragment extends Fragment {

	private static final int LOGIN_ACCOUNT = 0x1001;
	private static final int SHOW_PROGRESS = 0x1002;
	private static final int HIDE_PROGRESS = 0x1003;
	
	private Button mFinish;
	private EditText mPassword;
	private EditText mPasswordAgain;
	private EditText mAlaisName;
	private ProgressDialog mProgressView;
	
	private String account = null;
	private SQLiteOpenHelper helper;
	private DBOperate mylogin;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_PROGRESS:
				if (mProgressView == null) {
					mProgressView = new ProgressDialog(RegisterUserInfoFragment.this.getContext());
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
			case LOGIN_ACCOUNT:
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
						if ("success".equals(ans)) {
							mylogin.addAccount(account, mPassword.getText().toString());
							Intent intent = new Intent("wuxin.enroll.prediction.login.RegisterResultActivity");
							intent.putExtra("account", account);
							startActivity(intent);
						} else if ("failure".equals(ans)) {
							Toast.makeText(RegisterUserInfoFragment.this.getContext(), "注册失败！", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(RegisterUserInfoFragment.this.getContext(), "访问失败，稍后再试！", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(RegisterUserInfoFragment.this.getContext(), "访问错误，稍后再试！", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					Log.v("wuxin-login", e.toString());
				} catch (Exception e) {
					Log.v("wuxin-login", e.toString());
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		account = this.getActivity().getIntent().getStringExtra("account");
		helper = new DBHelper(getContext(), 2);
		mylogin = new DBOperate(helper);
		View root = inflater.inflate(R.layout.fragment_register_userinfo, container, false);
		mPassword = (EditText) root.findViewById(R.id.password);
		mPasswordAgain = (EditText) root.findViewById(R.id.password_again);
		mAlaisName = (EditText) root.findViewById(R.id.name);
		mFinish = (Button) root.findViewById(R.id.register_complete);
		mFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pass1 = mPassword.getText().toString();
				String pass2 = mPasswordAgain.getText().toString();
				String aname = mAlaisName.getText().toString();
				if (pass1 == null || pass1.length() < 6 || pass1.length() > 16) {
					Toast.makeText(RegisterUserInfoFragment.this.getContext(), "密码不合理！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (pass2 == null || !pass1.equals(pass2)) {
					Toast.makeText(RegisterUserInfoFragment.this.getContext(), "两次密码不一致！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (aname == null || aname.length() < 2) {
					Toast.makeText(RegisterUserInfoFragment.this.getContext(), "昵称不能少于2个字！", Toast.LENGTH_SHORT).show();
					return;
				}
				new HttpThread(new DoRegisterAccount()).start();
			}
		});
		return root;
	}

	class DoRegisterAccount implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(Tools.REQUEST_CODE, Tools.REGISTER_ACCOUNT_REQ);
				obj.put("account", account);
				obj.put("password", mPassword.getText().toString());
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
				msg.what = LOGIN_ACCOUNT;
				msg.obj = new String(result, 0, result.length, "GBK");
				mHandler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				Log.v("wuxin-login", e.toString());
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
