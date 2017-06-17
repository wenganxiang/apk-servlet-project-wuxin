package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.Tools;

public class FindPassFregment extends Fragment {

	private static final int FIND_ACCOUNT = 0x1001;
	private static final int SHOW_PROGRESS = 0x1002;
	private static final int HIDE_PROGRESS = 0x1003;
	
	private EditText mAccount;
	private EditText mAutjCode;
	private Button mFind;
	private ProgressDialog mProgressView;
	
	private String account = null;
	private String code = null;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_PROGRESS:
				if (mProgressView == null) {
					mProgressView = new ProgressDialog(FindPassFregment.this.getContext());
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
			case FIND_ACCOUNT:
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
						if ("registed".equals(ans)) {
							Intent intent = new Intent("wuxin.enroll.prediction.login.ChangePassActivity");
							intent.putExtra("account", account);
							startActivity(intent);
						} else if ("dbError".equals(ans)) {
							Toast.makeText(FindPassFregment.this.getContext(), "访问失败，稍后再试！", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(FindPassFregment.this.getContext(), "该账号未注册！", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(FindPassFregment.this.getContext(), "访问错误，稍后再试！", Toast.LENGTH_SHORT).show();
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
		View root = inflater.inflate(R.layout.fragment_pass_find, container, false);
		mAccount = (EditText) root.findViewById(R.id.findPasswId);
		mAutjCode = (EditText) root.findViewById(R.id.findPasswAuth);
		mFind = (Button) root.findViewById(R.id.start_find);
		mFind.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				account = mAccount.getText().toString();
				code = mAutjCode.getText().toString();
				if (account == null || account.length() != 11) {
					Toast.makeText(FindPassFregment.this.getContext(), "请输入11位手机号码！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (code == null || code.length() < 1) {
					Toast.makeText(FindPassFregment.this.getContext(), "验证码有误！", Toast.LENGTH_SHORT).show();
					return;
				}
				new HttpThread(new DoFindAccount()).start();
			}
		});
		return root;
	}

	class DoFindAccount implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(Tools.REQUEST_CODE, Tools.FIND_ACCOUNT_REQ);
				obj.put("account", account);
				obj.put("code", code);
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
				msg.what = FIND_ACCOUNT;
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
