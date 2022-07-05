package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.Parameter;
import com.komeoshi.pkfx.enumerator.ParameterPosition;
import com.komeoshi.pkfx.simulatedata.PKFXParameterDataCreator;
import com.komeoshi.pkfx.simulatedata.PKFXParameterDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
public class PKFXMiniDataGCFinderBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCFinderBatch.class);

    public static void main(String[] args) {
        PKFXMiniDataGCFinderBatch batch = new PKFXMiniDataGCFinderBatch();
        batch.init();
        try {
            batch.execute(ParameterPosition.PARAMETER1);
            batch.execute(ParameterPosition.PARAMETER2);
            batch.execute(ParameterPosition.PARAMETER3);
            batch.execute(ParameterPosition.PARAMETER4);
            batch.execute(ParameterPosition.PARAMETER5);
            batch.execute(ParameterPosition.PARAMETER6);
            batch.execute(ParameterPosition.PARAMETER7);
            batch.execute(ParameterPosition.PARAMETER8);
            batch.execute(ParameterPosition.PARAMETER9);
            batch.execute(ParameterPosition.PARAMETER10);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    Parameter parameter1 = null;
    Parameter parameter2 = null;
    Parameter parameter3 = null;
    Parameter parameter4 = null;
    Parameter parameter5 = null;
    Parameter parameter6 = null;
    Parameter parameter7 = null;
    Parameter parameter8 = null;
    Parameter parameter9 = null;
    Parameter parameter10 = null;

    List<Candle> candles = null;

    public void init() {
        PKFXParameterDataReader reader1 = new PKFXParameterDataReader("parameter1.dat");
        parameter1 = reader1.read();
        PKFXParameterDataReader reader2 = new PKFXParameterDataReader("parameter2.dat");
        parameter2 = reader2.read();
        PKFXParameterDataReader reader3 = new PKFXParameterDataReader("parameter3.dat");
        parameter3 = reader3.read();
        PKFXParameterDataReader reader4 = new PKFXParameterDataReader("parameter4.dat");
        parameter4 = reader4.read();
        PKFXParameterDataReader reader5 = new PKFXParameterDataReader("parameter5.dat");
        parameter5 = reader5.read();
        PKFXParameterDataReader reader6 = new PKFXParameterDataReader("parameter6.dat");
        parameter6 = reader6.read();
        PKFXParameterDataReader reader7 = new PKFXParameterDataReader("parameter7.dat");
        parameter7 = reader7.read();
        PKFXParameterDataReader reader8 = new PKFXParameterDataReader("parameter8.dat");
        parameter8 = reader8.read();
        PKFXParameterDataReader reader9 = new PKFXParameterDataReader("parameter9.dat");
        parameter9 = reader9.read();
        PKFXParameterDataReader reader10 = new PKFXParameterDataReader("parameter10.dat");
        parameter10 = reader10.read();
    }

    public void execute(ParameterPosition position) throws IOException {

        Parameter maxParameter = null;
        String filename = "";
        if (position == ParameterPosition.PARAMETER1) {
            maxParameter = parameter1;
            filename = "parameter1.dat";
        }
        if (position == ParameterPosition.PARAMETER2) {
            maxParameter = parameter2;
            filename = "parameter2.dat";
        }
        if (position == ParameterPosition.PARAMETER3) {
            maxParameter = parameter3;
            filename = "parameter3.dat";
        }
        if (position == ParameterPosition.PARAMETER4) {
            maxParameter = parameter4;
            filename = "parameter4.dat";
        }
        if (position == ParameterPosition.PARAMETER5) {
            maxParameter = parameter5;
            filename = "parameter5.dat";
        }
        if (position == ParameterPosition.PARAMETER6) {
            maxParameter = parameter6;
            filename = "parameter6.dat";
        }
        if (position == ParameterPosition.PARAMETER7) {
            maxParameter = parameter7;
            filename = "parameter7.dat";
        }
        if (position == ParameterPosition.PARAMETER8) {
            maxParameter = parameter8;
            filename = "parameter8.dat";
        }
        if (position == ParameterPosition.PARAMETER9) {
            maxParameter = parameter9;
            filename = "parameter9.dat";
        }
        if (position == ParameterPosition.PARAMETER10) {
            maxParameter = parameter10;
            filename = "parameter10.dat";
        }



        double maxDiff = -999.0;
        int loopCount = 0;
        for (int ii = 0; ii < 10; ii++) {
            loopCount++;

            PKFXMiniDataGCFinder finder = new PKFXMiniDataGCFinder();
            finder.setParameter1(parameter1);
            finder.setParameter2(parameter2);
            finder.setParameter3(parameter3);
            finder.setParameter4(parameter4);
            finder.setParameter5(parameter5);
            finder.setParameter6(parameter6);
            finder.setParameter7(parameter7);
            finder.setParameter8(parameter8);
            finder.setParameter9(parameter9);
            finder.setParameter10(parameter10);

            finder.setParameterPosition(position);
            finder.setBatch(true);
            finder.setExecuteMaxSize(500);
            finder.setDefaultParameter(maxParameter);
            finder.setMaxDiffAllTheTime(maxDiff);
            finder.setLoopCount(loopCount);
            finder.setCandles(candles);

            finder.execute();

            double tmpMaxDiff = finder.getMaxDiff();
            if (maxDiff < tmpMaxDiff) {
                maxDiff = tmpMaxDiff;
                maxParameter = finder.getMaxDiffParameter();
            }
            log.info("-- " + maxDiff + " ---------------------------------------------------------");

            this.candles = finder.getCandles();
        }

        if (position == ParameterPosition.PARAMETER1)
            parameter1 = maxParameter;
        if (position == ParameterPosition.PARAMETER2)
            parameter2 = maxParameter;
        if (position == ParameterPosition.PARAMETER3)
            parameter3 = maxParameter;
        if (position == ParameterPosition.PARAMETER4)
            parameter4 = maxParameter;
        if (position == ParameterPosition.PARAMETER5)
            parameter5 = maxParameter;
        if (position == ParameterPosition.PARAMETER6)
            parameter6 = maxParameter;
        if (position == ParameterPosition.PARAMETER7)
            parameter7 = maxParameter;
        if (position == ParameterPosition.PARAMETER8)
            parameter8 = maxParameter;
        if (position == ParameterPosition.PARAMETER9)
            parameter9 = maxParameter;
        if (position == ParameterPosition.PARAMETER10)
            parameter10 = maxParameter;

        PKFXParameterDataCreator creator = new PKFXParameterDataCreator();
        creator.output(filename, maxParameter);
    }
}


