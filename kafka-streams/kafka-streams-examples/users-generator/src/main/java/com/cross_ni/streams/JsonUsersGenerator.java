package com.cross_ni.streams;

import com.cross_ni.streams.model.Group;
import com.cross_ni.streams.model.User;
import com.github.javafaker.Faker;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUsersGenerator {

	protected final Faker faker = new Faker();

	public List<String> generate(int count) {
		final List<String> jsonUsers = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			jsonUsers.add(userToJsonString(createRandomUser()));
		}
		return jsonUsers;
	}

	private User createRandomUser() {
		return new User(
				faker.name().firstName(),
				faker.name().lastName(),
				faker.name().username(),
				faker.internet().emailAddress(),
				faker.internet().password(),
				createRandomGroups(faker.number().numberBetween(0, 4))
		);
	}

	private List<Group> createRandomGroups(int number) {
		final List<Group> groups = new ArrayList<>();
		for (int i = 0; i < number; ++i) {
			groups.add(createRandomGroup());
		}
		return groups;
	}

	private Group createRandomGroup() {
		return new Group(
				faker.number().numberBetween(0L, 100L),
				faker.app().name()
		);
	}

	public String userToJsonString(User user) {
		return userToJson(user).toString();
	}

	private JSONObject userToJson(User user) {
		return new JSONObject()
				.put("name", user.name())
				.put("surname", user.surname())
				.put("username", user.username())
				.put("email", user.email())
				.put("password", user.password())
				.put("groups", groupsToJson(user.groups()));
	}

	private JSONArray groupsToJson(List<Group> groups) {
		final JSONArray groupsJsonArray = new JSONArray();
		for (Group group : groups) {
			groupsJsonArray.put(groupToJson(group));
		}
		return groupsJsonArray;
	}

	private JSONObject groupToJson(Group group) {
		return new JSONObject()
				.put("id", group.id())
				.put("name", group.name());
	}
}
