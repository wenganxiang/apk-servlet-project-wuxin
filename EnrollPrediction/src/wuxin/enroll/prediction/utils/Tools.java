package wuxin.enroll.prediction.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wuxin.enroll.prediction.ObjectItem;
import wuxin.enroll.prediction.ObjectItem.MajorItem;
import wuxin.enroll.prediction.ObjectItem.SchoolItem;

public class Tools {

	public static final String URL_STR = "http://192.168.3.242:8888/wuxin/DbService";
	public static final int HTTP_READ_TIMEOUT = 60000;
	public static final int HTTP_CONNECT_TIMEOUT = 5000;
	public static final int BUFFER_SIZE = 1024;

	public static final String REQUEST_CODE = "request_code";
	public static final String REQUEST_PARAM = "request_param";
	public static final int LOGIN_ACCOUNT_REQ = 0x10001;
	public static final int UPDATE_ACCOUNT_REQ = 0x10002;
	public static final int REGISTER_CHECK_REQ = 0x10003;
	public static final int REGISTER_ACCOUNT_REQ = 0x10004;
	public static final int FIND_ACCOUNT_REQ = 0x10005;
	public static final int RESET_PASSWORD_REQ = 0x10006;
	public static final int UPDATE_SCORE_REQ = 0x10007;
	public static final int UPDATE_CLAZZ_REQ = 0x10008;
	public static final int QUERY_SCHOOLITEMS = 0x10009;

	// 判断是否为11位电话号码
	public static boolean isMobile(String mobiles) {
		Pattern p = Pattern.compile("^1(3|5|7|8|4)\\d{9}");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	// 将json 转为对象
	public static ArrayList<ObjectItem> getSchoolItemsFromJSONArray(JSONArray jsonArray) {
		if (jsonArray == null || jsonArray.length() <= 0) {
			return null;
		}
		JSONObject obj = null;
		JSONArray majorArray = null;
		SchoolItem schoolItem = null;
		ArrayList<ObjectItem> schoolItems = new ArrayList<ObjectItem>();
		try {
			for (int i = 0; i < jsonArray.length(); ++i) {
				obj = (JSONObject) jsonArray.get(i);
				schoolItem = new SchoolItem();
				schoolItem.sid = obj.getInt("s.sid");
				schoolItem.rate = obj.getDouble("s.rate");
				schoolItem.avg = obj.getString("s.avg");
				schoolItem.name = obj.getString("s.name");
				schoolItem.min = obj.getString("s.min");
				schoolItem.s_type = obj.getString("s.s_type");
				schoolItem.region = obj.getString("s.region");
				schoolItem.IconID = obj.getInt("s.IconID");
				if (obj.has("s.majors")) {
					majorArray = obj.getJSONArray("s.majors");
				} else {
					majorArray = null;
				}
				if (majorArray != null) {
					schoolItem.majors = getMajorItemsFromJSONArray(majorArray);
				} else {
					schoolItem.majors = null;
				}
				schoolItems.add(schoolItem);
			}
			return schoolItems;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 将json 转为对象
	public static ArrayList<MajorItem> getMajorItemsFromJSONArray(JSONArray jsonArray) {
		if (jsonArray == null || jsonArray.length() <= 0) {
			return null;
		}
		JSONObject obj = null;
		MajorItem majorItem = null;
		ArrayList<MajorItem> majorItems = new ArrayList<MajorItem>();
		try {
			for (int i = 0; i < jsonArray.length(); ++i) {
				obj = (JSONObject) jsonArray.get(i);
				majorItem = new MajorItem();
				majorItem.id = obj.getInt("m.id");
				majorItem.sid = obj.getInt("m.sid");
				majorItem.name = obj.getString("m.name");
				majorItem.max = obj.getString("m.max");
				majorItem.avg = obj.getString("m.avg");
				majorItem.rate = obj.getDouble("m.rate");
				majorItems.add(majorItem);
			}
			return majorItems;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 快速排序划分
	public static int partitionObjectItems(ArrayList<ObjectItem> items, int start, int end) throws Exception {
		if (items == null || items.size() <= 0 || start < 0 || end >= items.size()) {
			throw new Exception("error");
		}
		int index = new Random().nextInt(end - start + 1) + start;
		Collections.swap(items, index, end);
		int small = start - 1;
		for (index = start; index < end; ++index) {
			if (items.get(index).rate > items.get(end).rate) {
				++small;
				if (small != index) {
					Collections.swap(items, index, small);
				}
			}
		}
		++small;
		Collections.swap(items, small, end);
		return small;
	}

	// 快速排序算法
	public static void quickSortObjectItems(ArrayList<ObjectItem> items, int start, int end) throws Exception {
		if (start == end) {
			return;
		}
		int index = partitionObjectItems(items, start, end);
		if (index > start)
			quickSortObjectItems(items, start, index - 1);
		if (index < end)
			quickSortObjectItems(items, index + 1, end);
	}
}
