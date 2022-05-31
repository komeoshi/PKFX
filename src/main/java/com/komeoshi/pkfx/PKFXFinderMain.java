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

import java.time.LocalDateTime;
import java.time.ZoneId;

@SpringBootApplication
public class PKFXFinderMain {

    private static final Logger log = LoggerFactory.getLogger(PKFXFinderMain.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXFinderMain.class, args);
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
            double openRate = 0.0;
            LocalDateTime openTime = LocalDateTime.now();
            while (true) {
                sleep();

                Instrument i;
                try {
                    i = client.getInstrument(restTemplate);
                } catch (RestClientException e) {
                    log.error("", e);
                    continue;
                }

                Candle c = i.getCandles().get(0);

                if (status == Status.NONE) {
                    PKFXFinderAnalyzer finder = new PKFXFinderAnalyzer(c);
                    if (finder.isSignal(PKFXConst.CANDLE_LENGTH_MAGNIFICATION) && (!openTime.isEqual(c.getTime()))) {
                        log.info("signal>> " + c.getTime() + ", " + c.getMid().getO() + ", " + c.getMid().getH());

                        // シグナル点灯したので買う.
                        client.buy(restTemplate);
                        status = Status.HOLDING;
                        openRate = c.getMid().getO();
                        openTime = c.getTime();
                    }

                } else {
                    LocalDateTime targetTime = openTime.plusMinutes(PKFXConst.WAIT_TIME);
                    boolean isTargetReached = openRate * PKFXConst.CANDLE_TARGET_MAGNIFICATION < c.getMid().getH();
                    boolean isTimeReached = LocalDateTime.now(ZoneId.of("America/New_York")).isAfter(targetTime);
                    if (isTargetReached || isTimeReached) {
                        log.info("signal<< " + c.getTime() + ", " + openRate + ", " + c.getMid().getH() + ", "
                                + (c.getMid().getH() - openRate) + ", "
                                + isTargetReached + ", " + isTimeReached);

                        // シグナル終了したので売る.
                        client.sell(restTemplate);
                        status = Status.NONE;
                    }
                }
            }
        };

    }

    private void sleep() throws InterruptedException {
        Thread.sleep(PKFXConst.SLEEP_INTERVAL);
    }
}
