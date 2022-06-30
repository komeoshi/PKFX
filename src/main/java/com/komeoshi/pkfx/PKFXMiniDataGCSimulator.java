package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.*;
import com.komeoshi.pkfx.enumerator.*;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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

    private double param;

    public void setParam(double param) {
        this.param = param;
    }

    private List<Candle> candles = null;

    private static double SPREAD_COST = 0.004;
    private boolean isLogging = true;
    private boolean isResultLogging = true;
    private boolean isShortCut = false;


    public static void main(String[] args) {
        PKFXMiniDataGCSimulator sim = new PKFXMiniDataGCSimulator();
        sim.run();
    }


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

    Parameter parameter1 = Parameter.getParameter1();
    Parameter parameter2 = Parameter.getParameter2();
    Parameter parameter3 = Parameter.getParameter3();
    Parameter parameter4 = Parameter.getParameter4();
    Parameter parameter5 = Parameter.getParameter5();
    Parameter parameter6 = Parameter.getParameter6();
    Parameter parameter7 = Parameter.getParameter7();
    Parameter parameter8 = Parameter.getParameter8();
    Parameter parameter9 = Parameter.getParameter9();

    public void run() {
        init();
        if (candles == null) {
            this.candles = getCandlesFromFile();
        }

        Status status = Status.NONE;
        Position lastMacdPosition = Position.NONE;
        Position lastEmaPosition = Position.NONE;
        AdxPosition lastAdxPosition = AdxPosition.NONE;
        Candle openCandle = null;
        int continueCount = 0;
        final int CONTINUE_MAX = 0;
        for (int ii = 0; ii < candles.size(); ii++) {
            Candle candle = candles.get(ii);

            if (isShortCut && candles.size() / 100 < ii && totalCount == 0) {
                // これ以上やっても無駄
                break;
            }

            if (candle.getMacdPosition() == Position.NONE) {
                continue;
            }

            LocalDateTime from = LocalDateTime.of(2021, 1, 1, 0, 0, 0, 0);
            if (candle.getTime().isBefore(from)) {
                continue;
            }

            LocalDateTime to = LocalDateTime.of(2022, 6, 1, 0, 0, 0, 0);
            if (candle.getTime().isAfter(to)) {
                continue;
            }
            boolean emaPositionChanged = lastEmaPosition != candle.getEmaPosition();
            boolean macdPositionChanged = lastMacdPosition != candle.getMacdPosition();
            boolean adxPositionChanged = lastAdxPosition != candle.getAdxPosition();

            if ((emaPositionChanged || macdPositionChanged || adxPositionChanged) &&
                    lastMacdPosition != Position.NONE) {

                boolean doTrade1 = isDoTradeWithParameter(candle, parameter1);
                boolean doTrade2 = isDoTradeWithParameter(candle, parameter2);
                boolean doTrade3 = isDoTradeWithParameter(candle, parameter3);
                boolean doTrade4 = isDoTradeWithParameter(candle, parameter4);
                boolean doTrade5 = isDoTradeWithParameter(candle, parameter5);
                boolean doTrade6 = isDoTradeWithParameter(candle, parameter6);
                boolean doTrade7 = isDoTradeWithParameter(candle, parameter7);
                boolean doTrade8 = isDoTradeWithParameter(candle, parameter8);
                boolean doTrade9 = isDoTradeWithParameter(candle, parameter9);
                boolean doTrade = doTrade1 || doTrade2 || doTrade3 || doTrade4 || doTrade5 || doTrade6 || doTrade7 || doTrade8 || doTrade9;

                if ((macdPositionChanged && candle.getMacdPosition() == Position.LONG) ||
                        (emaPositionChanged && candle.getEmaPosition() == Position.LONG) ||
                        (adxPositionChanged && candle.getAdxPosition() == AdxPosition.OVER)) {
                    // 売り→買い

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

        if (isResultLogging)
            log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                    diff + "(" + (diff * 1000 / totalCount) + "), " +
                    "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
            );
    }

    private boolean isDoTradeWithParameter(Candle candle, Parameter parameter) {
        Candle tmpCandle = candle.getCandles().get(candle.getCandles().size() - 1);
        Candle tmpCandle2 = candle.getCandles().get(candle.getCandles().size() - 2);
        Candle tmpCandle3 = candle.getCandles().get(candle.getCandles().size() - 3);
        Candle tmpCandle4 = candle.getCandles().get(candle.getCandles().size() - 4);

        boolean checkSpread = candle.getSpreadMa() < 0.027;
        boolean hasLongCandle = hasLongCandle(candle);
        boolean hasShortCandle = hasShortCandle(candle);
        boolean checkAtr = candle.getAtr() > parameter.getParamA$01().getParameter() ||
                candle.getTr() > parameter.getParamA$02().getParameter() ||
                tmpCandle2.getTr() > parameter.getParamA$03().getParameter() ||
                tmpCandle3.getAtr() > parameter.getParamA$04().getParameter() ||
                tmpCandle4.getAtr() > parameter.getParamA$05().getParameter();

        boolean checkMacd = Math.abs(tmpCandle.getMacd()) > parameter.getParamD$01().getParameter() ||
                Math.abs(tmpCandle2.getMacd()) > parameter.getParamD$02().getParameter();
        boolean checkSig = Math.abs(tmpCandle.getSig()) > parameter.getParamD$03().getParameter();
        boolean checkBb = candle.getBollingerBandHigh() - candle.getBollingerBandLow() > parameter.getParamC$01().getParameter();
        boolean checkBb2 = tmpCandle2.getBollingerBandHigh() - tmpCandle2.getBollingerBandLow() < parameter.getParamC$02().getParameter();
        boolean checkAdx = candle.getAdx().getAdx() > parameter.getParamB$01().getParameter();
        boolean checkDx = Math.abs(candle.getAdx().getPlusDi() - candle.getAdx().getMinusDi()) > parameter.getParamC$03().getParameter() ||
                Math.abs(tmpCandle.getAdx().getPlusDi() - tmpCandle.getAdx().getMinusDi()) > parameter.getParamC$04().getParameter();
        boolean checkRsi = Math.abs(tmpCandle2.getRsi()) > parameter.getParamB$02().getParameter();
        boolean checkRsi2 = Math.abs(candle.getRsi()) < parameter.getParamB$03().getParameter() &&
                Math.abs(tmpCandle.getRsi()) < parameter.getParamB$04().getParameter();

        return (
                !hasLongCandle
                        && !hasShortCandle
                        && checkAtr
                        && checkSpread
                        && checkMacd
                        && checkSig
                        && checkBb
                        && checkBb2
                        && checkAdx
                        && checkDx
                        && checkRsi
                        && checkRsi2
        );
    }


    private Status losscut(Status status, Candle openCandle, Candle candle) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.00091;

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
        double mag = 0.37 / 1000;
        double targetRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 - mag);

        boolean okawariFlag = Math.abs(candle.getMacd()) > 0.0875 ||
                Math.abs(candle.getMacd()) < 0.007 ||
                candle.getAdx().getAdx() > 55 ||
                candle.getAdx().getAdx() < 25 ||
                Math.abs(candle.getRsi()) > 90 ||
                Math.abs(candle.getRsi()) < 20 ||
                candle.getBollingerBandHigh() - candle.getBollingerBandLow() > 0.380 ||
                candle.getBollingerBandHigh() - candle.getBollingerBandLow() < 0.097 ||
                Math.abs(candle.getSig()) < 0.004 ||
                candle.getAtr() < 0.016;

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {

                if (okawariFlag) {
                    if (isLogging)
                        log.info("okawari. 【" + openCandle.getNumber() + "】" + candle.getAsk().getC());
                    return status;
                }

                completeOrder(openCandle, candle, Reason.REACHED, status);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {

                if (okawariFlag) {
                    if (isLogging)
                        log.info("okawari. 【" + openCandle.getNumber() + "】" + candle.getAsk().getC());
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
        return count > 5;
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
