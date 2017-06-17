package wuxin.enroll.prediction.utils;

public class PublicInfo {
	private int lkline = -1;
	private int wkline = -1;
	private int lkoutstand = -1;
	private int wkoutstand = -1;
	
	private PublicInfo() {
		lkline = -1;
		wkline = -1;
		lkoutstand = -1;
		wkoutstand = -1;
	}
	
	public void resetInfo() {
		lkline = -1;
		wkline = -1;
		lkoutstand = -1;
		wkoutstand = -1;
	}
	
	private static class PublicInfoHolder {
		private static final PublicInfo INSTANCE = new PublicInfo();
	}
	
	public static PublicInfo getInstance() {
		return PublicInfoHolder.INSTANCE;
	}
	
	public void setLKLine(int line) {
		this.lkline = line;
	}

	public int getLKLine() {
		return this.lkline;
	}
	
	public void setWKLine(int line) {
		this.wkline = line;
	}
	
	public int getWKLine() {
		return this.wkline;
	}
	
	public void setLKOutstand(int outstand) {
		this.lkoutstand = outstand;
	}

	public int getLKOutstand() {
		return this.lkoutstand;
	}
	
	public void setWKOutstand(int outstand) {
		this.wkoutstand = outstand;
	}
	
	public int getWKOutstand() {
		return this.wkoutstand;
	}
}
