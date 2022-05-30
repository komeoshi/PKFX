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
public class PKFXMain {

    private static final Logger log = LoggerFactory.getLogger(PKFXMain.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXMain.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            PKFXRestClient client = new PKFXRestClient();
            Instrument i = client.run(restTemplate);

            // デフォルトパラメータで解析
            PKFXAnalyzeParameter analyze1 = new PKFXAnalyzeParameter();
            analyze1.run(i);

            // パラメータをいじって解析
            PKFXAnalyzeParameter analyze2
                    = new PKFXAnalyzeParameter(
                    1.0005,
                    1.0006,
                    5
            );
            analyze2.run(i);

        };
    }
}
