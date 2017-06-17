package wuxin.enroll.prediction;

import java.text.NumberFormat;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import wuxin.enroll.prediction.ObjectItem.MajorItem;

public class SmartSchoolMajorAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<MajorItem> mMajorItems;
	
	public SmartSchoolMajorAdapter(Context context, ArrayList<MajorItem> items) {
		this.mContext = context;
		this.mMajorItems = items;
	}

	@Override
	public int getCount() {
		return mMajorItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mMajorItems.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.predict_major_item, null);
			holder = new ViewHolder();
			holder.major = (TextView) convertView.findViewById(R.id.major);
			holder.school = (TextView) convertView.findViewById(R.id.school);
			holder.oldavg = (TextView) convertView.findViewById(R.id.oldavg);
			holder.oldmax = (TextView) convertView.findViewById(R.id.oldmax);
			holder.rateText = (TextView) convertView.findViewById(R.id.rateText);
			holder.rate = (TextView) convertView.findViewById(R.id.rate);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MajorItem item = mMajorItems.get(position);
		if(item.name != null) {
			holder.major.setText(item.name);
		} else {
			holder.major.setText("");
		}
		if(item.school != null) {
			holder.school.setText(item.school);
		} else {
			holder.school.setText("");
		}
		if(item.max != null) {
			holder.oldmax.setText("15年最高分 "+item.max);
		} else {
			holder.oldmax.setText("");
		}
		if(item.avg != null) {
			holder.oldavg.setText("15年平均分 "+item.avg);
		} else {
			holder.oldavg.setText("");
		}
		if(item.rate < -1) {
			holder.rateText.setVisibility(View.GONE);
			holder.rate.setVisibility(View.GONE);
		} else {
			holder.rateText.setVisibility(View.VISIBLE);
			NumberFormat nt = NumberFormat.getPercentInstance();
			nt.setMinimumFractionDigits(2); //设置百分数精确度2即保留两位小数
			String rate_str = item.rate == -1 ? " - " : nt.format(item.rate);
			holder.rate.setText(rate_str);
			holder.rate.setVisibility(View.VISIBLE);
		}
		return convertView;
	}
	
	class ViewHolder {
		TextView major;
		TextView school;
		TextView oldavg;
		TextView oldmax;
		TextView rateText;
		TextView rate;
	}

}
