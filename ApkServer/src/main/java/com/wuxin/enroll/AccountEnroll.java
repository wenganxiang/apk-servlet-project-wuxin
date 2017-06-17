package com.wuxin.enroll;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountEnroll {
	private ServletContext mApplication;
	private Connection mConn;
	private DbOperate mDBO;
	private String mAccount;
	private int mClazz;
	private int mScore;
	private int mOrder;
	private String mName;
	private boolean mLogin;
	private boolean mInit = false;

	public AccountEnroll(ServletContext application) {
		mApplication = application;
		init();
	}

	public boolean isPreparied() {
		return mInit;
	}

	private void init() {
		try {
			mConn = (Connection) mApplication.getAttribute(DbHelper.CONN);
			if (mConn == null) {
				System.out.println("init error: Connection is null!");
				return;
			}
			mDBO = new DbOperate(new DbHelper(mConn));
			ResultSet set = mDBO.queryOnlineAccount();
			if (set == null) {
				System.out.println("init error: No account is online!");
				return;
			} else {
				set.first();
				mClazz = set.getInt(6);
				mScore = set.getInt(7);
				mOrder = set.getInt(8);
				setName(set.getString(4));
				setAccount(set.getString(2));
				setLogin("true".equals(set.getString(5)));
				set.close();
				mInit = true;
			}
		} catch (SQLException e) {
			mApplication.log("HeTao" + e.toString());
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}

	public JSONArray getSchoolMajorColums(int sid, int year) {
		JSONArray majorItems = null;
		ResultSet set = mDBO.queryMajorInfo(sid, year, mClazz);
		if (set != null) {
			try {
				if (set.first()) {
					int mId, mSid;
					JSONObject obj;
					majorItems = new JSONArray();
					String strAvg, strMax, strName;
					do {
						strAvg = set.getString(4);
						if (!"--".equals(strAvg)) {
							if (scoreInPastYear(2016) < Integer.parseInt(strAvg)) {
								continue;
							}
						}
						mId = set.getInt(1);
						mSid = set.getInt(2);
						strName = set.getString(3);
						strMax = set.getString(5);
						obj = new JSONObject();
						obj.put("m.id", mId);
						obj.put("m.sid", mSid);
						obj.put("m.name", strName);
						obj.put("m.max", strMax);
						obj.put("m.avg", strAvg);
						obj.put("m.rate", -2); // 不计算专业的录取概率，赋值为-2作为标记
						majorItems.put(obj);
					} while (set.next());
				}
				set.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (Exception e3) {
				e3.printStackTrace();
			}
		}
		return majorItems;
	}

	// 获取某大学某年某科录取线（科目在全局已设定，可直接调用）
	public LinsColumns getSchoolScoreColums(int sid, int year) {
		try {
			String strMin = null, strAvg = null;
			ResultSet set = mDBO.querySchoolScoreInfo(sid, year, mClazz);
			if (set != null) {
				if (set.first()) {
					strMin = set.getString(1);
					strMin = strMin.equals("--") ? null : strMin;
					strAvg = set.getString(2);
					strAvg = strAvg.equals("--") ? null : strAvg;
				}
			}
			int min = strMin == null ? 0 : Integer.parseInt(strMin);
			int avg = strAvg == null ? 0 : Integer.parseInt(strAvg);
			return new LinsColumns(min, avg);
		} catch (SQLException e) {
			e.printStackTrace();
			return new LinsColumns(0, 0);
		}
	}

	class LinsColumns {
		int min;
		int avg;

		public LinsColumns(int min, int avg) {
			this.min = min;
			this.avg = avg;
		}
	}

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

	public JSONArray getSchoolItems() {
		JSONArray schoolItems = null;
		ResultSet set = mDBO.querySchoolInfo();
		try {
			if (set != null) {
				if (set.first()) {
					JSONObject obj = null;
					schoolItems = new JSONArray();
					do {
						int sid = set.getInt(8);
						double rate = getSchoolOfferRate(sid);
						if (rate < 0.01) {
							continue;
						}
						obj = new JSONObject();
						obj.put("s.sid", sid);
						obj.put("s.rate", rate);
						LinsColumns lines = getSchoolScoreColums(sid, 2016);// 取2016录取线作为参考
						obj.put("s.avg", String.valueOf(lines.avg));
						obj.put("s.name", set.getString(2));
						obj.put("s.min", String.valueOf(lines.min));
						obj.put("s.s_type", set.getString(7));
						obj.put("s.region", set.getString(3));
						obj.put("s.majors", getSchoolMajorColums(sid, 2015));
						obj.put("s.IconID", -1);
						schoolItems.put(obj);
					} while (set.next());
				}
				set.close();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		return schoolItems;
	}

	// 推算预估分数在过去某年的对应分数
	public int scoreInPastYear(int year) {
		int score = 0;
		if (mOrder != -1) {
			// 知道排名，可以准确查找到往年的等效分数
			score = mDBO.getScoreFromOrder(mClazz, mOrder, year);
			if (score == 0) {
				score = mScore;
			}
		} else {
			int line = mDBO.getCurrentLine(mClazz);
			int pastLine = mDBO.getPastLine(mClazz, year);
			if (line == -1) {
				// 知道分数不知道一档线，直接将分数作为往年等效分
				score = mScore;
			} else {
				// 知道分数和一档线，根据正态分布规律（认为今年和往年分数正态分布方差相同
				// ，平均数有差别），仅需按照双线差（今年和往年的一本线之差，也可以是平均分
				// 之差，只要两个分数对应概率密度曲线的左分位数一样就可以了）作平移
				score = mScore + pastLine - line;
				score = score > 750 ? 750 : score;
				score = score < 0 ? 0 : score;
			}
		}
		return score;
	}

	// 高斯分布密度函数值计算
	public double GaussProperty(double x, double u) {
		return Math.exp((u - x) * (x - u) / 2) / Math.sqrt(2 * Math.PI);
	}

	// 简单的数值积分方法计算高斯分布累计概率
	public double TotalProperty(double x, double u) {
		double totle = 0;
		int n = (int) (10 * u);
		x = x > u + n ? u + n : x;
		for (int i = (int) (u - n); i <= x; ++i) {
			totle += GaussProperty(i, u);
		}
		return totle;
	}

	public String getAccount() {
		return mAccount;
	}

	public void setAccount(String mAccount) {
		this.mAccount = mAccount;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public boolean isLogin() {
		return mLogin;
	}

	public void setLogin(boolean mLogin) {
		this.mLogin = mLogin;
	}

	public int getClazz() {
		return mClazz;
	}

	public void setClazz(int mClazz) {
		this.mClazz = mClazz;
	}

	public int getScore() {
		return mScore;
	}

	public void setScore(int mScore) {
		this.mScore = mScore;
	}

	public int getOrder() {
		return mOrder;
	}

	public void setOrder(int mOrder) {
		this.mOrder = mOrder;
	}
}
