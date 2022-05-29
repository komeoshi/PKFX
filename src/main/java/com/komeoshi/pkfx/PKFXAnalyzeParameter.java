package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PKFXAnalyzeParameter {
    private static final Logger log = LoggerFactory.getLogger(PKFXAnalyzeParameter.class);

    /**
     * プロパティ.
     * ローソクの長さ係数.
     */
    private Double candleLengthMagnification = 1.0005;

    /**
     * プロパティ.
     * 目標金額係数.
     */
    private Double targetMagnification = 1.0007;

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


    /**
     * コンストラクタ.
     */
    public PKFXAnalyzeParameter() {
    }

    /**
     * コンストラクタ.
     *
     * @param candleLengthMagnification ローソクの長さ係数.
     */
    public PKFXAnalyzeParameter(Double candleLengthMagnification, Double targetMagnification, Integer waitTime) {
        this.candleLengthMagnification = candleLengthMagnification;
        this.targetMagnification = targetMagnification;
        this.waitTime = waitTime;
    }

    public void run(Instrument instrument) {

        int lengthEnoughCount = 0;
        int targetReachedCount = 0;
        for (int i = 0; i < instrument.getCandles().size(); i++) {
            Candle candle = instrument.getCandles().get(i);

            if (isInsen(candle)) {
                continue;
            }

            if (!isLengthEnough(candle)) {
                continue;
            }

            lengthEnoughCount++;

            boolean isTargetReached = false;
            for (int j = 0; j < waitTime; j++) {
                Candle targetCandle = instrument.getCandles().get(i + j);

                // 目標金額達成閾値
                double target = candle.getOpenBid() * targetMagnification;
                if (target < targetCandle.getHighBid()) {
                    isTargetReached = true;
                    break;
                }
            }

            if (isTargetReached) {
                log.info(candle.toString());
                targetReachedCount++;
            }
        }

        this.total = ((double) targetReachedCount /
                ((double) lengthEnoughCount + (double) targetReachedCount));
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
        Double threshold = (candle.getOpenBid() * candleLengthMagnification);
        return candle.getCloseBid() > threshold;
    }

    private boolean isYousen(Candle candle) {
        return candle.getOpenBid() > candle.getCloseBid();
    }

    private boolean isInsen(Candle candle) {
        return !isYousen(candle);
    }
}
