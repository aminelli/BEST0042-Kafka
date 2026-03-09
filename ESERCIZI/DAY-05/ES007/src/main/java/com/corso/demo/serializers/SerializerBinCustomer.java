package com.corso.demo.serializers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.corso.demo.models.Customer2;

public class SerializerBinCustomer implements Serializer<Customer2> {

    @Override
    public byte[] serialize(String topic, Customer2 data) {
        if (data == null) {
            return new byte[0];
        }
        try {

            byte[] serFirstName = data.getFirstname().getBytes(StandardCharsets.UTF_8);
            byte[] serLastName = data.getLastname().getBytes(StandardCharsets.UTF_8);

            ByteBuffer buffer = ByteBuffer.allocate(
                // 4 byte per id
                4 +
                // 4 byte per lunghezza del nome + byte del nome
                4 + 
                // 4 byte per lunghezza del cognome + byte del cognome
                4 + 
                // nr byte che conterranno il firstname
                serFirstName.length +
                // nr byte che conterranno il firstname
                serLastName.length
            );

            buffer.putInt(data.getId());
            buffer.putInt(serFirstName.length);
            buffer.putInt(serLastName.length);
            buffer.put(serFirstName);
            buffer.put(serLastName);

            return buffer.array();

        } catch (Exception ex) {
            return new byte[0];
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // TODO Auto-generated method stub
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        Serializer.super.close();
    }

}
