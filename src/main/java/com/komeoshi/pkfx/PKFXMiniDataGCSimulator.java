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

    private static double SPREAD_COST = 0.0042;

    public void run() {
        init();
        if (candles == null) {
            this.candles = getCandlesFromFile();
            this.longCandles = getLongCandlesFromFile();
        }

        Status status = Status.NONE;
        Position lastPosition = Position.NONE;
        Candle openCandle = null;
        for (Candle candle : candles) {

            if (candle.getAdxPosition() == AdxPosition.NONE) {
                continue;
            }

            LocalDateTime from = LocalDateTime.of(2021, 6, 1, 0, 0, 0, 0);
            if (candle.getTime().isBefore(from)) {
                continue;
            }

            if (lastPosition != candle.getMacdPosition() && lastPosition != Position.NONE) {

                boolean checkSpread = candle.getSpreadMa() < 0.030;
                boolean hasLongCandle = hasLongCandle(candle);
                boolean hasShortCandle = hasShortCandle(candle);
                boolean checkAtr = candle.getAtr() > 0.0214;
                boolean checkVma = candle.getShortVma() > 9;
                boolean checkVma2 = candle.getShortVma() < candle.getVolume();

                if (candle.getMacdPosition() == Position.LONG) {
                    // 売り→買い

                    boolean doTrade = (
                            !hasLongCandle
                                    && !hasShortCandle
                                    && checkAtr
                                    && checkVma
                                    && checkVma2
                                    && checkSpread
                                    && candle.getSigPosition() == Position.LONG
                    );

                    if (status != Status.NONE) {
                        completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                        status = Status.NONE;
                    }

                    if (doTrade) {
                        buy(candle);
                        status = Status.HOLDING_BUY;
                        openCandle = candle;
                    }

                } else if (candle.getMacdPosition() == Position.SHORT) {
                    // 買い→売り

                    boolean doTrade = (
                            !hasLongCandle
                                    && !hasShortCandle
                                    && checkAtr
                                    && checkVma
                                    && checkVma2
                                    && checkSpread
                                    && candle.getSigPosition() == Position.SHORT
                    );

                    if (status != Status.NONE) {
                        completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                        status = Status.NONE;
                    }

                    if (doTrade) {
                        sell(candle);
                        status = Status.HOLDING_SELL;
                        openCandle = candle;
                    }
                }
            }
            lastPosition = candle.getMacdPosition();

            if (status != Status.NONE) {
                status = targetReach(status, openCandle, candle);
                status = losscut(status, openCandle, candle);
            }
        }

        log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                diff + "(" + (diff * 100 / totalCount) + "), " +
                "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
        );
    }

    private Status losscut(Status status, Candle openCandle, Candle candle) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.000740;

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
        double mag = 0.000227;
        double targetRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 - mag);

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

    private void buy(Candle openCandle) {
        if (isLogging)
            log.info("signal >> 【" + openCandle.getNumber() + "】" + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) +
                    " " + TradeReason.GC + " buy"
            );
    }

    private void sell(Candle openCandle) {
        if (isLogging)
            log.info("signal >> 【" + openCandle.getNumber() + "】" + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) +
                    " " + TradeReason.DC + " sell"
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
        time = time.minusMinutes(1);
        return candles.get(time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
    }

    private boolean hasShortCandle(Candle candle) {
        int size = 15;

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
        int size = 15;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);

            if (Math.abs(c.getAsk().getL() - c.getAsk().getH()) > 0.10) {
                count++;
            }
        }
        return count > 1;
    }
}
