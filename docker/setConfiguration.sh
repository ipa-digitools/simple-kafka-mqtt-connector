#!/usr/bin/env bash

# Read arguments
application_properties_file=${1}

KAFKA_HOST=${2}
KAFKA_PORT=${3}
KAFKA_CLIENT_ID=${4}

MQTT_HOST=${5}
MQTT_PORT=${6}
MQTT_CLIENT_ID=${7}
MQTT_QOS=${8}

TOPIC_MAPPING=${9}


echo "Configuration:"
echo "Application properties file: $application_properties_file"
echo "KAFKA_HOST: $KAFKA_HOST"
echo "KAFKA_PORT: $KAFKA_PORT"
echo "KAFKA_CLIENT_ID: $KAFKA_CLIENT_ID"
echo "MQTT_HOST: $MQTT_HOST"
echo "MQTT_PORT: $MQTT_PORT"
echo "MQTT_CLIENT_ID: $MQTT_CLIENT_ID"
echo "MQTT_QOS: $MQTT_QOS"
echo "TOPIC_MAPPING: $TOPIC_MAPPING"

# overwrite properties
sed -i "/kafka.host/c\kafka.host = $KAFKA_HOST" ${application_properties_file}
sed -i "/kafka.port/c\kafka.port = $KAFKA_PORT" ${application_properties_file}
sed -i "/kafka.client.id/c\kafka.client.id = $KAFKA_CLIENT_ID" ${application_properties_file}

sed -i "/mqtt.host/c\mqtt.host = $MQTT_HOST" ${application_properties_file}
sed -i "/mqtt.port/c\mqtt.port = $MQTT_PORT" ${application_properties_file}
sed -i "/mqtt.client.id/c\mqtt.client.id = $MQTT_CLIENT_ID" ${application_properties_file}
sed -i "/mqtt.qos/c\mqtt.qos = $MQTT_QOS" ${application_properties_file}

sed -i "/topic.mapping/c\topic.mapping = $TOPIC_MAPPING" ${application_properties_file}
