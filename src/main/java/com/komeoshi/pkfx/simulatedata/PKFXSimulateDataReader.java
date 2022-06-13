package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.dto.Candles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class PKFXSimulateDataReader {
    private static final Logger log = LoggerFactory.getLogger(PKFXSimulateDataReader.class);

    public static void main(String[] args) {
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("minData.dat");
        Candles candles = reader.read();

        log.info("" + candles.getCandles().size());
    }

    private String filename;
    public PKFXSimulateDataReader(String filename){
        this.filename = filename;
    }

    public Candles read() {

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
