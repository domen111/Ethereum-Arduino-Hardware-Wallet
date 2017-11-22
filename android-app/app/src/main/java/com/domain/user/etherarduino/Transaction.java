package com.domain.user.etherarduino;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.usb.*;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.app.PendingIntent;
import android.content.*;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

    private static final String ACTION_USB_PERMISSION = "com.domain.user.etherarduino.USB_PERMISSION";
    UsbDevice device;
    UsbDeviceConnection connection;
    UsbManager usbManager;
    UsbSerialDevice serialPort;
    PendingIntent pendingIntent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transection);

        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(broadcastReceiver, filter);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 1027 || deviceVID == 9025) { //Arduino Vendor ID
                    usbManager.requestPermission(device, pendingIntent);
                    break;
                } else {
                    connection = null;
                    device = null;
                }
            }
        }
    }

    public void SendTransaction(View view) {
        serialPort.write("ooo".getBytes());
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

    private boolean mode = false;

    private void arduino_sign()
    {
        EditText e = this.findViewById(R.id.address);
        serialPort.write(this.prepareTransaction(e.getText().toString()));
        mode = true;
    }

    private void putOnChain(byte[] data)
    {
        try {
            byte v = data[0];
            byte[] r = Arrays.copyOfRange(data, 1, 33);
            byte[] s = Arrays.copyOfRange(data, 33, 65);
            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

            List<RlpType> values = asRlpValues(rawTransaction, signatureData);
            RlpList rlpList = new RlpList(values);
            byte[] encodedTransaction = RlpEncoder.encode(rlpList);
            String hexValue = Numeric.toHexString(encodedTransaction);
            Log.d("MYDEBUG", hexValue);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
            final String transactionHash = ethSendTransaction.getTransactionHash();
            Log.d("MYDEBUG", transactionHash);
            final TextView TXHash_tv = this.findViewById(R.id.TransactionHash);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Spanned sp = Html.fromHtml("<a href =\"https://ropsten.etherscan.io/tx/" + transactionHash + "\">" + transactionHash + "</a>");
                    TXHash_tv.setText(sp);
                    TXHash_tv.setMovementMethod(LinkMovementMethod.getInstance());
                }
            });
        } catch (Exception e) {
            Log.d("MYDEBUG", e.getClass().getName() + " : " + e.getMessage());
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERMISSION NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {

            }
        };
    };
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        ArrayList<Byte> buffer = new ArrayList<>();

        @Override
        public void onReceivedData(byte[] arg0)
        {
            Log.d("MYDEBUG", Arrays.toString(arg0));
            if(mode) mode_sign(arg0);
            else mode_confirm(arg0);
        }

        private void mode_confirm(byte[] arg0)
        {
            for( byte b : arg0 )
            {
                buffer.add(b);
            }

            while( buffer.size() >= 3 )
            {
                Log.d("MYDEBUG", "Buffer " +Arrays.toString(buffer.toArray()));
                if( buffer.get(0) == 'o' && buffer.get(1) == 'o' && buffer.get(2) == 'o' )
                {
                    Log.d("MYDEBUG", "Password correct");
                    buffer.clear();
                    Transaction.this.arduino_sign();
                }
                else
                {
                    buffer.remove(0);
                }
            }
        }

        private void mode_sign(byte[] arg0)
        {

            for( byte b : arg0 )
            {
                buffer.add(b);
            }
            Log.d("MYDEBUG", "Buffer size " + buffer.size());
            if( buffer.size() >= 65 )
            {
                byte[] b = new byte[65];

                for( int i = 0; i < 65; i++ )
                    b[i] = buffer.get(i);
                Log.d("MYDEBUG", "Received signature");
                Transaction.this.putOnChain(b);
            }
        }
    };

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
