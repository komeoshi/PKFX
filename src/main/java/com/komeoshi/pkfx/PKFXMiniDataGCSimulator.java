package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.enumerator.*;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PKFXMiniDataGCSimulator {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCSimulator.class);
    private int countLosscut = 0;
    private int countReached = 0;
    private int countTimeoutWin = 0;
    private int countTimeoutLose = 0;
    private int countWin = 0;
    private int countLose = 0;
    private int totalCount = 0;
    private double diff = 0.0;

    public static void main(String[] args) {
        PKFXMiniDataGCSimulator sim = new PKFXMiniDataGCSimulator();
        sim.run();
    }

    private double param;

    public void setParam(double param) {
        this.param = param;
    }

    private List<Candle> candles = null;
    private Map<String, Candle> longCandles = null;

    private void init() {
        countLosscut = 0;
        countReached = 0;
        countTimeoutWin = 0;
        countTimeoutLose = 0;
        countWin = 0;
        countLose = 0;
        totalCount = 0;
        diff = 0.0;
    }

    private static double SPREAD_COST = 0.004;
    private boolean isLogging = false;

    public void run() {
        init();
        if (candles == null) {
            this.candles = getCandlesFromFile();
            this.longCandles = getLongCandlesFromFile();
        }

        Status status = Status.NONE;
        Position lastMacdPosition = Position.NONE;
        Position lastEmaPosition = Position.NONE;
        AdxPosition lastAdxPosition = AdxPosition.NONE;
        Candle openCandle = null;
        int continueCount = 0;
        final int CONTINUE_MAX = 0;
        for (Candle candle : candles) {

            if (candle.getMacdPosition() == Position.NONE) {
                continue;
            }

            LocalDateTime from = LocalDateTime.of(2021, 1, 1, 0, 0, 0, 0);
            if (candle.getTime().isBefore(from)) {
                continue;
            }
            boolean emaPositionChanged = lastEmaPosition != candle.getEmaPosition();
            boolean macdPositionChanged = lastMacdPosition != candle.getMacdPosition();
            boolean adxPositionChanged = lastAdxPosition != candle.getAdxPosition();

            if ((emaPositionChanged || macdPositionChanged || adxPositionChanged) &&
                    lastMacdPosition != Position.NONE) {

                Candle tmpCandle = candle.getCandles().get(candle.getCandles().size() - 1);
                Candle tmpCandle2 = candle.getCandles().get(candle.getCandles().size() - 2);

                boolean checkSpread = candle.getSpreadMa() < 0.027;
                boolean hasLongCandle = hasLongCandle(candle);
                boolean hasShortCandle = hasShortCandle(candle);
                boolean checkAtr = candle.getAtr() > 0.0243 ||
                        candle.getTr() > 0.0450 ||
                        tmpCandle2.getTr() > 0.059;

                int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
                int m = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getMinute();
                boolean checkTimeH = h != 0 && h != 2 && h != 5 && h != 8 && h != 10 && h != 12 && h != 13
                        && h != 14 && h != 17 && h != 18 && h != 20 && h != 22 && h != 23;
                boolean checkTimeM = true;
                boolean checkMacd = Math.abs(tmpCandle.getMacd()) > 0.00005 ||
                        Math.abs(tmpCandle2.getMacd()) > 0.005;
                boolean checkSig = Math.abs(tmpCandle.getSig()) > 0.00007;
                boolean checkBb = candle.getBollingerBandHigh() - candle.getBollingerBandLow() > 0.052;
                boolean checkBb2 = tmpCandle2.getBollingerBandHigh() - tmpCandle2.getBollingerBandLow() < 0.300;
                boolean checkAdx = candle.getAdx().getAdx() > 14;
                boolean checkDx = Math.abs(candle.getAdx().getPlusDi() - candle.getAdx().getMinusDi()) > 0.350 ||
                        Math.abs(tmpCandle.getAdx().getPlusDi() - tmpCandle.getAdx().getMinusDi()) > 0.350;
                boolean checkRsi = Math.abs(tmpCandle2.getRsi()) > 23;
                boolean checkRsi2 = Math.abs(candle.getRsi()) < 91 &&
                        Math.abs(tmpCandle.getRsi()) < 78;

                if ((macdPositionChanged && candle.getMacdPosition() == Position.LONG) ||
                        (emaPositionChanged && candle.getEmaPosition() == Position.LONG) ||
                        (adxPositionChanged && candle.getAdxPosition() == AdxPosition.OVER)) {
                    // 売り→買い

                    boolean doTrade = (
                            !hasLongCandle
                                    && !hasShortCandle
                                    && checkAtr
                                    && checkSpread
                                    && checkTimeH
                                    && checkTimeM
                                    && checkMacd
                                    && checkSig
                                    && checkBb
                                    && checkBb2
                                    && checkAdx
                                    && checkDx
                                    && checkRsi
                                    && checkRsi2
                    );
                    if (status != Status.NONE) {
                        if (candle.getAsk().getC() < openCandle.getAsk().getC() &&
                                !doTrade) {
                            // ﾏｹﾃﾙ
                            continueCount++;
                            if (isLogging)
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount + " " + candle.getAsk().getC());

                        } else if (candle.getAsk().getC() > openCandle.getAsk().getC() ||
                                continueCount >= CONTINUE_MAX ||
                                doTrade) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                            status = Status.NONE;
                            continueCount = 0;
                        }
                    }

                    if (doTrade) {
                        TradeReason reason;
                        if (emaPositionChanged) {
                            reason = TradeReason.EMA_GC;
                        } else if (macdPositionChanged) {
                            reason = TradeReason.MACD_GC;
                        } else {
                            reason = TradeReason.ADX_OVER;
                        }

                        buy(candle, reason);
                        status = Status.HOLDING_BUY;
                        openCandle = candle;
                    }

                } else if ((macdPositionChanged && candle.getMacdPosition() == Position.SHORT) ||
                        (emaPositionChanged && candle.getEmaPosition() == Position.SHORT) ||
                        (adxPositionChanged && candle.getAdxPosition() == AdxPosition.UNDER)) {
                    // 買い→売り

                    boolean doTrade = (
                            !hasLongCandle
                                    && !hasShortCandle
                                    && checkAtr
                                    && checkSpread
                                    && checkTimeH
                                    && checkTimeM
                                    && checkMacd
                                    && checkSig
                                    && checkBb
                                    && checkBb2
                                    && checkAdx
                                    && checkDx
                                    && checkRsi
                                    && checkRsi2
                    );

                    if (status != Status.NONE) {
                        if (candle.getAsk().getC() > openCandle.getAsk().getC() &&
                                !doTrade) {
                            // ﾏｹﾃﾙ
                            continueCount++;
                            if (isLogging)
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount + " " + candle.getAsk().getC());

                        } else if (candle.getAsk().getC() < openCandle.getAsk().getC() ||
                                continueCount >= CONTINUE_MAX ||
                                doTrade
                        ) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                            status = Status.NONE;
                            continueCount = 0;
                        }
                    }

                    if (doTrade) {
                        TradeReason reason;
                        if (emaPositionChanged) {
                            reason = TradeReason.EMA_DC;
                        } else if (macdPositionChanged) {
                            reason = TradeReason.MACD_DC;
                        } else {
                            reason = TradeReason.ADX_UNDER;
                        }

                        sell(candle, reason);
                        status = Status.HOLDING_SELL;
                        openCandle = candle;
                    }
                }
            }
            lastMacdPosition = candle.getMacdPosition();
            lastEmaPosition = candle.getEmaPosition();
            lastAdxPosition = candle.getAdxPosition();

            if (status != Status.NONE) {
                status = targetReach(status, openCandle, candle);
                status = losscut(status, openCandle, candle);
                if (status == Status.NONE) {
                    continueCount = 0;
                }
            }
        }

        log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                diff + "(" + (diff * 1000 / totalCount) + "), " +
                "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
        );
    }

    private Status losscut(Status status, Candle openCandle, Candle candle) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.00071;

        if (Math.abs(candle.getMacd()) > 0.011) {
            // ロスカットしやすくなる
            lossCutMag *= 0.98;
        }

        double lossCutRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 - lossCutMag);
        double lossCutRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 + lossCutMag);

        if (status == Status.HOLDING_BUY) {
            if (lossCutRateBuy > candle.getAsk().getC()) {

                completeOrder(openCandle, candle, Reason.LOSSCUT, status);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (lossCutRateSell < candle.getAsk().getC()) {

                completeOrder(openCandle, candle, Reason.LOSSCUT, status);
                status = Status.NONE;
            }
        }
        return status;
    }


    private Status targetReach(Status status, Candle openCandle, Candle candle) {
        double mag = 0.000310;
        double targetRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 - mag);

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {

                if (Math.abs(candle.getMacd()) > 0.088) {
                    return status;
                }

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {

                if (Math.abs(candle.getMacd()) > 0.088) {
                    return status;
                }

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        }
        return status;
    }

    private void buy(Candle openCandle, TradeReason reason) {
        if (isLogging)
            log.info("signal >> 【" + openCandle.getNumber() + "】" + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) +
                    " " + reason + " buy " + openCandle.getAsk().getC()
            );
    }

    private void sell(Candle openCandle, TradeReason reason) {
        if (isLogging)
            log.info("signal >> 【" + openCandle.getNumber() + "】" + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) +
                    " " + reason + " sell " + openCandle.getAsk().getC()
            );
    }

    private void completeOrder(Candle openCandle, Candle closeCandle, Reason reason, Status status) {

        if (status == Status.HOLDING_BUY) {
            if (openCandle.getAsk().getC() < closeCandle.getAsk().getC()) {
                countWin++;
            } else {
                countLose++;
            }
        } else {
            if (openCandle.getAsk().getC() > closeCandle.getAsk().getC()) {
                countWin++;
            } else {
                countLose++;
            }
        }
        totalCount++;
        double thisDiff;
        if (status == Status.HOLDING_BUY) {
            thisDiff = (closeCandle.getAsk().getC() - openCandle.getAsk().getC());
        } else {
            thisDiff = (openCandle.getAsk().getC() - closeCandle.getAsk().getC());
        }
        diff += (thisDiff - SPREAD_COST);

        switch (reason) {
            case LOSSCUT:
                countLosscut++;
                break;
            case REACHED:
                countReached++;
                break;
            case TIMEOUT:
                if (status == Status.HOLDING_BUY) {
                    if (openCandle.getAsk().getC() < closeCandle.getAsk().getC()) {
                        countTimeoutWin++;
                    } else {
                        countTimeoutLose++;
                    }
                } else {
                    if (openCandle.getAsk().getC() > closeCandle.getAsk().getC()) {
                        countTimeoutWin++;
                    } else {
                        countTimeoutLose++;
                    }
                }
                break;
        }

        if (isLogging)
            log.info("<< signal 【" + openCandle.getNumber() + "】" + closeCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) +
                    " 【" + closeCandle.getNumber() + "】" +
                    openCandle.getAsk().getC() + " -> " + closeCandle.getAsk().getC() + "(" + thisDiff + "), " +
                    countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                    diff + "(" + (diff / totalCount) + "), " + reason +
                    " LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
            );

        if (thisDiff < -0.10 && false) {
            log.info(
                    "【" + openCandle.getNumber() + "】 " +
                            openCandle.getTime() + "-" + closeCandle.getTime() + " thisDiff:" + thisDiff +
                            " " + openCandle.getEmaPosition() +
                            " openTr:" + openCandle.getTr() +
                            " openAtr:" + openCandle.getAtr() +
                            " openAdx:" + openCandle.getAdx().getAdx() +
                            " openPlusDi:" + openCandle.getAdx().getPlusDi() +
                            " openMinusDi:" + openCandle.getAdx().getMinusDi() +

                            " openSig:" + openCandle.getSig() +
                            " openMacd:" + openCandle.getMacd() + " pastMacd:" + openCandle.getPastCandle().getMacd() +
                            " openShortMa:" + openCandle.getShortMa() + " openLongMa:" + openCandle.getLongMa() + " " +
                            " pastShortMa:" + openCandle.getPastCandle().getShortMa() + " pastLongMa:" + openCandle.getPastCandle().getLongMa() +
                            " pastShortMa-openShortMa:" + Math.abs(openCandle.getShortMa() - openCandle.getPastCandle().getShortMa()) +
                            " pastLongMa-openLongMa:" + Math.abs(openCandle.getLongMa() - openCandle.getPastCandle().getLongMa()) +
                            " pastSuperLongMa-openSuperLongMa:" + Math.abs(openCandle.getSuperLongMa() - openCandle.getPastCandle().getSuperLongMa()) +
                            " "

            );
        }


    }

    private List<Candle> getCandlesFromFile() {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("minData.dat");
        List<Candle> candles = reader.read().getCandles();

        return new ArrayList<>(candles);
    }

    private Map<String, Candle> getLongCandlesFromFile() {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("data.dat");
        List<Candle> candles = reader.read().getCandles();

        Map<String, Candle> map = new HashMap<>();
        for (Candle candle : candles) {
            map.put(
                    candle.getTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")),
                    candle
            );
        }

        return map;
    }

    private Candle getCandleAt(Map<String, Candle> candles, LocalDateTime time) {
        time = time.minusMinutes(1);
        return candles.get(time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
    }

    private boolean hasShortCandle(Candle candle) {
        int size = 9;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);

            if (Math.abs(c.getAsk().getL() - c.getAsk().getH()) < 0.0021) {
                count++;
            }
        }
        return count > 1;
    }

    private boolean hasLongCandle(Candle candle) {
        int size = 9;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);

            if (Math.abs(c.getAsk().getL() - c.getAsk().getH()) > 0.09) {
                count++;
            }
        }
        return count > 1;
    }
}
