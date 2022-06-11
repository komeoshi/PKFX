package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.PKFXFinderAnalyzer;
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
            new PKFXSimulateDataCreator().execute();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void execute() throws IOException {
        Candles candles = getCandles();

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

    private Candles getCandles() {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> cs = client.runWithManyCandles(new RestTemplate());
        new PKFXFinderAnalyzer().setPosition(cs, true);

        Candles candles = new Candles();
        candles.setCandles(cs);
        return candles;
    }
}
