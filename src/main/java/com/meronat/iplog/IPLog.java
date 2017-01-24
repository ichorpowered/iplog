/*
 * This file is part of IPLog, licensed under the MIT License.
 *
 * Copyright (c) 2017 Meronat <http://meronat.com>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.meronat.iplog;

import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.meronat.iplog.commands.AddCommand;
import com.meronat.iplog.commands.AliasCommand;
import com.meronat.iplog.commands.BaseCommand;
import com.meronat.iplog.commands.HelpCommand;
import com.meronat.iplog.commands.HistoryCommand;
import com.meronat.iplog.commands.IpElement;
import com.meronat.iplog.commands.LookupCommand;
import com.meronat.iplog.commands.PurgeCommand;
import com.meronat.iplog.storage.Storage;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(
    id = "iplog",
    name = "IPLog",
    version = "0.2.0",
    description = "Connects IP addresses to users, allowing you to catch alternates and similar.",
    url = "http://meronat.com",
    authors = {"Meronat", "Nighteyes604", "Redrield"})
public final class IPLog {

    private static IPLog plugin;

    private Logger logger;
    private Storage storage;
    private Path parentPath;
    private PluginContainer pluginContainer;

    private static TaskChainFactory factory;

    @Inject
    public IPLog(Logger logger, @ConfigDir(sharedRoot = false) Path path, PluginContainer pluginContainer) {

        plugin = this;

        this.logger = logger;
        this.parentPath = path;
        this.pluginContainer = pluginContainer;

    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {

        factory = SpongeTaskChainFactory.create(pluginContainer);

        try {

            storage = new Storage();

        } catch (SQLException e) {

            this.logger.warn("IPLog will not load as it failed to connect or load storage.");

            e.printStackTrace();

            return;

        }

        registerCommands();

        Sponge.getEventManager().registerListeners(this, new JoinListener());

    }

    private void registerCommands() {

        Map<List<String>, CommandSpec> children = new HashMap<>();

        children.put(Lists.newArrayList("help", "helpme", "?"), CommandSpec.builder()
            .description(Text.of("Displays command information for IPLog."))
            .permission("iplog.viewer.help")
            .executor(new HelpCommand())
            .build());

        children.put(Lists.newArrayList("add", "plus", "create", "put"), CommandSpec.builder()
            .description(Text.of("Creates a connect between the specified player and IP in the registry."))
            .permission("iplog.admin.add")
            .arguments(
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))),
                GenericArguments.optional(GenericArguments.onlyOne(new IpElement(Text.of("ip")))))
            .executor(new AddCommand())
            .build());

        children.put(Lists.newArrayList("purge", "remove", "delete"), CommandSpec.builder()
            .description(Text.of("Removes a connection between the specified player and IP in the registry."))
            .permission("iplog.admin.purge")
            .arguments(
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))),
                GenericArguments.optional(GenericArguments.onlyOne(new IpElement(Text.of("ip")))))
            .executor(new PurgeCommand())
            .build());

        children.put(Lists.newArrayList("alias", "alts", "alternates", "related"), CommandSpec.builder()
            .description(Text.of("Dispays all players associated with the specified player in the registry."))
            .permission("iplog.viewer.alias")
            .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))))
            .executor(new AliasCommand())
            .build());

        children.put(Lists.newArrayList("lookup", "look", "search"), CommandSpec.builder()
            .description(Text.of("Displays all IPs associated with a player or all players associated with an IP."))
            .permission("iplog.viewer.lookup")
            .arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))),
                GenericArguments.optionalWeak(GenericArguments.onlyOne(new IpElement(Text.of("ip")))))
            .executor(new LookupCommand())
            .build());

        children.put(Lists.newArrayList("history", "past", "dates"), CommandSpec.builder()
            .description(Text.of("Displays the login history of an IP or a player."))
            .permission("iplog.viewer.history")
            .arguments(
                GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))),
                GenericArguments.optionalWeak(GenericArguments.onlyOne(new IpElement(Text.of("ip")))))
            .executor(new HistoryCommand())
            .build());

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("Displays basic information about the IPLog plugin."))
            .permission("iplog.viewer")
            .executor(new BaseCommand())
            .children(children)
            .build(), "ip", "iplog", "ipregister");

        this.logger.info("Commands have been successfully registered.");

    }

    public Path getParentPath() {

        return this.parentPath;

    }

    public Storage getStorage() {

        return this.storage;

    }

    public Logger getLogger() {

        return this.logger;

    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public static IPLog getPlugin() {

        return plugin;

    }

    public static <T> TaskChain<T> newChain() {
        return factory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return factory.newSharedChain(name);
    }
}
