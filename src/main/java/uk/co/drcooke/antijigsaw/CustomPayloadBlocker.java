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
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
    
    /**
     * There is a (very small) chance that the user has legitimately sent a very large packet on the same channel as
     * Jigsaw to avoid kicking users unfairly, their uuid is added to a list, if they then send a second very large packet,
     * they will be kicked. This list is cleared 4 times every second.
     */
    List<UUID> uuids = new ArrayList<>();
    
    /**
     * The maximum capacity of the buffer in custom payload packets
     */
    private int maxCapacity;
    
    /**
     * The channels that should be monitored.
     */
    private List<String> channels;
    
    /**
     * The commands to dispatch when the exploit is used.
     */
    private List<String> commands;
    
    /**
     * Bukkit doesn't allow async player kicks so the commands have to be delayed by 1 tick. During
     * this tick, loads of packets will be received and the commands will be spammed so this variable
     * is used to keep track of if the commands have been ran for a particular player.
     */
    private List<UUID> waitingUUIDs = new ArrayList<>();
    
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
        this.commands = config.getStringList("commands");
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
                dispatchCommands(event.getPlayer());
            } else {
                uuids.add(event.getPlayer().getUniqueId());
            }
            event.setCancelled(true);
        }
    }
    
    private void dispatchCommands(Player player) {
        if(waitingUUIDs.contains(player.getUniqueId()))
            return;
        waitingUUIDs.add(player.getUniqueId());
        
        for(String command : commands) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    command.replace("%player%", player.getName())), 1);
        }
    }
    
}
