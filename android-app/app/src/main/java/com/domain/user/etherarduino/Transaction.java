package com.domain.user.etherarduino;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.usb.*;
import android.view.View;
import android.app.PendingIntent;
import android.content.*;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import static org.web3j.crypto.TransactionEncoder.encode;

public class Transaction extends AppCompatActivity {
    Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/"));
    RawTransaction rawTransaction;

    UsbManager mUsbManager;
    UsbDevice device;
    private static final String ACTION_USB_PERMISSION = "com.domain.user.etherarduino.USB_PERMISSION";
    UsbInterface intf;
    UsbEndpoint endpoint;
    UsbDeviceConnection connection;
//    final int interfaceNo = 0, endpointNo_in = 1, endpointNo_out = 0;
    final int interfaceNo = 1, endpointNo_in = 0, endpointNo_out = 1;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // Set up device communication
                            // It should be done in another to prevent blocking of UI
                            intf = device.getInterface(interfaceNo);
                            connection = mUsbManager.openDevice(device);
                            new Thread(new Runnable() {
                                public void run() {
                                    sendToArduino();
                                }
                            }).start();
                        }
                    } else {
                        Log.d("ERROR", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transection);
    }

    public void SendTransaction(View view) {
        mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            device = deviceList.get(deviceList.keySet().toArray()[0]);
            Log.d("MYDEBUG", "Device vendor id: " + device.getVendorId());
        }
        if (device == null || device.getVendorId() != 0x403 && device.getVendorId() != 0x2341) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No arduino connected.")
                    .show();
            return;
        }
        try {
            // Request for permission
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            this.registerReceiver(mUsbReceiver, filter);
            mUsbManager.requestPermission(device, mPermissionIntent);
        } catch (Exception ex) {
            Log.d("ERROR", ex.getClass().getName() + " : " + ex.getMessage());
        }
    }

    void sendToArduino()
    {
        // Show all interfaces & endpoints
//        Log.d("MYDEBUG", "Device Name: " + device.getProductName());
//        Log.d("MYDEBUG", "interface count: " + device.getInterfaceCount());
//        for (int i = 0; i < device.getInterfaceCount(); i++) {
//            UsbInterface intf = device.getInterface(i);
//            Log.d("MYDEBUG", "interface #" + i + ": " + intf.getName());
//            for (int j = 0; j < intf.getEndpointCount(); j++) {
//                UsbEndpoint endpoint = intf.getEndpoint(j);
//                Log.d("MYDEBUG", "endpoint #" + j + " describeContents: " + endpoint.describeContents());
//                Log.d("MYDEBUG", "endpoint #" + j + " getType: " + endpoint.getType());
//                Log.d("MYDEBUG", "endpoint #" + j + " getDirection: " + endpoint.getDirection());
//                Log.d("MYDEBUG", "endpoint #" + j + " toString: " + endpoint.toString());
//            }
//        }
        endpoint = intf.getEndpoint(endpointNo_in);
        Log.d("MYDEBUG", "endpoint type: " + endpoint.getType());

        // Send TX data to arduino
        byte[] data = prepareTransaction("0xB0Ae540978bED7fF6e6C6658F38B6b206b98E65D");
        connection.claimInterface(intf, true);
        connection.bulkTransfer(endpoint, data, data.length, 3);

        Log.d("MYDEBUG", "done sending message");

        receiveFromArduino();
    }

    void receiveFromArduino() {
        endpoint = intf.getEndpoint(endpointNo_out);
        for(int j=0;j<1000000;j++) {
//            Log.d("MYDEBUG", "endpoint type: " + endpoint.getType());

            // Send TX data to arduino
            byte[] data = new byte[1];
            connection.claimInterface(intf, true);
            connection.bulkTransfer(endpoint, data, data.length, 3);

            String s = "";
            for (int i = 0; i < data.length; i++)
                s += data[i];
            if(data[0]!=0)
                Log.d("MYDEBUG", "Received: " + (char)data[0]);
//            Log.d("MYDEBUG", "Received: " + s);
            try {
                TimeUnit.MICROSECONDS.sleep(1);
            }catch (Exception e){
                Log.d("MYDEBUG", e.toString());
            }
        }
//        try {
//            byte v = data[0];
//            byte[] r = Arrays.copyOfRange(data, 1, 33);
//            byte[] s = Arrays.copyOfRange(data, 33, 65);
//            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
//            List<RlpType> values = asRlpValues(rawTransaction, signatureData);
//            RlpList rlpList = new RlpList(values);
//            byte[] encodedTransaction = RlpEncoder.encode(rlpList);
//            String hexValue = Numeric.toHexString(encodedTransaction);
//            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
//            String transactionHash = ethSendTransaction.getTransactionHash();
//            Log.d("MYDEBUG", transactionHash);
//            final TextView TXHash_tv = this.findViewById(R.id.TransactionHash);
//            TXHash_tv.setText(transactionHash);
//        } catch (Exception e) {
//            Log.d("MYDEBUG", e.toString());
//        }
    }

    byte[] prepareTransaction(String addr)
    {
        final EditText address_text = this.findViewById(R.id.address);
        final EditText value_text = this.findViewById(R.id.value);
        final EditText gasPrice_text = this.findViewById(R.id.price);
        try {
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                    addr, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            Log.d("MYDEBUG", "nonce " + nonce.toString());

            BigInteger value = Convert.toWei(value_text.getText().toString(), Convert.Unit.ETHER).toBigInteger();
            Log.d("MYDEBUG", "value " + value.toString());

            // nonce, gas_price, gas_limit, toAddress, value, data
            rawTransaction = RawTransaction.createTransaction(
                    nonce, new BigInteger(gasPrice_text.getText().toString()), BigInteger.valueOf(21000),
                    address_text.getText().toString(), value, "");
            //byte[] encodedTransaction = encode(rawTransaction);
            byte[] messageHash = Hash.sha3(encode(rawTransaction));
            String str = "";
            for (int i = 0; i < messageHash.length; i++) {
                str += (messageHash[i] & 0xFF) + " ";
            }
            Log.d("MYDEBUG", str);
            return messageHash;
        }catch (Exception e){
            Log.d("MYDEBUG", e.toString());
            return null;
        }

    }

    static List<RlpType> asRlpValues(
            RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(rawTransaction.getNonce()));
        result.add(RlpString.create(rawTransaction.getGasPrice()));
        result.add(RlpString.create(rawTransaction.getGasLimit()));

        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        if (signatureData != null) {
            result.add(RlpString.create(signatureData.getV()));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }
}
