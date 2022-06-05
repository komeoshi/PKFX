package com.komeoshi.pkfx;

public class PKFXConst {

    /**
     * 環境.
     * api-fxpractice.oanda.com
     * api-fxtrade.oanda.com
     */
    public static final String API_DOMAIN = "api-fxpractice.oanda.com";


    /**
     * APIアクセストークン.
     * 8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed
     * ae98edab9c693d5087a9c7e1edc3dafe-fcc028bfc5f97677660ad7cda52d512f
     */
    public static final String API_ACCESS_TOKEN = "8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed";

    /**
     * 口座番号.
     * 101-009-22492304-001
     * 101-009-22492304-002
     * 001-009-7946898-001
     */
    public static final String ACCOUNT_ID = "101-009-22492304-002";

    /**
     * 通貨
     */
    public static final String CURRENCY = "USD_JPY";

    /**
     * 足
     */
    public static final String GRANULARITY = "M1";

    /**
     * シグナル点灯閾値.
     */
    public static final Double CANDLE_LENGTH_MAGNIFICATION = 1.0002;
    /**
     * シグナル終了閾値.
     */
    public static final Double CANDLE_TARGET_MAGNIFICATION = 1.00015;

    public static final Double CANDLE_LOSSCUT_MAGNIFICATION = 0.999;

    public static final int MA_SHORT_PERIOD = 9;
    public static final int MA_MID_PERIOD = 26;
    public static final int MA_LONG_PERIOD = 50;

    /**
     * シグナル終了待ち時間.
     */
    public static final int WAIT_TIME = 10;

    /**
     * インターバル. ミリ秒.
     */
    public static final long SLEEP_INTERVAL = 10;

}
