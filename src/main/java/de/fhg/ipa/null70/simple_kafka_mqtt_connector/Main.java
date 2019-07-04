package de.fhg.ipa.null70.simple_kafka_mqtt_connector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("######## STARTING THE SIMPLE-KAFKA-MQTT-CONNECTOR ########");
        SimpleKafkaMQTTConnector simpleKafkaMQTTConnector = new SimpleKafkaMQTTConnector();
        simpleKafkaMQTTConnector.run();
    }

}
