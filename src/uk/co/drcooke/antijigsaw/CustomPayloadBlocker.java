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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomPayloadBlocker extends PacketAdapter{

    private int maxCapacity;
    private List<String> channels;
    private String message;
    ArrayList<UUID> uuids = new ArrayList<>();

    CustomPayloadBlocker(Plugin plugin, ConfigurationSection config) {
        super(plugin, PacketType.Play.Client.CUSTOM_PAYLOAD);
        this.maxCapacity = config.getInt("max-size");
        this.channels = config.getStringList("channels");
        this.message = config.getString("message");
    }

    @Override
    public void onPacketReceiving(PacketEvent event){
        if(channels.contains(event.getPacket().getStrings().getValues().get(0))){
            if(((ByteBuf)event.getPacket().getModifier().getValues().get(1)).capacity() > maxCapacity){
                if(uuids.contains(event.getPlayer().getUniqueId())){
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->event.getPlayer().kickPlayer(message));
                }else{
                    uuids.add(event.getPlayer().getUniqueId());
                }
                event.setCancelled(true);
            }
        }
    }

}
