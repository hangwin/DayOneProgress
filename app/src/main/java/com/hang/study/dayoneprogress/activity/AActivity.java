package com.hang.study.dayoneprogress.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hang.study.dayoneprogress.R;

public class AActivity extends Activity implements View.OnClickListener{
    public Button gotoB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_layout);
        gotoB= (Button) findViewById(R.id.gotoB);
        gotoB.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent=new Intent(this,BActivity.class);
        startActivity(intent);
    }
}
