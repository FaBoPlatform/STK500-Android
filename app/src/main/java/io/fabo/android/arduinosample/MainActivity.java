package io.fabo.android.arduinosample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.fabo.android.stk500.Stk500v1;

public class MainActivity extends AppCompatActivity {

    /** LOG. */
    private static final String TAG = MainActivity.class.getCanonicalName();

    /** Connnect button. */
    private Button mButtonConnect;

    /** Send button. */
    private Button mButtonSend;

    /** TextView. */
    private TextView mTextViewCommment;

    /** STK500. */
    private Stk500v1 mStk500v1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewCommment = (TextView) findViewById(R.id.textViewComment);
        mButtonConnect = (Button) findViewById(R.id.buttonConnect);
        mButtonSend = (Button) findViewById(R.id.buttonSend);

        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンがクリックされた時にUSBを開く.
                if (mStk500v1.openUsb()) {
                    mButtonSend.setVisibility(Button.VISIBLE);
                    mTextViewCommment.setText("USBに接続しました。");
                } else {
                    mTextViewCommment.setText("USBの接続に失敗しました。");
                }
            }
        });
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStk500v1.setData(R.raw.standardfirmata);
                mStk500v1.sendFirmware();
            }
        });

        // USBの装着、脱着をReceiverで取得.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Stk500v1.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextViewCommment.setText("USB初期化.");

        // SerialPortの生成
        mStk500v1 = new Stk500v1(this);
        mStk500v1.enableDebug();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // SerialPortを閉じる
        mStk500v1.closeUsb();
        mStk500v1 = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (mStk500v1.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mTextViewCommment.setText("USBに接続しました。");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // USBを閉じる
                mStk500v1.closeUsb();
                mTextViewCommment.setText("USBをクローズしました。");
            } else {
                mTextViewCommment.setText("不明なIntent");
            }
        }
    };

}
