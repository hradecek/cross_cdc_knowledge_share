package com.cross_ni.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Properties;

public class MainKafkaNamesCountsStream {

	private static final String SOURCE_USERS_TOPIC = "users-source";

	public static void main(String[] args) {
		final var streamsBuilder = new StreamsBuilder();

		userStream(streamsBuilder)
				.mapValues((key, userJson) -> userJson.get("name").asText(), Named.as("map-name"))
				.groupBy((key, name) -> name, Grouped.with("group-by-name", Serdes.String(), Serdes.String()))
				.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("names-counts").withKeySerde(Serdes.String()).withValueSerde(Serdes.Long()))
				.toStream(Named.as("name-counts-to-stream"))
				.to("user-name-counts", Produced.with(Serdes.String(), Serdes.Long()));

		start(streamsBuilder.build());
	}

	private static void start(Topology topology) {
		final KafkaStreams kafkaStreams = new KafkaStreams(topology, properties());
		kafkaStreams.start();
	}

	private static Properties properties() {
		final Properties properties = new Properties();
		properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "users-name-counts");
		properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(StreamsConfig.STATE_DIR_CONFIG, System.getProperty("java.io.tmpdir"));
		properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
		return properties;
	}

	private static KStream<String , JsonNode> userStream(StreamsBuilder streamsBuilder) {
		return streamsBuilder.stream(SOURCE_USERS_TOPIC, Consumed.with(Serdes.String(), jsonSerde()));
	}

	private static Serde<JsonNode> jsonSerde() {
		return Serdes.serdeFrom(new JsonSerializer(), new JsonDeserializer());
	}
}
