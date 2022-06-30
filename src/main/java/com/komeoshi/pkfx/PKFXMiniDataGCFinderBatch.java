package com.komeoshi.pkfx;

import com.google.common.collect.Lists;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.*;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

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
            maxParameter = finder.getMaxDiffParameter();
            double tmpMaxDiff = finder.getMaxDiff();
            if (maxDiff < tmpMaxDiff)
                maxDiff = tmpMaxDiff;


            log.info("-- " + maxDiff + " ---------------------------------------------------------");
        }

    }

}


