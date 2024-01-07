package me.brunosantana.mockserver.model;

import java.util.ArrayList;

public record BibleResponse(String reference,
                            ArrayList<Verse> verses,
                            String text,
                            String translation_id,
                            String translation_name,
                            String translation_note) {}
