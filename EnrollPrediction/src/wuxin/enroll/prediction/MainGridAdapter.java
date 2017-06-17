package wuxin.enroll.prediction;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainGridAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<GridItem> mGridItems;

	public MainGridAdapter(Context context, ArrayList<GridItem> items) {
		mContext = context;
		mGridItems = items;
	}

	@Override
	public int getCount() {
		return mGridItems.size();
	}

	@Override
	public GridItem getItem(int position) {
		return mGridItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_main_grid_item, null);
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		GridItem item = mGridItems.get(position);

		if (item.itemText != null) {
			holder.text.setText(item.itemText);
		} else {
			holder.text.setText(".");
		}
		if (item.itemImgId != -1) {
			holder.image.setImageResource(item.itemImgId);
		} else {
			holder.image.setBackgroundColor(0xff00ff00);
		}
		return convertView;
	}

	class ViewHolder {
		ImageView image;
		TextView text;
	}

	public static class GridItem {
		public int itemImgId = -1;
		public String itemText;
		public String onClicked;
	}
}
