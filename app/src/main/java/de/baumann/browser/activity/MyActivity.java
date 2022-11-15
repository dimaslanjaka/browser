package de.baumann.browser.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import de.baumann.browser.R;

public class MyActivity extends AppCompatActivity {
    private WebView mWebView;
    static boolean finished = false;
    static int scrollSpeed = 60;
    boolean doubleBackToExitPressedOnce = false;

    @SuppressLint({"ObsoleteSdkInt", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mWebView = findViewById(R.id.webview);
        mWebView.setDrawingCacheEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        //mWebView.getSettings().setUseWideViewPort(true);

        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.clearCache(true);

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        // set compatibility with auto scroll
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //FOR WEBPAGE SLOW UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(MyActivity.this, "Finished loading", Toast.LENGTH_SHORT).show();
                mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
                mWebView.getSettings().setUseWideViewPort(true);

                mWebView.getSettings().setAppCacheEnabled(false);
                mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                mWebView.clearCache(true);
                // use the param "view", and call getContentHeight in scrollTo
                // view.scrollTo(0, view.getContentHeight());

                scrollDown();
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Toast.makeText(MyActivity.this, title, Toast.LENGTH_SHORT).show();
            }

        });

        //get the UA of the current running device:
        //String userAgent = mWebView.getSettings().getUserAgentString() ;
        String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:106.0) Gecko/20100101 Firefox/106.0";
        //set the UA of the web-view to this value:
        mWebView.getSettings().setUserAgentString(userAgent);

        Map<String, String> headers = new HashMap<>();
        headers.put("referer", "https://indosat.com");
        headers.put("DNT", "1");

        clearCookies(getApplicationContext());

        mWebView.loadUrl("https://www.webmanajemen.com", headers);
        // mWebView.loadUrl("https://www.webmanajemen.com/page/bot-detect", headers);
        // mWebView.loadUrl("https://www.whatismybrowser.com/", headers);
        // mWebView.loadUrl("https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending", headers);

        // reload app when timeout
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        reload();
                    }
                },
                java.util.concurrent.TimeUnit.MINUTES.toMillis(2));
    }

    private void scrollup() {
        Handler scrollUpHandler = new Handler();
        Runnable scrollUpTask = new Runnable() {
            @Override
            public void run() {
                WebView webview = (WebView) findViewById(R.id.webview);
                webview.scrollBy(0, -scrollSpeed);
                float scrollY = webview.getScrollY();
                float contentHeight = webview.getContentHeight() * webview.getScaleY();
                float total = contentHeight * getResources().getDisplayMetrics().density - webview.getHeight();
                // on some devices just 1dp was missing to the bottom when scroll stopped, so we subtract it to reach 1
                float percent = Math.min(scrollY / (total - getResources().getDisplayMetrics().density), 1);
                // Log.d("scroll-up", "Percentage: " + percent);

                if (scrollY <= 0) {
                    Log.d("scroll-up", "Reached top");
                    finished = true;
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                public void run() {
                                    scrollDown();
                                }
                            },
                            5000);
                } else {
                    scrollUpHandler.postDelayed(this, 200);
                }
            }
        };
        scrollUpHandler.post(scrollUpTask);
    }

    private void scrollDown() {
        Handler scrollDownHandler = new Handler();
        Runnable scrollDownTask = new Runnable() {
            public void run() {
                WebView webview = (WebView) findViewById(R.id.webview);
                webview.scrollBy(0, scrollSpeed);
                float scrollY = webview.getScrollY();
                float contentHeight = webview.getContentHeight() * webview.getScaleY();
                float total = contentHeight * getResources().getDisplayMetrics().density - webview.getHeight();
                // on some devices just 1dp was missing to the bottom when scroll stopped, so we subtract it to reach 1
                float percent = Math.min(scrollY / (total - getResources().getDisplayMetrics().density), 1);
                // Log.d("scroll-down", "Percentage: " + percent);

                if (scrollY >= total - 1) {
                    Log.d("scroll-down", "Reached bottom");
                    finished = true;
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                public void run() {
                                    scrollup();
                                }
                            },
                            5000);
                } else {
                    scrollDownHandler.postDelayed(this, 200);
                }
            }
        };
        scrollDownHandler.post(scrollDownTask);
    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //Log.d("Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            //Log.d("Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        this.doubleBackToExitPressedOnce = false;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, "Shutting down", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            finish();
            return;
        }
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            Toast.makeText(this, "Back Webview", Toast.LENGTH_SHORT).show();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }
}

interface LoadOpt {
    String url = null;
    String useragent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:106.0) Gecko/20100101 Firefox/106.0";
}
