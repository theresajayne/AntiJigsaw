package uk.co.drcooke.antijigsaw;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.UUID;

public class CustomPayloadBlocker extends PacketAdapter{

    public CustomPayloadBlocker(Plugin plugin) {
        super(plugin, PacketType.Play.Client.CUSTOM_PAYLOAD);
    }

    public ArrayList<UUID> uuids = new ArrayList<>();

    @Override
    public void onPacketReceiving(PacketEvent event){
        if(event.getPacket().getStrings().getValues().get(0).equals("MC|BEdit") || event.getPacket().getStrings().getValues().get(0).equals("MC|BSign")){
            if(((ByteBuf)event.getPacket().getModifier().getValues().get(1)).capacity()>30000){
                if(uuids.contains(event.getPlayer().getUniqueId())){
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->event.getPlayer().kickPlayer("Jigsaw Crash"));
                }else{
                    uuids.add(event.getPlayer().getUniqueId());
                }
                event.setCancelled(true);
            }
        }
    }

}
