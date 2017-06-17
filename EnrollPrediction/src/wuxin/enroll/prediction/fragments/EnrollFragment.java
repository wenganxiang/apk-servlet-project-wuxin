package wuxin.enroll.prediction.fragments;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import wuxin.enroll.prediction.MainGridAdapter;
import wuxin.enroll.prediction.MainGridAdapter.GridItem;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.db.DBHelper;
import wuxin.enroll.prediction.db.DBOperate;
import wuxin.enroll.prediction.utils.LoginAccount;

public class EnrollFragment extends Fragment implements OnItemClickListener {

	private final static String Click1st = "ClickEvent1";
	private final static String Click2nd = "ClickEvent2";
	private final static String Click3rd = "ClickEvent3";
	private final static String Click4th = "ClickEvent4";
	private final static String Click5th = "ClickEvent5";
	private final static String Click6th = "ClickEvent6";
	private final static String Click7th = "ClickEvent7";
	private final static String Click8th = "ClickEvent8";

	private int mCount = 10;
	private View mTopBar;
	private ViewPager mViewPager;
	private ImagePagerAdapter mImagePagerAdapter;
	private RelativeLayout mViewPagerContainer;
	private RelativeLayout mRegionAndClass;
	private TextView mRegionAndClassText;
	private Button mSmartreferrer;

	private GridView mGridView;
	private MainGridAdapter mGridAdapter;
	private ArrayList<GridItem> mGridItems;
	private SQLiteOpenHelper helper;
	private DBOperate mylogin;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		helper = new DBHelper(getContext(), 2);
		mylogin = new DBOperate(helper);
		View root = inflater.inflate(R.layout.fragment_main_1st, container, false);
		mViewPagerContainer = (RelativeLayout) root.findViewById(R.id.gallery_container);
		mViewPager = (ViewPager) root.findViewById(R.id.gallery_pager);
		mImagePagerAdapter = new ImagePagerAdapter(this.getContext());
		mViewPager.setAdapter(mImagePagerAdapter);
		mViewPager.setOffscreenPageLimit(mCount);
		mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
		mRegionAndClassText = (TextView) root.findViewById(R.id.regionAndClassText);
		mRegionAndClass = (RelativeLayout) root.findViewById(R.id.regionAndClass);
		mRegionAndClass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (LoginAccount.getInstance().getAccount() != null) {
					Intent intent = new Intent("wuxin.enroll.prediction.GKScoreActivity");
					startActivity(intent);
				} else {
					Toast.makeText(getContext(), "请先登录！", Toast.LENGTH_LONG).show();
				}
			}
		});
		mSmartreferrer = (Button) root.findViewById(R.id.smartReferrer);
		mSmartreferrer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(checkInput()){
					Intent intent = new Intent();
					intent.putExtra("predict_type", 0);
					intent.setAction("wuxin.enroll.prediction.PredictResultActivity");
					startActivity(intent);
				}
			}
		});
		mTopBar = inflater.inflate(R.layout.ly_topbar, null);
		mTopBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		TextView title = (TextView) mTopBar.findViewById(R.id.topText2);
		title.setText("志愿指南");
		title.setTextColor(0xffffffff);
		title.setTextSize(20);
		RelativeLayout icon = (RelativeLayout) mTopBar.findViewById(R.id.topItem1);
		icon.setVisibility(View.INVISIBLE);

		TextView text = new TextView(getContext());
		text.setText("备选志愿");
		text.setTextColor(0xffffffff);
		text.setTextSize(14);
		RelativeLayout func = (RelativeLayout) mTopBar.findViewById(R.id.topItem3);
		func.addView(text);
		mTopBar.setBackgroundColor(0x33333333);
		mViewPagerContainer.addView(mTopBar);

		initItems();
		mGridAdapter = new MainGridAdapter(getContext(), mGridItems);
		mGridView = (GridView) root.findViewById(R.id.gridView);
		mGridView.setAdapter(mGridAdapter);
		mGridView.setOnItemClickListener(this);

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		String text = LoginAccount.getInstance().getAccount();
		int clazz = LoginAccount.getInstance().getClazz();
		if (text != null && clazz != -1) {
			String str = LoginAccount.getInstance().getClazz() == 1 ? "理科" : "文科";
			if (LoginAccount.getInstance().getScore() != -1) {
				str += " " + LoginAccount.getInstance().getScore() + "分";
			}
			if (LoginAccount.getInstance().getOrder() != -1) {
				str += " " + LoginAccount.getInstance().getOrder() + "名";
			}
			mRegionAndClassText.setText("湖南 | " + str);
		} else {
			mRegionAndClassText.setText("省份 | 分科");
		}
	}

	private void initItems() {
		mGridItems = new ArrayList<GridItem>();
		GridItem item = new GridItem();
		item.itemImgId = R.drawable.schools;
		item.itemText = "能上的学校";
		item.onClicked = Click1st;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.majors;
		item.itemText = "能上的专业";
		item.onClicked = Click2nd;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.get_aid;
		item.itemText = "按钮3";
		item.onClicked = Click3rd;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.get_aid;
		item.itemText = "按钮4";
		item.onClicked = Click4th;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.get_aid;
		item.itemText = "按钮5";
		item.onClicked = Click5th;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.get_aid;
		item.itemText = "按钮6";
		item.onClicked = Click6th;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.get_aid;
		item.itemText = "按钮7";
		item.onClicked = Click7th;
		mGridItems.add(item);
		item = new GridItem();
		item.itemImgId = R.drawable.get_aid;
		item.itemText = "按钮8";
		item.onClicked = Click8th;
		mGridItems.add(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		GridItem item = mGridItems.get(position);
		if (item == null)
			return;
		if (item.onClicked == null)
			return;
		Toast.makeText(getContext(), "功能待开放", Toast.LENGTH_SHORT).show();
		switch (item.onClicked) {
		case Click1st:
//			if(checkInput()){
//				Intent intent = new Intent();
//				intent.putExtra("predict_type", 0);
//				intent.setAction("wuxin.enroll.prediction.PredictResultActivity");
//				startActivity(intent);
//			}
			break;
		case Click2nd:
//			if(checkInput()){
//				Intent intent = new Intent();
//				intent.putExtra("predict_type", 1);
//				intent.setAction("wuxin.enroll.prediction.PredictResultActivity");
//				startActivity(intent);
//			}
			break;
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean checkInput(){
		if ((LoginAccount.getInstance().getScore() == -1 
				&& LoginAccount.getInstance().getOrder() == -1)
				|| LoginAccount.getInstance().getClazz() == -1) {
			Toast.makeText(getContext(), "请先输入高考科目,以及预测分数或排名！",
					Toast.LENGTH_LONG).show();
			return false;
		}
		if (mylogin.scoreInPastYear(2016) <= 0 ) {
			Toast.makeText(getContext(), "您的输入的分数或者排名有误，系统无法进行预测！",
					Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
		}

		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (mViewPagerContainer != null) { // to refresh frameLayout
				mViewPagerContainer.invalidate();
			}
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	}

	class ImagePagerAdapter extends PagerAdapter {

		Resources mRes;
		Context mContext;
		LayoutInflater mLayoutInflater;

		public ImagePagerAdapter(Context context) {
			mContext = context;
			mRes = mContext.getResources();
			mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mCount;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			int resId = mRes.getIdentifier("res_".concat(String.valueOf(position + 1)), "drawable",
					mContext.getPackageName());
			ImageView imageView = new ImageView(mContext);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setImageResource(resId);
			((ViewPager) container).addView(imageView, 0);
			// ((ViewPager) container).addView(imageView, position);//这样会报错
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}
}
