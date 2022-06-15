package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.enumerator.Position;
import com.komeoshi.pkfx.enumerator.Reason;
import com.komeoshi.pkfx.enumerator.Status;
import com.komeoshi.pkfx.enumerator.TradeReason;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public void run() {
        List<Candle> candles = getCandles();
        Map<String, Candle> longCandles = getLongCandles();

        Status status = Status.NONE;
        Position lastPosition = Position.NONE;
        Candle openCandle = null;
        for (Candle candle : candles) {

            if (candle.getPosition() == Position.NONE) {
                continue;
            }

            LocalDateTime from = LocalDateTime.of(2022, 1, 1, 0, 0, 0, 0);
            if (candle.getTime().isBefore(from)) {
                continue;
            }

            if (candle.getPosition() != lastPosition) {

                boolean checkDiff = Math.abs(candle.getShortMa() - candle.getMid().getC()) < 0.08;
                int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
                boolean checkTime = h != 0 && h != 1 && h != 5 &&
                        h != 14 && h != 19 && h != 21;

                Candle longCandle = getCandleAt(longCandles, candle.getTime());
                boolean checkLongAbs = Math.abs(longCandle.getMid().getC() - longCandle.getPastCandle().getMid().getC())
                        > 0.030;

                if (candle.getPosition() == Position.LONG) {
                    // 売り→買い

                    if (status != Status.NONE) {
                        completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                        status = Status.NONE;
                    }

                    if (checkDiff && checkTime && checkLongAbs &&
                            longCandle.getPosition() == Position.LONG) {
                        buy(TradeReason.GC, candle);
                        status = Status.HOLDING_BUY;
                        openCandle = candle;
                    }

                } else if (candle.getPosition() == Position.SHORT) {
                    // 買い→売り

                    if (status != Status.NONE) {
                        completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                        status = Status.NONE;
                    }

                    if (checkDiff && checkTime && checkLongAbs &&
                            longCandle.getPosition() == Position.SHORT) {
                        sell(TradeReason.DC, candle);
                        status = Status.HOLDING_SELL;
                        openCandle = candle;
                    }
                }
            }
            lastPosition = candle.getPosition();

            if (status != Status.NONE) {
                status = targetReach(status, openCandle, candle);
                status = losscut(status, openCandle, candle);
            }
        }

        log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                diff + "(" + (diff * 100 / totalCount) + "), " +
                "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
        );

        System.exit(0);

    }

    private Status losscut(Status status, Candle openCandle, Candle candle) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.000190;

        double lossCutRateBuy = openCandle.getMid().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getMid().getC() * (1 + lossCutMag);

        if (status == Status.HOLDING_BUY) {
            if (lossCutRateBuy > candle.getMid().getC()) {

                completeOrder(openCandle, candle, Reason.LOSSCUT, status);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (lossCutRateSell < candle.getMid().getC()) {

                completeOrder(openCandle, candle, Reason.LOSSCUT, status);
                status = Status.NONE;
            }
        }
        return status;
    }


    private Status targetReach(Status status, Candle openCandle, Candle candle) {
        double mag = 0.000275;

        if (isInUpperTIme(openCandle)) {
            mag *= 1.6;
        } else {
            mag *= 0.6;
        }

        double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
        double targetRateSell = openCandle.getMid().getC() * (1 - mag);

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getMid().getC()) {

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getMid().getC()) {

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        }
        return status;
    }

    private void buy(TradeReason tradeReason, Candle openCandle) {
        if (isLogging)
            log.info("signal >> " + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " " + tradeReason);
    }

    private void sell(TradeReason tradeReason, Candle openCandle) {
        if (isLogging)
            log.info("signal >> " + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " " + tradeReason);
    }

    private void completeOrder(Candle openCandle, Candle closeCandle, Reason reason, Status status) {

        if (status == Status.HOLDING_BUY) {
            if (openCandle.getMid().getC() < closeCandle.getMid().getC()) {
                countWin++;
            } else {
                countLose++;
            }
        } else {
            if (openCandle.getMid().getC() > closeCandle.getMid().getC()) {
                countWin++;
            } else {
                countLose++;
            }
        }
        totalCount++;
        double thisDiff;
        if (status == Status.HOLDING_BUY) {
            thisDiff = (closeCandle.getMid().getC() - openCandle.getMid().getC());
        } else {
            thisDiff = (openCandle.getMid().getC() - closeCandle.getMid().getC());
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
                    if (openCandle.getMid().getC() < closeCandle.getMid().getC()) {
                        countTimeoutWin++;
                    } else {
                        countTimeoutLose++;
                    }
                } else {
                    if (openCandle.getMid().getC() > closeCandle.getMid().getC()) {
                        countTimeoutWin++;
                    } else {
                        countTimeoutLose++;
                    }
                }
                break;
        }

        if (isLogging)
            log.info("<< signal " + closeCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) +
                    " 【" + closeCandle.getNumber() + "】" +
                    openCandle.getMid().getC() + " -> " + closeCandle.getMid().getC() + "(" + thisDiff + "), " +
                    countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                    diff + "(" + (diff / totalCount) + "), " + reason +
                    " LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
            );

        if (openCandle.getTime().isAfter(LocalDateTime.now().minusMonths(1))) {
            if (Math.abs(thisDiff) > 0.20) {
                log.info(
                        "【" + openCandle.getNumber() + "】 " +
                                openCandle.getTime() + "-" + closeCandle.getTime() + " thisDiff:" + thisDiff +
                                " " + openCandle.getPosition() +
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
    }

    private List<Candle> getCandles() {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("minData.dat");
        List<Candle> candles = reader.read().getCandles();

        return new ArrayList<>(candles);
    }

    private Map<String, Candle> getLongCandles() {
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

    private boolean isInUpperTIme(Candle candle) {
        int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
        return h == 0 ||
                h == 1 ||
                h == 3 ||
                h == 4 ||
                h == 5 ||
                h == 8 ||
                h == 9 ||
                h == 11 ||
                h == 12 ||
                h == 13 ||
                h == 14 ||
                h == 15 ||
                h == 23;
    }
}