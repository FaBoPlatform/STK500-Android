package io.fabo.android.stk500;

public class StkCmdV1 {

    // Command
    public final static byte[] CMD_STK_GET_SYNC =
            {(byte) StkParamV1.STK_GET_SYNC, (byte) StkParamV1.CRC_EOP};
    public final static byte[] CMD_GET_MAJOR =
            {(byte) StkParamV1.STK_GET_PARAMETER, (byte)0x81, (byte) StkParamV1.CRC_EOP};
    public final static byte[] CMD_GET_MINOR =
            {(byte) StkParamV1.STK_GET_PARAMETER, (byte)0x80, (byte) StkParamV1.CRC_EOP};
    public final static byte[] CMD_SET_DEVICE = {StkParamV1.STK_SET_DEVICE,
            (byte)0x86,  // device code
            (byte)0x00,  // revision
            (byte)0x00,  // progtype
            (byte)0x01,  // parmode
            (byte)0x01,  // polling
            (byte)0x01,  // selftimed
            (byte)0x01,  // lockbytes
            (byte)0x03,  // fusebytes
            (byte)0xFF,  // flashpollval1
            (byte)0xFF,  // flashpollval2
            (byte)0xFF,  // eeprompollval1
            (byte)0xFF,  // eeprompollval2
            (byte)0x00,  // pagesizehigh
            (byte)0x80,  // pagesizelow
            (byte)0x04,  // eepromsizehigh
            (byte)0x00,  // eepromsizelow
            (byte)0x00,  // flashsize4
            (byte)0x00,  // flashsize3
            (byte)0x80,  // flashsize2
            (byte)0x00,  // flashsize1
            StkParamV1.CRC_EOP};
    public final static byte[] CMD_SET_DEVICE_EXT = {StkParamV1.STK_SET_DEVICE_EXT,
            (byte)0x05,  // commandsize
            (byte)0x04,  // eeprompagesize
            (byte)0xD7,  // signalpagel
            (byte)0xC2,  // signalbs2:
            (byte)0x00,  // ResetDisable
            StkParamV1.CRC_EOP};
    public final static byte[] CMD_ENTER_PROGRAM_MODE = {StkParamV1.STK_ENTER_PROGMODE,
            StkParamV1.CRC_EOP};
    public final static byte[] CMD_READ_SIG = {StkParamV1.STK_READ_SIGN,
            StkParamV1.CRC_EOP};
    public final static byte CMD_LOAD_ADDRESS = StkParamV1.STK_LOAD_ADDRESS;
    public final static byte CMD_PROG_PAGE = StkParamV1.STK_PROG_PAGE;
    public final static byte[] CMD_LEAVE_PROGRAM_MODE = {StkParamV1.STK_LEAVE_PROGMODE, StkParamV1.CRC_EOP};

    // Status
    public final static int STATUS_STK_GET_SYN = 1;
    public final static int STATUS_GET_MAJOR = 2;
    public final static int STATUS_GET_MINOR = 3;
    public final static int STATUS_SET_DEVICE = 4;
    public final static int STATUS_SET_DEVICE_EXT = 5;
    public final static int STATUS_ENTER_PROGRAM_MODE = 6;
    public final static int STATUS_READ_SIG = 7;
    public final static int STATUS_LOAD_ADDRESS_FOR_WRITE = 8;
    public final static int STATUS_PROG_PAGE = 9;
    public final static int STATUS_LEAVE_PROGRAM_MODE = 12;
    public final static int STATUS_FINISH = 13;

    public final static int PAGE_SIZE = 128;

}
