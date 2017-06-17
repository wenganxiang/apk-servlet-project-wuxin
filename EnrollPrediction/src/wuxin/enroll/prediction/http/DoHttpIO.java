package wuxin.enroll.prediction.http;

public interface DoHttpIO {
	public byte[] beforeRequest();
	public void afterResponse(byte[] result);
}
