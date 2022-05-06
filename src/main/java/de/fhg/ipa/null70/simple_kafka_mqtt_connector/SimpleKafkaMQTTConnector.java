package de.fhg.ipa.null70.simple_kafka_mqtt_connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class SimpleKafkaMQTTConnector {
    private static final Logger logger = LogManager.getLogger(SimpleKafkaMQTTConnector.class);

    // Key = mqtt-topic input , Value = kafka-topics for output
    private static HashMap<String, ArrayList<String>> mqttKafkaTopicMap = new HashMap();


    public void run(String kafkaHost, String kafkaPort, String kafkaClientId, String mqttHost, String mqttPort, String mqttClientId, Integer mqttQos, String topicMapping) {
        // Initialize topic routing map
        initTopicsRoutingMap(topicMapping);

        // Init and start kafka producer
        KafkaProducer<Integer, String> kafkaProducer = initKafkaProducer(kafkaHost, kafkaPort, kafkaClientId);

        // Setup and start the mqtt client
        initMqttClient(mqttHost, mqttPort, mqttClientId, mqttQos, kafkaProducer);
    }

    private KafkaProducer<Integer, String> initKafkaProducer(String kafkaHost, String kafkaPort, String kafkaClientId) {
        logger.trace("Creating Kafka Producer...");
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaClientId);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(props);
        logger.trace("Kafka producer ready to produce...");
        return kafkaProducer;
    }

    private HashMap<String, ArrayList<String>> checkForKey(String mqttTopic)
    {
        HashMap<String, ArrayList<String>> keysForWhichTheTopicApplies = new HashMap<String, ArrayList<String>>();
        try{
            for (Map.Entry mapElement : mqttKafkaTopicMap.entrySet()) 
            {
                String key = (String)mapElement.getKey();
                if(MqttTopic.isMatched(key, mqttTopic))
                {
                    keysForWhichTheTopicApplies.put((String)mapElement.getKey(), ( ArrayList<String>)mapElement.getValue());
                }
            }
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return keysForWhichTheTopicApplies;
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
        // use a persistent session..
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

            @Override
            public void connectionLost(Throwable throwable) { }

            @Override
            public void messageArrived(String mqttTopic, MqttMessage mqttMessage) throws Exception {
                // Checks through which mqtt-topic this message was sent and sends it to the pre-configured corresponding kafka topics..

                String message = new String(mqttMessage.getPayload());
                logger.info(mqttTopic + " - " + message);

                try {
                    HashMap<String, ArrayList<String>> erg = checkForKey(mqttTopic);
                    if(erg.size()>=1) {
                        for (Map.Entry mapElement : erg.entrySet()) {
                            ((ArrayList<String>)mapElement.getValue()).forEach(kafkaTopic -> {
                                kafkaProducer.send(new ProducerRecord<>(kafkaTopic, message));
                            });
                            logger.trace("send Message to kafka - " + message);
                        }
                    }
                } catch (KafkaException e) {
                    logger.error("Exception occurred – Check log for more details.\n" + e.getMessage());
                    logger.warn("There seems to be an issue with the kafka connection. Currently no messages are forwarded to the kafka cluster!!!!");
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
                    String mqttTopic = pair[0];
                    String kafkaTopic = pair[1];
                    if( !mqttKafkaTopicMap.containsKey(mqttTopic) ){
                        mqttKafkaTopicMap.put(mqttTopic, new ArrayList<String>());
                    }
                    mqttKafkaTopicMap.get(mqttTopic).add(kafkaTopic);
                    logger.info(mqttTopic + " >>> " + kafkaTopic);
                }
        );
    }

}
