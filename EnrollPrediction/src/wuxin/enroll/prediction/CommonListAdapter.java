package wuxin.enroll.prediction;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonListAdapter extends BaseAdapter {

	private ArrayList<CommonListItem> mListItems;
	private Context mContext;

	public CommonListAdapter(Context context, ArrayList<CommonListItem> listItems) {
		mContext = context;
		mListItems = listItems;
	}

	public void updateItems(ArrayList<CommonListItem> listItems) {
		mListItems = listItems;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	@Override
	public CommonListItem getItem(int index) {
		// TODO Auto-generated method stub
		return mListItems.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.common_list_item, null);
			holder.item = convertView.findViewById(R.id.common_list_item);
			holder.right = (ImageView) convertView.findViewById(R.id.common_list_item_img_right);
			holder.left = (ImageView) convertView.findViewById(R.id.common_list_item_img_left);
			holder.title = (TextView) convertView.findViewById(R.id.common_list_item_title);
			holder.text = (TextView) convertView.findViewById(R.id.common_list_item_text);
			holder.line = convertView.findViewById(R.id.common_list_item_line);
			holder.lineFull = convertView.findViewById(R.id.common_list_item_line_full);
			holder.lineHeightProp = convertView.findViewById(R.id.common_list_item_height_prop);
			holder.longLineText = (TextView) convertView.findViewById(R.id.common_list_item_line_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CommonListItem item = mListItems.get(position);
		holder.line.setVisibility(View.GONE);
		holder.lineFull.setVisibility(View.GONE);
		if (item.longLineText != null) {
			holder.lineFull.setVisibility(View.VISIBLE);
			holder.item.setVisibility(View.GONE);
			holder.longLineText.setText(item.longLineText);
		} else {
			int checkPos = position + 1;
			if (checkPos == mListItems.size()) {
				checkPos--;
			}
			if (mListItems.get(checkPos).longLineText == null) {
				holder.line.setVisibility(View.VISIBLE);
			}
			holder.item.setVisibility(View.VISIBLE);
			holder.title.setText(item.title);
			if (item.text == null) {
				holder.text.setVisibility(View.GONE);
			} else {
				holder.text.setVisibility(View.VISIBLE);
				holder.text.setText(item.text);
			}
			if (item.rightIconId != -1) {
				holder.right.setImageResource(item.rightIconId);
			}
			Bitmap bp = getLoacalBitmap(item.leftIconPath);
			if (bp != null) {
				holder.left.setImageBitmap(bp);
				holder.left.setVisibility(View.VISIBLE);
				holder.lineHeightProp.setVisibility(View.GONE);
			} else if (item.leftIconId != -1) {
				holder.left.setImageResource(item.leftIconId);
				holder.left.setVisibility(View.VISIBLE);
				holder.lineHeightProp.setVisibility(View.GONE);
			} else {
				holder.left.setVisibility(View.GONE);
				holder.lineHeightProp.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

	class ViewHolder {
		View item;
		ImageView right;
		ImageView left;
		TextView title;
		TextView text;
		View line;
		View lineFull;
		View lineHeightProp;
		TextView longLineText;
	}

	Bitmap getLoacalBitmap(String path) {
		if (path == null) {
			return null;
		}
		try {
			FileInputStream fis = new FileInputStream(path);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
