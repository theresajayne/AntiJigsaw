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
