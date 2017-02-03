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

package com.meronat.iplog.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        List<Text> contents = new ArrayList<>();

        contents.add(formatHelpText("/ip", "Displays basic information about IPLog.", Text.of("IPLog v0.1.1")));

        contents.add(formatHelpText("/ip help", "Displays this page, giving information about IPLog commands.", Text.of("Click here for IPLog help")));

        contents.add(formatHelpText("/ip alias [player]", "Shows all possible players associated with this player.", Text.of("Good for finding alternate accounts")));

        contents.add(formatHelpText("/ip lookup [player]", "Lists all the IPs associated with the specified player.", Text.of("Can also be used with IPs")));

        contents.add(formatHelpText("/ip lookup [ip]", "Lists all the players associated with the specified IP.", Text.of("Can also be used with users")));

        contents.add(formatHelpText("/ip history [player]", "Displays all IPs associated with a player and their last date of login", Text.of("Can also be used with IPs")));

        contents.add(formatHelpText("/ip history [ip]", "Displays all users associated with an IP and their last date of login", Text.of("Can also be used with users")));

        contents.add(formatHelpText("/ip add [player] [ip]", "Adds a connection between a player and an IP", Text.of("You must specify both")));

        contents.add(formatHelpText("/ip purge [player] [ip]", "Removes the connection between a player and an IP", Text.of("You must specify both")));

        Sponge.getServiceManager().provide(PaginationService.class).get().builder()
            .title(Text.of(TextColors.DARK_GREEN, "IPLog Help"))
            .linesPerPage(14)
            .padding(Text.of(TextColors.GRAY, "="))
            .contents(contents)
            .sendTo(src);

        return CommandResult.success();

    }

    private Text formatHelpText(String command, String description, Text extendedDescription) {
        return Text.of(Text.builder(command)
            .color(TextColors.GREEN)
            .onClick(TextActions.suggestCommand(command))
            .onHover(TextActions.showText(extendedDescription))
            .build(),Text.of(TextColors.GRAY, " - ", description));
    }

}
