package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.okawari.OkawariParameter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Getter
@Setter
public class PKFXMiniDataGCOkawariFinderBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCOkawariFinderBatch.class);

    public static void main(String[] args) {
        PKFXMiniDataGCOkawariFinderBatch batch = new PKFXMiniDataGCOkawariFinderBatch();
        batch.execute();
    }

    public void execute() {

        OkawariParameter maxParameter = OkawariParameter.getOkawariParameter();
        double maxDiff = -999.0;
        List<Candle> candles = null;
        while (true) {
            PKFXMiniDataGCOkawariFinder finder = new PKFXMiniDataGCOkawariFinder();
            finder.setBatch(true);
            finder.setExecuteMaxSize(150);
            finder.setDefaultParameter(maxParameter);
            finder.setMaxDiffAllTheTime(maxDiff);
            finder.setCandles(candles);

            finder.execute();

            candles = finder.getCandles();

            double tmpMaxDiff = finder.getMaxDiff();
            if (maxDiff < tmpMaxDiff) {
                maxDiff = tmpMaxDiff;
                maxParameter = finder.getMaxDiffParameter();
            }
            log.info("-- " + maxDiff + " ---------------------------------------------------------");
        }
    }
}


