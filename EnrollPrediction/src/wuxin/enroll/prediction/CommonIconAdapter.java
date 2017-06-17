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

public class CommonIconAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<IconItem> mIcomItems;
	
	public CommonIconAdapter(Context context, ArrayList<IconItem> items) {
		mContext = context;
		mIcomItems = items;
	}
	
	@Override
	public int getCount() {
		return mIcomItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mIcomItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_main_image_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.image);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		IconItem item = mIcomItems.get(position);
		if (item.IconID != -1) {
			holder.icon.setImageResource(item.IconID);
		} else {
			holder.icon.setVisibility(View.GONE);
		}
		if (item.Text != null) {
			holder.text.setText(item.Text);
		} else {
			holder.text.setVisibility(View.GONE);
		}
		if (item.GoTo) {
			holder.arrow.setVisibility(View.VISIBLE);
		} else {
			holder.arrow.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}
	
	class ViewHolder {
		ImageView icon;
		TextView text;
		ImageView arrow;
	}
	
	public static class IconItem {
		public int IconID = -1;
		public String Text = null;
		public boolean GoTo = true;
		public String onClicked;
	}

}
