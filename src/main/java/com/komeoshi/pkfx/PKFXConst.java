package com.komeoshi.pkfx;

public class PKFXConst {

    /**
     * practice
     * production
     */
    private static final String ENV = "practice";

    /**
     * 環境.
     * api-fxpractice.oanda.com
     * api-fxtrade.oanda.com
     */
    private static final String API_DOMAIN_PRACTICE = "api-fxpractice.oanda.com";
    private static final String API_DOMAIN_PRODUCTION = "api-fxtrade.oanda.com";

    public static String getApiDomain() {
        if (ENV.equals("practice")) {
            return API_DOMAIN_PRACTICE;
        } else {
            return API_DOMAIN_PRODUCTION;
        }
    }

    /**
     * APIアクセストークン.
     * 8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed
     * ae98edab9c693d5087a9c7e1edc3dafe-fcc028bfc5f97677660ad7cda52d512f
     */
    private static final String API_ACCESS_TOKEN_PRACTICE = "8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed";
    private static final String API_ACCESS_TOKEN_PRODUCTION = "ae98edab9c693d5087a9c7e1edc3dafe-fcc028bfc5f97677660ad7cda52d512f";

    public static String getApiAccessToken() {
        if (ENV.equals("practice")) {
            return API_ACCESS_TOKEN_PRACTICE;
        } else {
            return API_ACCESS_TOKEN_PRODUCTION;
        }
    }

    /**
     * 口座番号.
     * 101-009-22492304-001
     * 101-009-22492304-002
     * 001-009-7946898-001
     */
    private static final String ACCOUNT_ID_PRACTICE = "101-009-22492304-002";
    private static final String ACCOUNT_ID_PRODUCTION = "001-009-7946898-001";

    public static String getAccountId() {
        if (ENV.equals("practice")) {
            return ACCOUNT_ID_PRACTICE;
        } else {
            return ACCOUNT_ID_PRODUCTION;
        }
    }

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

    public static final Double GC_CANDLE_TARGET_MAGNIFICATION = 0.000010;

    public static final Double GC_SIG_MAGNIFICATION = 0.00080;

    public static final Double GC_LOSSCUT_MAGNIFICATION = 0.00080;
    public static final int MA_SHORT_PERIOD = 9;
    public static final int MA_MID_PERIOD = 26;
    public static final int MA_LONG_PERIOD = 50;
    public static final int VMA_SHORT_PERIOD = 1;
    public static final int VMA_LONG_PERIOD = 10;

    /**
     * シグナル終了待ち時間.
     */
    public static final int WAIT_TIME = 10;

    /**
     * インターバル. ミリ秒.
     */
    public static final long SLEEP_INTERVAL = 10;

}
