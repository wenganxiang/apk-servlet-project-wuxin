package wuxin.enroll.prediction.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import wuxin.enroll.prediction.CommonIconAdapter;
import wuxin.enroll.prediction.CommonIconAdapter.IconItem;
import wuxin.enroll.prediction.R;

public class StrategyFragment extends Fragment implements OnItemClickListener{
	
	private final static String Click1st = "ClickEvent1";
	private final static String Click2nd = "ClickEvent2";
	private final static String Click3rd = "ClickEvent3";
	private final static String Click4th = "ClickEvent4";
	
	private Context mContext;
	private ArrayList<IconItem> mIconItems;
	private CommonIconAdapter mAdapter;
	private ListView mIconList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getContext();
		View root = inflater.inflate(R.layout.fragment_main_2nd, container, false);
		RelativeLayout icon = (RelativeLayout) root.findViewById(R.id.topItem1);
		icon.setVisibility(View.INVISIBLE);
		TextView title = (TextView) root.findViewById(R.id.topText2);
		title.setText("高考攻略");
		mIconList = (ListView) root.findViewById(R.id.icon_list);
		mIconList.setOnItemClickListener(this);
		initListItems();
		return root;
	}
	
	private void initListItems() {
		mIconItems = new ArrayList<IconItem>();
		//第1个网址
		IconItem item = new IconItem();
		item.IconID = R.drawable.gkstk;
		item.onClicked = Click1st;
		mIconItems.add(item);
		//第2个网址
		item = new IconItem();
		item.IconID = R.drawable.zxxk;
		item.onClicked = Click2nd;
		mIconItems.add(item);
		//第3个网址
		item = new IconItem();
		item.IconID = R.drawable.gkxx;
		item.onClicked = Click3rd;
		mIconItems.add(item);
		//第4个网址
		item = new IconItem();
		item.IconID = R.drawable.ks5u;
		item.onClicked = Click4th;
		mIconItems.add(item);
		mAdapter = new CommonIconAdapter(mContext, mIconItems);
		mIconList.setAdapter(mAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		IconItem item = mIconItems.get(position);
		if (item == null)
			return;
		if (item.onClicked == null)
			return;
		Intent intent = new Intent();
		switch (item.onClicked) {
		case Click1st:
			intent.putExtra("web_url", "http://www.gkstk.com/");
			break;
		case Click2nd:
			intent.putExtra("web_url", "http://gaokao.zxxk.com/");
			break;
		case Click3rd:
			intent.putExtra("web_url", "http://www.gkxx.com/");
			break;
		case Click4th:
			intent.putExtra("web_url", "http://www.ks5u.com/");
			break;
		}
		intent.setAction("wuxin.enroll.prediction.WebActivity");
		startActivity(intent);
	}
}
