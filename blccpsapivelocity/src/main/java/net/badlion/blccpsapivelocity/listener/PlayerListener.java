package net.badlion.blccpsapivelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;

import net.badlion.blccpsapivelocity.BlcCpsApiVelocity;

public class PlayerListener {

    private BlcCpsApiVelocity plugin;

    public PlayerListener(BlcCpsApiVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(PostLoginEvent event) {
        // Send the click speed limitation to players when they login to the proxy. A notification will appear on the Badlion Client so they know their CPS has been limited
        Player player = event.getPlayer();
        player.sendPluginMessage(this.plugin.getBlcCpsChannel(), BlcCpsApiVelocity.GSON_NON_PRETTY.toJson(this.plugin.getConf()).getBytes());
    }
}
