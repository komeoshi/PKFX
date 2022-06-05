package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PKFXSimulatorAnalyzeParameter {
    private static final Logger log =
            LoggerFactory.getLogger(PKFXSimulatorAnalyzeParameter.class);

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
    public PKFXSimulatorAnalyzeParameter() {
    }

    /**
     * コンストラクタ.
     * パラメータを指定する.
     *
     * @param candleLengthMagnification ローソクの長さ係数.
     * @param targetMagnification       目標金額係数.
     * @param waitTime                  ローソクの長さ閾値達成後の最大待ち時間.
     */
    public PKFXSimulatorAnalyzeParameter(Double candleLengthMagnification, Double targetMagnification, Integer waitTime) {
        this.candleLengthMagnification = candleLengthMagnification;
        this.targetMagnification = targetMagnification;
        this.waitTime = waitTime;
    }

    public void run2(Instrument instrument) {
        List<Candle> allCandles = instrument.getCandles();


        int countBuy = 0;
        int countSell = 0;
        int targetReachedCount = 0;
        int losscutReachedCount = 0;
        int timeoutReachedCount = 0;
        double totalDiff = 0.0;
        for (int i = 75; i < allCandles.size(); i++) {
            List<Candle> currentCandles = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                currentCandles.add(allCandles.get(j));
            }

            double openRate = 0.0;
            double openMa = 0.0;
            LocalDateTime openTime = LocalDateTime.now();

            Candle currentCandle = allCandles.get(i);
            PKFXFinderAnalyzer finder = new PKFXFinderAnalyzer();
            boolean isMaOk = finder.isMaOk(currentCandles);
            if (isMaOk) {

                openRate = currentCandle.getMid().getO();
                openTime = currentCandle.getTime();
                openMa = finder.getMa(currentCandles, PKFXConst.MA_SHORT_PERIOD);

                log.info("signal>> " + openTime.atZone(ZoneId.of("Asia/Tokyo")) + ", OPEN:" + currentCandle.getMid().getO() + ", HIGH:" + currentCandle.getMid().getH() + ", ");

                countBuy++;

                for (int j = i + 1; j < i + PKFXConst.WAIT_TIME; j++) {

                    if (j == allCandles.size()) {
                        break;
                    }

                    Candle targetCandle = allCandles.get(j);
                    boolean isTargetReached = openMa * PKFXConst.CANDLE_TARGET_MAGNIFICATION < targetCandle.getMid().getH();
                    boolean isLosscutReached = openMa * PKFXConst.CANDLE_LOSSCUT_MAGNIFICATION > targetCandle.getMid().getC();
                    boolean isTimeoutReached = false;
                    if (j - (i+1) > waitTime)
                        isTimeoutReached = true;


                    double diff = Math.abs(targetCandle.getMid().getH() - openMa);

                    if (isTargetReached) {
                        targetReachedCount++;
                    }
                    if (isLosscutReached) {
                        losscutReachedCount++;
                    }
                    if (isTimeoutReached) {
                        timeoutReachedCount++;
                    }


                    if (isTargetReached || isLosscutReached || isTimeoutReached) {
                        log.info("<<signal " + targetCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + ", OPEN:" + openRate + ", HIGH:" + targetCandle.getMid().getH() + ", DIFF:"
                                + diff + ", "
                                + isTargetReached + ", " + isLosscutReached);

                        countSell++;
                        totalDiff += diff;
                        break;
                    }
                }
            }
        }

        double rate = (double) targetReachedCount / ((double) targetReachedCount + (double) losscutReachedCount + (double) timeoutReachedCount);

        log.info(countBuy + ", " + countSell);
        log.info("rate:" + rate);
        log.info("targetReachedCount:" + targetReachedCount);
        log.info("losscutReachedCount:" + losscutReachedCount);
        log.info("timeoutReachedCount:" + timeoutReachedCount);
        log.info("totalDiff:" + totalDiff);
        log.info("totalDiff/countBuy:" + totalDiff / (countBuy));
    }

    public void run(Instrument instrument) {

        int lengthEnoughCount = 0;
        int targetReachedCount = 0;
        int searchLength = instrument.getCandles().size() - waitTime;

        for (int i = 0; i < searchLength; i++) {
            // 起点ローソク
            Candle candle = instrument.getCandles().get(i);

            if (candle.isInsen() || !candle.isLengthEnough(candleLengthMagnification)) {
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
                    // showLog(candle, targetCandle);

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

    private void showLog(Candle candle, Candle targetCandle) {
        log.info("signal<< " + candle.getTime() + ", " + candle.getMid().getO() + ", " + targetCandle.getMid().getH() + ", "
                + (targetCandle.getMid().getH() - candle.getMid().getO()) );
    }

    public void printResult() {
        log.info("lengthEnoughCount:" + lengthEnoughCount);
        log.info("targetReachedCount:" + targetReachedCount);
        log.info("total:" + total);
    }


}
