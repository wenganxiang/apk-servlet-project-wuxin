package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import wuxin.enroll.prediction.utils.PublicInfo;
import wuxin.enroll.prediction.utils.Tools;

public class GKScoreFragment extends Fragment implements OnClickListener {

	private static final int UPDATE_GKSCORE = 0x1001;
	private static final int SHOW_PROGRESS = 0x1002;
	private static final int HIDE_PROGRESS = 0x1003;
	
	private Activity mActivity;
	private TextView mImputScore;
	private TextView mImputOrder;
	private TextView mRegionClassText;
	private EditText mScoreOrOrder;
	private RelativeLayout mRegionClass;
	private ProgressDialog mProgressView;
	private Button mSetOK;
	
	private int mInputValue;
	private String mScore = null;
	private String mOrder = null;
	private boolean isScore = true;
	private SQLiteOpenHelper helper;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_PROGRESS:
				if (mProgressView == null) {
					mProgressView = new ProgressDialog(GKScoreFragment.this.getContext());
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
			case UPDATE_GKSCORE:
				if (mProgressView != null) {
					mProgressView.hide();
					mProgressView = null;
				}
				String jsStr = (String) msg.obj;
				JSONObject jsObj = null;
				try {
					jsObj = new JSONObject(jsStr);
					String ans = jsObj.getString("ans");
					String account = jsObj.getString("account");
					int inputValue = jsObj.getInt("input-value");
					boolean isByScore = jsObj.getBoolean("by-score");
					if ("success".equals(ans) && !"null".equals(account)) {
						SQLiteDatabase db = helper.getWritableDatabase();
						if (isByScore) {
							db.execSQL("UPDATE " + DBHelper.ACCOUNT_TABLE + " SET " + DBHelper.SCORE + "=" + inputValue
									+ " WHERE " + DBHelper.ACCOUNT + "=" + account);
							LoginAccount.getInstance().setScore(inputValue);
						} else {
							db.execSQL("UPDATE " + DBHelper.ACCOUNT_TABLE + " SET " + DBHelper.ORDER + "=" + inputValue
									+ " WHERE " + DBHelper.ACCOUNT + "=" + account);
							LoginAccount.getInstance().setOrder(inputValue);
						}
						db.close();
					} else {
						Toast.makeText(mActivity.getApplicationContext(), "请求错误！", Toast.LENGTH_LONG).show();
					}
					mActivity.finish();
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
		mActivity = getActivity();
		helper = new DBHelper(getContext(), 2);
		View root = inflater.inflate(R.layout.fragment_set_score, container, false);
		mImputScore = (TextView) root.findViewById(R.id.tv_score);
		mImputOrder = (TextView) root.findViewById(R.id.tv_order);
		mScoreOrOrder = (EditText) root.findViewById(R.id.scoreOrOrder);
		mRegionClass = (RelativeLayout) root.findViewById(R.id.regionAndClass);
		mRegionClassText = (TextView) root.findViewById(R.id.regionAndClassText);
		mSetOK = (Button) root.findViewById(R.id.btn_ok);
		mSetOK.setOnClickListener(this);
		mImputScore.setOnClickListener(this);
		mImputOrder.setOnClickListener(this);
		mRegionClass.setOnClickListener(this);
		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		String text = LoginAccount.getInstance().getAccount();
		int clazz = LoginAccount.getInstance().getClazz();
		if (text != null && clazz != -1) {
			String str = null;
			if ( clazz == 1 ) {
				str = "湖南 | 理科";
				if (PublicInfo.getInstance().getLKLine() != -1) {
					str = "湖南 | 理科，一档线" + PublicInfo.getInstance().getLKLine() + "分";
				}
			} else {
				str = "湖南 | 文科";
				if (PublicInfo.getInstance().getWKLine() != -1) {
					str = "湖南 | 文科，一档线" + PublicInfo.getInstance().getWKLine() + "分";
				}
			}
			mRegionClassText.setText(str);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_score:
			isScore = true;
			mOrder = mScoreOrOrder.getText().toString();
			mScoreOrOrder.setText(mScore);
			mScoreOrOrder.setHint("输入高考预估分数");
			mImputScore.setTextColor(ContextCompat.getColor(getContext(), R.color.whites));
			mImputScore.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sky_blue));
			mImputOrder.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
			mImputOrder.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
			break;
		case R.id.tv_order:
			isScore = false;
			mScore = mScoreOrOrder.getText().toString();
			mScoreOrOrder.setText(mOrder);
			mScoreOrOrder.setHint("输入高考预估排名");
			mImputScore.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
			mImputScore.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
			mImputOrder.setTextColor(ContextCompat.getColor(getContext(), R.color.whites));
			mImputOrder.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sky_blue));
			break;
		case R.id.regionAndClass:
			startActivity(new Intent("wuxin.enroll.prediction.GKClassActivity"));
			break;
		case R.id.btn_ok:
			String sOo = mScoreOrOrder.getText().toString();
			String acc = LoginAccount.getInstance().getAccount();
			if (acc == null) {
				Toast.makeText(getContext(), "您还未登录！", Toast.LENGTH_LONG).show();
				startActivity(new Intent("wuxin.enroll.prediction.login.LoginActivity"));
				this.getActivity().finish();
				return;
			}
			if (LoginAccount.getInstance().getClazz() == -1) {
				Toast.makeText(getContext(), "您还未设定科目！", Toast.LENGTH_LONG).show();
				return;
			}
			if (sOo != null && sOo.length() > 0) {
				mInputValue = Integer.parseInt(sOo);
				if (isScore) {
					if (mInputValue < 0 || mInputValue > 750) {
						Toast.makeText(getContext(), "高考总分需在0~750分之间！", Toast.LENGTH_LONG).show();
						return;
					}
				} else {
					if (mInputValue <= 0) {
						Toast.makeText(getContext(), "排名必须大于0！", Toast.LENGTH_LONG).show();
						return;
					}
				}
				new HttpThread(new DoUpdateScore()).start();
			} else {
				Toast.makeText(getContext(), "输入有误！", Toast.LENGTH_LONG).show();
			}
			break;
		}
	}
	
	class DoUpdateScore implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(Tools.REQUEST_CODE, Tools.UPDATE_SCORE_REQ);
				obj.put("account", "null");
				obj.put("account", LoginAccount.getInstance().getAccount());
				obj.put("by-score", isScore);
				obj.put("input-value", mInputValue);
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
				msg.what = UPDATE_GKSCORE;
				msg.obj = new String(result, 0, result.length, "GBK");
				mHandler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				Log.v("wuxin-login", e.toString());
			}
		}
	}
	
}