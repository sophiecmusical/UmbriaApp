package com.projects.mantitagames.umbriaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.InputStream;

import com.projects.mantitagames.umbriaapp.R;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;
    DrawerLayout drawerLayout;
    NavigationView navView;
    Toolbar toolbar;

    String user = "";
    String pass = "";
    Boolean tinyeditor;

    SwipeRefreshLayout swipeRefreshLayout;

    String baseUrl = "https://www.comunidadumbria.com/usuario/novedades/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);

        setUpToolbar();
        initNavMenu();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        user = sharedPref.getString("user", "");
        pass = sharedPref.getString("pass", "");
        Boolean ckeditor = sharedPref.getBoolean("ckeditor", false);
        tinyeditor = sharedPref.getBoolean("tinyeditor", false);
        Log.d("ANDROID", tinyeditor.toString());

        progressBar = findViewById(R.id.progressBar);
        setWebViewConfig(ckeditor);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewConfig(Boolean ckeditor) {
        webView = findViewById(R.id.partidasJugador);
        webView.setInitialScale(1);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);

        if (ckeditor && !tinyeditor) {
            webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        } else if (tinyeditor) {
            String script = "if (!document.querySelector('script[src=\"https://cdn.tiny.cloud/1/no-api-key/tinymce/5/tinymce.min.js\"]')) {" +
                    "  var script = document.createElement('script');" +
                    "  script.src = 'https://cdn.tiny.cloud/1/no-api-key/tinymce/5/tinymce.min.js';" +
                    "  script.type = 'text/javascript';" +
                    "  document.head.appendChild(script);" +
                    "}";
            webView.evaluateJavascript(script, null);

            webView.evaluateJavascript("if (typeof tinymce !== 'undefined') {" +
                    "tinymce.init({" +
                    "selector: 'textarea', " +
                    "plugins: 'advlist autolink lists link image charmap print preview anchor', " +
                    "toolbar: 'undo redo | formatselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image'" +
                    "});" +
                    "}", null);
        }

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setEnabled(!url.contains("/chat/"));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);


                if (user != null && pass != null) {
                    String script = "var acceso = document.getElementsByName('ACCESO')[0];" +
                            "var clave = document.getElementsByName('CLAVE')[0];" +
                            "if (acceso && clave) {" +
                            "acceso.value = '" + user + "';" +
                            "clave.value = '" + pass + "';" +
                            "}";

                    webView.evaluateJavascript(script, null);
                }
                if (!url.contains("natilla.comunidadumbria.com")) {
                    injectCSS();
                }

                super.onPageFinished(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            swipeRefreshLayout.setRefreshing(false);
        });

        webView.loadUrl(baseUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (item.getItemId() == R.id.exit_to_nav) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNavMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navview);
        navView.setCheckedItem(R.id.partidas);


        navView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.partidas) {
                webView.loadUrl("https://www.comunidadumbria.com/usuario/novedades/");
            } else if (itemId == R.id.news) {
                webView.loadUrl("https://www.comunidadumbria.com/");
            } else if (itemId == R.id.profile) {
                webView.loadUrl("https://www.comunidadumbria.com/usuario/perfil");
            } else if (itemId == R.id.forum) {
                webView.loadUrl("https://www.comunidadumbria.com/comunidad/foros");
            } else if (itemId == R.id.messages) {
                webView.loadUrl("https://www.comunidadumbria.com/usuario/mensajes");
            } else if (itemId == R.id.settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (itemId == R.id.sign_out) {
                webView.loadUrl("https://www.comunidadumbria.com/logout");
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void injectCSS() {
        try {
            InputStream inputStream = getAssets().open("style.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style);" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_hamburguer);
        }
    }
}
