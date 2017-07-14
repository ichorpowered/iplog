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

package com.ichorpowered.iplog.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IpElement extends CommandElement {

    private static Text IP = LiteralText.of("<ip address>");
    private static List<String> EMPTY_LIST = new ArrayList<>();

    public IpElement( Text key) {
        super(key);
    }

    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if(!args.hasNext()) {
            throw args.createError(Text.of(TextColors.RED, "You must specify an IP address."));
        }

        try {
            return InetAddress.getByName(args.next());
        } catch (UnknownHostException e) {
            throw args.createError(Text.of(TextColors.RED, "This is not a valid IP address."));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return EMPTY_LIST;
    }

    @Override
    public Text getUsage(CommandSource src) {
        return IP;
    }

}
