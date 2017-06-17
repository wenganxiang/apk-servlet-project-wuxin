package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
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
import android.widget.TextView;
import android.widget.Toast;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.db.DBOperate;
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.Tools;
import wuxin.enroll.prediction.db.DBHelper;

public class ChangePassFragment extends Fragment {

	private static final int RESET_PASSWORD = 0x1001;
	private static final int SHOW_PROGRESS = 0x1002;
	private static final int HIDE_PROGRESS = 0x1003;

	private Button mChange;
	private TextView mAccount;
	private EditText mPassNew1;
	private EditText mPassNew2;
	private ProgressDialog mProgressView;

	private String account = null;
	private String passnew1 = null;
	private String passnew2 = null;

	private SQLiteOpenHelper helper;
	private DBOperate mylogin;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_PROGRESS:
				if (mProgressView == null) {
					mProgressView = new ProgressDialog(ChangePassFragment.this.getContext());
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
			case RESET_PASSWORD:
				if (mProgressView != null) {
					mProgressView.hide();
					mProgressView = null;
				}
				String jsStr = (String) msg.obj;
				JSONObject jsObj = null;
				try {
					jsObj = new JSONObject(jsStr);
					String ans = jsObj.getString("ans");
					if ("success".equals(ans)) {
						mylogin.updateAccount(account, passnew1);
						Toast.makeText(ChangePassFragment.this.getContext(), "修改密码成功！", Toast.LENGTH_LONG).show();
						Intent intent = new Intent("wuxin.enroll.prediction.login.LoginActivity");
						intent.putExtra("account", account);
						startActivity(intent);
					} else {
						Toast.makeText(ChangePassFragment.this.getContext(), "修改密码出错！", Toast.LENGTH_LONG).show();
						Intent intent = new Intent();
						intent.setComponent(
								new ComponentName("wuxin.enroll.prediction", "wuxin.enroll.prediction.MainActivity"));
						startActivity(intent);
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
		helper = new DBHelper(getContext(), 2);
		mylogin = new DBOperate(helper);
		View root = inflater.inflate(R.layout.fragment_pass_change, container, false);
		mAccount = (TextView) root.findViewById(R.id.changePwId);
		mPassNew1 = (EditText) root.findViewById(R.id.changePwNew);
		mPassNew2 = (EditText) root.findViewById(R.id.changePwNewAgain);
		mChange = (Button) root.findViewById(R.id.start_change);
		mChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				passnew1 = mPassNew1.getText().toString();
				passnew2 = mPassNew2.getText().toString();
				if (passnew1 == null || passnew1.length() < 6 || passnew1.length() > 16) {
					Toast.makeText(ChangePassFragment.this.getContext(), "密码设置不合理！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (passnew2 == null || !passnew2.equals(passnew1)) {
					Toast.makeText(ChangePassFragment.this.getContext(), "两次密码不一致！", Toast.LENGTH_SHORT).show();
					return;
				}
				new HttpThread(new DoChangePassword()).start();
			}
		});
		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		account = getActivity().getIntent().getStringExtra("account");
		if (account != null) {
			mAccount.setText(account);
		} else {
			mAccount.setText("error");
			mChange.setVisibility(View.INVISIBLE);
			mPassNew1.setVisibility(View.INVISIBLE);
			mPassNew2.setVisibility(View.INVISIBLE);
		}
	}

	class DoChangePassword implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(Tools.REQUEST_CODE, Tools.RESET_PASSWORD_REQ);
				obj.put("account", account);
				obj.put("password", passnew1);
				result = obj.toString().getBytes("GBK");
				if (result != null && result.length > 0) {
					mHandler.obtainMessage(SHOW_PROGRESS).sendToTarget();// 非UI线程不能更新UI
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
				msg.what = RESET_PASSWORD;
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
