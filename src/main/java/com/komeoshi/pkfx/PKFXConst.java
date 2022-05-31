package com.komeoshi.pkfx;

public class PKFXConst {

    /**
     * 環境.
     */
    public static final String API_DOMAIN = "api-fxpractice.oanda.com";

    /**
     * APIアクセストークン.
     */
    public static final String API_ACCESS_TOKEN = "8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed";

    /**
     * 口座番号.
     */
    public static final String ACCOUNT_ID = "101-009-22492304-001";

    /**
     * 売買単位.
     */
    public static final String DEFAULT_UNIT = "30000";

    /**
     * シグナル点灯閾値.
     */
    public static final Double CANDLE_LENGTH_MAGNIFICATION = 1.0001;
    /**
     * シグナル終了閾値.
     */
    public static final Double CANDLE_TARGET_MAGNIFICATION = 1.0007;
    /**
     * シグナル終了待ち時間.
     */
    public static final int WAIT_TIME = 5;

    /**
     * インターバル. ミリ秒.
     */
    public static final long SLEEP_INTERVAL = 100;

}
