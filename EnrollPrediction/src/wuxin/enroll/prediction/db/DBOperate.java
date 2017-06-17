package wuxin.enroll.prediction.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import wuxin.enroll.prediction.ObjectItem;
import wuxin.enroll.prediction.ObjectItem.MajorItem;
import wuxin.enroll.prediction.ObjectItem.SchoolItem;
import wuxin.enroll.prediction.utils.LoginAccount;
import wuxin.enroll.prediction.utils.PublicInfo;

public class DBOperate {

	private static final String TABLE = DBHelper.ACCOUNT_TABLE;
	private SQLiteOpenHelper mHelper = null;

	public DBOperate(SQLiteOpenHelper helper) {
		this.mHelper = helper;
	}

	/**
	 * 增加数据
	 */
	public void addAccount(String account, String password) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String sql = "INSERT INTO " + TABLE + "(" + DBHelper.ACCOUNT + "," + DBHelper.PASSWORD + ") VALUES (?,?)";// SQL语句
		Object args[] = new Object[] { account, password }; // 设置参数
		db.execSQL(sql, args); // 执行SQL数据
		db.close();
	}

	/**
	 * 修改密码
	 */
	public void updateAccount(String account, String password) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String sql = "UPDATE " + TABLE + " SET " + DBHelper.PASSWORD + "=? WHERE " + DBHelper.ACCOUNT + "=?";
		Object args[] = new Object[] { password, account };
		db.execSQL(sql, args);
		db.close();
	}

	/**
	 * 删除账号
	 */
	public void deleteAccount(String account) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String sql = "DELETE FROM " + TABLE + " WHERE " + DBHelper.ACCOUNT + "=?";
		Object args[] = new Object[] { account };
		db.execSQL(sql, args);
		db.close();
	}

	/**
	 * 账号、密码查找
	 */
	public String queryAccount(String account) {
		String result = "null";
		SQLiteDatabase db = mHelper.getReadableDatabase();
		String sql = "SELECT " + DBHelper.PASSWORD + " FROM " + TABLE + " WHERE (" + DBHelper.ACCOUNT + " LIKE ?) ";
		String args[] = new String[] { "%" + account + "%" };
		Cursor cursor = db.rawQuery(sql, args);
		if (cursor != null && cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close(); // 关闭数据库连接
		return result;
	}
	
	/**
	 * @param year
	 * @return 
	 * 推算预估分数在过去某年的对应分数
	 * 该方法已废弃，改为在服务端调用
	 */
	@Deprecated 
	public int scoreInPastYear(int year) {
		int score = 0;
		String sql = null;
		Cursor cursor = null;
		SQLiteDatabase db = mHelper.getReadableDatabase();
		if (LoginAccount.getInstance().getOrder() != -1) {
			// 知道排名，可以准确查找到往年的等效分数
			sql = getScoreFromOrderSQL(year);
			cursor = db.rawQuery(sql, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					score = cursor.getInt(0);
				}
				cursor.close();
			}
			if (score == 0) {
				score = LoginAccount.getInstance().getScore();
			}
		} else {
			int line = -1, pastLine = 0;
			if (LoginAccount.getInstance().getClazz() == 1) {
				line = PublicInfo.getInstance().getLKLine();
			} else {
				line = PublicInfo.getInstance().getWKLine();
			}
			sql = "SELECT " + DBHelper.CONTROL + " FROM " + DBHelper.LINES_TABLE + " WHERE ( " + DBHelper.YEAR + " = "
					+ year + " AND " + DBHelper.CATE + " = " + LoginAccount.getInstance().getClazz() + " )";
			cursor = db.rawQuery(sql, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					pastLine = cursor.getInt(0);
				}
				cursor.close();
			}
			if (line == -1) {
				// 知道分数不知道一档线，直接将分数作为往年等效分
				score = LoginAccount.getInstance().getScore();
			} else {
				// 知道分数和一档线，根据正态分布规律（认为今年和往年分数正态分布方差相同
				// ，平均数有差别），仅需按照双线差（今年和往年的一本线之差，也可以是平均分
				// 之差，只要两个分数对应概率密度曲线的左分位数一样就可以了）作平移
				score = LoginAccount.getInstance().getScore() + pastLine - line;
				score = score > 750 ? 750 : score;
				score = score < 0 ? 0 : score;
			}
		}
		db.close();
		return score;
	}

	/**
	 * @param year
	 * @param score
	 * @return
	 * 根据分数查询该分数排名（数据中相同分数记录的是最低排名）
	 */
	public int orderSearch(int year, String score) {
		int temp = 0;
		SQLiteDatabase db = mHelper.getReadableDatabase();
		String sql = "SELECT " + DBHelper.ORDER + " FROM " + DBHelper.ORDERS_TABLE + " WHERE ( " + DBHelper.YEAR + " = "
				+ year + " AND " + DBHelper.CLASS + " = " + LoginAccount.getInstance().getClazz() + " AND "
				+ DBHelper.SCORE + " = " + score + ")";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				temp = cursor.getInt(0);
			}
			cursor.close();
		}
		return temp;
	}

	/**
	 * @param year
	 * @return
	 */
	@Deprecated
	private String getScoreFromOrderSQL(int year) {
		String sql = "SELECT " + DBHelper.SCORE + " FROM " + DBHelper.ORDERS_TABLE + " WHERE ( " + DBHelper.YEAR + " = "
				+ year + " AND " + DBHelper.CLASS + " = " + LoginAccount.getInstance().getClazz() + " AND "
				+ DBHelper.ORDER + " >= " + LoginAccount.getInstance().getOrder() + " ) ORDER BY " + DBHelper.SCORE
				+ " DESC ";
		return sql;
	}

	/**
	 * @return
	 * 已废弃，改为在服务端调用
	 */
	@Deprecated
	public ArrayList<ObjectItem> getSchoolItems() {
		ArrayList<ObjectItem> schoolItems = new ArrayList<ObjectItem>();
		String sql = null;
		Cursor cursor = null;
		SQLiteDatabase db = mHelper.getReadableDatabase();
		sql = "SELECT * FROM " + DBHelper.SCHOOLS_TABLE;
		cursor = db.rawQuery(sql, null);
		SchoolItem item = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					item = new SchoolItem();
					item.sid = cursor.getInt(7);
					item.rate = getSchoolOfferRate(item.sid);
					if (item.rate < 0.01) {
						continue;
					}
					LinsColumns lines = getSchoolScoreColums(item.sid, 2016);// 取2016录取线作为参考
					item.avg = String.valueOf(lines.avg);
					item.name = cursor.getString(1);
					item.min = String.valueOf(lines.min);
					item.s_type = cursor.getString(6);
					item.region = cursor.getString(2);
					item.majors = getSchoolMajorsColums(item.sid, 2015);
					item.IconID = -1;
					schoolItems.add(item);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		try {
			quickSortObjectItems(schoolItems, 0, schoolItems.size() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return schoolItems;
	}

	/**
	 * @param sid
	 * @return
	 * 已废弃，改为在服务端调用
	 */
	@Deprecated
	private double getSchoolOfferRate(int sid) {
		// 获取某年某高校录取线和平均分
		LinsColumns MAA2016 = getSchoolScoreColums(sid, 2016);
		LinsColumns MAA2015 = getSchoolScoreColums(sid, 2015);
		LinsColumns MAA2014 = getSchoolScoreColums(sid, 2014);
		// 计算某年可超过某高校录取线的分数（双线差）
		// 在不知道高校录取线时，返回-750，作为无法计算录取率的标记
		int deta2016 = MAA2016.min == 0 ? -750 : scoreInPastYear(2016) - MAA2016.min;
		int deta2015 = MAA2015.min == 0 ? -750 : scoreInPastYear(2015) - MAA2015.min;
		int deta2014 = MAA2014.min == 0 ? -750 : scoreInPastYear(2014) - MAA2014.min;
		if (deta2016 == -750 && deta2015 == -750 && deta2014 == -750) {
			return -1; // 三年都无法计算分数，直接返回-1作为标记
		}
		// 每个学校所录取学生的分数和对应人数也应服从正态分布（标准差根样本分析得出），平均分可查数据库；
		// 根据正态分布曲线，计算累计概率录取概率
		// 最后对各年的录取概率求平均值；
		int detaAvg2Min2016 = MAA2016.avg == 0 ? 10 : MAA2016.avg - MAA2016.min;
		int detaAvg2Min2015 = MAA2015.avg == 0 ? 10 : MAA2015.avg - MAA2015.min;
		int detaAvg2Min2014 = MAA2014.avg == 0 ? 10 : MAA2014.avg - MAA2014.min;
		double p2016 = deta2016 == -750 ? -1 : TotalProperty(deta2016, detaAvg2Min2016);
		double p2015 = deta2015 == -750 ? -1 : TotalProperty(deta2015, detaAvg2Min2015);
		double p2014 = deta2014 == -750 ? -1 : TotalProperty(deta2014, detaAvg2Min2014);
		p2016 = p2016 >= TotalProperty(0, detaAvg2Min2016) ? 0.5 * p2016 + 0.5 : p2016;
		p2015 = p2015 >= TotalProperty(0, detaAvg2Min2015) ? 0.5 * p2015 + 0.5 : p2015;
		p2014 = p2014 >= TotalProperty(0, detaAvg2Min2014) ? 0.5 * p2014 + 0.5 : p2014;
		int num = 0;
		double p_avg, sum = 0;
		double p[] = { p2016, p2015, p2014 };
		for (int i = 0; i < 3; ++i) {
			if (p[i] >= 0) {
				num++;
				sum += p[i];
			}
		}
		if (num == 3) {
			p_avg = sum / num;
		} else if (num == 2) {
			p_avg = sum * 0.8 / num;
		} else {
			p_avg = sum * 0.6 / num;
		}
		return p_avg > 0.99 ? 0.9901 : p_avg;
	}

	/**
	 * @param sid
	 * @param year
	 * @return
	 * 获取某大学某年某科录取线（科目在全局已设定，可直接调用）
	 * 已废弃，改为在服务端调用
	 */
	@Deprecated
	public LinsColumns getSchoolScoreColums(int sid, int year) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		String sql = "SELECT " + DBHelper.MIN + " , " + DBHelper.AVG + " FROM " + DBHelper.LINES_TABLE + " WHERE ( "
				+ DBHelper.CODE + " = " + sid + " AND " + DBHelper.YEAR + " = " + year + " AND " + DBHelper.CATE + " = "
				+ LoginAccount.getInstance().getClazz() + " )";
		Cursor cursor = db.rawQuery(sql, null);
		String strMin = null, strAvg = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				strMin = cursor.getString(0);
				strMin = strMin.equals("--") ? null : strMin;
				strAvg = cursor.getString(1);
				strAvg = strAvg.equals("--") ? null : strAvg;
			}
			cursor.close();
		}
		int min = strMin == null ? 0 : Integer.parseInt(strMin);
		int avg = strAvg == null ? 0 : Integer.parseInt(strAvg);
		return new LinsColumns(min, avg);
	}

	class LinsColumns {
		int min;
		int avg;

		public LinsColumns(int min, int avg) {
			this.min = min;
			this.avg = avg;
		}
	}

	/**
	 * @param sid
	 * @param year
	 * @return
	 * 获取某大学可被录取专业的信息
	 * 已废弃，改为在服务端调用
	 */
	@Deprecated
	public ArrayList<MajorItem> getSchoolMajorsColums(int sid, int year) {
		ArrayList<MajorItem> majorItems = new ArrayList<MajorItem>();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		String sql = "SELECT " + DBHelper.ID + ", " + DBHelper.CODE+ ", " + DBHelper.NAME + ", " + DBHelper.AVG + ", "
				+ DBHelper.MAX + " FROM " + DBHelper.MAJORS_TABLE + " WHERE ( " + DBHelper.CODE + " = " + sid + " AND "
				+ DBHelper.YEAR + " = " + year + " AND " + DBHelper.TYPE + " = " + LoginAccount.getInstance().getClazz()
				+ " )";
		Cursor cursor = db.rawQuery(sql, null);
		int id = 0, msid = 0;
		String strName = null, strMax = null, strAvg = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					strAvg = cursor.getString(3);
					if (!"--".equals(strAvg)){
						if(scoreInPastYear(2016) < Integer.parseInt(strAvg)) {
							continue;
						}
					}
					id = cursor.getInt(0);
					msid = cursor.getInt(1);
					strName = cursor.getString(2);
					strMax = cursor.getString(4);
					// 添加专业信息
					MajorItem mItem = new MajorItem();
					mItem.id = id;
					mItem.sid = msid;
					mItem.name = strName;
					mItem.max = strMax;
					mItem.avg = strAvg;
					mItem.rate = -2;// 不计算专业的录取概率，赋值为-2作为标记
					majorItems.add(mItem);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return majorItems;
	}

	class MajorsColumns {
		int id;
		int sid;
		String max;
		String avg;
		String name;

		public MajorsColumns(int id, int sid, String name, String avg, String max) {
			this.id = id;
			this.sid = sid;
			this.avg = avg;
			this.max = max;
			this.name = name;
		}
	}

	/**
	 * @param x
	 * @param u
	 * @return
	 * 高斯分布密度函数值计算
	 * 已废弃，改为在服务端调用
	 */
	@Deprecated
	public double GaussProperty(double x, double u) {
		return Math.exp((u - x) * (x - u) / 2) / Math.sqrt(2 * Math.PI);
	}

	/**
	 * @param x
	 * @param u
	 * @return
	 * 简单的数值积分方法计算高斯分布累计概率
	 * 已废弃，改为在服务端调用
	 */
	@Deprecated
	public double TotalProperty(double x, double u) {
		double totle = 0;
		int n = (int) (10 * u);
		x = x > u + n ? u + n : x;
		for (int i = (int) (u - n); i <= x; ++i) {
			totle += GaussProperty(i, u);
		}
		return totle;
	}

	// 快速排序划分
	private int partitionObjectItems(ArrayList<ObjectItem> items, int start, int end) throws Exception {
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
	private void quickSortObjectItems(ArrayList<ObjectItem> items, int start, int end) throws Exception {
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
