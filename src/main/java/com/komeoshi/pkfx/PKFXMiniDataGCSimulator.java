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
    private List<Candle> fiveMinCandles = null;

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
            this.fiveMinCandles = get5MinCandlesFromFile();
        }

        Status status = Status.NONE;
        Position lastPosition = Position.NONE;
        Candle openCandle = null;
        for (Candle candle : candles) {

            if (candle.getEmaPosition() == Position.NONE) {
                continue;
            }

            LocalDateTime from = LocalDateTime.of(2022, 1, 4, 0, 0, 0, 0);
            if (candle.getTime().isBefore(from)) {
                continue;
            }

            if (candle.getEmaPosition() != lastPosition) {
                Candle longCandle = getCandleAt(longCandles, candle.getTime());
                if(longCandle==null || longCandle.getPastCandle() == null){
                    log.info(""+longCandle.getTime() + " is null,");
                    continue;
                }

                boolean checkLongAbs = Math.abs(longCandle.getMid().getC() - longCandle.getPastCandle().getMid().getC())
                        > 0.014;
                boolean checkLongRange = checkRange(longCandle, 0.06, 0.5);

                if (candle.getEmaPosition() == Position.LONG) {
                    // 売り→買い

                    if (status != Status.NONE) {
                        completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                        status = Status.NONE;
                    }

                    if (true
                    ) {
                        buy(TradeReason.GC, candle);
                        status = Status.HOLDING_BUY;
                        openCandle = candle;
                    }

                } else if (candle.getEmaPosition() == Position.SHORT) {
                    // 買い→売り

                    if (status != Status.NONE) {
                        completeOrder(openCandle, candle, Reason.TIMEOUT, status);
                        status = Status.NONE;
                    }

                    if (true
                    ) {
                        sell(TradeReason.DC, candle);
                        status = Status.HOLDING_SELL;
                        openCandle = candle;
                    }
                }
            }
            lastPosition = candle.getEmaPosition();

            if (status != Status.NONE) {
                status = targetReach(status, openCandle, candle);
                status = losscut(status, openCandle, candle);
            }
        }

        log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                diff + "(" + (diff * 100 / totalCount) + "), " +
                "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
        );

        // System.exit(0);

    }

    private Status losscut(Status status, Candle openCandle, Candle candle) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.000500;

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
        double mag = 0.000280;

        if (isInUpperTIme(openCandle)) {
            mag *= 1.1;
        } else {
            mag *= 0.5;
        }

        if (candle.getSuperShortMa() < candle.getShortMa() &&
                status == Status.HOLDING_BUY) {
            mag *= 0.6;
        }
        if (candle.getSuperShortMa() > candle.getShortMa() &&
                status == Status.HOLDING_SELL) {
            mag *= 0.6;
        }

        if (hasLongCandle(candle)) {
            mag *= 0.7;
        }

        if (checkSen(candle, status)) {
            mag *= 0.4;
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
                                " " + openCandle.getEmaPosition() +
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

    private List<Candle> get5MinCandlesFromFile() {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("5MinData.dat");
        List<Candle> candles = reader.read().getCandles();

        return new ArrayList<>(candles);
    }

    private Candle getCandleAt(List<Candle> candles, LocalDateTime time) {

        Candle candle = null;
        for (Candle c : candles) {
            if (c.getTime().isAfter(time)) {
                break;
            } else {
                candle = c;
            }
        }

        return candle;
    }

    private boolean isUpper(Candle candle, int size) {
        List<Candle> candles = candle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            if (c.getMid().getL() > c.getLongMa()) {
                count++;
            }
            total++;
        }
        return count / total > 0.0;
    }

    private boolean isLower(Candle candle, int size) {
        List<Candle> candles = candle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            if (c.getMid().getH() < c.getLongMa()) {
                count++;
            }
            total++;
        }
        return count / total > 0.0;
    }

    private boolean isInUpperTIme(Candle candle) {
        int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
        return h == 0 ||
                h == 1 ||
                h == 2 ||
                h == 3 ||
                h == 4 ||
                h == 5 ||
                h == 6 ||
                h == 7 ||
                h == 8 ||
                h == 9 ||
                h == 10 ||
                h == 11 ||
                h == 12 ||
                h == 13 ||
                h == 14 ||
                h == 15 ||
                h == 16 ||
                // h == 17 ||
                h == 18 ||
                h == 19 ||
                h == 20 ||
                h == 21 ||
                h == 22 ||
                h == 23;
    }

    private boolean hasLongCandle(Candle candle) {
        int size = 20;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            double threshold = c.getMid().getC() * 0.00030;
            if (Math.abs(c.getMid().getL() - c.getMid().getH()) > threshold) {
                count++;
            }
        }
        return count > 1;
    }

    private boolean checkRange(Candle baseCandle, double range, double targetRate) {
        int size = 30;
        double thresholdHigh = baseCandle.getMid().getC() + range;
        double thresholdLow = baseCandle.getMid().getC() - range;

        List<Candle> candles = baseCandle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            double currentRate = c.getMid().getC();
            if (currentRate > thresholdHigh || thresholdLow > currentRate) {
                count++;
            }
            total++;
        }
        double rate = count / total;
        return rate > targetRate;
    }

    private boolean checkSen(Candle candle, Status status) {
        int size = 20;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            if (status == Status.HOLDING_BUY && c.isInsen()) {
                count++;
            } else if (status == Status.HOLDING_SELL && c.isYousen()) {
                count++;
            }
            total++;
        }
        return count / total > 0.8;
    }
}
