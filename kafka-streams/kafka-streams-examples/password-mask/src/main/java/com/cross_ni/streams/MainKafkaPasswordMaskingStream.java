package com.cross_ni.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Properties;

public class MainKafkaPasswordMaskingStream {

	private static final String SOURCE_USERS_TOPIC = "users-source";

	public static void main(String[] args) {
		final var streamsBuilder = new StreamsBuilder();

		new KafkaUsersStream()
				.usersStream(userStream(streamsBuilder))
				.to("users", Produced.with(Serdes.String(), jsonSerde()).withName("sink-users"));

		start(streamsBuilder.build());
	}

	private static void start(Topology topology) {
		final KafkaStreams kafkaStreams = new KafkaStreams(topology, properties());
		System.out.println(topology.describe());
		kafkaStreams.start();
	}

	private static Properties properties() {
		final Properties properties = new Properties();
		properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "users-pass-mask");
		properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(StreamsConfig.STATE_DIR_CONFIG, System.getProperty("java.io.tmpdir"));
		properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
		return properties;
	}

	private static KStream<String , JsonNode> userStream(StreamsBuilder streamsBuilder) {
		return streamsBuilder.stream(SOURCE_USERS_TOPIC, Consumed.with(Serdes.String(), jsonSerde()).withName("source-users"));
	}

	private static Serde<JsonNode> jsonSerde() {
		return Serdes.serdeFrom(new JsonSerializer(), new JsonDeserializer());
	}

	private static class KafkaUsersStream {

		private static final String JSON_KEY_PASSWORD = "password";
		private static final String JSON_KEY_GROUPS = "groups";

		private static final String PASSWORD_MASK = "********";

		public KStream<String, JsonNode> usersStream(KStream<String, JsonNode> sourceUsersStream)  {
			return sourceUsersStream
					.mapValues(KafkaUsersStream::maskPassword, Named.as("mask-password"))
					.filter(KafkaUsersStream::hasAtLeastTwoGroups, Named.as("filter-at-least-two-groups"));
		}

		private static JsonNode maskPassword(JsonNode userJsonNode) {
			final ObjectNode userObjectNode = userJsonNode.deepCopy();
			userObjectNode.set(JSON_KEY_PASSWORD, new TextNode(PASSWORD_MASK));

			return userObjectNode;
		}

		private static boolean hasAtLeastTwoGroups(String key, JsonNode userJsonNode) {
			return hasAtLeastNumOfGroups(userJsonNode, 2);
		}

		private static boolean hasAtLeastNumOfGroups(JsonNode userJsonNode, int expectedNumOfGroups) {
			return userJsonNode.get(JSON_KEY_GROUPS).size() >= expectedNumOfGroups;
		}
	}
}
