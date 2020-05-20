# A simple MQTT to Apache Kafka Connector 
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fipa-digitools%2Fsimple-kafka-mqtt-connector.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fipa-digitools%2Fsimple-kafka-mqtt-connector?ref=badge_shield)




## Usage

#### Prerequisites
* Java 8 or higher


#### Configuration

* Edit the application.properties-file

* kafka.host = IP THROUGH WHICH THE KAFKA BROKER WILL BE REACHED [HOSTNAME|IP]
* kafka.port = [INTEGER]
* kafka.client.id = [STRING]

* mqtt.host = IP THROUGH WHICH THE MQTT BROKER WILL BE REACHED [HOSTNAME|IP]
* mqtt.port = [INTEGER]
* mqtt.client.id = [STRING]
* mqtt.qos = Quality of service for MQTT - [INTEGER] Allowed[0,1,2]

* topic.mapping = How are topics routed from MQTT to Kafka - (Separators >>> and ;) Example mqttTopicA>>>kafkaTopicA;mqttTopicB>>>kafkaTopicB;mqttTopicC>>>kafkaTopicC

#### How to build

* Run: `mvn clean install`


#### How to run

* Place the jar with dependencies and your edited application.properties-file in the same directory

* Open a bash or CMD in the same directory as the .jar

* Run: `java -jar simple_kafka_mqtt_connector-0.0.1-SNAPSHOT-jar-with-dependencies.jar`


## Usage with Docker

* Docker Hub https://hub.docker.com/r/arthurgrigo/simple-kafka-mqtt-connector

* Run (edit enviroment variables to your needs!) : `docker run -d -t -i -e KAFKA_HOST='localhost' -e KAFKA_PORT=9092 -e KAFKA_CLIENT_ID='testing-kafka-producer-1' -e MQTT_HOST='localhost' -e MQTT_PORT=1883 -e MQTT_CLIENT_ID='mqtt-client-1' -e MQTT_QOS=2 -e TOPIC_MAPPING='robotgroup001/robot001>>>test;robotgroup001/robot002>>>test02;robotgroup001/robot003>>>test03'  --name simple-kafka-mqtt-connector arthurgrigo/simple-kafka-mqtt-connector:latest`


## Usage with Docker-Compose

* See [docker-compose-examples](docker-compose)

#### Standalone

* Stand-alone container

* [docker-compose.yml](docker-compose/standalone/docker-compose.yml)

* Run: `docker-compose up -d`


#### Full Stack

* Full Stack (mqtt-broker, zookeeper, kafka-broker, simple-kafka-mqtt-connector)

* [docker-compose.yml](docker-compose/fullstack/docker-compose.yml)

* [env.list](docker-compose/fullstack/env.list)

* Place docker-compose.yml and env.list in the same directory

* Edit env.list to your needs!

* Run: `docker-compose up -d`


## License
See [LICENSE](LICENSE) file for License

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fipa-digitools%2Fsimple-kafka-mqtt-connector.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fipa-digitools%2Fsimple-kafka-mqtt-connector?ref=badge_large)