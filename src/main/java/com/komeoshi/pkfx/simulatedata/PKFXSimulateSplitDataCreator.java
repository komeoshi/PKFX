package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.PKFXAnalyzer;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Candles;
import com.komeoshi.pkfx.restclient.PKFXSimulatorRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.List;

public class PKFXSimulateSplitDataCreator {

    private static final Logger log = LoggerFactory.getLogger(PKFXSimulateSplitDataCreator.class);

    public static void main(String[] args) {
        try {
            new PKFXSimulateSplitDataCreator().saveDaysData();
            new PKFXSimulateSplitDataCreator().saveMinsData();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private LocalDate from = LocalDate.of(2020, 1, 1);
    private LocalDate to = LocalDate.of(2022, 7, 1);

    public void saveDaysData() throws IOException {

        do {
            LocalDate tmp = from.plusDays(1);
            Candles candles = getDaysCandles(from, tmp);

            String filename = "data/data_" + from.getYear() + String.format("%02d", from.getMonthValue()) + String.format("%02d", from.getDayOfMonth()) + ".dat";
            output(candles, filename);

            from = from.plusDays(1);

        } while (!from.isAfter(to));
    }

    private Candles getDaysCandles(LocalDate from, LocalDate to) {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> cs = client.runWithManyCandles(new RestTemplate(), from, to);
        new PKFXAnalyzer().setPosition(cs, true);

        Candles candles = new Candles();
        candles.setCandles(cs);
        return candles;
    }


    public void saveMinsData() throws IOException {

        do {
            LocalDate tmp = from.plusDays(1);
            Candles candles = getMinsCandles(from, tmp);

            String filename = "data/dataMins_" + from.getYear() + String.format("%02d", from.getMonthValue()) + String.format("%02d", from.getDayOfMonth()) + ".dat";
            output(candles, filename);

            from = from.plusDays(1);

        } while (!from.isAfter(to));
    }

    private Candles getMinsCandles(LocalDate from, LocalDate to) {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> cs = client.runMins(new RestTemplate(), from, to);
        new PKFXAnalyzer().setPosition(cs, true);

        Candles candles = new Candles();
        candles.setCandles(cs);
        return candles;
    }


    private void output(Candles candles, String filename) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);

        Kryo kryo = new Kryo();
        kryo.register(Candles.class, new JavaSerializer());

        kryo.writeObject(output, candles);

        output.flush();
        output.close();

        byte[] binary = baos.toByteArray();

        try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);) {

            // 特定のクラスのオブジェクトの状態をストリームに書き込む
            objectOutputStream.writeObject(binary);
            objectOutputStream.flush();

        }
    }


}
