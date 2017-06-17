package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.db.DBHelper;
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.LoginAccount;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.Tools;

public class LoginFragment extends Fragment implements OnClickListener {

	private static final int LOGIN_ACCOUNT = 0x1001;
	private static final int UPDATE_REMOTE = 0x1002;
	private static final int SHOW_PROGRESS = 0x1003;
	private static final int HIDE_PROGRESS = 0x1004;

	private Context mContext;
	private RelativeLayout mRl_user;
	private Button mLogin;
	private EditText mNumber;
	private EditText mPassword;
	private TextView mURLText;
	private String oldAccount;
	private String accountInput;
	private String accountImport;
	private String passInput;
	private String passExist;
	private ProgressDialog mProgressView;
	private SQLiteOpenHelper helper;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String jsonStr = null;
			JSONObject jsonObj = null;
			switch (msg.what) {
			case SHOW_PROGRESS:
				if (mProgressView == null) {
					mProgressView = new ProgressDialog(mContext);
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
				try {
					jsonStr = (String) msg.obj;
					jsonObj = new JSONObject(jsonStr);
					passExist = jsonObj.getString("password");
					if (passExist == null) {
						return;
					} else if (passExist.equals("null")) {
						Toast.makeText(mContext, "账号不存在！", Toast.LENGTH_LONG).show();
					} else if (passExist.equals("error")){
						Toast.makeText(mContext, "账号密码不匹配！", Toast.LENGTH_LONG).show();
					} else {
						int clazz = jsonObj.getInt("clazz");
						int score = jsonObj.getInt("score");
						long order = jsonObj.getLong("order");
						String name = jsonObj.getString("name");
						LoginAccount.getInstance().setAccount(mNumber.getText().toString(), mPassword.getText().toString(),
								name, clazz, score, order);
						Intent intent = new Intent();
						intent.setComponent(new ComponentName("wuxin.enroll.prediction", "wuxin.enroll.prediction.MainActivity"));
						startActivity(intent);
						new HttpThread(new DoLoginOK()).start();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case UPDATE_REMOTE:
				try {
					jsonStr = (String) msg.obj;
					jsonObj = new JSONObject(jsonStr);
					Log.v("wuxin-login", jsonObj.getString("ans"));
					// update local db
					String account = jsonObj.getString("account");
					String oldAccount = jsonObj.getString("oldAccount");
					if (!account.equals(oldAccount)) {
						SQLiteDatabase db = helper.getWritableDatabase();
						db.execSQL("UPDATE " + DBHelper.ACCOUNT_TABLE + " SET " +
								DBHelper.LOGIN + "='false' WHERE "
								+ DBHelper.ACCOUNT + "=" + oldAccount);
						db.execSQL("UPDATE " + DBHelper.ACCOUNT_TABLE + " SET " +
								DBHelper.LOGIN + "='true' WHERE "
								+ DBHelper.ACCOUNT + "=" + account);
						db.close();
					}
				} catch (JSONException e) {
					Log.v("wuxin-login", "JSONException:" + e.toString());
				} catch (Exception e) {
					Log.v("wuxin-login", "DBException:" + e.toString());
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = this.getContext();
		helper = new DBHelper(mContext, 2);
		View root = inflater.inflate(R.layout.fragment_login, container, false);
		mLogin = (Button) root.findViewById(R.id.login);
		mLogin.setOnClickListener(this);
		mURLText = (TextView) root.findViewById(R.id.tv_forget_password);
		mURLText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mURLText.setOnClickListener(this);
		mNumber = (EditText) root.findViewById(R.id.account);
		mPassword = (EditText) root.findViewById(R.id.password);
		mRl_user = (RelativeLayout) root.findViewById(R.id.rl_user);
		animShowView();
		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		accountImport = getActivity().getIntent().getStringExtra("account");
		if (accountImport != null) {
			mNumber.setText(accountImport);
		}
	}

	private void animShowView() {
		Animation anim = AnimationUtils.loadAnimation(mContext, R.animator.login_anim);
		anim.setFillAfter(true);
		mRl_user.startAnimation(anim);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			accountInput = mNumber.getText().toString();
			if (accountInput == null || accountInput.length() != 11) {
				Toast.makeText(mContext, "账号必须是11位手机号码！", Toast.LENGTH_LONG).show();
				return;
			}
			passInput = mPassword.getText().toString();
			if (passInput == null || passInput.length() < 6) {
				Toast.makeText(mContext, "密码长度至少是6位！", Toast.LENGTH_LONG).show();
				return;
			}
			oldAccount = LoginAccount.getInstance().getAccount();
			oldAccount = oldAccount == null ? "null" : oldAccount;
			if (oldAccount != null && oldAccount.equals(accountInput)) {
				Toast.makeText(mContext, "该账号已登录！", Toast.LENGTH_LONG).show();
				return;
			}
			new HttpThread(new DoLogin()).start();
			break;
		case R.id.tv_forget_password:
			Intent intent = new Intent("wuxin.enroll.prediction.login.FindPassActivity");
			startActivity(intent);
			break;
		}
	}

	class DoLogin implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = new JSONObject();
			try {
				obj.put(Tools.REQUEST_CODE, Tools.LOGIN_ACCOUNT_REQ);
				obj.put("account", accountInput);
				obj.put("password", passInput);
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
				msg.obj = new String(result, 0, result.length, "GBK");;
				mHandler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				Log.v("wuxin-login", e.toString());
			}
		}
	}
	
	class DoLoginOK implements DoHttpIO  {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = new JSONObject();
			try {
				obj.put(Tools.REQUEST_CODE, Tools.UPDATE_ACCOUNT_REQ);
				obj.put("account", accountInput);
				obj.put("oldAccount", oldAccount);
				result = obj.toString().getBytes("GBK");
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
				msg.what = UPDATE_REMOTE;
				msg.obj = new String(result, 0, result.length, "GBK");;
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
