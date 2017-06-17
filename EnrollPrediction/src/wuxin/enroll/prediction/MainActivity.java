package wuxin.enroll.prediction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import wuxin.enroll.prediction.db.DBHelper;
import wuxin.enroll.prediction.fragments.EnrollFragment;
import wuxin.enroll.prediction.fragments.MeFragment;
import wuxin.enroll.prediction.fragments.StrategyFragment;
import wuxin.enroll.prediction.utils.LoginAccount;
import wuxin.enroll.prediction.utils.PublicInfo;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private DBHelper helper;
	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private List<Fragment> mFragments = new ArrayList<Fragment>();

	// 底部三个按钮
	private LinearLayout mTabBtnEnroll;
	private LinearLayout mTabBtnStrate;
	private LinearLayout mTabBtnMe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		LoginAccount.getInstance();
		try {
			initDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initViews();
		mViewPager = (ViewPager) findViewById(R.id.id_ly_container);
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mFragments.get(arg0);
			}
		};
		mViewPager.setAdapter(mAdapter);
		mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
		// startActivity(new
		// Intent("wuxin.enroll.prediction.login.FirstADPage"));
	}

	private void initViews() {

		mTabBtnEnroll = (LinearLayout) findViewById(R.id.bottomItem1);
		mTabBtnStrate = (LinearLayout) findViewById(R.id.bottomItem2);
		mTabBtnMe = (LinearLayout) findViewById(R.id.bottomItem3);

		mTabBtnEnroll.setOnClickListener(this);
		mTabBtnStrate.setOnClickListener(this);
		mTabBtnMe.setOnClickListener(this);

		EnrollFragment enrollTab = new EnrollFragment();
		StrategyFragment strategyTab = new StrategyFragment();
		MeFragment meTab = new MeFragment();

		mFragments.add(enrollTab);
		mFragments.add(strategyTab);
		mFragments.add(meTab);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bottomItem1:
			mViewPager.setCurrentItem(0);
			break;
		case R.id.bottomItem2:
			mViewPager.setCurrentItem(1);
			break;
		case R.id.bottomItem3:
			mViewPager.setCurrentItem(2);
			break;
		}

	}

	protected void resetTabBtn() {
		((ImageView) findViewById(R.id.bottomImg1)).setImageResource(R.drawable.enroll_unpress);
		((ImageView) findViewById(R.id.bottomImg2)).setImageResource(R.drawable.strategy_unpreess);
		((ImageView) findViewById(R.id.bottomImg3)).setImageResource(R.drawable.me_unpress);
	}

	class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
		}

		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			resetTabBtn();
			switch (position) {
			case 0:
				((ImageView) findViewById(R.id.bottomImg1)).setImageResource(R.drawable.enroll_pressed);
				break;
			case 1:
				((ImageView) findViewById(R.id.bottomImg2)).setImageResource(R.drawable.strategy_pressed);
				break;
			case 2:
				((ImageView) findViewById(R.id.bottomImg3)).setImageResource(R.drawable.me_pressed);
				break;
			}
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	}

	private void initDatabase() {
		helper = new DBHelper(this, 1);
		try {
			helper.createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		helper = new DBHelper(this, 2);// 升级数据库，加入用户表
		SQLiteDatabase db = helper.getReadableDatabase();
		String sql = "SELECT * FROM " + DBHelper.ACCOUNT_TABLE + " WHERE (" + DBHelper.LOGIN + " = ? ) ";
		String args[] = new String[] { "true" };
		Cursor cursor = db.rawQuery(sql, args);
		if (cursor != null && cursor.moveToFirst()) {
			String acc = cursor.getString(1);
			String pass = cursor.getString(2);
			String name = cursor.getString(3);
			int clazz = cursor.getInt(5);
			int score = cursor.getInt(6);
			long order = cursor.getLong(7);
			LoginAccount.getInstance().setAccount(acc, pass, name, clazz, score, order);
		}
		sql = "SELECT * FROM " + DBHelper.SETTINGS;
		cursor = db.rawQuery(sql, null);
		if (cursor == null || !cursor.moveToFirst()) {
			sql = "INSERT INTO " + DBHelper.SETTINGS + "(" + DBHelper.KEY_NAME + ","
					+ DBHelper.KEY_VALUE + ") VALUES (?,?)";// SQL语句
			Object args1[] = null;
			args1 = new Object[] { "lk_line", "-1" }; 
			db.execSQL(sql, args1);
			args1 = new Object[] { "wk_line", "-1" }; 
			db.execSQL(sql, args1);
			args1 = new Object[] { "lk_oustand", "-1" }; 
			db.execSQL(sql, args1);
			args1 = new Object[] { "wk_oustand", "-1" }; 
			db.execSQL(sql, args1);
		}
		sql = "SELECT * FROM " + DBHelper.SETTINGS;
		cursor = db.rawQuery(sql, null);
		String name, key;
		if (cursor != null && cursor.moveToFirst()) {
			do {
				name = cursor.getString(1);
				key = cursor.getString(2);
				if ("lk_line".equals(name)) {
					PublicInfo.getInstance().setLKLine(Integer.parseInt(key));
				} else if ("wk_line".equals(name)) {
					PublicInfo.getInstance().setWKLine(Integer.parseInt(key));
				} else if ("lk_oustand".equals(name)) {
					PublicInfo.getInstance().setLKOutstand(Integer.parseInt(key));
				} else if ("wk_oustand".equals(name)) {
					PublicInfo.getInstance().setWKOutstand(Integer.parseInt(key));
				}
			} while (cursor.moveToNext());
		}
		cursor.close(); // 关闭数据库连接
		db.close();
	}
}
