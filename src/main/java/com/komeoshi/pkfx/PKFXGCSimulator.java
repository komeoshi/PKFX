package com.komeoshi.pkfx;

import com.komeoshi.pkfx.enumerator.Position;
import com.komeoshi.pkfx.enumerator.Reason;
import com.komeoshi.pkfx.enumerator.Status;
import com.komeoshi.pkfx.enumerator.TradeReason;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import com.komeoshi.pkfx.dto.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class PKFXGCSimulator {

    private static final Logger log = LoggerFactory.getLogger(PKFXGCSimulator.class);

    private boolean isLogging = true;

    private int countLosscut = 0;
    private int countReached = 0;
    private int countTimeoutWin = 0;
    private int countTimeoutLose = 0;
    private int countWin = 0;
    private int countLose = 0;
    private int totalCount = 0;
    private double diff = 0.0;

    public static void main(String[] args) {
        SpringApplication.run(PKFXGCSimulator.class, args);
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
                    boolean isVmaOver = candle.getLongVma() > 1;

                    int h = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getHour();
                    int m = candle.getTime().atZone(ZoneId.of("Asia/Tokyo")).getMinute();
                    boolean isDeadTime = h == 6 || h == 17 || h == 18 || h == 20 || h == 21;
                    boolean isDeadMinute = m == 59;

                    boolean checkMacd = candle.getMacd() < 0.040;

                    boolean isRsiHot = candle.getRsi() > 50;
                    boolean isRsiCold = candle.getRsi() <= 50;

                    if (candle.getPosition() == Position.LONG) {
                        // 売り→買い

                        if (status != Status.NONE) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.SHORT);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime && isNotInRange(candle)
                                && checkMacd && !isDeadMinute) {
                            buy(TradeReason.GC, candle);
                            status = Status.HOLDING_BUY;
                            openCandle = candle;
                        }
                    } else if (candle.getPosition() == Position.SHORT) {
                        // 買い→売り

                        if (status != Status.NONE) {
                            completeOrder(openCandle, candle, Reason.TIMEOUT, Position.LONG);
                            status = Status.NONE;
                        }
                        if (isSigOver && isVmaOver && !isDeadTime && isNotInRange(candle)
                                && checkMacd && !isDeadMinute) {
                            sell(TradeReason.DC, candle);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                    }

                    if(status == Status.NONE) {
                        if (isRsiHot) {
                            sell(TradeReason.RSI_HOT, candle);
                            status = Status.HOLDING_SELL;
                            openCandle = candle;
                        }
                        if (isRsiCold) {
                            buy(TradeReason.RSI_COLD, candle);
                            status = Status.HOLDING_BUY;
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

        // RSI超過ならロスカットしやすくなる
        double rsiMag = 5.0;
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
        double macdMag = 1.5;

        double mag = PKFXConst.GC_CANDLE_TARGET_MAGNIFICATION * 12.1;
        if (isInUpperTIme(openCandle)) {
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

    private void buy(TradeReason tradeReason, Candle openCandle) {
        if (isLogging)
            log.info("signal >> " + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " " + tradeReason);
    }

    private void sell(TradeReason tradeReason, Candle openCandle) {
        if (isLogging)
            log.info("signal >> " + openCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " " + tradeReason);
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

        if (isLogging)
            log.info("<< signal " + closeCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " 【" +
                    closeCandle.getNumber() + "】" +
                    openCandle.getMid().getC() + " -> " + closeCandle.getMid().getC() + "(" + thisDiff + "), " +
                    countWin + "/" + countLose + "/" + totalCount + "(" + ((double) countWin / (double) totalCount) + ") " +
                    diff + "(" + (diff / totalCount) + "), " + reason +
                    " LOSSCUT:" + countLosscut + " REACHED:" + countReached + " TIMEOUT:" + countTimeoutWin + "/" + countTimeoutLose
            );

        if (openCandle.getTime().isAfter(LocalDateTime.now().minusMonths(1))) {
            if (Math.abs(thisDiff) > 0.20) {
                log.info(
                        "【" + openCandle.getNumber() + "】 " +
                                openCandle.getTime() + "-" + closeCandle.getTime() + " thisDiff:" + thisDiff +
                                " " + openCandle.getPosition() +
                                " openMacd:" + openCandle.getMacd() + " pastMacd:" + openCandle.getPastCandle().getMacd() +
                                " openShortMa:" + openCandle.getShortMa() + " openLongMa:" + openCandle.getLongMa() + " " +
                                " pastShortMa:" + openCandle.getPastCandle().getShortMa() + " pastLongMa:" + openCandle.getPastCandle().getLongMa() +
                                " pastShortMa-openShortMa:" + Math.abs(openCandle.getShortMa() - openCandle.getPastCandle().getShortMa()) +
                                " pastLongMa-openLongMa:" + Math.abs(openCandle.getLongMa() - openCandle.getPastCandle().getLongMa()) +
                                " pastSuperLongMa-openSuperLongMa:" + Math.abs(openCandle.getSuperLongMa() - openCandle.getPastCandle().getSuperLongMa()) +
                                " "

                );
            }

        }
    }
}


