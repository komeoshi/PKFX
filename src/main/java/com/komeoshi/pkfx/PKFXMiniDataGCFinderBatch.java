package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.parameter.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class PKFXMiniDataGCFinderBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCFinderBatch.class);

    public static void main(String[] args) {
        PKFXMiniDataGCFinderBatch batch = new PKFXMiniDataGCFinderBatch();
        batch.execute();
    }

    public void execute() {

        Parameter maxParameter = Parameter.getParameterSim();
        double maxDiff = -999.0;
        while (true) {
            PKFXMiniDataGCFinder finder = new PKFXMiniDataGCFinder();
            finder.setBatch(true);
            finder.setExecuteMaxSize(5000);
            finder.setDefaultParameter(maxParameter);
            finder.setMaxDiffAllTheTime(maxDiff);

            finder.execute();

            double tmpMaxDiff = finder.getMaxDiff();
            if (maxDiff < tmpMaxDiff) {
                maxDiff = tmpMaxDiff;
                maxParameter = finder.getMaxDiffParameter();
            }
            log.info("-- " + maxDiff + " ---------------------------------------------------------");
        }
    }
}


