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

import java.util.List;

@SpringBootApplication
public class PKFXSimulatorGC {

    private static final Logger log = LoggerFactory.getLogger(PKFXSimulatorGC.class);

    private int countWin = 0;
    private int countLose = 0;
    private int totalCount = 0;
    private double diff = 0.0;

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
            long startTime = System.currentTimeMillis();
            PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
            List<Candle> candles = client.runWithManyCandles(restTemplate);
            // List<Candle> candles = client.run(restTemplate).getCandles();

            new PKFXFinderAnalyzer().setPosition(candles, true);

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            for (Candle candle : candles) {
                if (candle.getPosition() == Position.NONE) {
                    continue;
                }

                if (candle.getPosition() != lastPosition) {
                    boolean isShortVmaLargerThanLongVma = candle.getShortVma() > candle.getLongVma();
                    log.info("cross detected. " + candle.getPosition() + " vma:" + candle.getShortVma() + "/" + candle.getLongVma() + " " + isShortVmaLargerThanLongVma);

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い
                        if (status == Status.HOLDING_SELL) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.SHORT);
                            status = Status.NONE;
                        }
                        if (isShortVmaLargerThanLongVma) {
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
                        if (isShortVmaLargerThanLongVma) {
                            sell(candle);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }
                }
                lastPosition = candle.getPosition();

                if(status != Status.NONE) {
                    double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION;
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

            long endTime = System.currentTimeMillis();
            log.info((endTime - startTime) +"ms.");
        };
    }

    private void buy(Candle candle) {
        log.info("signal (buy )>> " + candle.getTime() + " 【" +
                candle.getNumber() + "】");
    }

    private void sell(Candle candle) {
        log.info("signal (sell)>> " + candle.getTime() + " 【" +
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

        log.info("<< signal " + mark + closeCandle.getTime() + " 【" +
                closeCandle.getNumber() + "】" +
                openCandle.getMid().getC() + " -> " + closeCandle.getMid().getC() + "(" + thisDiff + "), " +
                countWin + "/" + countLose + "/" + totalCount + "(" +((double)countWin/(double)totalCount) + ") " +
                diff + "("+(diff/totalCount)+"), " + reason
        );

    }

}


