package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class PKFXUnitCalculator {

    private static final Logger log = LoggerFactory.getLogger(PKFXUnitCalculator.class);


    private static final int MAG = 25;
    private static final int MAX = 250_000;

    private static final double THRESHOLD = 0.95;

    public int calculate(double currentPrice, RestTemplate restTemplate) {
        PKFXFinderRestClient client = new PKFXFinderRestClient();
        Account account = client.getAccount(restTemplate);

        return calculate(currentPrice, account.getNav());
    }

    /**
     * 取引可能数量を計算.
     *
     * @param currentPrice 　現在値
     * @param currency     　証拠金
     * @return 取引可能数量
     */
    private int calculate(double currentPrice, double currency) {
        // 必要証拠金/ 単位
        double unitCurrency = currentPrice / MAG;
        double unit = currency / unitCurrency;

        int result = Double.valueOf(unit * THRESHOLD).intValue();
        if (result > MAX)
            result = MAX;
        return result;
    }
}
