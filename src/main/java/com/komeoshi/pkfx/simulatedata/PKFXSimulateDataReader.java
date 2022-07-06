package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.PKFXAnalyzer;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Candles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PKFXSimulateDataReader {
    private static final Logger log = LoggerFactory.getLogger(PKFXSimulateDataReader.class);

    public static void main(String[] args) {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("data/mindata");
        List<Candle> candles = reader.read();

        log.info("" + candles.size());
    }

    private final String dirname;

    public PKFXSimulateDataReader(String dir) {
        this.dirname = dir;
    }

    public List<Candle> read() {
        List<Candle> candles = new ArrayList<>();

        File dir = new File(dirname);
        File[] files = dir.listFiles();

        int count = 0;
        for (File file : files) {
            count++;
            candles.addAll(read(file.getAbsolutePath()).getCandles());

            if (count % 100 == 0) {
                log.info("【" + count + "】 / " + files.length + " " + file.getAbsoluteFile());
                PKFXAnalyzer.showMemoryUsage();
            }
        }

        PKFXAnalyzer analyzer = new PKFXAnalyzer();
        analyzer.setPosition(candles, true);

        return candles;
    }

    private Candles read(String filename) {

        Candles candles = null;
        try (FileInputStream fileInputStream = new FileInputStream(filename);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            byte[] binary = (byte[]) objectInputStream.readObject();

            Kryo kryo = new Kryo();
            kryo.register(Candles.class, new JavaSerializer());

            Input input = new Input(new ByteArrayInputStream(binary));

            candles = kryo.readObject(input, Candles.class);

        } catch (IOException | ClassNotFoundException e) {
            log.error("", e);
        }
        return candles;
    }

}
