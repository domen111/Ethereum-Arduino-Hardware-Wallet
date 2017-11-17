package com.domain.user.etherarduino;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getBalance(View v)
    {
        Intent it = new Intent();
        it.setClass(this , Balance.class);
        this.startActivity(it);
    }

    public void sendTransaction(View v)
    {
        Intent it = new Intent();
        it.setClass(this , Transaction.class);
        this.startActivity(it);
    }
}
