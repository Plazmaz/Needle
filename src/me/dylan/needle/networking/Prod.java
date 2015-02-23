package me.dylan.needle.networking;

import me.dylan.needle.logging.LogLevel;
import me.dylan.needle.logging.Logger;

import java.io.IOException;
import java.net.*;

/**
 * A ping interfacing class for Needle
 */
public class Prod {

    public static boolean prodIp(String host, int timeout) throws IOException {
        return Inet4Address.getByName(host).isReachable(timeout);
    }

    public static boolean prodPort(String host, int port, int timeout) {
        SocketAddress address = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        boolean canConnect = true;

        try {
            socket.connect(address, timeout);

        } catch (IOException e) {
            canConnect = false;
            Logger.log("Failed to prod port. Error: " + e.toString(), LogLevel.ERROR);
        } finally {

            try {
                socket.close();
            } catch (Exception e) {
                Logger.log("Could not close socket to " + host + " on port " + port + ".", LogLevel.ERROR);
                Logger.log("Reason: " + e.getMessage(), LogLevel.DEBUG);
            }

        }
        return canConnect;
    }
}
