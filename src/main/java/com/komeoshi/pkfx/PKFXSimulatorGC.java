package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class PKFXSimulatorGC {

    private static final Logger log = LoggerFactory.getLogger(PKFXSimulatorGC.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXSimulatorGC.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();

            List<Candle> candles = client.runWithManyCandles(restTemplate);
            // List<Candle> candles = client.run(restTemplate).getCandles();

            setPosition(candles);

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            for (Candle candle : candles) {
                if (candle.getPosition() == Position.NONE) {
                    continue;
                }

                if (candle.getPosition() != lastPosition) {
                    // クロスした

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い
                        if (status == Status.HOLDING_SELL) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.SHORT);
                            status = Status.NONE;
                        }
                        if (candle.getMid().getH() > candle.getShortMa() && candle.getVolume() > candle.getShortVma()) {
                            buy(candle);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }
                    } else if (candle.getPosition() == Position.SHORT) {
                        // 買い→売り
                        if (status == Status.HOLDING_BUY) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.LONG);
                            status = Status.NONE;
                        }
                        if (candle.getMid().getL() < candle.getShortMa() && candle.getVolume() > candle.getShortVma()) {
                            sell(candle);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }
                }
                lastPosition = candle.getPosition();

                if(status != Status.NONE) {
                    double mag = 0.00007;
                    double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
                    double targetRateSell = openCandle.getMid().getC() * (1 - mag);
                    if (status == Status.HOLDING_BUY &&
                            targetRateBuy < candle.getMid().getC()) {

                        completeOrder(openCandle, candle, Reason.REACHED, Position.LONG);
                        status = Status.NONE;
                    } else if (status == Status.HOLDING_SELL &&
                            targetRateSell > candle.getMid().getC()) {

                        completeOrder(openCandle, candle, Reason.REACHED, Position.SHORT);
                        status = Status.NONE;
                    }
                }
            }
        };
    }

    private void buy(Candle candle) {
        log.info("signal (buy )>> " + candle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " 【" +
                candle.getNumber() + "】");
    }

    private void sell(Candle candle) {
        log.info("signal (sell)>> " + candle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " 【" +
                candle.getNumber() + "】");
    }


    private void completeOrder(Candle openCandle, Candle closeCandle, Reason reason, Position position) {

        String mark;
        if (position == Position.LONG) {
            if (openCandle.getMid().getC() < closeCandle.getMid().getC()) {
                countWin++;
            } else {
                countLose++;
            }
            mark = "(buy )";
        } else {
            if (openCandle.getMid().getC() > closeCandle.getMid().getC()) {
                countWin++;
            } else {
                countLose++;
            }
            mark = "(sell)";
        }
        totalCount++;
        double thisDiff;
        if (position == Position.LONG) {
            thisDiff = (closeCandle.getMid().getC() - openCandle.getMid().getC());
        } else {
            thisDiff = (openCandle.getMid().getC() - closeCandle.getMid().getC());
        }
        diff += thisDiff;

        log.info("<< signal " + mark + closeCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " 【" +
                closeCandle.getNumber() + "】" +
                openCandle.getMid().getC() + " -> " + closeCandle.getMid().getC() + "(" + thisDiff + "), " +
                countWin + "/" + countLose + "/" + totalCount + ", " +
                diff + ", " + reason
        );

    }

    private int countWin = 0;
    private int countLose = 0;
    private int totalCount = 0;

    private double diff = 0.0;

    private void setPosition(List<Candle> candles) {
        for (int ii = 0; ii < candles.size(); ii++) {
            Candle currentCandle = candles.get(ii);
            currentCandle.setNumber(ii);
            if (ii < 75) {
                continue;
            }

            List<Candle> currentCandles = new ArrayList<>();

            for (int jj = 0; jj < ii; jj++) {
                currentCandles.add(candles.get(jj));
            }
            PKFXFinderAnalyzer finder = new PKFXFinderAnalyzer(currentCandle);
            double shortMa = finder.getMa(currentCandles, 9);
            double longMa = finder.getMa(currentCandles, 26);
            double superLongMa = finder.getMa(currentCandles, 50);

            double shortVma = finder.getVma(currentCandles, 25);
            double longVma = finder.getVma(currentCandles, 50);

            currentCandle.setShortMa(shortMa);
            currentCandle.setLongMa(longMa);
            currentCandle.setSuperLongMa(superLongMa);

            currentCandle.setShortVma(shortVma);
            currentCandle.setLongVma(longVma);

            if (shortMa > longMa) {
                currentCandle.setPosition(Position.LONG);
            } else {
                currentCandle.setPosition(Position.SHORT);
            }
            log.info(ii + " " + currentCandle.getTime() + " " + currentCandle.getPosition());
        }
    }
}


