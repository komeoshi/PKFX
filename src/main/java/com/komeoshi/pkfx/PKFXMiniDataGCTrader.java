package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import com.komeoshi.pkfx.enumerator.AdxPosition;
import com.komeoshi.pkfx.enumerator.Position;
import com.komeoshi.pkfx.enumerator.Status;
import com.komeoshi.pkfx.restclient.PKFXFinderRestClient;
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

    private static final double SPREAD_COST = 0.004;

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {

            PKFXFinderRestClient client = new PKFXFinderRestClient();

            Status status = Status.NONE;
            Position lastMacdPosition = Position.NONE;
            Position lastEmaPosition = Position.NONE;
            AdxPosition lastAdxPosition = AdxPosition.NONE;
            Candle openCandle = null;
            int continueCount = 0;
            final int CONTINUE_MAX = 0;
            while (true) {
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
                    // クロスした

                    Candle tmpCandle = candle.getCandles().get(candle.getCandles().size() - 1);
                    Candle tmpCandle2 = candle.getCandles().get(candle.getCandles().size() - 2);

                    boolean checkSpread = candle.getSpreadMa() < 0.027;
                    boolean hasLongCandle = hasLongCandle(candle);
                    boolean hasShortCandle = hasShortCandle(candle);
                    boolean checkAtr = candle.getAtr() > 0.0243 ||
                            candle.getTr() > 0.0450 ||
                            tmpCandle2.getTr() > 0.059;

                    int h = LocalDateTime.now().getHour();
                    boolean checkTimeH = h != 0 && h != 2 && h != 5 && h != 8 && h != 10 && h != 12 && h != 13
                            && h != 14 && h != 17 && h != 18 && h != 20 && h != 22 && h != 23;
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

                    log.info("---crossed.---");
                    log.info("spread        :" + checkSpread + " " + candle.getSpreadMa() + "< " + 0.029);
                    log.info("hasLongCandle :" + !hasLongCandle);
                    log.info("hasShortCandle:" + !hasShortCandle);
                    log.info("checkAtr      :" + checkAtr + " " + candle.getAtr() + "> " + 0.0243);
                    log.info("checkTimeH    :" + checkTimeH + " " + h);

                    boolean doTrade = (
                            !hasLongCandle
                                    && !hasShortCandle
                                    && checkAtr
                                    && checkSpread
                                    && checkTimeH
                                    && checkMacd
                                    && checkSig
                                    && checkBb
                                    && checkBb2
                                    && checkAdx
                                    && checkDx
                                    && checkRsi
                                    && checkRsi2
                    );

                    if ((macdPositionChanged && candle.getMacdPosition() == Position.LONG) ||
                            (emaPositionChanged && candle.getEmaPosition() == Position.LONG) ||
                            (adxPositionChanged && candle.getAdxPosition() == AdxPosition.OVER)) {
                        // 売り→買い

                        if (status != Status.NONE) {
                            if (candle.getAsk().getC() < openCandle.getAsk().getC() &&
                                    !doTrade) {
                                // ﾏｹﾃﾙ
                                continueCount++;
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount);

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
                        // 買い→売り

                        if (status != Status.NONE) {
                            if (candle.getAsk().getC() > openCandle.getAsk().getC() &&
                                    !doTrade) {
                                // ﾏｹﾃﾙ
                                continueCount++;
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount);
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
            }
        };

    }

    private Status losscut(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
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
        double mag = 0.000341;

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
                Math.abs(candle.getSig()) < 0.004;

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {
                if (okawariFlag) {
                    return status;
                }

                log.info("<<signal (buy_reached)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {
                if (okawariFlag) {
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
