package net.badlion.blccpsapibungee.listener;

import net.badlion.blccpsapibungee.BlcCpsApiBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class PlayerListener implements Listener {

	private BlcCpsApiBungee plugin;

	public PlayerListener(BlcCpsApiBungee plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onLogin(PostLoginEvent event) {
		// Send the click speed limitation to players when they login to the proxy. A notification will appear on the Badlion Client so they know their CPS has been limited
		ProxiedPlayer player = event.getPlayer();
		if ( player.getPendingConnection().getVersion() <= ProtocolConstants.MINECRAFT_1_12_2 ) { // do not send to 1.13+ clients, this would break the connection
			player.unsafe().sendPacket( new PluginMessage( "BLC|C", BlcCpsApiBungee.GSON_NON_PRETTY.toJson( this.plugin.getConf() ).getBytes(), false ) );
		}
	}
}
