package me.marvin.proxy;

import me.marvin.proxy.utils.Loggers;

import java.io.IOException;
import java.util.Random;

/**
 * Entry point of the proxy.
 */
public class ProxyBootstrap {
    public static void main(String[] args) throws IOException {
        Loggers.setupForwarding();

        Random rand = new Random();
        int randomPort = rand.nextInt(21000, 25565);
        int port = Integer.getInteger("port", randomPort);
        String targetAddr = System.getProperty("target", ":" + (randomPort + 1));

        InteractiveProxy instance = new InteractiveProxy(port, targetAddr);
        instance.start();

    }
}
