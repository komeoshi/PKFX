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
                    boolean isSigOver = candle.getSig() > PKFXConst.GC_SIG_MAGNIFICATION;
                    log.info("cross detected. " + candle.getPosition() + " sig:" + candle.getSig() + " " + isSigOver);

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い
                        if (status == Status.HOLDING_SELL) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (isSigOver && anal.checkActiveTime()) {
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
                        if (isSigOver && anal.checkActiveTime()) {
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
        double lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION;
        double lossCutRateBuy = openCandle.getMid().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getMid().getC() * (1 + lossCutMag);
        if(status == Status.HOLDING_BUY &&
                lossCutRateBuy > candle.getMid().getC()){

            log.info("<<signal (buy)(losscut)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
            client.complete(restTemplate);
            status = Status.NONE;
        }else if (status == Status.HOLDING_SELL &&
                lossCutRateSell < candle.getMid().getC()) {

            log.info("<<signal (sell)(losscut)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
            client.complete(restTemplate);
            status = Status.NONE;
        }
        return status;
    }

    private Status targetReach(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION;
        double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
        double targetRateSell = openCandle.getMid().getC() * (1 - mag);

        double rsiMagnification = 20;
        boolean isSigEnough = openCandle.getSig() > candle.getSig();
        boolean isRsiHot = (candle.getRsi() > 100 - rsiMagnification) ;
        boolean isRsiCOld = (candle.getRsi() < rsiMagnification);
        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getMid().getC() && isSigEnough ||
                    targetRateBuy < candle.getMid().getC() && isRsiHot) {

                log.info("<<signal (buy)(reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getMid().getC() && isSigEnough ||
                    targetRateSell > candle.getMid().getC() && isRsiCOld) {

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
}
