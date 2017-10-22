package uk.co.drcooke.antijigsaw;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiJigsaw extends JavaPlugin{

    public void onEnable(){
        saveDefaultConfig();
        CustomPayloadBlocker listener = new CustomPayloadBlocker(this, getConfig());
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> listener.uuids.clear(), 5);
    }

}
