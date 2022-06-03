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
            Instrument i = client.run(restTemplate);
            List<Candle> candles = i.getCandles();

            setPosition(candles);

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            boolean initialBuy = false;
            Candle openCandle = null;
            for (int ii = 0; ii < candles.size(); ii++) {
                Candle candle = candles.get(ii);

                if (!initialBuy) {
                    if (candle.getPosition() == Position.LONG) {
                        initialBuy = true;
                    } else {
                        continue;
                    }
                }

                if (candle.getPosition() != lastPosition) {
                    if (candle.getPosition() == Position.LONG) {
                        buy(candle);
                        status = Status.HOLDING;
                        lastPosition = candle.getPosition();

                        openCandle = candle;
                    } else if (candle.getPosition() == Position.SHORT && status == Status.HOLDING) {
                        sell(openCandle, candle, Reason.TIMEOUT);
                        status = Status.NONE;
                        lastPosition = candle.getPosition();
                    }
                }

                if(status == Status.HOLDING && openCandle.getMid().getC() * 1.00015 < candle.getMid().getC()){
                    sell(openCandle, candle, Reason.REACHED);

                    status = Status.NONE;
                    lastPosition = Position.SHORT;
                }




            }

        };
    }

    private void buy(Candle candle) {

        log.info("signal >> " + candle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " 【" +
                candle.getNumber() + "】");
    }

    private void sell(Candle openCandle, Candle closeCandle, Reason reason) {

        if (openCandle.getMid().getC() < closeCandle.getMid().getC()) {
            countWin++;
            totalCount++;
        } else {
            countLose++;
            totalCount++;
        }

        diff += (closeCandle.getMid().getC() - openCandle.getMid().getC());

        log.info("<< signal " + closeCandle.getTime().atZone(ZoneId.of("Asia/Tokyo")) + " 【" +
                closeCandle.getNumber() + "】" + openCandle.getMid().getC() + " -> " + closeCandle.getMid().getC() + ", " +
                countWin + "/" + countLose + "/" + totalCount + ", " +
                Math.abs(diff) + ", " + reason
        );

    }

    private int countWin = 0;
    private int countLose = 0;
    private int totalCount = 0;

    private double rate = (double) countWin / totalCount;
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

            if (shortMa > longMa) {
                currentCandle.setPosition(Position.LONG);
            } else {
                currentCandle.setPosition(Position.SHORT);
            }
        }
    }
}


