/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint.api;

import dev.xavier.stein.sprint.SteinSprint;

/**
 * What other mods may ask of Sprint. Declare it as {@code "optional": {"steinsprint": "0.1.0"}} in
 * steinmod.json (Sprint then starts, and ticks, before your mod) and check {@code
 * Mods.isLoaded("steinsprint")} before touching this class.
 *
 * <p>Call from the game thread.
 */
public final class Sprint {

    private Sprint() {}

    /**
     * Toggle sprint is holding the game's sprint key right now.
     *
     * @return true while the mod holds the key
     */
    public static boolean holding() {
        SteinSprint mod = SteinSprint.instance();
        return mod != null && mod.sprintHolding();
    }

    /**
     * The sprint reset after a hit (a W-tap): stops sprinting and lets go of the sprint key until
     * Sprint's next tick. The client tells the server it stopped and, a tick later, that it started
     * again; the server, which drops the attacker's sprinting on every hit that knocks back, sees
     * it sprinting for the next hit. Does nothing when toggle sprint is not holding the key (a key
     * held by hand stays the player's). Call it at the end of a tick ({@code onTickEnd}).
     *
     * @return true when the reset happened
     */
    public static boolean reset() {
        SteinSprint mod = SteinSprint.instance();
        return mod != null && mod.resetSprint();
    }
}
