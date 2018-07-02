/*
 * Copyright 2018 David Cooke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.co.drcooke.antijigsaw;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.ByteBuf;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Processes all custom payload packets. If the packets are too big and sent on certain channels, it will
 * kick the user with a configurable message.
 *
 * @author David Cooke
 */
public class CustomPayloadBlocker extends PacketAdapter {
    
    private final boolean kick;
    private final boolean ban;
    /**
     * There is a (very small) chance that the user has legitimately sent a very large packet on the same channel as
     * Jigsaw to avoid kicking users unfairly, their uuid is added to a list, if they then send a second very large packet,
     * they will be kicked. This list is cleared 4 times every second.
     */
    ArrayList<UUID> uuids = new ArrayList<>();
    /**
     * The maximum capacity of the buffer in custom payload packets
     */
    private int maxCapacity;
    /**
     * The channels that should be monitored
     */
    private List<String> channels;
    /**
     * The message that should be used to kick the user.
     */
    private String message;
    
    /**
     * Constructs the custom payload monitor. Reads the settings from the config into variables.
     *
     * @param plugin instance of AntiJigsaw
     * @param config the configuration of AntiJigsaw
     */
    CustomPayloadBlocker(Plugin plugin, ConfigurationSection config) {
        super(plugin, PacketType.Play.Client.CUSTOM_PAYLOAD);
        this.maxCapacity = config.getInt("max-size");
        this.channels = config.getStringList("channels");
        this.message = config.getString("message");
        this.kick = config.getBoolean("kick");
        this.ban = config.getBoolean("ban");
    }
    
    /**
     * Listens for custom payload packets and evaluates whether they are legitimate.
     *
     * @param event the event from protocollib
     */
    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (channels.contains(event.getPacket().getStrings().getValues().get(0))
                && ((ByteBuf) event.getPacket().getModifier().getValues().get(1)).capacity() > maxCapacity) {
            if (uuids.contains(event.getPlayer().getUniqueId())) {
                if (kick) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                            () -> event.getPlayer().kickPlayer(message));
                }
                if (ban) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                            () -> event.getPlayer().getServer().getBanList(BanList.Type.NAME)
                                    .addBan(event.getPlayer().getName(), message, null, null));
                }
            } else {
                uuids.add(event.getPlayer().getUniqueId());
            }
            event.setCancelled(true);
        }
    }
    
}
