package com.domain.user.etherarduino;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Balance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
    }

    public void send(View v)
    {
        Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/"));
        try {
            EditText e = this.findViewById(R.id.address);
            EthGetBalance ethGetBalance = web3.ethGetBalance(e.getText().toString(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger balance = ethGetBalance.getBalance();
            Log.d("MYDEBUG", balance.toString());

            this.print(balance);
        } catch (Exception e) {
            Log.d("MYDEBUG", e.toString());
        }
    }

    public void print(BigInteger res)
    {
        final TextView res_wei = this.findViewById(R.id.res_wei);
        final TextView res_ether = this.findViewById(R.id.res_ether);

        res_wei.setText(res + " Wei");

        BigDecimal b1 = new BigDecimal(res);
        BigDecimal b2 = new BigDecimal("1000000000000000000");
        res_ether.setText(b1.divide(b2).toString() + " Ether");
    }
}
