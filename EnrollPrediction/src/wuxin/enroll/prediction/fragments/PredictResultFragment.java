package wuxin.enroll.prediction.fragments;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import wuxin.enroll.prediction.ObjectItem;
import wuxin.enroll.prediction.ObjectItem.SchoolItem;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.SmartSchoolAdapter;
import wuxin.enroll.prediction.SmartSchoolMajorAdapter;
import wuxin.enroll.prediction.db.DBHelper;
import wuxin.enroll.prediction.db.DBOperate;
import wuxin.enroll.prediction.http.DoHttpIO;
import wuxin.enroll.prediction.http.HttpThread;
import wuxin.enroll.prediction.utils.ProgressDialog;
import wuxin.enroll.prediction.utils.Tools;

public class PredictResultFragment extends Fragment {

	private static final int QUERY_RESULT = 0x1001;
	private static final int SHOW_PROGRESS = 0x1002;
	private static final int HIDE_PROGRESS = 0x1003;
	private static final int RESULT_READED = 0x1004;

	private RelativeLayout mColumns;
	private ListView mList;

	private int mType;
	private Context mContext;
	// private Activity mActivity;
	private SQLiteOpenHelper helper;
	private DBOperate mDBOperate;

	private ArrayList<ObjectItem> mSchoolItems;
	private SmartSchoolAdapter mPredictSchoolAdapter;

	private ProgressDialog mProgressView;
	private boolean mShowed = false;

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
			case QUERY_RESULT:
				String jsStr = (String) msg.obj;
				JSONObject jsObj = null;
				try {
					jsObj = new JSONObject(jsStr);
					String ans = jsObj.getString("ans");
					final JSONArray jsArray = jsObj.getJSONArray("result");
					if ("success".equals(ans)) {
						new Thread(new Runnable() {
							public void run() {
								mSchoolItems = Tools.getSchoolItemsFromJSONArray(jsArray);
								try {
									Tools.quickSortObjectItems(mSchoolItems, 0, mSchoolItems.size() - 1);
								} catch (Exception e) {
									e.printStackTrace();
								}
								mHandler.obtainMessage(RESULT_READED).sendToTarget();
							}
						}).start();
					} else {
						if (mProgressView != null) {
							mProgressView.hide();
							mProgressView = null;
						}
						showTextView("网络错误，查询失败，请稍后再试！");
					}
				} catch (JSONException e) {
					Log.v("wuxin-login", e.toString());
				} catch (Exception e) {
					Log.v("wuxin-login", e.toString());
				}
				break;
			case RESULT_READED:
				if (mProgressView != null) {
					mProgressView.hide();
					mProgressView = null;
					mShowed = true;
				}
				mPredictSchoolAdapter = new SmartSchoolAdapter(mContext, mSchoolItems);
				mList.setAdapter(mPredictSchoolAdapter);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getContext();
		// mActivity = getActivity();
		helper = new DBHelper(mContext, 2);
		mDBOperate = new DBOperate(helper);
		mType = getActivity().getIntent().getIntExtra("predict_type", 0);
		View root = inflater.inflate(R.layout.predict_list_fragment, container, false);
		mList = (ListView) root.findViewById(R.id.predict_list);
		mColumns = (RelativeLayout) root.findViewById(R.id.table_columns);

		if (mType == 0) {
			mColumns.setVisibility(View.GONE);
			mList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					SchoolItem item = (SchoolItem) mSchoolItems.get(position);
					Intent intent = new Intent();
					intent.putExtra("predict_type", 1);
					intent.putExtra("school_item", item);
					intent.setAction("wuxin.enroll.prediction.PredictResultActivity");
					startActivity(intent);
				}
			});
		} else if (mType == 1) {
			SchoolItem sItem = getActivity().getIntent().getParcelableExtra("school_item");
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			View view = inflater.inflate(R.layout.predict_major_head, null);
			view.setLayoutParams(lp);
			//
			TextView schoolName = (TextView) view.findViewById(R.id.school_name);
			schoolName.setText(sItem.name);
			TextView schoolRegion = (TextView) view.findViewById(R.id.region);
			schoolRegion.setText(sItem.region);
			TextView schoolOldMin = (TextView) view.findViewById(R.id.oldmin);
			schoolOldMin.setText("16年最低分 " + sItem.min);
			TextView schoolOldOrder = (TextView) view.findViewById(R.id.oldorder);
			schoolOldOrder.setText("16年最低排名  " + mDBOperate.orderSearch(2016, sItem.min));
			//
			ImageView iconRight = (ImageView) view.findViewById(R.id.iconRight);
			iconRight.setImageResource(R.drawable.nan);
			if (sItem.rate >= 0.9) {
				iconRight.setImageResource(R.drawable.bao);
			} else if (sItem.rate >= 0.7 && sItem.rate < 0.9) {
				iconRight.setImageResource(R.drawable.wen);
			} else if (sItem.rate >= 0.5 && sItem.rate < 0.7) {
				iconRight.setImageResource(R.drawable.chong);
			}
			TextView schoolRate = (TextView) view.findViewById(R.id.rate);
			NumberFormat nt = NumberFormat.getPercentInstance();
			nt.setMinimumFractionDigits(2); // 设置百分数精确度2即保留两位小数
			String rate_str = sItem.rate == -1 ? " - " : nt.format(sItem.rate);
			schoolRate.setText("录取概率 " + rate_str);

			SmartSchoolMajorAdapter adapter = new SmartSchoolMajorAdapter(mContext, sItem.majors);
			mList.setAdapter(adapter);
			mColumns.addView(view);
		}
		return root;
	}

	private void showTextView(String text) {
		RelativeLayout.LayoutParams LP1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		LP1.addRule(RelativeLayout.CENTER_IN_PARENT);
		TextView textView = new TextView(mContext);
		textView.setLayoutParams(LP1);
		textView.setText(text);
		textView.setTextColor(0xff444444);
		textView.setTextSize(20);
		LayoutParams LP2 = mColumns.getLayoutParams();
		LP2.height = LayoutParams.MATCH_PARENT;
		mColumns.setLayoutParams(LP2);
		mColumns.addView(textView);
		mColumns.setVisibility(View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mShowed && mType == 0) {
			new HttpThread(new DoQueryItems()).start();
			// if (mProgressView == null) {
			// mProgressView = new ProgressDialog(mContext);
			// }
			// mProgressView.show();
			// new Thread(new Runnable() {
			// @Override
			// public void run() {
			// mSchoolItems = mDBOperate.getSchoolItems();
			// mHandler.obtainMessage(0x1001).sendToTarget();
			// }
			// }).start();
		}
	}

	class DoQueryItems implements DoHttpIO {
		@Override
		public byte[] beforeRequest() {
			byte[] result = null;
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(Tools.REQUEST_CODE, Tools.QUERY_SCHOOLITEMS);
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
				msg.what = QUERY_RESULT;
				msg.obj = new String(result, 0, result.length, "GBK");
				mHandler.sendMessage(msg);
			} catch (UnsupportedEncodingException e) {
				Log.v("wuxin-login", e.toString());
			}
		}
	}
}
