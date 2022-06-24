package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import com.komeoshi.pkfx.dto.Revenge;
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
import java.time.ZoneId;
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

    private static final double SPREAD_COST = 0.0042;

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {

            PKFXFinderRestClient client = new PKFXFinderRestClient();

            Status status = Status.NONE;
            Position lastMacdPosition = Position.NONE;
            Position lastEmaPosition = Position.NONE;
            Candle openCandle = null;
            while (true) {
                Instrument instrument = getInstrument(restTemplate, client);
                if (instrument == null) continue;

                PKFXAnalyzer anal = new PKFXAnalyzer();
                anal.setPosition(instrument.getCandles(), false);
                Candle candle = instrument.getCandles().get(instrument.getCandles().size() - 1);

                Candle longCandle = getLongCandle(restTemplate, client);

                if (candle.getMacdPosition() == Position.NONE) {
                    continue;
                }
                if (longCandle == null) {
                    continue;
                }
                if (!anal.checkActiveTime()) {
                    continue;
                }

                boolean emaPositionChanged = lastEmaPosition != candle.getEmaPosition();
                boolean macdPositionChanged = lastMacdPosition != candle.getMacdPosition();

                if ((emaPositionChanged || macdPositionChanged) &&
                        lastMacdPosition != Position.NONE) {
                    // クロスした

                    boolean checkSpread = candle.getSpreadMa() < 0.030;
                    boolean hasLongCandle = hasLongCandle(candle);
                    boolean hasShortCandle = hasShortCandle(candle);
                    boolean checkAtr = candle.getAtr() > 0.0203;
                    boolean checkVma = candle.getShortVma() < candle.getVolume() * 1.10;

                    log.info("---crossed.---");
                    log.info("spread        :" + checkSpread + " " + candle.getSpreadMa());
                    log.info("hasLongCandle :" + !hasLongCandle);
                    log.info("hasShortCandle:" + !hasShortCandle);
                    log.info("checkAtr      :" + checkAtr + " " + candle.getAtr());
                    log.info("checkVma      :" + checkVma + " " + candle.getShortVma() + " " + candle.getVolume());

                    if ((macdPositionChanged && candle.getMacdPosition() == Position.LONG) ||
                            (emaPositionChanged && candle.getEmaPosition() == Position.LONG)) {
                        // 売り→買い
                        boolean doTrade = (
                                !hasLongCandle
                                        && !hasShortCandle
                                        && checkAtr
                                        && checkVma
                                        && checkSpread
                        );

                        if (status != Status.NONE) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (doTrade) {
                            log.info("signal (GC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.buy(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }

                    } else if ((macdPositionChanged && candle.getMacdPosition() == Position.SHORT) ||
                            (emaPositionChanged && candle.getEmaPosition() == Position.SHORT)) {
                        // 買い→売り
                        boolean doTrade = (
                                !hasLongCandle
                                        && !hasShortCandle
                                        && checkAtr
                                        && checkVma
                                        && checkSpread
                        );

                        if (status != Status.NONE) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
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

                if (status != Status.NONE) {
                    status = targetReach(restTemplate, client, status, openCandle, candle);
                    status = losscut(restTemplate, client, status, openCandle, candle);
                }
            }
        };

    }

    private Status losscut(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
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
        double mag = 0.000225;

        double targetRateBuy = (openCandle.getAsk().getC() + SPREAD_COST) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - SPREAD_COST) * (1 - mag);

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {
                log.info("<<signal (buy_reached)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {
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

    private Candle getLongCandle(RestTemplate restTemplate, PKFXFinderRestClient client) {
        Instrument i;
        try {
            i = client.getInstrument(restTemplate, "M1");
        } catch (RestClientException e) {
            log.error(" " + e.getLocalizedMessage());
            return null;
        }
        PKFXAnalyzer anal = new PKFXAnalyzer();
        anal.setPosition(i.getCandles(), false);

        return i.getCandles().get(i.getCandles().size() - 1);
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
}
