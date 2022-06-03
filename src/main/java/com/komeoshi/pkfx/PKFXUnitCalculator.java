package com.komeoshi.pkfx;

public class PKFXUnitCalculator {

    /**
     * 現在値
     */
    private final double currentPrice;
    /**
     * 証拠金
     */
    private final double currency;
    private static final int MAG = 25;
    private static final int MAX = 250_000;

    public PKFXUnitCalculator(
            double currentPrice,
            double currency
    ) {
        this.currentPrice = currentPrice;
        this.currency = currency;
    }

    public int calculate() {
        // 必要証拠金/ 単位
        double unitCurrency = currentPrice / MAG;
        double unit = currency / unitCurrency;

        int result = Double.valueOf(unit * 0.8).intValue();
        if (result > MAX)
            result = MAX;
        return result;
    }
}
