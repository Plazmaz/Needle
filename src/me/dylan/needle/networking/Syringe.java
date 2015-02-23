package me.dylan.needle.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A Minecraft interfacing class for Needle
 */
public class Syringe {

    public static String mcPing(String address, int port) throws IOException {
        Socket connection = new Socket(address, port);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        DataInputStream in = new DataInputStream(connection.getInputStream());

        sendHandshake(out, address, port);
        out.writeByte(0x01);
        out.writeByte(0x00);
        int stringLen = readVarInt(in);
        byte[] data = new byte[stringLen];
        in.readFully(data);
        String jsonData = new String(data);
        return "Length: " + stringLen + "Data: " + jsonData;
    }

    private static void sendHandshake(DataOutputStream out, String host, int port) throws IOException {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(bytes);
        handshake.writeByte(0x00);
        writeVarInt(handshake, 4);
        writeVarInt(handshake, host.length());
        handshake.writeBytes(host);
        handshake.writeShort(port);
        writeVarInt(handshake, 1);
        writeVarInt(out, handshake.size());
        out.write(bytes.toByteArray());

    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    private static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }

    }

}
