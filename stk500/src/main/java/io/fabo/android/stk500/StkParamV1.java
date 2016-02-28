package io.fabo.android.stk500;

public class StkParamV1 {
    public final static int STK_OK  =               0x10;
    public final static int STK_FAILED =            0x11;  // Not used
    public final static int STK_UNKNOWN =           0x12;  // Not used
    public final static int STK_NODEVICE =          0x13;  // Not used
    public final static int STK_INSYNC =            0x14;  // ' '
    public final static int STK_NOSYNC =            0x15;  // Not used
    public final static int ADC_CHANNEL_ERROR =     0x16;  // Not used
    public final static int ADC_MEASURE_OK =        0x17;  // Not used
    public final static int PWM_CHANNEL_ERROR =     0x18;  // Not used
    public final static int PWM_ADJUST_OK =         0x19;  // Not used
    public final static int CRC_EOP =               0x20;  // 'SPACE'
    public final static int STK_GET_SYNC =          0x30;  // '0'
    public final static int STK_GET_SIGN_ON =       0x31;  // '1'
    public final static int STK_SET_PARAMETER =     0x40;  // '@'
    public final static int STK_GET_PARAMETER =     0x41;  // 'A'
    public final static int STK_SET_DEVICE =        0x42;  // 'B'
    public final static int STK_SET_DEVICE_EXT =    0x45;  // 'E'
    public final static int STK_ENTER_PROGMODE =    0x50;  // 'P'
    public final static int STK_LEAVE_PROGMODE =    0x51;  // 'Q'
    public final static int STK_CHIP_ERASE =        0x52;  // 'R'
    public final static int STK_CHECK_AUTOINC =     0x53;  // 'S'
    public final static int STK_LOAD_ADDRESS =      0x55;  // 'U'
    public final static int STK_UNIVERSAL =         0x56;  // 'V'
    public final static int STK_PROG_FLASH =        0x60;  // '`'
    public final static int STK_PROG_DATA =         0x61;  // 'a'
    public final static int STK_PROG_FUSE =         0x62;  // 'b'
    public final static int STK_PROG_LOCK =         0x63;  // 'c'
    public final static int STK_PROG_PAGE =         0x64;  // 'd'
    public final static int STK_PROG_FUSE_EXT =     0x65;  // 'e'
    public final static int STK_READ_FLASH =        0x70;  // 'p'
    public final static int STK_READ_DATA =         0x71;  // 'q'
    public final static int STK_READ_FUSE =         0x72;  // 'r'
    public final static int STK_READ_LOCK =         0x73;  // 's'
    public final static int STK_READ_PAGE =         0x74;  // 't'
    public final static int STK_READ_SIGN =         0x75;  // 'u'
    public final static int STK_READ_OSCCAL =       0x76;  // 'v'
    public final static int STK_READ_FUSE_EXT =     0x77;  // 'w'
    public final static int STK_READ_OSCCAL_EXT =   0x78;  // 'x'
}
