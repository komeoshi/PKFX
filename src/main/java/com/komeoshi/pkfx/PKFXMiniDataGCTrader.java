package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import com.komeoshi.pkfx.dto.parameter.Parameter;
import com.komeoshi.pkfx.dto.parameter.okawari.OkawariParameter;
import com.komeoshi.pkfx.enumerator.AdxPosition;
import com.komeoshi.pkfx.enumerator.Position;
import com.komeoshi.pkfx.enumerator.Status;
import com.komeoshi.pkfx.restclient.PKFXFinderRestClient;
import com.komeoshi.pkfx.simulatedata.PKFXParameterDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class PKFXMiniDataGCTrader {

    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCTrader.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXMiniDataGCTrader.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public void init() {
        PKFXParameterDataReader reader1 = new PKFXParameterDataReader("parameter1.dat");
        parameter1 = reader1.read();
        PKFXParameterDataReader reader2 = new PKFXParameterDataReader("parameter2.dat");
        parameter2 = reader2.read();
        PKFXParameterDataReader reader3 = new PKFXParameterDataReader("parameter3.dat");
        parameter3 = reader3.read();
        PKFXParameterDataReader reader4 = new PKFXParameterDataReader("parameter4.dat");
        parameter4 = reader4.read();
        PKFXParameterDataReader reader5 = new PKFXParameterDataReader("parameter5.dat");
        parameter5 = reader5.read();
        PKFXParameterDataReader reader6 = new PKFXParameterDataReader("parameter6.dat");
        parameter6 = reader6.read();
        PKFXParameterDataReader reader7 = new PKFXParameterDataReader("parameter7.dat");
        parameter7 = reader7.read();
        PKFXParameterDataReader reader8 = new PKFXParameterDataReader("parameter8.dat");
        parameter8 = reader8.read();
        PKFXParameterDataReader reader9 = new PKFXParameterDataReader("parameter9.dat");
        parameter9 = reader9.read();
        PKFXParameterDataReader reader10 = new PKFXParameterDataReader("parameter10.dat");
        parameter10 = reader10.read();
    }
    Parameter parameter1 = null;
    Parameter parameter2 = null;
    Parameter parameter3 = null;
    Parameter parameter4 = null;
    Parameter parameter5 = null;
    Parameter parameter6 = null;
    Parameter parameter7 = null;
    Parameter parameter8 = null;
    Parameter parameter9 = null;
    Parameter parameter10 = null;

    private static final double SPREAD_COST = 0.004;
    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {

            PKFXFinderRestClient client = new PKFXFinderRestClient();
            init();

            Status status = Status.NONE;
            Position lastMacdPosition = Position.NONE;
            Position lastEmaPosition = Position.NONE;
            AdxPosition lastAdxPosition = AdxPosition.NONE;
            Candle openCandle = null;
            int continueCount = 0;
            final int CONTINUE_MAX = 0;
            long loopCount = 0;
            long totalTime = 0;
            while (true) {
                loopCount++;
                long startTime = System.currentTimeMillis();

                Instrument instrument = getInstrument(restTemplate, client);
                if (instrument == null) continue;

                PKFXAnalyzer anal = new PKFXAnalyzer();
                anal.setPosition(instrument.getCandles(), false);
                Candle candle = instrument.getCandles().get(instrument.getCandles().size() - 1);

                if (candle.getMacdPosition() == Position.NONE) {
                    continue;
                }
                if (!anal.checkActiveTime()) {
                    continue;
                }

                boolean emaPositionChanged = lastEmaPosition != candle.getEmaPosition();
                boolean macdPositionChanged = lastMacdPosition != candle.getMacdPosition();
                boolean adxPositionChanged = lastAdxPosition != candle.getAdxPosition();

                if ((emaPositionChanged || macdPositionChanged) &&
                        lastMacdPosition != Position.NONE) {
                    // ???????????????

                    boolean doTrade1 = isDoTradeWithParameter(candle, parameter1);
                    boolean doTrade2 = isDoTradeWithParameter(candle, parameter2);
                    boolean doTrade3 = isDoTradeWithParameter(candle, parameter3);
                    boolean doTrade4 = isDoTradeWithParameter(candle, parameter4);
                    boolean doTrade5 = isDoTradeWithParameter(candle, parameter5);
                    boolean doTrade6 = isDoTradeWithParameter(candle, parameter6);
                    boolean doTrade7 = isDoTradeWithParameter(candle, parameter7);
                    boolean doTrade8 = isDoTradeWithParameter(candle, parameter8);
                    boolean doTrade9 = isDoTradeWithParameter(candle, parameter9);
                    boolean doTrade10 = isDoTradeWithParameter(candle, parameter10);
                    boolean doTrade = doTrade1 || doTrade2 || doTrade3 || doTrade4 || doTrade5 || doTrade6 || doTrade7 || doTrade8 || doTrade9 || doTrade10;

                    if ((macdPositionChanged && candle.getMacdPosition() == Position.LONG) ||
                            (emaPositionChanged && candle.getEmaPosition() == Position.LONG) ||
                            (adxPositionChanged && candle.getAdxPosition() == AdxPosition.OVER)) {
                        // ???????????????

                        if (status != Status.NONE) {
                            if (candle.getAsk().getC() < openCandle.getAsk().getC() &&
                                    !doTrade) {
                                // ????????????
                                continueCount++;
                                log.info("continue. ???" + openCandle.getNumber() + "???" + continueCount);

                            } else if (candle.getAsk().getC() > openCandle.getAsk().getC() ||
                                    continueCount >= CONTINUE_MAX ||
                                    doTrade) {
                                log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                                client.complete(restTemplate);
                                status = Status.NONE;
                                continueCount = 0;
                            }
                        }
                        if (doTrade) {
                            log.info("signal (GC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.buy(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }

                    } else if ((macdPositionChanged && candle.getMacdPosition() == Position.SHORT) ||
                            (emaPositionChanged && candle.getEmaPosition() == Position.SHORT) ||
                            (adxPositionChanged && candle.getAdxPosition() == AdxPosition.UNDER)) {
                        // ???????????????

                        if (status != Status.NONE) {
                            if (candle.getAsk().getC() > openCandle.getAsk().getC() &&
                                    !doTrade) {
                                // ????????????
                                continueCount++;
                                log.info("continue. ???" + openCandle.getNumber() + "???" + continueCount);
                            } else if (candle.getAsk().getC() < openCandle.getAsk().getC() ||
                                    continueCount >= CONTINUE_MAX ||
                                    doTrade
                            ) {
                                log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                                client.complete(restTemplate);
                                status = Status.NONE;
                                continueCount = 0;
                            }
                        }
                        if (doTrade) {
                            log.info("signal (DC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.sell(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }

                    }
                }
                lastMacdPosition = candle.getMacdPosition();
                lastEmaPosition = candle.getEmaPosition();
                lastAdxPosition = candle.getAdxPosition();

                if (status != Status.NONE) {
                    status = targetReach(restTemplate, client, status, openCandle, candle);
                    status = losscut(restTemplate, client, status, openCandle, candle);
                    if (status == Status.NONE) {
                        continueCount = 0;
                    }
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                totalTime += elapsedTime;
                if (loopCount % 1000 == 0) {
                    log.info("???" + loopCount + "??? " + candle.getTime() + " " + elapsedTime + "ms. " + (totalTime / loopCount) + "ms.");
                }
            }
        };

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

        int m = LocalDateTime.now().getMinute();
        boolean checkTimeM = m != 59;
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
                        && checkTimeM
        );
    }

    private Status losscut(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        // ???????????????????????????????????????????????????
        double lossCutMag = 0.00091;

        if (Math.abs(candle.getMacd()) > 0.011) {
            // ?????????????????????????????????
            lossCutMag *= 0.98;
        }

        double lossCutRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 - lossCutMag);
        double lossCutRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 + lossCutMag);

        if (status == Status.HOLDING_BUY) {
            if (lossCutRateBuy > candle.getAsk().getC()) {

                log.info("<<signal (buy_losscut)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (lossCutRateSell < candle.getAsk().getC()) {

                log.info("<<signal (sell_losscut)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        }
        return status;
    }

    private Status targetReach(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        double mag = 0.4 / 1000;

        double targetRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 - mag);

        OkawariParameter okawariParameter = OkawariParameter.getOkawariParameter();

        boolean okawariFlag = Math.abs(candle.getMacd()) > okawariParameter.getParamA().getParameter() ||
                Math.abs(candle.getMacd()) < okawariParameter.getParamB().getParameter() ||
                candle.getAdx().getAdx() > okawariParameter.getParamC().getParameter() ||
                candle.getAdx().getAdx() < okawariParameter.getParamD().getParameter() ||
                Math.abs(candle.getRsi()) > okawariParameter.getParamE().getParameter() ||
                Math.abs(candle.getRsi()) < okawariParameter.getParamF().getParameter() ||
                candle.getBollingerBandHigh() - candle.getBollingerBandLow() > okawariParameter.getParamG().getParameter() ||
                candle.getBollingerBandHigh() - candle.getBollingerBandLow() < okawariParameter.getParamH().getParameter() ||
                Math.abs(candle.getSig()) < okawariParameter.getParamI().getParameter() ||
                candle.getAtr() < okawariParameter.getParamJ().getParameter();


        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {
                if (okawariFlag) {
                    log.info("continue. ???" + openCandle.getNumber() + "???");
                    return status;
                }

                log.info("<<signal (buy_reached)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {
                if (okawariFlag) {
                    log.info("continue. ???" + openCandle.getNumber() + "???" );
                    return status;
                }

                log.info("<<signal (sell_reached)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        }
        return status;
    }

    private Instrument getInstrument(RestTemplate restTemplate, PKFXFinderRestClient client) {
        Instrument i;
        try {
            i = client.getInstrument(restTemplate, "S5");
        } catch (RestClientException e) {
            log.error(" " + e.getLocalizedMessage());
            return null;
        }
        return i;
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
