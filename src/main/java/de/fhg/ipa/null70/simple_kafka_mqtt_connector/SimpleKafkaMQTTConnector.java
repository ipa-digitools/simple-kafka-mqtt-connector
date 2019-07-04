package de.fhg.ipa.null70.simple_kafka_mqtt_connector;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SimpleKafkaMQTTConnector {
    private static final Logger logger = LogManager.getLogger(SimpleKafkaMQTTConnector.class);

    // Key = mqtt-topic input , Value = kafka-topic for output
    private static HashMap<String, String> mqttKafkaTopicMap = new HashMap<String, String>();


    public void run() {
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

        // read config file into the system
        initTopicsRoutingMap(topicMapping);

        logger.trace("Creating Kafka Producer...");
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaClientId);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(props);
        logger.trace("Start sending messages...");

        // Setup and start Mqtt Client
        initMqttClient(mqttHost, mqttPort, mqttClientId, mqttQos, kafkaProducer);
    }

    private void initMqttClient(String mqttHost, String mqttPort, String mqttClientId, Integer mqttQos, KafkaProducer<Integer, String> kafkaProducer) {

        /***
         * MQTT Client
         * **/
        MqttClient client = null;
        try {
            client = new MqttClient(
                    "tcp://" + mqttHost + ":" + mqttPort, mqttClientId);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        MqttConnectOptions options = new MqttConnectOptions();
        //Setzen einer Persistent Session
        options.setCleanSession(false);

        options.setWill(
                "will/topic",               //Topic
                "Disconnected!".getBytes(), //Nachrichteninhalt
                mqttQos,      //QoS
                false); //Retained message?

        try {
            client.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        try {
            // Subscribe all configured topics via mqtt
            for (Map.Entry mapElement : mqttKafkaTopicMap.entrySet()) {
                String key = (String)mapElement.getKey();
                client.subscribe(key);
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            int i = 0;

            @Override
            public void connectionLost(Throwable throwable) { }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                // Checks through which mqtt-topic this message was sent and sends it to the pre-configured corresponding kafka topic..

                String message = new String(mqttMessage.getPayload());
                logger.info(topic + " - " + message);

                try {
                    if(!mqttKafkaTopicMap.get(topic).isEmpty()) {

                        kafkaProducer.send(new ProducerRecord<>(mqttKafkaTopicMap.get(topic), i++, message));
                        logger.trace("send Message to kafka - " + message);
                    }
                } catch (KafkaException e) {
                    logger.error("Exception occurred – Check log for more details.\n" + e.getMessage());
                    logger.warn("There seems to be issue with the kafka connection. Currently no messages are forwarded to the kafka cluster!!!!");
//                    System.exit(-1);
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken t) { }
        });
    }

    public static void initTopicsRoutingMap(String topicMappingString){
        logger.info("Setting up topic mapping (MQTT >>> Kafka) ...");
        Arrays.asList(topicMappingString.split(";")).forEach(s -> {
                    String[] pair = s.split(">>>");
                    mqttKafkaTopicMap.put( pair[0], pair[1]);
                    logger.info(pair[0] + " >>> " + pair[1]);
                }
        );
    }

}
