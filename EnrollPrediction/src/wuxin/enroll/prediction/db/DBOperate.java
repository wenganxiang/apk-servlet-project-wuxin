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
	 * ��������
	 */
	public void addAccount(String account, String password) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String sql = "INSERT INTO " + TABLE + "(" + DBHelper.ACCOUNT + "," + DBHelper.PASSWORD + ") VALUES (?,?)";// SQL���
		Object args[] = new Object[] { account, password }; // ���ò���
		db.execSQL(sql, args); // ִ��SQL����
		db.close();
	}

	/**
	 * �޸�����
	 */
	public void updateAccount(String account, String password) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String sql = "UPDATE " + TABLE + " SET " + DBHelper.PASSWORD + "=? WHERE " + DBHelper.ACCOUNT + "=?";
		Object args[] = new Object[] { password, account };
		db.execSQL(sql, args);
		db.close();
	}

	/**
	 * ɾ���˺�
	 */
	public void deleteAccount(String account) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String sql = "DELETE FROM " + TABLE + " WHERE " + DBHelper.ACCOUNT + "=?";
		Object args[] = new Object[] { account };
		db.execSQL(sql, args);
		db.close();
	}

	/**
	 * �˺š��������
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
		cursor.close(); // �ر����ݿ�����
		return result;
	}
	
	/**
	 * @param year
	 * @return 
	 * ����Ԥ�������ڹ�ȥĳ��Ķ�Ӧ����
	 * �÷����ѷ�������Ϊ�ڷ���˵���
	 */
	@Deprecated 
	public int scoreInPastYear(int year) {
		int score = 0;
		String sql = null;
		Cursor cursor = null;
		SQLiteDatabase db = mHelper.getReadableDatabase();
		if (LoginAccount.getInstance().getOrder() != -1) {
			// ֪������������׼ȷ���ҵ�����ĵ�Ч����
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
				// ֪��������֪��һ���ߣ�ֱ�ӽ�������Ϊ�����Ч��
				score = LoginAccount.getInstance().getScore();
			} else {
				// ֪��������һ���ߣ�������̬�ֲ����ɣ���Ϊ��������������̬�ֲ�������ͬ
				// ��ƽ�����в�𣩣����谴��˫�߲����������һ����֮�Ҳ������ƽ����
				// ֮�ֻҪ����������Ӧ�����ܶ����ߵ����λ��һ���Ϳ����ˣ���ƽ��
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
	 * ���ݷ�����ѯ�÷�����������������ͬ������¼�������������
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
	 * �ѷ�������Ϊ�ڷ���˵���
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
					LinsColumns lines = getSchoolScoreColums(item.sid, 2016);// ȡ2016¼ȡ����Ϊ�ο�
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
	 * �ѷ�������Ϊ�ڷ���˵���
	 */
	@Deprecated
	private double getSchoolOfferRate(int sid) {
		// ��ȡĳ��ĳ��У¼ȡ�ߺ�ƽ����
		LinsColumns MAA2016 = getSchoolScoreColums(sid, 2016);
		LinsColumns MAA2015 = getSchoolScoreColums(sid, 2015);
		LinsColumns MAA2014 = getSchoolScoreColums(sid, 2014);
		// ����ĳ��ɳ���ĳ��У¼ȡ�ߵķ�����˫�߲
		// �ڲ�֪����У¼ȡ��ʱ������-750����Ϊ�޷�����¼ȡ�ʵı��
		int deta2016 = MAA2016.min == 0 ? -750 : scoreInPastYear(2016) - MAA2016.min;
		int deta2015 = MAA2015.min == 0 ? -750 : scoreInPastYear(2015) - MAA2015.min;
		int deta2014 = MAA2014.min == 0 ? -750 : scoreInPastYear(2014) - MAA2014.min;
		if (deta2016 == -750 && deta2015 == -750 && deta2014 == -750) {
			return -1; // ���궼�޷����������ֱ�ӷ���-1��Ϊ���
		}
		// ÿ��ѧУ��¼ȡѧ���ķ����Ͷ�Ӧ����ҲӦ������̬�ֲ�����׼������������ó�����ƽ���ֿɲ����ݿ⣻
		// ������̬�ֲ����ߣ������ۼƸ���¼ȡ����
		// ���Ը����¼ȡ������ƽ��ֵ��
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
	 * ��ȡĳ��ѧĳ��ĳ��¼ȡ�ߣ���Ŀ��ȫ�����趨����ֱ�ӵ��ã�
	 * �ѷ�������Ϊ�ڷ���˵���
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
	 * ��ȡĳ��ѧ�ɱ�¼ȡרҵ����Ϣ
	 * �ѷ�������Ϊ�ڷ���˵���
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
					// ���רҵ��Ϣ
					MajorItem mItem = new MajorItem();
					mItem.id = id;
					mItem.sid = msid;
					mItem.name = strName;
					mItem.max = strMax;
					mItem.avg = strAvg;
					mItem.rate = -2;// ������רҵ��¼ȡ���ʣ���ֵΪ-2��Ϊ���
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
	 * ��˹�ֲ��ܶȺ���ֵ����
	 * �ѷ�������Ϊ�ڷ���˵���
	 */
	@Deprecated
	public double GaussProperty(double x, double u) {
		return Math.exp((u - x) * (x - u) / 2) / Math.sqrt(2 * Math.PI);
	}

	/**
	 * @param x
	 * @param u
	 * @return
	 * �򵥵���ֵ���ַ��������˹�ֲ��ۼƸ���
	 * �ѷ�������Ϊ�ڷ���˵���
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

	// �������򻮷�
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

	// ���������㷨
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
