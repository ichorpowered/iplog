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

import com.meronat.iplog.IPLog;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AliasCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<User> optionalUser = args.getOne("player");

        if (!optionalUser.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "You must specify an existing user."));
        }

        User user = optionalUser.get();

        IPLog.newChain()
                .asyncFirst(() -> {
                    Set<UUID> users = IPLog.getPlugin().getStorage().getAliases(user.getUniqueId());
                    if (src instanceof User) {
                        UUID sender = ((User) src).getUniqueId();
                        if (sender.equals(user.getUniqueId())) {
                            users.remove(sender);
                        }
                    }
                    if (users.size() == 0) {
                        src.sendMessage(Text.of(TextColors.RED, "There are no aliases associated with the specified user."));
                        return null;
                    }
                    return users;
                })
                .abortIfNull()
                .syncLast(users -> {
                    UserStorageService userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).get();
                    Sponge.getServiceManager().provide(PaginationService.class).get().builder()
                            .title(Text.of(TextColors.DARK_GREEN, "Aliases of ", TextColors.GREEN, user.getName()))
                            .contents(users.stream()
                                .map(userStorageService::get)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(User::getName)
                                .map(Text::of)
                                .map(username -> Text.of(TextColors.DARK_GREEN, username))
                                .collect(Collectors.toList()))
                            .linesPerPage(14)
                            .padding(Text.of(TextColors.GRAY, "="))
                            .sendTo(src);
                }).execute();

        return CommandResult.success();

    }

}
