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

@SpringBootApplication
public class PKFXGCTrader {

    private static final Logger log = LoggerFactory.getLogger(PKFXGCTrader.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXGCTrader.class, args);
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

                if (candle.getPosition() == Position.NONE) {
                    continue;
                }

                if (candle.getPosition() != lastPosition) {
                    // クロスした
                    boolean isSigOver = candle.getSig() > PKFXConst.GC_SIG_MAGNIFICATION;
                    boolean isVmaOver = candle.getLongVma() > 0.1;
                    log.info("cross detected. " + candle.getPosition() + " sig:" + candle.getSig() +
                            " longVma:" + candle.getLongVma() + " macd:" + candle.getMacd());

                    int h = LocalDateTime.now().getHour();
                    int m = LocalDateTime.now().getMinute();
                    boolean isDeadTime =
                            h == 2 || h == 5 || h == 6 || h == 7 || h == 10 || h == 16 ||
                                    h == 17 || h == 18 || h == 19 || h == 20 || h == 21 || h == 22;
                    boolean isDeadMinute = m == 59;

                    boolean checkMacd = candle.getMacd() < 0.040;

                    boolean isRsiHot = candle.getRsi() > 50;
                    boolean isRsiCold = candle.getRsi() <= 50;

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い
                        if (status != Status.NONE) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime && isNotInRange(candle)
                                && checkMacd && !isDeadMinute) {
                            log.info("signal (GC) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.buy(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }
                    } else if (candle.getPosition() == Position.SHORT) {
                        // 買い→売り
                        if (status != Status.NONE) {
                            log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.complete(restTemplate);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime && isNotInRange(candle)
                                && checkMacd && !isDeadMinute) {
                            log.info("signal (DC) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.sell(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }

                    if(status == Status.NONE) {
                        if (isRsiHot) {
                            log.info("signal (HOT) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.sell(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                        if (isRsiCold) {
                            log.info("signal (COLD) >> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                            client.buy(candle.getMid().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
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

        // RSI超過ならロスカットしやすくなる
        double rsiMag = 4.8;
        boolean isRsiHot = candle.getRsi() > 100 - rsiMag;
        boolean isRsiCold = candle.getRsi() < rsiMag;
        if (isRsiHot || isRsiCold) {
            lossCutMag /= 300;
        }

        double lossCutRateBuy = openCandle.getMid().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getMid().getC() * (1 + lossCutMag);

        boolean isUpper = candle.getPastCandle().getLongMa() < candle.getLongMa();

        if (status == Status.HOLDING_BUY) {
            if (lossCutRateBuy > candle.getMid().getC() && isUpper) {

                log.info("<<signal (buy)(losscut)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (lossCutRateSell < candle.getMid().getC() && !isUpper) {

                log.info("<<signal (sell)(losscut)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                client.complete(restTemplate);
                status = Status.NONE;
            }
        }
        return status;
    }

    private Status targetReach(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        double macdMag = 1.5;

        double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION * 12.1;
        if (isInUpperTIme()) {
            mag *= 1.653;
        } else {
            mag *= 0.5;
        }

        boolean isUpperCloudLong = candle.getLongMa() < candle.getMid().getH();
        boolean isUpperCloudShort = candle.getShortMa() > candle.getMid().getH();

        boolean checkVma = candle.getLongVma() * 1.005 < candle.getShortVma();
        boolean checkMacd = candle.getMacd() < candle.getSig() * macdMag;

        double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
        double targetRateSell = openCandle.getMid().getC() * (1 - mag);


        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getMid().getC() && isUpperCloudLong && checkVma && checkMacd) {

                log.info("<<signal (buy)(reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());
                client.complete(restTemplate);
                status = Status.NONE;

            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getMid().getC() && isUpperCloudShort && checkVma && checkMacd) {

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
            log.error(" " + e.getLocalizedMessage());
            return null;
        }
        return i;
    }

    private boolean isNotInRange(Candle candle) {
        final double mag = 1.0004;
        boolean isUpperRange = isUpperRange(candle, mag);
        boolean isLowerRange = isLowerRange(candle, mag);

        return isUpperRange || isLowerRange;
    }

    private boolean isLowerRange(Candle candle, double mag) {
        boolean bb1 = candle.getShortMa() < candle.getLongMa() * mag;
        boolean bb2 = candle.getLongMa() < candle.getSuperLongMa() * mag;
        return bb1 && bb2;
    }

    private boolean isUpperRange(Candle candle, double mag) {
        boolean b1 = candle.getShortMa() * mag > candle.getLongMa();
        boolean b2 = candle.getLongMa() * mag > candle.getSuperLongMa();
        return b1 && b2;
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
