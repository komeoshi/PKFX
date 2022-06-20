package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.enumerator.Position;
import com.komeoshi.pkfx.enumerator.Reason;
import com.komeoshi.pkfx.enumerator.Status;
import com.komeoshi.pkfx.enumerator.TradeReason;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@Setter
public class PKFXMiniDataGCSimulator {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCSimulator.class);
    private boolean isLogging = false;

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

    public void run() {
        init();
        if (candles == null) {
            this.candles = getCandlesFromFile();
            this.longCandles = getLongCandlesFromFile();
        }

        Status status = Status.NONE;
        Position lastPosition = Position.NONE;
        Candle openCandle = null;
        int continueCount = 0;
        final int CONTINUE_MAX = 2;
        for (Candle candle : candles) {

            if (candle.getEmaPosition() == Position.NONE) {
                continue;
            }

            LocalDateTime from = LocalDateTime.of(2021, 6, 1, 0, 0, 0, 0);
            if (candle.getTime().isBefore(from)) {
                continue;
            }

            if (candle.getEmaPosition() != lastPosition) {

                Candle longCandle = getCandleAt(longCandles, candle.getTime());
                boolean checkLongAbs = Math.abs(longCandle.getAsk().getC() - longCandle.getPastCandle().getAsk().getC())
                        > 0.0785;
                boolean checkSpread = candle.getSpreadMa() < 0.023;
                int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
                boolean checkTime = h != 3 && h != 20 && h != 22;
                int m = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getMinute();
                boolean checkMin = m != 59;
                boolean hasLongCandle = hasLongCandle(longCandle);
                boolean hasShortCandle = hasShortCandle(longCandle);

                if (candle.getEmaPosition() == Position.LONG) {
                    // 売り→買い

                    boolean doTrade = checkLongAbs
                            && checkTime
                            && checkMin
                            && checkSpread
                            && !hasLongCandle
                            && !hasShortCandle
                            && longCandle.getAsk().getL() > longCandle.getLongMa()
                            ;

                    if (status != Status.NONE) {

                        if (candle.getAsk().getC() > openCandle.getAsk().getC() &&
                                !doTrade) {
                            // ﾏｹﾃﾙ
                            continueCount++;
                            if (isLogging)
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount);

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
                        buy(TradeReason.GC, candle);
                        status = Status.HOLDING_BUY;
                        openCandle = candle;
                    }

                } else if (candle.getEmaPosition() == Position.SHORT) {
                    // 買い→売り

                    boolean doTrade = checkLongAbs
                            && checkTime
                            && checkMin
                            && checkSpread
                            && !hasLongCandle
                            && !hasShortCandle
                            && longCandle.getAsk().getH() < longCandle.getLongMa()
                            ;

                    if (status != Status.NONE) {
                        if (candle.getAsk().getC() < openCandle.getAsk().getC() &&
                                !doTrade) {
                            // ﾏｹﾃﾙ
                            continueCount++;
                            if (isLogging)
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount);

                        } else if (candle.getAsk().getC() > openCandle.getAsk().getC() ||
                                continueCount >= CONTINUE_MAX ||
                                doTrade
                        ) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                            status = Status.NONE;
                            continueCount = 0;
                        }
                    }

                    if (doTrade) {
                        sell(TradeReason.DC, candle);
                        status = Status.HOLDING_SELL;
                        openCandle = candle;
                    }
                }
            }
            lastPosition = candle.getEmaPosition();

            if (status != Status.NONE) {
                status = targetReach(status, openCandle, candle);
                status = losscut(status, openCandle, candle, continueCount);
                if (status == Status.NONE) {
                    continueCount = 0;
                }
            }
        }

        log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                diff + "(" + (diff * 100 / totalCount) + "), " +
                "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
        );
    }

    private Status losscut(Status status, Candle openCandle, Candle candle, int continueCount) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.000440;
        if (continueCount > 0) {
            // コンテニューがある場合、ロスカットしやすくなる
            lossCutMag *= (continueCount * 0.97);
        }

        if (Math.abs(candle.getMacd()) > 0.011) {
            // ロスカットしやすくなる
            lossCutMag *= 0.98;
        }

        double lossCutRateBuy = openCandle.getAsk().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getAsk().getC() * (1 + lossCutMag);

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
        double mag = 0.000008;
        double targetRateBuy = (openCandle.getAsk().getC() + 0.004) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - 0.004) * (1 - mag);

        if (Math.abs(candle.getMacd()) > 0.011) {
            // ｵｶﾜﾘ
            if (isLogging)
                log.info("okawari.【" + openCandle.getNumber() + "】");
            return status;
        }

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        }
        return status;
    }

    private void buy(TradeReason tradeReason, Candle openCandle) {
        if (isLogging)
            log.info("signal >> buy【" + openCandle.getNumber() + "】" + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " " + tradeReason);
    }

    private void sell(TradeReason tradeReason, Candle openCandle) {
        if (isLogging)
            log.info("signal >> sell【" + openCandle.getNumber() + "】" + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " " + tradeReason);
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
        diff += thisDiff;

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

        if (reason == Reason.LOSSCUT && false) {
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
        return candles.get(time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
    }

    private boolean hasShortCandle(Candle candle) {
        int size = 20;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);

            if (Math.abs(c.getAsk().getL() - c.getAsk().getH()) < 0.0020) {
                count++;
            }
        }
        return count > 1;
    }
    private boolean hasLongCandle(Candle candle) {
        int size = 20;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);

            if (Math.abs(c.getAsk().getL() - c.getAsk().getH()) > 0.07) {
                count++;
            }
        }
        return count > 1;
    }
}
