package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PKFXSimulatorMain {

    private static final Logger log = LoggerFactory.getLogger(PKFXSimulatorMain.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXSimulatorMain.class, args);
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

            // デフォルトパラメータで解析
            PKFXSimulatorAnalyzeParameter analyze1 = new PKFXSimulatorAnalyzeParameter();
            analyze1.run(i);

            // パラメータをいじって解析
            PKFXSimulatorAnalyzeParameter analyze5
                    = new PKFXSimulatorAnalyzeParameter(
                    PKFXConst.CANDLE_LENGTH_MAGNIFICATION,
                    PKFXConst.CANDLE_TARGET_MAGNIFICATION,
                    PKFXConst.WAIT_TIME
            );
            analyze5.run(i);
        };
    }
}
