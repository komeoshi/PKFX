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

import java.time.ZoneId;
import java.util.ArrayList;
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
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {

            List<Candle> candles = getCandles();

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            for (Candle candle : candles) {
                if (candle.getPosition() == Position.NONE) {
                    continue;
                }

                if (candle.getPosition() != lastPosition) {
                    boolean isSigOver = candle.getShortSig() > PKFXConst.GC_SIG_MAGNIFICATION;
                    boolean isVmaOver = candle.getLongVma() > 0.1;

                    int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
                    boolean isDeadTime = h==6 || h==17 || h==18 || h==20 || h==21;

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い

                        if (status == Status.HOLDING_SELL) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.SHORT);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime) {
                            buy();
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }
                    } else if (candle.getPosition() == Position.SHORT) {
                        // 買い→売り

                        if (status == Status.HOLDING_BUY) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.LONG);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime) {
                            sell();
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

    private List<Candle> getCandles() {
        PKFXSimulateDataReader reader201201 = new PKFXSimulateDataReader("data.dat");
        List<Candle> candles201201 = reader201201.read().getCandles();

        return new ArrayList<>(candles201201);
    }

    private Status losscut(Status status, Candle openCandle, Candle candle) {
        boolean isLower = candle.getPastCandle().getLongMa() > candle.getLongMa();
        double lossCutMag;
        if (isLower) {
            lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION / 0.20;
        } else {
            lossCutMag = PKFXConst.GC_LOSSCUT_MAGNIFICATION;
        }

        if (!isInUpperTIme(candle)) {
            lossCutMag *= 1.45;
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
        double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION * 10.86;
        if (isInUpperTIme(candle)) {
            mag *= 1.5;
        } else {
            mag *= 0.5;
        }
        double targetRateBuy = openCandle.getMid().getC() * (1 + mag);
        double targetRateSell = openCandle.getMid().getC() * (1 - mag);

        boolean isUpperCloudLong = candle.getLongMa() < candle.getMid().getH();
        boolean isUpperCloudShort = candle.getShortMa() > candle.getMid().getH();

        boolean checkVma = candle.getLongVma() * 1.005 < candle.getShortVma();
        boolean checkMacd = candle.getMacd() < candle.getSig() * 1.5;

        if (status == Status.HOLDING_BUY) {
            if (targetRateBuy < candle.getMid().getC() && isUpperCloudLong && checkVma && checkMacd) {

                completeOrder(openCandle, candle, Reason.REACHED, Position.LONG);
                status = Status.NONE;
            }
        } else if (status == Status.HOLDING_SELL) {
            if (targetRateSell > candle.getMid().getC() && isUpperCloudShort && checkMacd) {

                completeOrder(openCandle, candle, Reason.REACHED, Position.SHORT);
                status = Status.NONE;
            }
        }
        return status;
    }

    private boolean isInUpperTIme(Candle candle) {
        int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
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

    private void buy() {
    }

    private void sell() {
    }


    private void completeOrder(Candle openCandle, Candle closeCandle, Reason reason, Position position) {

        if (position == Position.LONG) {
            if (openCandle.getMid().getC() < closeCandle.getMid().getC()) {
                countWin++;
            } else {
                countLose++;
            }
        } else {
            if (openCandle.getMid().getC() > closeCandle.getMid().getC()) {
                countWin++;
            } else {
                countLose++;
            }
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
    }
}


