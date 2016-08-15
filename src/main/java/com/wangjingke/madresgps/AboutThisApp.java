package com.wangjingke.madresgps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


public class AboutThisApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView about = (TextView) findViewById(R.id.aboutText);
        about.setText(Html.fromHtml(getString(R.string.aboutThisAppText)));
        about.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
