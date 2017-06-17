package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.db.DBHelper;
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.LoginAccount;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.PublicInfo;
import wuxin.enroll.prediction.utils.Tools;

public class GKClassFragment extends Fragment implements OnClickListener {

	private static final int SHOW_PROGRESS = 0x1001;
	private static final int HIDE_PROGRESS = 0x1002;
	private static final int UPDAT_GKCLASS = 0x1003;
	
	private Button mClassLK;
	private Button mClassWK;
	private Button mBtnChange;
	private CheckBox mCheck;
	private EditText mLine;
	private EditText mOutStand;
	private LinearLayout mMoreInfo;
	private ProgressDialog mProgressView;

	private int clazz = 0;
	private Context mContext;
	private Activity mActivity;
	private SQLiteOpenHelper helper;
	private JSONObject mObj;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
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
			case UPDAT_GKCLASS:
				if (mProgressView != null) {
					mProgressView.hide();
					mProgressView = null;
				}
				String jsStr = (String) msg.obj;
				JSONObject jsObj = null;
				try {
					jsObj = new JSONObject(jsStr);
					String ans = jsObj.getString("ans-clazz");
					SQLiteDatabase db = helper.getWritableDatabase();
					if (!"null".equals(ans)) {
						if (ans.equals("success")) {
							db.execSQL("UPDATE " + DBHelper.ACCOUNT_TABLE + " SET " + DBHelper.CLASS + "=" + clazz + " WHERE "
									+ DBHelper.ACCOUNT + "=" + LoginAccount.getInstance().getAccount());
							LoginAccount.getInstance().setClazz(clazz);
							LoginAccount.getInstance().setOrder(-1);
							LoginAccount.getInstance().setScore(-1);
						} else {
							Toast.makeText(mContext, "请求错误！", Toast.LENGTH_LONG).show();
						}
					}
					ans = jsObj.getString("ans-line");
					if (!"null".equals(ans)) {
						if (ans.equals("success")) {
							String lineKey = jsObj.getString("line-key");
							if (lineKey != null) {
								int lineValue = jsObj.getInt("line-value");
								if (lineKey.equals("lk_line")) {
									PublicInfo.getInstance().setLKLine(lineValue);
								} else {
									PublicInfo.getInstance().setWKLine(lineValue);
								}
								db.execSQL("UPDATE " + DBHelper.SETTINGS + " SET " + DBHelper.KEY_VALUE + "=" + lineValue
										+ " WHERE " + DBHelper.KEY_NAME + "='" + lineKey + "'");
							}
						} else {
							Toast.makeText(mContext, "请求错误！", Toast.LENGTH_LONG).show();
						}
					}
					ans = jsObj.getString("ans-oustand");
					if (!"null".equals(ans)) {
						if (ans != null && ans.equals("success")) {
							String outstandKey = jsObj.getString("oustand-key");
							if (outstandKey != null) {
								int outstandValue = jsObj.getInt("oustand-value");
								if (outstandKey.equals("lk_oustand")) {
									PublicInfo.getInstance().setLKOutstand(outstandValue);
								} else {
									PublicInfo.getInstance().setWKOutstand(outstandValue);
								}
								db.execSQL("UPDATE " + DBHelper.SETTINGS + " SET " + DBHelper.KEY_VALUE + "=" + outstandValue
										+ " WHERE " + DBHelper.KEY_NAME + "='" + outstandKey + "'");
							}
						} else {
							Toast.makeText(mContext, "请求错误！", Toast.LENGTH_LONG).show();
						}
					}
					db.close();
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
		mContext = getContext();
		mActivity = getActivity();
		helper = new DBHelper(mContext, 2);
		clazz = LoginAccount.getInstance().getClazz();
		clazz = clazz == -1 ? 1 : clazz;
		View root = inflater.inflate(R.layout.fragment_set_class, container, false);
		mClassLK = (Button) root.findViewById(R.id.classLK);
		mClassLK.setOnClickListener(this);
		mClassWK = (Button) root.findViewById(R.id.classWK);
		mClassWK.setOnClickListener(this);
		mBtnChange = (Button) root.findViewById(R.id.btn_change);
		mBtnChange.setOnClickListener(this);
		mLine = (EditText) root.findViewById(R.id.ybScore);
		mOutStand = (EditText) root.findViewById(R.id.maxScore);
		mMoreInfo = (LinearLayout) root.findViewById(R.id.moreInfo);
		mCheck = (CheckBox) root.findViewById(R.id.checkBox1);
		mCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mMoreInfo.setVisibility(View.VISIBLE);
				} else {
					mMoreInfo.setVisibility(View.GONE);
				}
			}
		});
		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (clazz == 1) {
			mClassLK.setText("理科√");
			mClassLK.setTextColor(ContextCompat.getColor(mContext, R.color.sky_blue));
			mClassLK.setBackgroundResource(R.drawable.class_btn_normal);
			mClassWK.setText("文科");
			mClassWK.setTextColor(ContextCompat.getColor(mContext, R.color.gray_font));
			mClassWK.setBackgroundResource(R.drawable.class_btn_pressed);
		} else {
			mClassWK.setText("文科√");
			mClassWK.setTextColor(ContextCompat.getColor(mContext, R.color.sky_blue));
			mClassWK.setBackgroundResource(R.drawable.class_btn_normal);
			mClassLK.setText("理科");
			mClassLK.setTextColor(ContextCompat.getColor(mContext, R.color.gray_font));
			mClassLK.setBackgroundResource(R.drawable.class_btn_pressed);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.classLK:
			clazz = 1;
			mClassLK.setText("理科√");
			mClassLK.setTextColor(ContextCompat.getColor(mContext, R.color.sky_blue));
			mClassLK.setBackgroundResource(R.drawable.class_btn_normal);
			mClassWK.setText("文科");
			mClassWK.setTextColor(ContextCompat.getColor(mContext, R.color.gray_font));
			mClassWK.setBackgroundResource(R.drawable.class_btn_pressed);
			break;
		case R.id.classWK:
			clazz = 0;
			mClassWK.setText("文科√");
			mClassWK.setTextColor(ContextCompat.getColor(mContext, R.color.sky_blue));
			mClassWK.setBackgroundResource(R.drawable.class_btn_normal);
			mClassLK.setText("理科");
			mClassLK.setTextColor(ContextCompat.getColor(mContext, R.color.gray_font));
			mClassLK.setBackgroundResource(R.drawable.class_btn_pressed);
			break;
		case R.id.btn_change:
			if (LoginAccount.getInstance().getAccount() == null) {
				Intent intent = new Intent();
				intent.setComponent(
						new ComponentName("wuxin.enroll.prediction", "wuxin.enroll.prediction.MainActivity"));
				startActivity(intent);
				Toast.makeText(mContext, "请先登录账号，再修改科目！", Toast.LENGTH_LONG).show();
			} else {
				try {
					mObj = new JSONObject();
					mObj.put("clazz", -1);
					mObj.put("account", "null");
					mObj.put("line-key", "null");
					mObj.put("oustand-key", "null");
					if (LoginAccount.getInstance().getClazz() != clazz) {
						mObj.put("clazz", clazz);
						mObj.put("account", LoginAccount.getInstance().getAccount());
					}
					if (mCheck.isChecked()) {
						String temp = null;
						String ybscore = mLine.getText().toString();
						String maxscore = mOutStand.getText().toString();
						if (ybscore != null && ybscore.length() > 0) {
							int ybline = Integer.parseInt(ybscore);
							if (ybline >= 400 && ybline <= 750) {
								if (clazz == 1) {
									temp = "lk_line";
								} else {
									temp = "wk_line";
								}
								mObj.put("line-key", temp);
								mObj.put("line-value", ybline);
							} else {
								Toast.makeText(mContext, "输入的分数不合理！", Toast.LENGTH_LONG).show();
								return;
							}
						}
						if (maxscore != null && maxscore.length() > 0) {
							int outstand = Integer.parseInt(maxscore);
							if (outstand >= 400 && outstand <= 750) {
								if (clazz == 1) {
									temp = "lk_oustand";
								} else {
									temp = "wk_oustand";
								}
								mObj.put("oustand-key", temp);
								mObj.put("oustand-value", outstand);
							} else {
								Toast.makeText(mContext, "输入的分数不合理！", Toast.LENGTH_LONG).show();
								return;
							}
						}
					}
					new HttpThread(new DoUpdateClazzInfo()).start();
				} catch (JSONException e) {
					Log.v("wuxin-login", e.toString());
				} catch (Exception e) {
					Log.v("wuxin-login", e.toString());
				}
			} 
			break;
		}
	}

	class DoUpdateClazzInfo implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			try {
				mObj.put(Tools.REQUEST_CODE, Tools.UPDATE_CLAZZ_REQ);
				result = mObj.toString().getBytes("GBK");
				if (result != null  && mObj.length() > 0) {
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
				msg.what = UPDAT_GKCLASS;
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
