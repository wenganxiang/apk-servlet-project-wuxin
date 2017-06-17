package wuxin.enroll.prediction.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import wuxin.enroll.prediction.R;

public class WebFragment extends Fragment {

	private static final String APP_CACAHE_DIRNAME = "/webcache";

	private String wenUrl;
	private WebView mWebView;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 0x0001: {
				mWebView.goBack();
			}
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_web_view, container, false);
		mWebView = (WebView) root.findViewById(R.id.webView);
		return root;
	}

	@Override
	@SuppressLint("SetJavaScriptEnabled")
	public void onResume() {
		super.onResume();
		wenUrl = getActivity().getIntent().getStringExtra("web_url");
		if (wenUrl != null) {
			WebSettings settings = mWebView.getSettings();
			settings.setJavaScriptEnabled(true);
			mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			mWebView.getSettings().setSupportZoom(true); // 支持放大缩小
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.loadUrl(wenUrl);
			settings.setUseWideViewPort(true);
			settings.setLoadWithOverviewMode(true);
			mWebView.getSettings().setSaveFormData(true);// 保存表单数据
			mWebView.setWebViewClient(client);
			String cacheDirPath = getActivity().getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME; // 缓存路径

			mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 缓存模式
			mWebView.getSettings().setAppCachePath(cacheDirPath); // 设置缓存路径
			mWebView.getSettings().setAppCacheEnabled(true); // 开启缓存功能

			mWebView.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
						mHandler.sendEmptyMessage(0x0001);
						return true;
					}
					return false;
				}
			});
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	WebViewClient client = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);

		}
	};

}
