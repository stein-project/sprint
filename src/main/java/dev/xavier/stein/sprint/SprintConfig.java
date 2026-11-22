/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint;

/**
 * Sprint's settings, {@code config/steinsprint.json}. The field names are the file's keys: they
 * never change (a new option is a new field).
 */
final class SprintConfig {

    /** The HUD text: {@code [Sprinting (Toggled)]}. */
    static final int STYLE_BRACKETS = 0;

    /** The HUD text: {@code Sprinting (Toggled)}. */
    static final int STYLE_PLAIN = 1;

    /** The HUD text: {@code Sprinting}. */
    static final int STYLE_SHORT = 2;

    static final int STYLE_COUNT = 3;

    /** Toggle sprint on: the mod holds the game's sprint key (its key, G, flips it). */
    boolean toggleSprint = true;

    /** Toggle sneak on: the mod holds the game's sneak key (its key, none by default, flips it). */
    boolean toggleSneak = false;

    /** The game's own sprint and sneak keys also flip the toggles (a press, not a hold). */
    boolean gameKeysToggle = false;

    /** Double-tapping forward starts a sprint, as in the game. */
    boolean doubleTapSprint = true;

    /** Starting to fly turns toggle sneak off (otherwise the player keeps descending). */
    boolean unsneakOnFly = true;

    /** One of the STYLE_ constants. */
    int hudStyle = STYLE_BRACKETS;

    /**
     * The HUD also shows a toggle that is on while it does nothing (standing still with toggle
     * sprint on). Off: only what is happening.
     */
    boolean hudAlways = false;

    /** While flying, the HUD also says the player sprints ("Flying + Sprinting"). */
    boolean hudSprintWhileFlying = true;

    /** Repairs what a hand-edited file may leave. */
    void normalize() {
        if (hudStyle < 0 || hudStyle >= STYLE_COUNT) {
            hudStyle = STYLE_BRACKETS;
        }
    }
}
