package com.microsoft.samples.flink;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


class StreamingJobCommon {

    private static final Logger LOG = LoggerFactory.getLogger(StreamingJobCommon.class);

    static StreamExecutionEnvironment createStreamExecutionEnvironment(ParameterTool params) {

        // set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // make parameters available in the web interface, if available
        env.getConfig().setGlobalJobParameters(params);

        // Set Flink task parallelism
        env.setParallelism(params.getInt("parallelism", 1));

        // start a checkpoint every 1000 ms
        env.enableCheckpointing(params.getLong("checkpoint.interval", 1000L));

        return env;
    }

    static <T> FlinkKafkaConsumerBase<T> createKafkaConsumer(ParameterTool params, KafkaDeserializationSchema<T> schema) {
        // set up the execution environment
        Properties properties = new Properties();

        setProperties(params, "kafka.in.", properties);
        String topicIn = (String) properties.remove("topic");
        if (topicIn == null) throw new IllegalArgumentException("Missing configuration value kafka.topic.in");
        LOG.info("Consuming from Kafka topic: {}", topicIn);

        // Create Kafka consumer deserializing from JSON.
        // Flink recommends using Kafka 0.11 consumer as Kafka 1.0 consumer is not stable.
        return new FlinkKafkaConsumer011<>(topicIn, schema, properties);
    }

    static <T> FlinkKafkaProducer011<T> createKafkaProducer(ParameterTool params, SerializationSchema<T> schema) {
        Properties propertiesOut = new Properties();
        setProperties(params, "kafka.out.", propertiesOut);
        String topicOut = (String) propertiesOut.remove("topic");
        if (topicOut == null) throw new IllegalArgumentException("Missing configuration value kafka.topic.out");
        LOG.info("Writing into Kafka topic: {}", topicOut);

        FlinkKafkaProducer011<T> kafkaOut = new FlinkKafkaProducer011<>(
                topicOut,
                schema,
                propertiesOut
        );
        return kafkaOut;
    }

    private static void setProperties(ParameterTool params, String prefix, Properties properties) {
        params.toMap().forEach(
                (k, v) -> {
                    if (k.startsWith(prefix)) properties.put(k.replace(prefix, ""), v);
                });
    }


}

