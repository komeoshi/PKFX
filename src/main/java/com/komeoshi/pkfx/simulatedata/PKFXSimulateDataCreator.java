package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.PKFXAnalyzer;
import com.komeoshi.pkfx.restclient.PKFXSimulatorRestClient;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Candles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class PKFXSimulateDataCreator {

    private static final Logger log = LoggerFactory.getLogger(PKFXSimulateDataCreator.class);

    public static void main(String[] args) {
        try {
            new PKFXSimulateDataCreator().execute1();
            new PKFXSimulateDataCreator().execute3();
            new PKFXSimulateDataCreator().execute2();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void execute1() throws IOException {
        Candles candles = getCandles1();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);

        Kryo kryo = new Kryo();
        kryo.register(Candles.class, new JavaSerializer());

        kryo.writeObject(output, candles);

        output.flush();
        output.close();

        byte[] binary = baos.toByteArray();

        try (FileOutputStream fileOutputStream = new FileOutputStream("data.dat");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);) {

            // 特定のクラスのオブジェクトの状態をストリームに書き込む
            objectOutputStream.writeObject(binary);
            objectOutputStream.flush();

        }

    }

    private Candles getCandles1() {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> cs = client.runWithManyCandles(new RestTemplate());
        new PKFXAnalyzer().setPosition(cs, true);

        Candles candles = new Candles();
        candles.setCandles(cs);
        return candles;
    }

    public void execute2() throws IOException {
        Candles candles = getCandles2();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);

        Kryo kryo = new Kryo();
        kryo.register(Candles.class, new JavaSerializer());

        kryo.writeObject(output, candles);

        output.flush();
        output.close();

        byte[] binary = baos.toByteArray();

        try (FileOutputStream fileOutputStream = new FileOutputStream("minData.dat");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);) {

            // 特定のクラスのオブジェクトの状態をストリームに書き込む
            objectOutputStream.writeObject(binary);
            objectOutputStream.flush();

        }

    }

    private Candles getCandles2() {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> cs = client.runMins(new RestTemplate());
        new PKFXAnalyzer().setPosition(cs, true);

        Candles candles = new Candles();
        candles.setCandles(cs);
        return candles;
    }


    public void execute3() throws IOException {
        Candles candles = getCandles3();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);

        Kryo kryo = new Kryo();
        kryo.register(Candles.class, new JavaSerializer());

        kryo.writeObject(output, candles);

        output.flush();
        output.close();

        byte[] binary = baos.toByteArray();

        try (FileOutputStream fileOutputStream = new FileOutputStream("5MinData.dat");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);) {

            // 特定のクラスのオブジェクトの状態をストリームに書き込む
            objectOutputStream.writeObject(binary);
            objectOutputStream.flush();

        }

    }

    private Candles getCandles3() {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> cs = client.run5Mins(new RestTemplate());
        new PKFXAnalyzer().setPosition(cs, true);

        Candles candles = new Candles();
        candles.setCandles(cs);
        return candles;
    }
}
