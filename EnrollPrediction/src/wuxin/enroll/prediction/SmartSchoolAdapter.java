package wuxin.enroll.prediction;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import wuxin.enroll.prediction.ObjectItem.SchoolItem;

public class SmartSchoolAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ObjectItem> mSchoolItems;
	
	public SmartSchoolAdapter(Context context, ArrayList<ObjectItem> items) {
		mContext = context;
		mSchoolItems = items;
	}
	
	@Override
	public int getCount() {
		return mSchoolItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mSchoolItems.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.predict_school_item, null);
			holder = new ViewHolder();
			holder.iconLeft_iv = (ImageView) convertView.findViewById(R.id.iconLeft);
			holder.name_tv = (TextView) convertView.findViewById(R.id.school);
			holder.region_tv = (TextView) convertView.findViewById(R.id.region);
			holder.type_tv = (TextView) convertView.findViewById(R.id.type);
			holder.oldLine_tv = (TextView) convertView.findViewById(R.id.oldLine);
			holder.rate_tv = (TextView) convertView.findViewById(R.id.rate);
			holder.iconRight_iv = (ImageView) convertView.findViewById(R.id.iconRight);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SchoolItem item = (SchoolItem) mSchoolItems.get(position);
		if (item.IconID != -1) {
			holder.iconLeft_iv.setImageResource(item.IconID);
		} else {
			holder.iconLeft_iv.setVisibility(View.GONE);
		}
		if (item.name != null) {
			holder.name_tv.setText(item.name);
		} else {
			holder.name_tv.setText("---");
		}
		if (item.region != null) {
			holder.region_tv.setText("（" +item.region+"）");
		} else {
			holder.region_tv.setText("");
		}
		if (item.s_type != null) {
			holder.type_tv.setText("本科第一批（" +item.s_type+"）");
		} else {
			holder.type_tv.setText("");
		}
		if (item.min != null) {
			holder.oldLine_tv.setText("16年最低分 " +item.min);
		} else {
			holder.oldLine_tv.setText("");
		}
		int color = R.color.gray;
		holder.iconRight_iv.setImageResource(R.drawable.nan);
		if (item.rate >= 0.9) {
			color = R.color.green_stroke;
			holder.iconRight_iv.setImageResource(R.drawable.bao);
		} else if (item.rate >= 0.7 && item.rate < 0.9) {
			color = R.color.yellow;
			holder.iconRight_iv.setImageResource(R.drawable.wen);
		} else if (item.rate >= 0.5 && item.rate < 0.7) {
			color = R.color.red;
			holder.iconRight_iv.setImageResource(R.drawable.chong);
		} else if (item.rate > 0.2 && item.rate <= 0.4) {
			color = R.color.sky_blue;
		} else if (item.rate >= 0 && item.rate <= 0.2){
			color = R.color.gray_font;
		} else {
			color = R.color.black;
		}
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2); //设置百分数精确度2即保留两位小数
		String rate_str = item.rate == -1 ? " - " : nt.format(item.rate);
		holder.rate_tv.setText(rate_str);
		holder.rate_tv.setTextColor(ContextCompat.getColor(mContext, color));
		return convertView;
	}
	
	class ViewHolder {
		ImageView iconLeft_iv;
		TextView name_tv;
		TextView region_tv;
		TextView type_tv;
		TextView oldLine_tv;
		TextView rate_tv;
		ImageView iconRight_iv;
	}
	
}
