/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint;

import dev.xavier.stein.loader.api.Keys;
import dev.xavier.stein.loader.api.Lang;
import dev.xavier.stein.loader.api.Option;
import dev.xavier.stein.loader.api.Page;

/**
 * Sprint's page in the Panel: the two toggles with their keys, and how the game's own keys behave.
 * The HUD text's options live on the element itself (HUD editor).
 */
final class SprintPage {

    /** Sub-options are indented under their option. */
    private static final String INDENT = "  ";

    private SprintPage() {}

    /** Builds the page; the Panel calls it every time it opens. */
    static void build(SteinSprint mod, Page page) {
        SprintConfig c = mod.config();
        page.icon("run");

        page.section(Lang.tr("steinsprint.section.sprint"));
        page.option(
                Option.toggle(
                                Lang.tr("steinsprint.toggleSprint"),
                                () -> c.toggleSprint,
                                v -> {
                                    c.toggleSprint = v;
                                    mod.save();
                                })
                        .withKey(mod.sprintKey::key, mod.sprintKey::setKey));
        page.option(
                Option.toggle(
                        INDENT + Lang.tr("steinsprint.doubleTapSprint"),
                        () -> c.doubleTapSprint,
                        v -> {
                            c.doubleTapSprint = v;
                            mod.save();
                        }));

        page.section(Lang.tr("steinsprint.section.sneak"));
        page.option(
                Option.toggle(
                                Lang.tr("steinsprint.toggleSneak"),
                                () -> c.toggleSneak,
                                v -> {
                                    c.toggleSneak = v;
                                    mod.save();
                                })
                        .withKey(mod.sneakKey::key, mod.sneakKey::setKey));
        page.option(
                Option.toggle(
                        INDENT + Lang.tr("steinsprint.unsneakOnFly"),
                        () -> c.unsneakOnFly,
                        v -> {
                            c.unsneakOnFly = v;
                            mod.save();
                        }));

        page.section(Lang.tr("steinsprint.section.keys"));
        page.option(
                Option.toggle(
                        Lang.tr("steinsprint.gameKeysToggle"),
                        () -> c.gameKeysToggle,
                        v -> {
                            c.gameKeysToggle = v;
                            mod.save();
                        }));
        page.restore(
                () -> {
                    SprintConfig d = new SprintConfig();
                    c.toggleSprint = d.toggleSprint;
                    c.toggleSneak = d.toggleSneak;
                    c.doubleTapSprint = d.doubleTapSprint;
                    c.unsneakOnFly = d.unsneakOnFly;
                    c.gameKeysToggle = d.gameKeysToggle;
                    mod.save();
                    mod.sprintKey.setKey(Keys.KEY_G);
                    mod.sneakKey.setKey(Keys.KEY_NONE);
                });
    }
}
