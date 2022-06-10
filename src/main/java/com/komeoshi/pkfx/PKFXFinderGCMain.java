package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
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

@SpringBootApplication
public class PKFXFinderGCMain {

    private static final Logger log = LoggerFactory.getLogger(PKFXFinderGCMain.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXFinderGCMain.class, args);
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

                PKFXFinderAnalyzer anal = new PKFXFinderAnalyzer();
                anal.setPosition(instrument.getCandles(), false);
                Candle candle = instrument.getCandles().get(instrument.getCandles().size() - 1);

                if (candle.getPosition() == Position.NONE) {
                    continue;
                }

                if (candle.getPosition() != lastPosition) {
                    // クロスした
                    boolean isSigOver = candle.getSig() > PKFXConst.GC_SIG_MAGNIFICATION;
                    boolean isVmaOver = candle.getLongVma() > 0.1;
                    log.info("cross detected. " + candle.getPosition() + " sig:" + candle.getSig() + " longVma:" + candle.getLongVma());

                    int h = LocalDateTime.now().getHour();
                    boolean isDeadTime = h==6 || h==17 || h==18 || h==20 || h==21;

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い
                        if (status == Status.HOLDING_SELL) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime) {
                            log.info("signal (buy) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.buy(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }
                    } else if (candle.getPosition() == Position.SHORT) {
                        // 買い→売り
                        if (status == Status.HOLDING_BUY) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime) {
                            log.info("signal (sell) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.sell(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }

                }
                lastPosition = candle.getPosition();

                if (status != Status.NONE) {
                    status = targetReach(restTemplate, client, status, openCandle, candle);
                    status = losscut(restTemplate, client, status, openCandle, candle);
                }
            }
        };

    }

    private Status losscut(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        boolean isLower = candle.getPastCandle().getLongMa() > candle.getLongMa();
        double lossCutMag;
        if (isLower) {
            lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION / 0.20;
        } else {
            lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION;
        }

        if (!isInUpperTIme()) {
            lossCutMag *= 1.45;
        }

        double lossCutRateBuy = openCandle.getMid().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getMid().getC() * (1 + lossCutMag);
        if (status == Status.HOLDING_BUY &&
                lossCutRateBuy > candle.getMid().getC()) {

            log.info("<<signal (buy)(losscut)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
            client.complete(restTemplate);
            status = Status.NONE;
        } else if (status == Status.HOLDING_SELL &&
                lossCutRateSell < candle.getMid().getC()) {

            log.info("<<signal (sell)(losscut)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
            client.complete(restTemplate);
            status = Status.NONE;
        }
        return status;
    }

    private Status targetReach(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION * 10.86;
        if (isInUpperTIme()) {
            mag *= 1.5;
        } else {
            mag *= 0.5;
        }
        double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
        double targetRateSell = openCandle.getMid().getC() * (1 - mag);

        boolean isUpperCloud = candle.getLongMa() < candle.getMid().getC();

        boolean checkVma = candle.getLongVma() * 1.005 < candle.getShortVma();
        boolean checkMacd = candle.getMacd() < candle.getSig() * 1.5;

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getMid().getC() && isUpperCloud && checkVma && checkMacd) {

                log.info("<<signal (buy)(reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                client.complete(restTemplate);
                status = Status.NONE;

            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getMid().getC() && !isUpperCloud && checkVma && checkMacd) {

                log.info("<<signal (sell)(reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        }
        return status;
    }

    private Instrument getInstrument(RestTemplate restTemplate, PKFXFinderRestClient client) {
        Instrument i;
        try {
            i = client.getInstrument(restTemplate);
        } catch (RestClientException e) {
            log.error("", e);
            return null;
        }
        return i;
    }

    private boolean isInUpperTIme() {
        int h = LocalDateTime.now().getHour();
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
