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

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {

            PKFXFinderRestClient client = new PKFXFinderRestClient();

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            int continueCount = 0;
            final int CONTINUE_MAX = 2;
            Revenge revenge = new Revenge();
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

                    boolean checkLongAbs = Math.abs(longCandle.getAsk().getC() - longCandle.getPastCandle().getAsk().getC())
                            > 0.0685;
                    boolean checkSpread = candle.getSpreadMa() < 0.038;
                    int h = LocalDateTime.now().getHour();
                    boolean checkTime = h != 1 && h != 3 && h != 5 &&
                            h != 10 && h != 17 && h != 19 && h != 21 && h != 22;
                    int m = LocalDateTime.now().getMinute();
                    boolean checkMin = m != 59;
                    boolean hasLongCandle = hasLongCandle(longCandle);
                    boolean hasShortCandle = hasShortCandle(longCandle);

                    log.info("crossed." +
                            " spread:" + candle.getSpreadMa() +
                            " longAbs:" + Math.abs(longCandle.getAsk().getC() - longCandle.getPastCandle().getAsk().getC())
                    );

                    if (candle.getEmaPosition() == Position.LONG) {
                        // 売り→買い
                        log.info("doTrade= " +
                                checkLongAbs +
                                " " + !hasLongCandle +
                                " " + !hasShortCandle +
                                " " + (longCandle.getAsk().getL() > longCandle.getLongMa())
                        );
                        boolean doTrade =
                                revenge.isRevenge() || (
                                        checkLongAbs
                                                && checkTime
                                                && checkMin
                                                && checkSpread
                                                && !hasLongCandle
                                                && !hasShortCandle
                                                && longCandle.getAsk().getL() > longCandle.getLongMa()
                                );
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
                            if (revenge.isRevenge()) {
                                log.info("revenge. 【" + openCandle.getNumber() + "】");
                            }
                            log.info("signal (GC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.buy(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                            revenge.setRevenge(false);
                        }

                    } else if (candle.getEmaPosition() == Position.SHORT) {
                        // 買い→売り
                        log.info("doTrade= " +
                                checkLongAbs +
                                " " + !hasLongCandle +
                                " " + !hasShortCandle +
                                " " + (longCandle.getAsk().getH() < longCandle.getLongMa())
                        );
                        boolean doTrade =
                                revenge.isRevenge() || (
                                        checkLongAbs
                                                && checkTime
                                                && checkMin
                                                && checkSpread
                                                && !hasLongCandle
                                                && !hasShortCandle
                                                && longCandle.getAsk().getH() < longCandle.getLongMa()
                                );

                        if (status != Status.NONE) {
                            if (candle.getAsk().getC() < openCandle.getAsk().getC() &&
                                    !doTrade) {
                                // ﾏｹﾃﾙ
                                continueCount++;
                                log.info("continue. 【" + openCandle.getNumber() + "】" + continueCount);

                            } else if (candle.getAsk().getC() > openCandle.getAsk().getC() ||
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
                            if (revenge.isRevenge()) {
                                log.info("revenge. 【" + openCandle.getNumber() + "】");
                            }
                            log.info("signal (DC) >> " + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                            client.sell(candle.getAsk().getH(), restTemplate);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                            revenge.setRevenge(false);
                        }

                    }
                }
                lastPosition = candle.getEmaPosition();

                if (status != Status.NONE) {
                    status = targetReach(restTemplate, client, status, openCandle, candle);
                    status = losscut(restTemplate, client, status, openCandle, candle, continueCount, revenge);
                    if (status == Status.NONE) {
                        continueCount = 0;
                    }
                }
            }
        };

    }

    private Status losscut(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle, int continueCount,
                           Revenge revenge) {
        // 小さくするとロスカットしやすくなる
        double lossCutMag = 0.000540;
        if (continueCount > 0) {
            // コンテニューがある場合、ロスカットしやすくなる
            lossCutMag *= (continueCount * 0.97);
        }

        if (Math.abs(candle.getMacd()) > 0.011) {
            // ロスカットしやすくなる
            lossCutMag *= 0.98;
        }

        double lossCutRateBuy = openCandle.getAsk().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getAsk().getC() * (1 + lossCutMag);

        if (status == Status.HOLDING_BUY) {
            if (lossCutRateBuy > candle.getAsk().getC()) {

                log.info("<<signal (buy_losscut)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
                revenge.setRevenge(true);
            }
        } else if (status == Status.HOLDING_SELL) {
            if (lossCutRateSell < candle.getAsk().getC()) {

                log.info("<<signal (sell_losscut)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;
                revenge.setRevenge(true);
            }
        }
        return status;
    }

    private Status targetReach(RestTemplate restTemplate, PKFXFinderRestClient client, Status status, Candle openCandle, Candle candle) {
        double mag = 0.000037;

        double targetRateBuy = (openCandle.getAsk().getC() + 0.0041) * (1 + mag);
        double targetRateSell = (openCandle.getAsk().getC() - 0.0041) * (1 - mag);

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getAsk().getC()) {
                if (Math.abs(candle.getMacd()) > 0.011 ||
                        Math.abs(candle.getShortSig()) > 0.010) {
                    // ｵｶﾜﾘ
                    log.info("okawari.【" + openCandle.getNumber() + "】");
                    return status;
                }

                log.info("<<signal (buy_reached)" + candle.getTime() + ", OPEN:" + candle.getAsk().getO() + ", HIGH:" + candle.getAsk().getH());
                client.complete(restTemplate);
                status = Status.NONE;

            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getAsk().getC()) {
                if (Math.abs(candle.getMacd()) > 0.011 ||
                        Math.abs(candle.getShortSig()) > 0.010) {
                    // ｵｶﾜﾘ
                    log.info("okawari.【" + openCandle.getNumber() + "】");
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
