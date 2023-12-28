package me.brunosantana.wiremock.model;

public record Verse(String book_id,
                    String book_name,
                    int chapter,
                    int verse,
                    String text) {}
