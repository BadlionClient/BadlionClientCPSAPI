package net.badlion.blccpsapibukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.badlion.blccpsapibukkit.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class BlcCpsApiBukkit extends JavaPlugin {

    public static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().setPrettyPrinting().create();

    private Conf conf;

    @Override
    public void onEnable() {
        // Only support <= 1.12.2 at the moment, we will add 1.13 support when BLC 1.13 is ready
        if (this.getServer().getBukkitVersion().startsWith("1.13")) {
            this.getLogger().log(Level.SEVERE, "BLC CPS API is not currently compatible with 1.13 Minecraft. Check back later for updates.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdir()) {
                this.getLogger().log(Level.SEVERE, "Failed to create plugin directory.");
            }
        }

        try {
            this.conf = loadConf(new File(this.getDataFolder(), "config.json"));

            // Register channel
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BLC|C");

            // Only register the listener if the config loads successfully
            this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

            this.getLogger().log(Level.INFO, "Successfully setup BadlionClientCPSAPI plugin.");
        } catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Error with config for BadlionClientCPSAPI plugin : " + ex.getMessage(), ex);
        }
    }

    @Override
    public void onDisable() {

    }

    private Conf loadConf(File file) {
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(file);
            return BlcCpsApiBukkit.GSON_NON_PRETTY.fromJson(fileReader, Conf.class);
        } catch (FileNotFoundException ex) {
            this.getLogger().log(Level.INFO,"No Config Found: Saving default...");
            Conf conf = new Conf();
            this.saveConf(conf, file);
            return conf;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    private void saveConf(Conf conf, File file) {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            BlcCpsApiBukkit.GSON_PRETTY.toJson(conf, fileWriter);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    public Conf getConf() {
        return this.conf;
    }
}
