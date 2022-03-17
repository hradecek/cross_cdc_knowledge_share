package com.cross_ni.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.stream.Stream;

public class MainJavaPasswordMaskingStream {

	private static final JsonUsersGenerator JSON_USERS_GENERATOR = new JsonUsersGenerator();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static void main(String[] args) {
		new JavaUsersStream()
				.usersStream(usersStream())
				.forEach(user -> System.out.println(user.toPrettyString()));
	}

	private static Stream<JsonNode> usersStream() {
		return JSON_USERS_GENERATOR.generate(20).stream().map(MainJavaPasswordMaskingStream::stringToJsonNode);
	}

	private static JsonNode stringToJsonNode(String jsonString) {
		try {
			return OBJECT_MAPPER.readTree(jsonString);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static class JavaUsersStream {

		private static final String JSON_KEY_PASSWORD = "password";
		private static final String JSON_KEY_GROUPS = "groups";

		private static final String PASSWORD_MASK = "********";

		public Stream<JsonNode> usersStream(Stream<JsonNode> sourceUsersStream)  {
			return sourceUsersStream
					.map(JavaUsersStream::maskPassword)
					.filter(JavaUsersStream::hasAtLeastTwoGroups);
		}

		private static JsonNode maskPassword(JsonNode userJsonNode) {
			final ObjectNode userObjectNode = userJsonNode.deepCopy();
			userObjectNode.set(JSON_KEY_PASSWORD, new TextNode(PASSWORD_MASK));

			return userObjectNode;
		}

		private static boolean hasAtLeastTwoGroups(JsonNode userJsonNode) {
			return hasAtLeastNumOfGroups(userJsonNode, 2);
		}

		private static boolean hasAtLeastNumOfGroups(JsonNode userJsonNode, int expectedNumOfGroups) {
			return userJsonNode.get(JSON_KEY_GROUPS).size() >= expectedNumOfGroups;
		}
	}
}
