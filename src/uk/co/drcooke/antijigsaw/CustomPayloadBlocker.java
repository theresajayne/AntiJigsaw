package uk.co.drcooke.antijigsaw;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_8_R3.PacketPlayInCustomPayload;
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
        PacketPlayInCustomPayload packet = (PacketPlayInCustomPayload)event.getPacket().getHandle();
        if(packet.a().equals("MC|BEdit") || packet.a().equals("MC|BSign")){
            if(packet.b().capacity()>30000){
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
