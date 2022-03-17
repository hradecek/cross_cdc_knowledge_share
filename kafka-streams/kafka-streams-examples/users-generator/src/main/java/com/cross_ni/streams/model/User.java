package com.cross_ni.streams.model;

import lombok.Builder;

import java.util.List;

@Builder
public record User(String name, String surname, String username, String email, String password, List<Group> groups) { }
