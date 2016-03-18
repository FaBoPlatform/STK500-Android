package io.fabo.android.arduinosample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.fabo.android.stk500.StkWriter;
import io.fabo.android.stk500.StkWriterListenerInterface;

public class MainActivity extends AppCompatActivity implements StkWriterListenerInterface {

    /** LOG. */
    private static final String TAG = "SKT500_TEST";

    /** Connnect button. */
    private Button mButtonConnect;

    /** Send button. */
    private Button mButtonSend;

    /** TextView. */
    private static TextView mTextViewCommment;

    /** STK500. */
    private StkWriter mStkWriter;

    /** Activity. */
    private static Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        mTextViewCommment = (TextView) findViewById(R.id.textViewComment);
        mTextViewCommment.setText("USB初期化.");

        mButtonConnect = (Button) findViewById(R.id.buttonConnect);
        mButtonSend = (Button) findViewById(R.id.buttonSend);

        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンがクリックされた時にUSBを開く.
                if (mStkWriter.openUsb()) {

                } else {
                    mTextViewCommment.setText("USBの接続に失敗しました。");
                }
            }
        });
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStkWriter.setData(R.raw.standardfirmata);
                mStkWriter.sendFirmware();
            }
        });

        // USBの装着、脱着をReceiverで取得.
        IntentFilter filter = new IntentFilter();
        filter.addAction(StkWriter.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SerialPortの生成
        mStkWriter = new StkWriter(this);
        mStkWriter.enableDebug();
        mStkWriter.setListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // SerialPortを閉じる
        mStkWriter.closeUsb();
        mStkWriter = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (mStkWriter.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mTextViewCommment.setText("USBに接続しました。");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // USBを閉じる
                mStkWriter.closeUsb();
                mTextViewCommment.setText("USBをクローズしました。");
            } else {
                mTextViewCommment.setText("不明なIntent");
            }
        }
    };

    @Override
    public void onChangeStatus(int status) {

        Log.i(TAG, "status:" + status);

        switch (status) {
            case StkWriter.STATUS_USB_INIT:

                break;
            case StkWriter.STATUS_USB_OPEN:

                break;
            case StkWriter.STATUS_USB_CONNECT:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mButtonSend.setVisibility(Button.VISIBLE);
                        mTextViewCommment.setText("Arduino Unoと接続しました。");
                    }
                });
                break;
            case StkWriter.STATUS_USB_CLOSE:

                break;
            case StkWriter.STATUS_UART_START:

                break;
            case StkWriter.STATUS_FIRMWARE_SEND_INIT:

                break;
            case StkWriter.STATUS_FIRMWARE_SEND_START:

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCommment.setText("Firmwareの転送を開始します。");
                    }
                });
                break;
            case StkWriter.STATUS_FIRMWARE_SEND_FINISH:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCommment.setText("Firmwareの転送が成功しました。");
                    }
                });
                break;
        }
    }

    @Override
    public void onError(int status) {
        Log.i(TAG, "error status:" + status);
        switch (status) {
            case StkWriter.ERROR_FAILED_CONNECTION:

                break;
            case StkWriter.ERROR_FAILED_OPEN:

                break;
            case StkWriter.ERROR_FAILED_SEND_FIRMRARE:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCommment.setText("Firmwareの転送に失敗しました。");
                    }
                });
                break;
            case StkWriter.ERROR_NO_FOUND_FIRMARE:

                break;
            case StkWriter.ERROR_NOT_INIT_USB:

                break;
            case StkWriter.ERROR_NOT_WRITE_UART:

                break;
        }
    }
}
