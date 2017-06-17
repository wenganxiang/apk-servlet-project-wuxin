package com.wuxin.enroll;

import java.sql.ResultSet;

public class DbOperate {

	public static final String DB_NAME = "wuxin";

	public static final String ID = "id"; // int:lines,majors,schools,accounts,orders
	public static final String SID = "sid"; // int:lines,majors
	public static final String YEAR = "year"; // int:lines,majors,orders
	public static final String TYPE = "type"; // int:majors,text:schools
	public static final String NAME = "name"; // text:majors,schools
	public static final String AVG = "avg"; // text:lines,majors
	public static final String MIN = "min"; // text:lines,majors
	public static final String MAX = "max"; // text:lines,majors
	public static final String CODE = "code"; // text:lines,majors,schools

	public static final String ACCOUNT_TABLE = "accounts";
	public static final String ACCOUNT = "account"; // text
	public static final String PASSWORD = "password"; // text
	public static final String LOGIN = "login"; // text
	public static final String REGION = "region"; // text

	public static final String LINES_TABLE = "score_lines";// 4+3
	public static final String CONTROL = "control"; // text
	public static final String CATE = "cate"; // text

	public static final String MAJORS_TABLE = "majors"; // 6+1

	public static final String SCHOOLS_TABLE = "schools"; // 3+2
	public static final String LOCATION = "location"; // text

	public static final String ORDERS_TABLE = "orders"; // 3+2
	public static final String CLASS = "_class"; // int 0理科， 1文科
	public static final String SCORE = "_score"; // int
	public static final String TOTAL = "_total"; // int
	public static final String ORDER = "_order"; // int

	public static final String SETTINGS = "settings"; // 存储一些其他数据，以键值对形式
	public static final String KEY_NAME = "_name";
	public static final String KEY_VALUE = "_value";

	private DbHelper mHelper;

	public DbOperate(DbHelper helper) {
		this.mHelper = helper;
	}

	public void clean() {
		try {
			mHelper.closeConn();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.mHelper = null;
		}
	}

