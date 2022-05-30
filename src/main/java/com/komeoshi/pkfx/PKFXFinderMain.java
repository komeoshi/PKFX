package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import org.apache.tomcat.jni.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            LocalDateTime openTime = null;
            while (true) {
                Thread.sleep(1000);

                Instrument i = client.getInstrument(restTemplate);

                Candle c = i.getCandles().get(0);

                if (status == Status.NONE) {
                    PKFXFinderAnalyzer finder = new PKFXFinderAnalyzer(c);
                    if (finder.isSignal(1.0002)) {
                        log.info("signal>> " + c.getTime() + ", " + c.getMid().getO() + ", " + c.getMid().getH());

                        // シグナル点灯したので買う.
                        client.buy();
                        status = Status.HOLDING;
                        openRate = c.getMid().getO();
                        openTime = c.getTime();
                    }

                } else {

                    boolean isTargetReached = openRate * 1.0003 < c.getMid().getH();

                    if(isTargetReached || openTime.plusMinutes(5).isAfter(LocalDateTime.now())) {
                        log.info("signal<< " + c.getTime() + ", " + c.getMid().getO() + ", " + c.getMid().getH());

                        // シグナル終了したので売る.
                        client.sell();
                        status = Status.NONE;
                    }
                }
            }

        };

    }
}
