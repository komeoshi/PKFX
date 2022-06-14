package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Candles;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PKFXMiniDataChecker {

    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataChecker.class);

    public static void main(String[] args) {
        PKFXMiniDataChecker checker = new PKFXMiniDataChecker("minData.dat");
        checker.check();
    }

    private String filename = "";

    public PKFXMiniDataChecker(String filename) {
        this.filename = filename;
    }

    public void check() {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader(filename);
        Candles candles = reader.read();

        Map<String, Integer> map = new HashMap<>();
        for (Candle candle : candles.getCandles()) {
            String time = candle.getTime().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

            Integer count = map.get(time);
            if (count == null) {
                count = 1;

            } else {
                count++;
            }

            map.put(time, count);

        }
        log.info("" + map);
    }
}
