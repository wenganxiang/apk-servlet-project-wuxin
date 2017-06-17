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
					URL url = new URL(Tools.URL_STR); // 创建url资源
					HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 建立http连接
					conn.setDoInput(true);
					conn.setDoOutput(true); // 设置允许输出
					conn.setUseCaches(false); // 设置不用缓存
					conn.setReadTimeout(Tools.HTTP_READ_TIMEOUT); //读取超时
					conn.setConnectTimeout(Tools.HTTP_CONNECT_TIMEOUT); //请求超时
					conn.setRequestMethod("POST"); // 设置传递方式
					conn.setRequestProperty("Connection", "Keep-Alive"); // 设置维持长连接
					// conn.setRequestProperty("Charset", "GBK"); // 设置文件字符集:
					// conn.setRequestProperty(HTTP.CONTENT_TYPE,
					// "application/json"); // 设置文件类型:
					byte[] req = doHttpIO.beforeRequest();
					if (req != null) {
						conn.setRequestProperty("Content-Length", String.valueOf(req.length)); // 设置文件长度
						conn.connect(); // 开始连接请求
						OutputStream out = conn.getOutputStream();
						out.write(req); // 写入请求的字符串
						out.flush();
						out.close();
					}
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 请求返回的状态
						// byte[] temp = new byte[1024];
						// StringBuilder result = new StringBuilder();
						// while ((readLen = in.read(temp)) > 0) {
						// result.append(new String(temp, 0, readLen, "GBK"));
						// }
						// String json = result.toString(); // 转成字符串
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
						} else { //长字节读取
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
