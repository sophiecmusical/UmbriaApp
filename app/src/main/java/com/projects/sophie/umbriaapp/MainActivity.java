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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
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
        }

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        String simpleTextEditor =
                "(function(){"
                        + " function addCss(href,cb){var l=document.createElement('link');l.rel='stylesheet';l.href=href;l.onload=cb;document.head.appendChild(l);} "
                        + " function addScript(src,cb){var s=document.createElement('script');s.src=src;s.onload=cb;document.head.appendChild(s);} "
                        + " function hardKillCKE(){ try{ if(window.CKEDITOR && CKEDITOR.instances){ for(var i in CKEDITOR.instances){ try{CKEDITOR.instances[i].destroy(true);}catch(e){} } } }catch(e){} } "
                        + " function fire(el){ try{ el.dispatchEvent(new Event('input',{bubbles:true})); el.dispatchEvent(new Event('change',{bubbles:true})); }catch(e){} } "
                        + " function boot(){ "
                        + "   var qMap=new WeakMap(); "
                        + "   function makeQuill(ta,idx){ if(!ta || qMap.has(ta)) return; if(!ta.id) ta.id='ta_'+(ta.name||idx||0); "
                        + "     var box=document.createElement('div'); box.style.height='250px'; box.style.margin='0 0 10px 0'; "
                        + "     ta.parentNode.insertBefore(box, ta); ta.style.display='none'; "
                        + "     var quill=new Quill(box,{ theme:'snow', modules:{ toolbar:[[\"bold\",\"italic\",\"underline\",\"strike\"],[{list:'ordered'},{list:'bullet'}],[\"link\",\"image\"]] } }); "
                        + "     if(ta.value && ta.value.trim()){ quill.clipboard.dangerouslyPasteHTML(ta.value); } "
                        + "     function sync(){ var html=(quill&&quill.root)?quill.root.innerHTML:''; ta.value=html; fire(ta);} "
                        + "     quill.once('editor-change', sync); quill.on('text-change', sync); "
                        + "     qMap.set(ta,{quill:quill,sync:sync}); "
                        + "     var form=ta.closest('form'); if(form && !form.__quillSync){ form.addEventListener('submit',function(){ "
                        + "       var tas=form.querySelectorAll('textarea'); for(var i=0;i<tas.length;i++){ var m=qMap.get(tas[i]); if(m&&m.sync) m.sync(); } "
                        + "     }, true); form.__quillSync=true; } "
                        + "   } "
                        + "   hardKillCKE(); "
                        + "   var tas=document.querySelectorAll('textarea'); for(var i=0;i<tas.length;i++){ makeQuill(tas[i], i); } "
                        + "   var t0=Date.now(), guard=setInterval(function(){ hardKillCKE(); "
                        + "     var visibles=document.querySelectorAll('textarea:not([style*=\"display: none\"])'); "
                        + "     for(var i=0;i<visibles.length;i++){ makeQuill(visibles[i]); } "
                        + "     if(Date.now()-t0>6000){ clearInterval(guard);} "
                        + "   },250); "
                        + "   var mo=new MutationObserver(function(muts){ muts.forEach(function(m){ "
                        + "     m.addedNodes && m.addedNodes.forEach(function(n){ if(n.nodeType!==1) return; "
                        + "       if(n.id && /^cke_/.test(n.id)){ try{ n.remove(); }catch(e){} hardKillCKE(); } "
                        + "       if(n.classList && (n.classList.contains('ck')||n.classList.contains('ck-editor'))){ try{ n.remove(); }catch(e){} } "
                        + "       if(n.tagName==='TEXTAREA'){ makeQuill(n); } "
                        + "       if(n.querySelectorAll){ var as=n.querySelectorAll('textarea'); for(var j=0;j<as.length;j++){ makeQuill(as[j]); } } "
                        + "     }); }); }); "
                        + "   mo.observe(document.documentElement,{childList:true,subtree:true}); "
                        + " } "
                        + " function start(){ addCss('https://cdn.quilljs.com/1.3.7/quill.snow.css', function(){ addScript('https://cdn.quilljs.com/1.3.7/quill.js', boot); }); } "
                        + " if(document.readyState==='loading'){ document.addEventListener('DOMContentLoaded', start); } else { start(); } "
                        + "})();";



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

                if (tinyeditor) {
                    webView.evaluateJavascript(simpleTextEditor, null);
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
