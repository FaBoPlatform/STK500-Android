package io.fabo.android.stk500;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Stk500v1 {

    /** LOG. */
    private static final String TAG = Stk500v1.class.getCanonicalName();

    /** Context. */
    private Context mContext;

    /** USB Manager. */
    private UsbManager mUsbManager;

    /** USB Port. */
    private static UsbSerialPort mSerialPort = null;

    /** USB Device. */
    private static UsbDevice mUsbDevice = null;

    /** USBのSerial IO Manager. */
    private static SerialInputOutputManager mSerialIoManager;

    /** Executor. */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /** Baudrate. */
    private final static int SPEEDRATE = 115200;

    /** Debug flag. */
    private static boolean debugFlag = false;

    /** USBの装着、脱着イベント. */
    public final static String ACTION_USB_PERMISSION = "io.fabo.android.usb_permission";

    /** Permission用のIntent. */
    private static PendingIntent mPermissionIntent;

    /** Commandの状態. */
    private static int cmdStatus;

    /** 取得したバイナリコード. */
    private static ArrayList<Byte> myHex = new ArrayList<Byte>();

    /** 転送済みの回数. */
    private static int progCount = 0;

    /** 転送ページ数. */
    private static int pageCount = 0;

    /**
     * Constructor.
     *
     * @param context
     */
    public Stk500v1(Context context){
        this.mContext = context;
    }

    /**
     * USBを開く.
     * @return true 開けた場合, false 失敗した場合
     */
    public boolean openUsb() {

        // USBManagerを取得.
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        // 使用可能なUSB Portを取得.
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
        final List<UsbDevice> device = new ArrayList<UsbDevice>();

        if (drivers.size() == 0) {
            if(debugFlag) {
                Toast.makeText(mContext, R.string.not_found_usb, Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {

            // 発見したPortをResultに一時格納.
            for (final UsbSerialDriver driver : drivers) {
                final List<UsbSerialPort> ports = driver.getPorts();
                result.addAll(ports);
                device.add(driver.getDevice());
            }

            // 一番最後に発見されたPortをmPortに格納.
            int count = result.size();
            mSerialPort = result.get(count - 1);
            mUsbDevice = device.get(count - 1);

            // USBの抜き差しでイベントを飛ばす.
            mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);

            // PortをOpen.
            UsbDeviceConnection connection = mUsbManager.openDevice(mSerialPort.getDriver().getDevice());

            if (connection == null) {
                if(debugFlag) {
                    Toast.makeText(mContext, R.string.can_not_open_usb, Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            try {
                mSerialPort.open(connection);
                mSerialPort.setParameters(SPEEDRATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                if (debugFlag) {
                    Toast.makeText(mContext, "Open USB SerialPort.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                if(debugFlag) {
                    Toast.makeText(mContext, "Open Error:" + e, Toast.LENGTH_SHORT).show();
                }
                try {
                    mSerialPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialPort = null;
                return false;
            }
        }

        // io managerをStart.
        onDeviceStateChange();

        return true;
    }

    /**
     * Arduino側から返答のあるメッセージを受信するLisener.
     */
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    if (debugFlag) {
                        Toast.makeText(mContext, "Runner stopped:" + e, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNewData(final byte[] data) {
                    Log.i(TAG, "data.length" + data.length);
                    if(cmdStatus == StkCmdV1.STATUS_STK_GET_SYN &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[1] == StkParamV1.STK_OK) {
                        cmdStatus = StkCmdV1.STATUS_GET_MAJOR;
                        sendMessage(StkCmdV1.CMD_GET_MAJOR);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_GET_MAJOR &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[2] == StkParamV1.STK_OK) {
                        cmdStatus = StkCmdV1.STATUS_GET_MINOR;
                        sendMessage(StkCmdV1.CMD_GET_MINOR);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_GET_MINOR &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[2] == StkParamV1.STK_OK) {
                        cmdStatus = StkCmdV1.STATUS_SET_DEVICE;
                        sendMessage(StkCmdV1.CMD_SET_DEVICE);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_SET_DEVICE &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[1] == StkParamV1.STK_OK) {
                        cmdStatus = StkCmdV1.STATUS_SET_DEVICE_EXT;
                        sendMessage(StkCmdV1.CMD_SET_DEVICE_EXT);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_SET_DEVICE_EXT &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[1] == StkParamV1.STK_OK) {
                        cmdStatus = StkCmdV1.STATUS_ENTER_PROGRAM_MODE;
                        sendMessage(StkCmdV1.CMD_LEAVE_PROGRAM_MODE);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_ENTER_PROGRAM_MODE &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[1] == StkParamV1.STK_OK) {
                        cmdStatus = StkCmdV1.STATUS_READ_SIG;
                        sendMessage(StkCmdV1.CMD_READ_SIG);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_READ_SIG &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[4] == StkParamV1.STK_OK) {


                        progCount = 0;
                        int startPos = 0;
                        int high = (startPos >> 8) & 0xff;
                        int low = startPos & 0xff;

                        byte[] cmdLoadAddress = new byte[4];
                        cmdLoadAddress[0] = StkCmdV1.CMD_LOAD_ADDRESS;
                        cmdLoadAddress[1] = (byte)low;
                        cmdLoadAddress[2] = (byte)high;
                        cmdLoadAddress[3] = (byte)StkParamV1.CRC_EOP;

                        cmdStatus = StkCmdV1.STATUS_LOAD_ADDRESS_FOR_WRITE;
                        sendMessage(cmdLoadAddress);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_LOAD_ADDRESS_FOR_WRITE &&
                            data[0] == StkParamV1.STK_INSYNC &&
                            data[1] == StkParamV1.STK_OK) {

                        int startPos = StkCmdV1.PAGE_SIZE * progCount;
                        byte[] cmdProg = new byte[128 + 5];
                        byte[] progData = new byte[128];

                        Log.i(TAG, "startPos:" + startPos);
                        Log.i(TAG, "myHex.size():" + myHex.size());
                        for(int b = 0; b < StkCmdV1.PAGE_SIZE; b++){
                            if( (startPos + b) < myHex.size()){
                                progData[b] = myHex.get(startPos + b);
                            } else {
                                progData[b] = (byte)0xff;
                            }
                        }

                        cmdProg[0] = StkCmdV1.CMD_PROG_PAGE;
                        cmdProg[1] = (byte)0x00;
                        cmdProg[2] = (byte)progData.length;
                        cmdProg[3] = (byte)0x46; // "F", Flash
                        System.arraycopy(progData, 0, cmdProg, 4, 128);
                        cmdProg[cmdProg.length - 1] = StkParamV1.CRC_EOP;

                        progCount++;

                        cmdStatus = StkCmdV1.STATUS_PROG_PAGE;
                        sendMessage(cmdProg);
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_PROG_PAGE && data[0] == StkParamV1.STK_OK) {

                        if (progCount <= pageCount) {
                            int startPos = StkCmdV1.PAGE_SIZE * progCount; // next page is after 128byte

                            int high = ((startPos / 2) >> 8) & 0xff;
                            int low = (startPos / 2) & 0xff;

                            byte[] cmdLoadAddress = new byte[4];
                            cmdLoadAddress[0] = StkCmdV1.CMD_LOAD_ADDRESS;
                            cmdLoadAddress[1] = (byte)low;
                            cmdLoadAddress[2] = (byte)high;
                            cmdLoadAddress[3] = (byte)StkParamV1.CRC_EOP;

                            cmdStatus = StkCmdV1.STATUS_LOAD_ADDRESS_FOR_WRITE;
                            sendMessage(cmdLoadAddress);
                        } else {
                            cmdStatus = StkCmdV1.STATUS_LEAVE_PROGRAM_MODE;
                            sendMessage(StkCmdV1.CMD_LEAVE_PROGRAM_MODE);
                        }
                    }
                    else if(cmdStatus == StkCmdV1.STATUS_LEAVE_PROGRAM_MODE){
                        cmdStatus = StkCmdV1.STATUS_FINISH;
                        Log.i(TAG, "OK");
                    }
                }
            };

    /**
     * シリアル通信を開始.
     */
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    /**
     * シリアル通信をストップする.
     */
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            if (debugFlag) {
                Toast.makeText(mContext, "Stopping io manager.", Toast.LENGTH_SHORT).show();
            }
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    /**
     * シリアル通信を開始する.
     */
    private void startIoManager() {
        if (mSerialPort != null) {
            if (debugFlag) {
                Toast.makeText(mContext, "Starting io manager.", Toast.LENGTH_SHORT).show();
            }
            mSerialIoManager = new SerialInputOutputManager(mSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    /**
     * Close usb.
     */
    public void closeUsb(){
        if(mSerialPort != null) {
            try {
                mSerialPort.close();
                mSerialPort = null;
                if (debugFlag) {
                    Toast.makeText(mContext, R.string.close_usb, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                if (debugFlag) {
                    Toast.makeText(mContext, "Close USB Error:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Show toast message.
     */
    public void enableDebug(){
        debugFlag = true;
    }

    /**
     * メッセージの送信
     * @param mByte Byte型のメッセージ
     */
    public void sendMessage(byte[] mByte) {
        if (debugFlag) {
            logCommand(mByte);
        }
        if(mSerialIoManager != null) {
            try {
                mSerialPort.write(mByte, 10000);
            }catch(Exception e){
                if (debugFlag) {
                    Toast.makeText(mContext, "SendMessageError:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
        }
    }

    public void logCommand(byte[] cmdArray){
        String logCmd = "[Command:";
        for(int i = 0; i < cmdArray.length; i++){
            String tmpStr = Integer.toHexString(cmdArray[i] & 0xff);
            if(tmpStr.length() == 1){
                tmpStr = "0x0" + tmpStr;
            } else {
                tmpStr = "0x" + tmpStr;
            }
            logCmd += tmpStr += ",";
        }
        logCmd += "]";
        Log.i(TAG,logCmd);
    }

    public void sendFirmware() {
        try {
            mSerialPort.setRTS(false);
            mSerialPort.setDTR(false);

            Thread.sleep(500);

            mSerialPort.setRTS(true);
            mSerialPort.setDTR(true);

            Thread.sleep(500);
        } catch (Exception e) {
        }

        this.sendMessage(StkCmdV1.CMD_STK_GET_SYNC);
        cmdStatus = StkCmdV1.STATUS_STK_GET_SYN;
    }

    public void setData(int id) {
        InputStream input;
        byte[] firmwareData;

        try {
            // firmwareを読み込む
            input = mContext.getResources().openRawResource(id);
            firmwareData = new byte[(int) input.available()];
            input.read(firmwareData);
            input.close();

            if(firmwareData == null || firmwareData.length == 0){
                if (debugFlag) {
                    Toast.makeText(mContext, "Can not open firmware, file size is ZERO.", Toast.LENGTH_SHORT).show();
                }
            } else {
                int length = firmwareData.length;

                byte[] hex = new byte[length - 13];
                System.arraycopy(firmwareData, 0, hex, 0, length - 13);

                // calc line number of data.
                int hexLines = (int)Math.ceil((double)hex.length / 45);
                // calc unusedBytes.
                int unusedBytes = (45 - 32) * hexLines;
                // calc count of data.
                pageCount = Math.round(Math.round(((hex.length - unusedBytes)/2))/StkCmdV1.PAGE_SIZE);

                int lastCount = 0;
                int nowPos = 0;

                for(int l = 0; l < hexLines; l++){
                    nowPos = nowPos + lastCount;
                    lastCount = 0;
                    for(int i = 9; i < 41; i += 2){
                        if(nowPos + i < hex.length - 4){
                            if(hex[nowPos + i + 2] == '\r'){
                                lastCount = i + 4; // ¥rの次も飛ばす
                                break;
                            }
                            byte msb = hex[nowPos + i];
                            byte lsb = hex[nowPos + i + 1];
                            byte[] asciiStr = {msb, lsb};
                            String strHex = new String(asciiStr);
                            myHex.add((byte) (Integer.parseInt(strHex, 16) & 0xff));
                            if(i == 39){
                                lastCount = 45;
                            }
                        }
                    }
                }

                if(debugFlag) {
                    Log.i(TAG, "load hex size" + hex.length);
                    Log.i(TAG, "use size:" + (hex.length - unusedBytes));
                    Log.i(TAG, "use hex size:" + myHex.size());
                    Log.i(TAG, "pase size:" + pageCount);
                }
            }

        } catch(Exception e){}
    }
}
