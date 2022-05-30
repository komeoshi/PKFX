package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PKFXAnalyzeParameter {
    private static final Logger log =
            LoggerFactory.getLogger(PKFXAnalyzeParameter.class);

    /**
     * プロパティ.
     * ローソクの長さ係数.
     */
    private Double candleLengthMagnification = 1.0002;

    /**
     * プロパティ.
     * 目標金額係数.
     */
    private Double targetMagnification = 1.0003;

    /**
     * プロパティ.
     * ローソクの長さ閾値達成後の最大待ち時間.
     */
    private Integer waitTime = 5;

    /**
     * ローソクの長さ達成回数.
     */
    private int lengthEnoughCount = 0;
    /**
     * 目標金額達成回数.
     */
    private int targetReachedCount = 0;
    /**
     * 勝率.
     */
    private Double total = 0.0;

    public int getLengthEnoughCount() {
        return lengthEnoughCount;
    }

    public int getTargetReachedCount() {
        return targetReachedCount;
    }

    public double getTotal() {
        return total;
    }

    /**
     * コンストラクタ.
     */
    public PKFXAnalyzeParameter() {
    }

    /**
     * コンストラクタ.
     * パラメータを指定する.
     *
     * @param candleLengthMagnification ローソクの長さ係数.
     * @param targetMagnification       目標金額係数.
     * @param waitTime                  ローソクの長さ閾値達成後の最大待ち時間.
     */
    public PKFXAnalyzeParameter(Double candleLengthMagnification, Double targetMagnification, Integer waitTime) {
        this.candleLengthMagnification = candleLengthMagnification;
        this.targetMagnification = targetMagnification;
        this.waitTime = waitTime;
    }

    public void run(Instrument instrument) {

        int lengthEnoughCount = 0;
        int targetReachedCount = 0;
        int searchLength = instrument.getCandles().size() - waitTime;

        for (int i = 0; i < searchLength; i++) {
            // 起点ローソク
            Candle candle = instrument.getCandles().get(i);

            if (isInsen(candle) || !isLengthEnough(candle)) {
                // 起点が陰線は対象外
                // 起点ローソクの長さが足りない場合は対象外
                continue;
            }

            // 起点ローソクの長さが十分
            lengthEnoughCount++;

            for (int j = 0; j < waitTime; j++) {
                Candle targetCandle = instrument.getCandles().get(i + j);

                // 目標金額達成閾値
                double target = candle.getMid().getO() * targetMagnification;
                if (target < targetCandle.getMid().getH()) {
                    // 目標金額達成した
                    targetReachedCount++;
                    break;
                }
            }
        }

        this.total = ((double) targetReachedCount /
                ((double) lengthEnoughCount));
        this.lengthEnoughCount = lengthEnoughCount;
        this.targetReachedCount = targetReachedCount;
        printResult();
    }

    public void printResult() {
        log.info("lengthEnoughCount:" + lengthEnoughCount);
        log.info("targetReachedCount:" + targetReachedCount);
        log.info("total:" + total);
    }

    /**
     * ローソクの長さ閾値を達成するか.
     *
     * @param candle ローソク
     * @return true:ローソクの長さ閾値を達成する
     */
    private boolean isLengthEnough(Candle candle) {
        // ローソクの長さ閾値
        double threshold = (candle.getMid().getO() * candleLengthMagnification);
        return candle.getMid().getC() > threshold;
    }

    private boolean isYousen(Candle candle) {
        // 始値よりも終値が高ければ陽線
        return candle.getMid().getC() > candle.getMid().getO();
    }

    private boolean isInsen(Candle candle) {
        return !isYousen(candle);
    }
}
