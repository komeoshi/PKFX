package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
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

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {

            PKFXFinderRestClient client = new PKFXFinderRestClient();

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            while (true) {
                Instrument instrument = getInstrument(restTemplate, client);
                if (instrument == null) continue;

                PKFXAnalyzer anal = new PKFXAnalyzer();
                anal.setPosition(instrument.getCandles(), false);
                Candle candle = instrument.getCandles().get(instrument.getCandles().size() - 1);

                Candle longCandle = getLongCandle(restTemplate, client);

                if (candle.getEmaPosition() == Position.NONE) {
                    continue;
                }
                if (longCandle == null) {
                    continue;
                }
                if (!anal.checkActiveTime()) {
                    continue;
                }

                if (candle.getEmaPosition() != lastPosition) {
                    // クロスした

                    double longAbs = Math.abs(longCandle.getAsk().getC() - longCandle.getPastCandle().getAsk().getC());
                    boolean checkLongAbs = longAbs
                            > 0.014;
                    boolean checkLongRange = checkRange(longCandle, 0.06, 0.5);
                    boolean checkSpread = candle.getSpreadMa() < 0.022;

                    log.info("cross detected. "  +
                            "spread:" + candle.getSpreadMa() + " "
                            + candle.getEmaPosition() + " " + longCandle.getEmaPosition() +
                            " abs:" + longAbs + " sig:" + candle.getSig() +
                            " longVma:" + candle.getLongVma() + " macd:" + candle.getMacd());

                    if (candle.getEmaPosition() == Position.LONG) {
                        // 売り→買い
                        if (status != Status.NONE) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (checkLongRange &&
                                checkSpread &&
                                checkLongAbs &&
                                longCandle.getAsk().getL() > longCandle.getLongMa()
                        ) {
                            log.info("signal (GC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.buy(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }

                    } else if (candle.getEmaPosition() == Position.SHORT) {
                        // 買い→売り
                        if (status != Status.NONE) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (checkLongRange &&
                                checkSpread &&
                                checkLongAbs &&
                                longCandle.getAsk().getH() < longCandle.getLongMa()
                        ) {
                            log.info("signal (DC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.sell(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }

                    }
                }
                lastPosition = candle.getEmaPosition();

                if (status != Status.NONE) {
                    status = targetReach(restTemplate, client, status, openCandle, candle);
                    status = losscut(restTemplate, client, status, openCandle, candle);
                }
            }
        };

    }

    private Status losscut(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.000500;

        double lossCutRateBuy = openCandle.getAsk().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getAsk().getC() * (1 + lossCutMag);

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
        double mag = 0.000001;

        if (isInUpperTIme()) {
            mag *= 1.1;
        } else {
            mag *= 0.6;
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
            mag *= 0.6;
        }

        if (checkSen(candle, status)) {
            mag *= 0.6;
        }

        double targetRateBuy = (openCandle.getAsk().getC() + 0.004) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - 0.004) * (1 - mag);

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

    private Candle getFiveMinCandle(RestTemplate restTemplate, PKFXFinderRestClient client) {
        Instrument i;
        try {
            i = client.getInstrument(restTemplate, "M5");
        } catch (RestClientException e) {
            log.error(" " + e.getLocalizedMessage());
            return null;
        }
        PKFXAnalyzer anal = new PKFXAnalyzer();
        anal.setPosition(i.getCandles(), false);

        return i.getCandles().get(i.getCandles().size() - 1);
    }

    private boolean isUpper(Candle candle) {
        int size = 60;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            if (c.getAsk().getL() > c.getLongMa()) {
                count++;
            }
            total++;
        }
        return count / total > 0.0;
    }

    private boolean isLower(Candle candle) {
        int size = 60;

        List<Candle> candles = candle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            if (c.getAsk().getH() < c.getLongMa()) {
                count++;
            }
            total++;
        }
        return count / total > 0.0;
    }

    private boolean isInUpperTIme() {
        int h = LocalDateTime.now().getHour();
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
            double threshold = c.getAsk().getC() * 0.00030;
            if (Math.abs(c.getAsk().getL() - c.getAsk().getH()) > threshold) {
                count++;
            }
        }
        return count > 1;
    }

    private boolean checkRange(Candle baseCandle, double range, double targetRate) {
        int size = 30;
        double thresholdHigh = baseCandle.getAsk().getC() + range;
        double thresholdLow = baseCandle.getAsk().getC() - range;

        List<Candle> candles = baseCandle.getCandles();
        double count = 0;
        double total = 0;
        for (int ii = candles.size() - size; ii < candles.size(); ii++) {
            Candle c = candles.get(ii);
            double currentRate = c.getAsk().getC();
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
