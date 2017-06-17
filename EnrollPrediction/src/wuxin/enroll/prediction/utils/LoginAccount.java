package wuxin.enroll.prediction.utils;

public class LoginAccount {
	private String account = null;
	private String password = null;
	private String name = null;
	private int clazz = 0;
	private int score = -1;
	private long order = -1;

	public LoginAccount() {
		this.account = null;
		this.password = null;
		this.name = null;
		this.clazz = 0;
		this.score = -1;
		this.order = -1;
	}

	public void setAccount(String acc, String pass, String name, int clazz, int score, long order) {
		this.account = acc;
		this.password = pass;
		this.name = name;
		this.clazz = clazz;
		this.score = score;
		this.order = order;
	}

	private static class LoginAccountHolder {
		private final static LoginAccount INSTANCE = new LoginAccount();
	}

	public static LoginAccount getInstance() {
		return LoginAccountHolder.INSTANCE;
	}

	public void resetAccount() {
		this.account = null;
		this.password = null;
		this.name = null;
		this.clazz = 0;
		this.score = -1;
		this.order = -1;
	}

	public String getAccount() {
		return this.account;
	}

	public String getPassword() {
		return this.password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setClazz(int clazz) {
		this.clazz = clazz;
	}

	public int getClazz() {
		return this.clazz;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return this.score;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public long getOrder() {
		return this.order;
	}
}
