/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.xavier.stein.loader.api.Keys;
import dev.xavier.stein.loader.api.Log;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Toggle sprint used to live in Combat. On Sprint's first start (no config/steinsprint.json yet),
 * what the player had there comes along: whether toggle sprint was on, and the key they had chosen
 * for it.
 *
 * <p>The key is read from options.txt ({@code key_key.steincombat.toggleSprint:<code>}), where the
 * game kept it. A Combat without toggle sprint no longer registers that key, so the game drops the
 * line on its next save of the options; that Combat copies the code into its own file first ({@code
 * legacySprintKey} in config/steincombat.json), and that copy is the fallback here.
 */
final class CombatMigration {

    /** The key's name in Combat, as the game saved it in options.txt. */
    private static final String OPTIONS_PREFIX = "key_key.steincombat.toggleSprint:";

    /** What a Combat without toggle sprint saves of the old key. */
    private static final String SAVED_KEY = "legacySprintKey";

    private CombatMigration() {}

    /**
     * Brings Combat's toggle sprint into a new config and onto the toggle key. Only on the first
     * start: afterwards the player's choices here are the ones that count.
     */
    static void run(File gameDir, SprintConfig config, Keys.Binding sprintKey, Log log) {
        JsonObject combat = readJson(new File(gameDir, "config/steincombat.json"));
        if (combat != null && combat.has("toggleSprint")) {
            config.toggleSprint = bool(combat.get("toggleSprint"), config.toggleSprint);
        }
        Integer key = keyFromOptions(new File(gameDir, "options.txt"));
        if (key == null && combat != null && combat.has(SAVED_KEY)) {
            key = integer(combat.get(SAVED_KEY));
        }
        if (key != null && key != sprintKey.key()) {
            sprintKey.setKey(key);
        }
        if (combat != null || key != null) {
            log.info(
                    "brought toggle sprint over from Combat (on: "
                            + config.toggleSprint
                            + ", key: "
                            + (key == null ? "default" : key)
                            + ")");
        }
    }

    /** The code Combat's key had in options.txt, or null without the line. */
    private static Integer keyFromOptions(File options) {
        if (!options.isFile()) {
            return null;
        }
        try {
            // ISO-8859-1 never fails on read; the key lines are ASCII.
            for (String line : Files.readAllLines(options.toPath(), StandardCharsets.ISO_8859_1)) {
                if (line.startsWith(OPTIONS_PREFIX)) {
                    return Integer.parseInt(line.substring(OPTIONS_PREFIX.length()).trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            // unreadable: the default key stays
        }
        return null;
    }

    @SuppressWarnings("deprecation") // the game's Gson only has the instance parse
    private static JsonObject readJson(File file) {
        if (!file.isFile()) {
            return null;
        }
        try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement e = new JsonParser().parse(r);
            return e.isJsonObject() ? e.getAsJsonObject() : null;
        } catch (Exception e) {
            return null; // a broken Combat file is Combat's problem: start from the defaults
        }
    }

    private static boolean bool(JsonElement e, boolean def) {
        try {
            return e.getAsBoolean();
        } catch (RuntimeException ex) {
            return def;
        }
    }

    private static Integer integer(JsonElement e) {
        try {
            return e.getAsInt();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
