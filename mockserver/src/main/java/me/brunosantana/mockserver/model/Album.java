package me.brunosantana.mockserver.model;

import java.util.List;

public record Album(Long albumId, List<Photo> photos) {}
