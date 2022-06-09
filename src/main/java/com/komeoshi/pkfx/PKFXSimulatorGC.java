package com.komeoshi.pkfx;

import com.komeoshi.pkfx.SimulateData.PKFXSimulateDataReader;
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

    private int countLosscut = 0;
    private int countReached = 0;
    private int countTimeoutWin = 0;
    private int countTimeoutLose = 0;
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

            PKFXSimulateDataReader reader = new PKFXSimulateDataReader();
            List<Candle> candles = reader.read().getCandles();

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            for (Candle candle : candles) {
                if (candle.getPosition() == Position.NONE) {
                    continue;
                }

                if (candle.getPosition() != lastPosition) {
                    boolean isSigOver = candle.getSig() > PKFXConst.GC_SIG_MAGNIFICATION;

                    int h = candle.getTime().getHour();
                    boolean isActiveTime = h != 6;

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い

                        if (status == Status.HOLDING_SELL) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.SHORT);
                            status = Status.NONE;
                        }
                        if (isSigOver && isActiveTime) {
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
                        if (isSigOver && isActiveTime) {
                            sell(candle);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }
                }
                lastPosition = candle.getPosition();

                if (status != Status.NONE) {
                    status = targetReach(status, openCandle, candle);
                    status = losscut(status, openCandle, candle);
                }
            }

            log.info(countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                    diff + ", " +
                    "LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
            );
            System.exit(0);
        };
    }

    private Status losscut(Status status, Candle openCandle, Candle candle) {
        boolean isLower = candle.getPastCandle().getLongMa() > candle.getLongMa();
        double lossCutMag;
        if (isLower) {
            lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION / 0.6;
        } else {
            lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION;
        }

        double lossCutRateBuy = openCandle.getMid().getC() * (1 - lossCutMag);
        double lossCutRateSell = openCandle.getMid().getC() * (1 + lossCutMag);

        boolean isUpper = candle.getPastCandle().getLongMa() < candle.getLongMa();

        if (status == Status.HOLDING_BUY) {
            if (lossCutRateBuy > candle.getMid().getC() && isUpper) {

                completeOrder(openCandle, candle, Reason.LOSSCUT, Position.LONG);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (lossCutRateSell < candle.getMid().getC() && !isUpper) {

                completeOrder(openCandle, candle, Reason.LOSSCUT, Position.SHORT);
                status = Status.NONE;
            }
        }
        return status;
    }

    private Status targetReach(Status status, Candle openCandle, Candle candle) {
        double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION * 10.85;
        double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
        double targetRateSell = openCandle.getMid().getC() * (1 - mag);

        boolean isUpperCloudLong = candle.getLongMa() < candle.getMid().getH();
        boolean isUpperCloudShort = candle.getShortMa() > candle.getMid().getH();

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getMid().getC() && isUpperCloudLong) {

                completeOrder(openCandle, candle, Reason.REACHED, Position.LONG);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getMid().getC() && isUpperCloudShort) {

                completeOrder(openCandle, candle, Reason.REACHED, Position.SHORT);
                status = Status.NONE;
            }
        }
        return status;
    }

    private void buy(Candle candle) {
//        log.info("signal (buy )>> " + candle.getTime() + " 【" +
//                candle.getNumber() + "】");
    }

    private void sell(Candle candle) {
//        log.info("signal (sell)>> " + candle.getTime() + " 【" +
//                candle.getNumber() + "】");
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

        switch (reason) {
            case LOSSCUT:
                countLosscut++;
                break;
            case REACHED:
                countReached++;
                break;
            case TIMEOUT:
                if (position == Position.LONG) {
                    if (openCandle.getMid().getC() < closeCandle.getMid().getC()) {
                        countTimeoutWin++;
                    } else {
                        countTimeoutLose++;
                    }
                } else {
                    if (openCandle.getMid().getC() > closeCandle.getMid().getC()) {
                        countTimeoutWin++;
                    } else {
                        countTimeoutLose++;
                    }
                }
                break;
        }

//        log.info("<< signal " + mark + closeCandle.getTime() + " 【" +
//                closeCandle.getNumber() + "】" +
//                openCandle.getMid().getC() + " -> " + closeCandle.getMid().getC() + "(" + thisDiff + "), " +
//                countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
//                diff + "(" + (diff / totalCount) + "), " + reason +
//                " LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
//        );

    }

}


