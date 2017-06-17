package wuxin.enroll.prediction.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;
import wuxin.enroll.prediction.utils.Tools;

public class HttpThread extends Thread {

	public HttpThread(final DoHttpIO doHttpIO) {
		super(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(Tools.URL_STR); // ����url��Դ
					HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // ����http����
					conn.setDoInput(true);
					conn.setDoOutput(true); // �����������
					conn.setUseCaches(false); // ���ò��û���
					conn.setReadTimeout(Tools.HTTP_READ_TIMEOUT); //��ȡ��ʱ
					conn.setConnectTimeout(Tools.HTTP_CONNECT_TIMEOUT); //����ʱ
					conn.setRequestMethod("POST"); // ���ô��ݷ�ʽ
					conn.setRequestProperty("Connection", "Keep-Alive"); // ����ά�ֳ�����
					// conn.setRequestProperty("Charset", "GBK"); // �����ļ��ַ���:
					// conn.setRequestProperty(HTTP.CONTENT_TYPE,
					// "application/json"); // �����ļ�����:
					byte[] req = doHttpIO.beforeRequest();
					if (req != null) {
						conn.setRequestProperty("Content-Length", String.valueOf(req.length)); // �����ļ�����
						conn.connect(); // ��ʼ��������
						OutputStream out = conn.getOutputStream();
						out.write(req); // д��������ַ���
						out.flush();
						out.close();
					}
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // ���󷵻ص�״̬
						// byte[] temp = new byte[1024];
						// StringBuilder result = new StringBuilder();
						// while ((readLen = in.read(temp)) > 0) {
						// result.append(new String(temp, 0, readLen, "GBK"));
						// }
						// String json = result.toString(); // ת���ַ���
						byte[] temp = null;
						int readLen = conn.getContentLength();
						InputStream in = conn.getInputStream();
						if (readLen <= Tools.BUFFER_SIZE) {
							int total = 0, once = 0;
							temp = new byte[readLen];
							while ((total < readLen) && (once >= 0)) {
								once = in.read(temp, total, readLen);
								total += once;
							}
						} else { //���ֽڶ�ȡ
							temp = InputStream2ByteArray(in);
						}
						doHttpIO.afterResponse(temp);
					}
				} catch (Exception e) {
					Log.v("HttpThread", "run exception:" + e.toString());
				} finally {
					if (connection != null) {
						connection.disconnect();
						connection = null;
					}
				}
			}
		});
	}

    public static byte[] InputStream2ByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[Tools.BUFFER_SIZE];
        int length = -1;
        while ((length = in.read(data, 0, Tools.BUFFER_SIZE)) != -1)
            outStream.write(data, 0, length);
        data = null;
        return outStream.toByteArray();
    }
}
