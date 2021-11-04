package main.java;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.net.server.ServerListener;

public class Application {

	private final static Logger LOGGER = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

    public static void main(String[] args) {

        LOGGER.info("Server starting...");

        try {
            ServerThread runningThread = new ServerThread(10008);
            runningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