	/**
	 * 增加账户
	 */
	public boolean addAccount(String account, String password) {
		String sql = "INSERT INTO " + ACCOUNT_TABLE + " ( " + ACCOUNT + ", " + PASSWORD + ") VALUES (?, ?)";
		Object args[] = new Object[] { account, password };
		try {
			mHelper.insert(sql, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改密码
	 */
	public boolean updateAccount(String account, String password) {
		String sql = "UPDATE " + ACCOUNT_TABLE + " SET " + PASSWORD + "=? WHERE " + ACCOUNT + "=?";
		Object args[] = new Object[] { password, account };
		try {
			mHelper.modify(sql, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改科目
	 */
	public boolean updateClazz(String account, int clazz) {
		String sql = "UPDATE " + ACCOUNT_TABLE + " SET " + CLASS + "=? WHERE " + ACCOUNT + "=?";
		Object args[] = new Object[] { clazz, account };
		try {
			mHelper.modify(sql, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改一本线
	 */
	public boolean updateYBLine(String key, int value) {
		String sql = "UPDATE " + SETTINGS + " SET " + KEY_VALUE + "=?" + " WHERE " + KEY_NAME + "=?";
		Object args[] = new Object[] { value, key };
		try {
			mHelper.modify(sql, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改最高分
	 */
	public boolean updateOutstand(String key, int value) {
		String sql = "UPDATE " + SETTINGS + " SET " + KEY_VALUE + "=?" + " WHERE " + KEY_NAME + "=?";
		Object args[] = new Object[] { value, key };
		try {
			mHelper.modify(sql, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改分数或排名
	 */
	public boolean updateScoreOrOrder(String account, int value, boolean isScore) {
		String sOo = isScore ? SCORE : ORDER;
		String sql = "UPDATE " + ACCOUNT_TABLE + " SET " + sOo + "=?" + " WHERE " + ACCOUNT + "=?";
		Object args[] = new Object[] { value, account };
		try {
			mHelper.modify(sql, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 修改密码
	 */
	public void updateLoginState(String account, String login) {
		String sql = "UPDATE " + ACCOUNT_TABLE + " SET " + LOGIN + "=? WHERE " + ACCOUNT + "=?";
		Object args[] = new Object[] { login, account };
		try {
			mHelper.modify(sql, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除账号
	 */
	public void deleteAccount(String account) {
		String sql = "DELETE FROM " + ACCOUNT_TABLE + " WHERE " + ACCOUNT + "=?";
		Object args[] = new Object[] { account };
		try {
			mHelper.delete(sql, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 账号、密码查找
	 */
	public String queryAccount(String account) {
		String result = "null";
		String sql = "SELECT " + PASSWORD + " FROM " + ACCOUNT_TABLE + " WHERE (" + ACCOUNT + " LIKE ?) ";
		Object args[] = new Object[] { "%" + account + "%" };
		ResultSet set = null;
		try {
			set = mHelper.query(sql, args);
			if (set != null) {
				if (set.first()) {
					result = set.getString(1);
				}
				set.close();
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}
	}

	/**
	 * 账号
	 */
	public ResultSet queryAccountData(String account) {
		String sql = "SELECT * FROM " + ACCOUNT_TABLE + " WHERE (" + ACCOUNT + " LIKE ?) ";
		Object args[] = new Object[] { "%" + account + "%" };
		ResultSet set = null;
		try {
			set = mHelper.query(sql, args);
			if (set != null) {
				if (set.first()) {
					return set;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 账号
	 */
	public ResultSet queryOnlineAccount() {
		String sql = "SELECT * FROM " + ACCOUNT_TABLE + " WHERE (" + LOGIN + "='true') ";
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					return set;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据排名查分数
	 */
	public int getScoreFromOrder(int clazz, int order, int year) {
		String sql = "SELECT " + SCORE + " FROM " + ORDERS_TABLE + " WHERE ( " + YEAR + " = " + year + " AND " + CLASS
				+ " = " + clazz + " AND " + ORDER + " >= " + order + " ) ORDER BY " + SCORE + " DESC ";
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					return set.getInt(1);
				}
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getCurrentLine(int clazz) {
		String sql = "SELECT " + KEY_VALUE + " FROM " + SETTINGS + " WHERE ( " + KEY_NAME + " =? )";
		ResultSet set = null;
		try {
			String keyName = clazz == 1 ? "lk_line" : "wk_line";
			set = mHelper.query(sql, keyName);
			if (set != null) {
				if (set.first()) {
					return set.getInt(1);
				}
			}
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public int getPastLine(int clazz, int year) {
		String sql = "SELECT " + CONTROL + " FROM " + LINES_TABLE + " WHERE ( " + YEAR + " = " + year + " AND " + CATE
				+ " = " + clazz + " )";
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					return set.getInt(1);
				}
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public ResultSet queryMajorInfo(int sid, int year, int clazz) {
		String sql = "SELECT " + ID + ", " + CODE + ", " + NAME + ", " + AVG + ", " + MAX + " FROM " + MAJORS_TABLE
				+ " WHERE ( " + CODE + " = " + sid + " AND " + YEAR + " = " + year + " AND " + TYPE + " = " + clazz + " )";
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					return set;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet querySchoolScoreInfo(int sid, int year, int clazz) {
		String sql = "SELECT " + MIN + " , " + AVG + " FROM " + LINES_TABLE + " WHERE ( " + CODE + " = " + sid + " AND "
				+ YEAR + " = " + year + " AND " + CATE + " = " + clazz + " )";
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					return set;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet querySchoolInfo() {
		String sql = "SELECT * FROM " + SCHOOLS_TABLE;
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					return set;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 根据分数查询该分数排名（数据中相同分数记录的是最低排名）
	public int orderSearch(int year, String score, int clazz) {
		int temp = 0;
		String sql = "SELECT " + ORDER + " FROM " + ORDERS_TABLE + " WHERE ( " + YEAR + " = " + year + " AND " + CLASS
				+ " = " + clazz + " AND " + SCORE + " = " + score + ")";
		ResultSet set = null;
		try {
			set = mHelper.query(sql);
			if (set != null) {
				if (set.first()) {
					temp = set.getInt(1);
				}
				set.close();
			}
			return temp;
		} catch (Exception e) {
			return temp;
		}
	}
}
