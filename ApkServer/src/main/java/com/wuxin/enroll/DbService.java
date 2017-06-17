package com.wuxin.enroll;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class DbService extends HttpServlet {

	public static final String REQUEST_CODE = "request_code";

	public static final int LOGIN_ACCOUNT_REQ = 0x10001;
	public static final int UPDATE_ACCOUNT_REQ = 0x10002;
	public static final int REGISTER_CHECK_REQ = 0x10003;
	public static final int REGISTER_ACCOUNT_REQ = 0x10004;
	public static final int FIND_ACCOUNT_REQ = 0x10005;
	public static final int RESET_PASSWORD_REQ = 0x10006;
	public static final int UPDATE_SCORE_REQ = 0x10007;
	public static final int UPDATE_CLAZZ_REQ = 0x10008; 
	public static final int QUERY_SCHOOLITEMS = 0x10009;
	
	private Connection mConn = null;
	private ServletContext mApplication = null;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		mApplication = getServletContext();
		mConn = (Connection) mApplication.getAttribute(DbHelper.CONN);
		// resp.setContentType("application/json; charset=gbk");
		// req.setCharacterEncoding("GBK");
		try {
			int total = 0, once = 0;
			int readLen = req.getContentLength();
			byte[] temp = new byte[readLen];
			ServletInputStream in = req.getInputStream();
			while ((total < readLen) && (once >= 0)) {
				once = in.read(temp, total, readLen);
				total += once;
			}
			String request = new String(temp, 0, readLen, "GBK");

			System.out.println("request:" + request);
			if (request != null) {
				JSONObject object = new JSONObject(request);
				int code = object.getInt(REQUEST_CODE);
				switch (code) {
				case LOGIN_ACCOUNT_REQ:
					doLoginCheck(object);
					System.out.println("response:" + object.toString());
					break;
				case UPDATE_ACCOUNT_REQ:
					doStateUpdate(object);
					System.out.println("response:" + object.toString());
					break;
				case REGISTER_CHECK_REQ:
					doRegisterCheck(object);
					System.out.println("response:" + object.toString());
					break;
				case REGISTER_ACCOUNT_REQ:
					doRegisterAccount(object);
					System.out.println("response:" + object.toString());
					break;
				case FIND_ACCOUNT_REQ:
					doFindAccount(object);
					System.out.println("response:" + object.toString());
					break;
				case RESET_PASSWORD_REQ:
					doResetPassword(object);
					System.out.println("response:" + object.toString());
					break;
				case UPDATE_CLAZZ_REQ:
					doUpdateClazz(object);
					System.out.println("response:" + object.toString());
					break;
				case UPDATE_SCORE_REQ:
					doUpdateScore(object);
					System.out.println("response:" + object.toString());
					break;
				case QUERY_SCHOOLITEMS:
					doQueryItems(object);
					System.out.println("response:" + object.getString("ans"));
					break;
				}
				String response = object.toString();
				temp = response.getBytes();
				resp.setContentLength(response.getBytes("GBK").length);
				OutputStream out = resp.getOutputStream();
				out.write(response.getBytes("GBK")); // 写入请求的字符串
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	private void doRegisterCheck(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "dbError");
			return;
		}
		try {
			String account = object.getString("account");
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			String pass = dbo.queryAccount(account);
			if ("null".equals(pass)) {
				object.put("ans", "unRegister");
			} else {
				object.put("ans", "registed");
			}
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doRegisterAccount(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "dbError");
			return;
		}
		try {
			String account = object.getString("account");
			String password = object.getString("password");
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			boolean ans = dbo.addAccount(account, password);
			if (ans) {
				object.put("ans", "success");
			} else {
				object.put("ans", "failure");
			}
			object.remove("account");
			object.remove("password");
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doFindAccount(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "dbError");
			return;
		}
		try {
			String account = object.getString("account");
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			String pass = dbo.queryAccount(account);
			if ("null".equals(pass)) {
				object.put("ans", "unRegister");
			} else {
				object.put("ans", "registed");
			}
			object.remove("account");
			object.remove("code");
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doResetPassword(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "dbError");
			return;
		}
		try {
			String account = object.getString("account");
			String password = object.getString("password");
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			boolean ans = dbo.updateAccount(account, password);
			if (ans) {
				object.put("ans", "success");
			} else {
				object.put("ans", "failure");
			}
			object.remove("account");
			object.remove("password");
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doLoginCheck(JSONObject object) {
		if (mConn == null) {
			return;
		}
		try {
			String account = object.getString("account");
			String password = object.getString("password");
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			ResultSet set = dbo.queryAccountData(account);
			if (set == null) {
				object.put("password", "null");
			} else {
				set.first();
				String pass = set.getString(3);
				if (pass.equals(password)) {
					object.put("name", set.getString(4));
					object.put("clazz", set.getInt(6));
					object.put("score", set.getInt(7));
					object.put("order", set.getLong(8));
				} else {
					object.put("password", "error");
				}
				set.close();
			}
		} catch (SQLException e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doStateUpdate(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "数据库连接失败！");
			return;
		}
		try {
			String account = object.getString("account");
			String oldAccount = object.getString("oldAccount");
			if (account.equals(oldAccount)) {
				object.put("ans", "重复登录!");
			} else {
				DbOperate dbo = new DbOperate(new DbHelper(mConn));
				dbo.updateLoginState(account, "true");
				dbo.updateLoginState(oldAccount, "false");
				object.put("ans", "登录成功!");
			}
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}

	private void doUpdateScore(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "数据库连接失败！");
			return;
		}
		try {
			int input = object.getInt("input-value");
			String account = object.getString("account");
			boolean isScore = object.getBoolean("by-score");
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			if (dbo.updateScoreOrOrder(account, input, isScore)) {
				object.put("ans", "success");
			} else {
				object.put("ans", "failure");
			}
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doUpdateClazz(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "数据库连接失败！");
			return;
		}
		try {
			DbOperate dbo = new DbOperate(new DbHelper(mConn));
			object.put("ans-clazz", "null");
			int clazz = object.getInt("clazz");
			String account = object.getString("account");
			if (clazz != -1 && !"null".equals(account)) {
				if (dbo.updateClazz(account, clazz)) {
					object.put("ans-clazz", "success");
				} else {
					object.put("ans-clazz", "failure");
				}
				System.out.println("response ans-clazz:" + object.getString("ans-clazz"));
			}
			object.put("ans-line", "null");
			String lineKey = object.getString("line-key");
			if (!"null".equals(lineKey)) {
				if (dbo.updateYBLine(lineKey, object.getInt("line-value"))){
					object.put("ans-line", "success");
				} else {
					object.put("ans-line", "failure");
				}
				System.out.println("response ans-line:" + object.getString("ans-line"));
			}
			object.put("ans-oustand", "null");
			String outstandKey = object.getString("oustand-key");
			if (!"null".equals(outstandKey)) {
				if (dbo.updateOutstand(outstandKey, object.getInt("oustand-value"))){
					object.put("ans-oustand", "success");
				} else {
					object.put("ans-oustand", "failure");
				}
				System.out.println("response ans-oustand:" + object.getString("ans-oustand"));
			}
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
	
	private void doQueryItems(JSONObject object) {
		if (mConn == null) {
			object.put("ans", "数据库连接失败！");
			return;
		}
		try {
			AccountEnroll acc = new AccountEnroll(mApplication);
			if (acc.isPreparied()) {
				JSONArray result = acc.getSchoolItems();
				if (result != null) {
					object.put("ans", "success");
					object.put("result", result);
				}
			} else {
				object.put("ans", "failure");
			}
		} catch (Exception e) {
			mApplication.log("HeTao" + e.toString());
		}
	}
}
