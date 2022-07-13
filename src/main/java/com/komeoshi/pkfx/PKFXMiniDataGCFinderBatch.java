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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@Setter
public class PKFXMiniDataGCFinderBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCFinderBatch.class);

    private static final LocalDate initialFrom = LocalDate.of(2004, 1, 1);
    private static final LocalDate lastDate = LocalDate.of(2022, 1, 1);

    private static final int LENGTH_YEAR = 15;

    public static void main(String[] args) {

        Map<String, Double> resultSummaryMap = new TreeMap<>();
        LocalDate from = initialFrom;
        LocalDate to = from.plusYears(LENGTH_YEAR);
        while (true) {

            PKFXMiniDataGCFinderBatch.prepareFile(from, to);
            PKFXMiniDataGCFinderBatch batch = new PKFXMiniDataGCFinderBatch();
            batch.init();
            try {
                List<ParameterPosition> list = new ArrayList<>();
                list.add(ParameterPosition.PARAMETER1);
                list.add(ParameterPosition.PARAMETER2);
                list.add(ParameterPosition.PARAMETER3);
                list.add(ParameterPosition.PARAMETER4);
                list.add(ParameterPosition.PARAMETER5);
                list.add(ParameterPosition.PARAMETER6);
                list.add(ParameterPosition.PARAMETER7);
                list.add(ParameterPosition.PARAMETER8);
                list.add(ParameterPosition.PARAMETER9);
                list.add(ParameterPosition.PARAMETER10);
                Collections.shuffle(list);

                for (ParameterPosition position : list)
                    batch.execute(position, from, to);

                resultSummaryMap.put(LocalDateTime.now() + " " + from + " - " + to, batch.getMaxDiff());

            } catch (IOException e) {
                log.error("", e);
                break;
            }

            if (from.isAfter(lastDate.minusYears(LENGTH_YEAR).minusDays(1))) {
                from = initialFrom;
            } else {
                from = from.plusYears(1);
            }
            to = from.plusYears(LENGTH_YEAR);

            for (Map.Entry<String, Double> entry : resultSummaryMap.entrySet()) {
                log.info("result summary:" + entry.getKey() + ":" + entry.getValue());
            }
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
    double maxDiff = -999.0;

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

    public void execute(ParameterPosition position, LocalDate from, LocalDate to) throws IOException {

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
        for (int ii = 0; ii < 3; ii++) {
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
            finder.setExecuteMaxSize(250);
            finder.setDefaultParameter(maxParameter);
            finder.setMaxDiffAllTheTime(maxDiff);
            finder.setLoopCount(loopCount);
            finder.setCandles(candles);
            finder.setFrom(from);
            finder.setTo(to);

            finder.execute();

            double tmpMaxDiff = finder.getMaxDiff();
            if (maxDiff < tmpMaxDiff) {
                maxDiff = tmpMaxDiff;
                maxParameter = finder.getMaxDiffParameter();
            }
            log.info("-- " + maxDiff + " ---------------------------------------------------------");
            log.info("summary / year");
            Map<String, Double> summaryMap = finder.getSummaryMap();
            for (Map.Entry<String, Double> entry : summaryMap.entrySet())
                log.info(entry.getKey() + ":" + entry.getValue());

            this.candles = finder.getCandles();

            PKFXParameterDataCreator creator = new PKFXParameterDataCreator();
            creator.output(filename, maxParameter);
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

        this.maxDiff = maxDiff;
    }

    public static void prepareFile(LocalDate from, LocalDate to) {

        log.info("moving datafiles. " + from + " - " + to);

        try {
            File fromDir = new File("data/mindata");
            File[] files = fromDir.listFiles();
            for (File file : Objects.requireNonNull(files)) {
                String toName = "data/mindatabak/" + file.getName();
                Files.move(file.toPath(), new File(toName).toPath());
            }

            while (!from.isEqual(to)) {
                String date = from.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String filename = "dataMins_" + date + ".dat";
                Files.move(new File("data/mindatabak/" + filename).toPath(), new File("data/mindata/" + filename).toPath());

                from = from.plusDays(1);
            }

        } catch (IOException e) {
            log.error("", e);
            System.exit(1);
        }
    }
}


