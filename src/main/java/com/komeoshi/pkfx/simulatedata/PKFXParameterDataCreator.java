package com.komeoshi.pkfx.simulatedata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.komeoshi.pkfx.dto.Candles;
import com.komeoshi.pkfx.dto.parameter.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class PKFXParameterDataCreator {

    private static final Logger log = LoggerFactory.getLogger(PKFXParameterDataCreator.class);

    public static void main(String[] args) {
        try {
            PKFXParameterDataCreator p = new PKFXParameterDataCreator();
            p.output("parameter1.dat", Parameter.getParameter1());
            p.output("parameter2.dat", Parameter.getParameter2());
            p.output("parameter3.dat", Parameter.getParameter3());
            p.output("parameter4.dat", Parameter.getParameter4());
            p.output("parameter5.dat", Parameter.getParameter5());
            p.output("parameter6.dat", Parameter.getParameter6());
            p.output("parameter7.dat", Parameter.getParameter7());
            p.output("parameter8.dat", Parameter.getParameter8());
            p.output("parameter9.dat", Parameter.getParameter9());
            p.output("parameter10.dat", Parameter.getParameter10());
        } catch (IOException e) {
            log.error("", e);
        }

    }

    public void output(String filename, Parameter parameter) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);

        Kryo kryo = new Kryo();
        kryo.register(Parameter.class, new JavaSerializer());

        kryo.writeObject(output, parameter);

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
