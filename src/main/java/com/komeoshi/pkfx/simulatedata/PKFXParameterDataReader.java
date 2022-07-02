package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.dto.Candles;
import com.komeoshi.pkfx.dto.parameter.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class PKFXParameterDataReader {
    private static final Logger log = LoggerFactory.getLogger(PKFXParameterDataReader.class);

    public static void main(String[] args) {
        PKFXParameterDataReader reader1 = new PKFXParameterDataReader("parameter1.dat");
        Parameter parameter1 = reader1.read();
        PKFXParameterDataReader reader2 = new PKFXParameterDataReader("parameter2.dat");
        Parameter parameter2 = reader2.read();
        PKFXParameterDataReader reader3 = new PKFXParameterDataReader("parameter3.dat");
        Parameter parameter3 = reader3.read();
        PKFXParameterDataReader reader4 = new PKFXParameterDataReader("parameter4.dat");
        Parameter parameter4 = reader4.read();
        PKFXParameterDataReader reader5 = new PKFXParameterDataReader("parameter5.dat");
        Parameter parameter5 = reader5.read();
        PKFXParameterDataReader reader6 = new PKFXParameterDataReader("parameter6.dat");
        Parameter parameter6 = reader6.read();
        PKFXParameterDataReader reader7 = new PKFXParameterDataReader("parameter7.dat");
        Parameter parameter7 = reader7.read();
        PKFXParameterDataReader reader8 = new PKFXParameterDataReader("parameter8.dat");
        Parameter parameter8 = reader8.read();
        PKFXParameterDataReader reader9 = new PKFXParameterDataReader("parameter9.dat");
        Parameter parameter9 = reader9.read();
        PKFXParameterDataReader reader10 = new PKFXParameterDataReader("parameter10.dat");
        Parameter parameter10 = reader10.read();

        log.info("" + parameter1.toString());
        log.info("" + parameter2.toString());
        log.info("" + parameter3.toString());
        log.info("" + parameter4.toString());
        log.info("" + parameter5.toString());
        log.info("" + parameter6.toString());
        log.info("" + parameter7.toString());
        log.info("" + parameter8.toString());
        log.info("" + parameter9.toString());
        log.info("" + parameter10.toString());
    }

    private String filename;

    public PKFXParameterDataReader(String filename) {
        this.filename = filename;
    }

    public Parameter read() {

        Parameter parameter = null;
        try (FileInputStream fileInputStream = new FileInputStream(filename);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            byte[] binary = (byte[]) objectInputStream.readObject();

            Kryo kryo = new Kryo();
            kryo.register(Parameter.class, new JavaSerializer());

            Input input = new Input(new ByteArrayInputStream(binary));

            parameter = kryo.readObject(input, Parameter.class);

        } catch (IOException | ClassNotFoundException e) {
            log.error("", e);
        }
        return parameter;
    }

}
