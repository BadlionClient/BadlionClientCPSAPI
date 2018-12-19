package net.badlion.blccpsapibungee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.badlion.blccpsapibungee.listener.PlayerListener;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class BlcCpsApiBungee extends Plugin {

	public static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
	private static final Gson GSON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().setPrettyPrinting().create();

	private Conf conf;

	@Override
	public void onEnable() {
		if (!this.getDataFolder().exists()) {
			if (!this.getDataFolder().mkdir()) {
				this.getLogger().log(Level.SEVERE, "Failed to create plugin directory.");
			}
		}

		try {
			this.conf = loadConf(new File(this.getDataFolder(), "config.json"));

			// Only register the listener if the config loads successfully
			this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));

			this.getLogger().log(Level.INFO, "Successfully setup BadlionClientCPSAPI plugin.");
		} catch (Exception e) {
			this.getLogger().log(Level.SEVERE, "Error with config for BadlionClientCPSAPI plugin.");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {

	}

	private Conf loadConf(File file) throws IOException {

		try (FileReader fileReader = new FileReader(file)) {
			return BlcCpsApiBungee.GSON_NON_PRETTY.fromJson(fileReader, Conf.class);
		} catch (FileNotFoundException ex) {
			this.getLogger().log(Level.INFO, "No Config Found: Saving default...");
			Conf conf = new Conf();
			this.saveConf(conf, file);
			return conf;
		}
	}

	private void saveConf(Conf conf, File file) {

		try (FileWriter fileWriter = new FileWriter(file)) {
			BlcCpsApiBungee.GSON_PRETTY.toJson(conf, fileWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Conf getConf() {
		return this.conf;
	}
}
