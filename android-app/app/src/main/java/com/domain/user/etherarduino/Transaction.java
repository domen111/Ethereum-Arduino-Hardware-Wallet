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

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
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
    }

    public void SendTransaction(View view) {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();

                if (deviceVID == 1027 || deviceVID == 9025) { //Arduino Vendor ID
                    usbManager.requestPermission(device, pendingIntent);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }
                if (!keep)
                    break;
            }
        }
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
                //onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //can add something to close the connection
            }
        };
    };
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                if ((int)(arg0[0] & 0xFF) == 245) {
                    
                }
                for (int i = 0; i < arg0.length; i++) {
                    Log.d("MYDEBUG", (arg0[i] & 0xFF) + "");
                }
                //Log.d("MYDEBUG", Arrays.toString(arg0));
            } catch (Exception e) {
                e.printStackTrace();
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
