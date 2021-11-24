package main.java;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

	private final static Logger LOGGER = LoggerFactory.getLogger(Application.class);

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
