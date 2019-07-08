package de.fhg.ipa.null70.simple_kafka_mqtt_connector;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("######## STARTING THE SIMPLE-KAFKA-MQTT-CONNECTOR ########");
        SimpleKafkaMQTTConnector simpleKafkaMQTTConnector = new SimpleKafkaMQTTConnector();


        // Read application.properties
        CompositeConfiguration config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());

        config.addConfiguration(new PropertiesConfiguration());
        try {
            config.addConfiguration(new PropertiesConfiguration("application.properties"));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        // Properties configuration
        String kafkaHost = config.getString("kafka.host");
        String kafkaPort = config.getString("kafka.port");
        String kafkaClientId = config.getString("kafka.client.id");

        String mqttHost = config.getString("mqtt.host");
        String mqttPort = config.getString("mqtt.port");
        String mqttClientId = config.getString("mqtt.client.id");
        Integer mqttQos = Integer.parseInt(config.getString("mqtt.qos").trim());

        String topicMapping = config.getString("topic.mapping");

        logger.info("-------- APPLICATION PROPERTIES --------");
        logger.info("kafkaHost = " + kafkaHost);
        logger.info("kafkaPort = " + kafkaPort);
        logger.info("mqttHost = " + mqttHost);
        logger.info("mqttPort = " + mqttPort);
        logger.info("topicMapping = " + topicMapping);
        logger.info("----------------------------------------");
        logger.info("");

        simpleKafkaMQTTConnector.run(kafkaHost, kafkaPort, kafkaClientId, mqttHost, mqttPort, mqttClientId, mqttQos, topicMapping);
    }

}
