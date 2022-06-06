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
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
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
                    boolean isShortVmaLargerThanLongVma = candle.getShortVma() > candle.getLongVma();
                    log.info("cross detected. " + candle.getPosition() + " vma:" + candle.getShortVma() + "/" + candle.getLongVma() + " " + isShortVmaLargerThanLongVma);

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い
                        if (status == Status.HOLDING_SELL) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (isShortVmaLargerThanLongVma && anal.checkActiveTime()) {
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
                        if (isShortVmaLargerThanLongVma && anal.checkActiveTime()) {
                            log.info("signal (sell) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.sell(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }

                }
                lastPosition = candle.getPosition();

                if (status != Status.NONE) {
                    double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION;
                    double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
                    double targetRateSell = openCandle.getMid().getC() * (1 - mag);
                    if (status == Status.HOLDING_BUY &&
                            targetRateBuy < candle.getMid().getC()) {

                        log.info("<<signal (buy)(reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                        client.complete(restTemplate);
                        status = Status.NONE;
                    } else if (status == Status.HOLDING_SELL &&
                            targetRateSell > candle.getMid().getC()) {

                        log.info("<<signal (sell)(reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                        client.complete(restTemplate);
                        status = Status.NONE;
                    }
                }
            }
        };

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
}
