package asia.chumbaka.kayam.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * In-app WebView host for the Looker Studio (data studio) dashboard.
 *
 * Launched from MainActivity.openDashboard() once the API has returned a
 * `view_url`. Keeps the kid inside the launcher rather than handing them off
 * to whatever default browser the tablet ships with, so they can't escape
 * into other web pages.
 */
public class DashboardWebActivity extends Activity {

    public static final String EXTRA_URL = "view_url";

    private WebView webView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-edge full-screen so the WebView gets the maximum real estate
        // on a 10" tablet. Same flag set the rest of the launcher uses.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dashboard_web);
        Util.hideSystemUI(this);

        webView = (WebView) findViewById(R.id.dashboard_webview);
        progress = (ProgressBar) findViewById(R.id.dashboard_progress);

        // On-screen back button — closes the dashboard and returns to the
        // previous screen (system bars are hidden, so this is the way out).
        View backButton = findViewById(R.id.dashboard_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Looker Studio needs JS + DOM storage and cookies (auth/session).
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        // Modern UA so Looker Studio doesn't fall through to a "browser not supported" page
        s.setUserAgentString(s.getUserAgentString() + " KayamSchoolLauncher/1.0");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.isEmpty()) {
            finish();
            return;
        }
        webView.loadUrl(url);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) Util.hideSystemUI(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Make Back navigate inside the WebView's history first, only leave
        // the activity once we're at the dashboard's root page.
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(new WebViewClient());
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
