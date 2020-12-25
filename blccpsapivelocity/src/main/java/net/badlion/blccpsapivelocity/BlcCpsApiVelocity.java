package net.badlion.blccpsapivelocity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import net.badlion.blccpsapivelocity.listener.PlayerListener;

@Plugin(id = "blccpsapivelocity", name = "BadlionClientCPSAPI", version = "1.0", authors = {"Badlion"})
public final class BlcCpsApiVelocity {

    public static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().setPrettyPrinting().create();

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataFolderPath;
    private Conf conf;
    private MinecraftChannelIdentifier blcCpsChannel;

    @Inject
    public BlcCpsApiVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataFolderPath) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataFolderPath = dataFolderPath;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        final File dataFolder = this.dataFolderPath.toFile();
        
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdir()) {
                this.logger.error("Failed to create plugin directory.");
            }
        }
        
        try {
            this.conf = loadConf(new File(dataFolder, "config.json"));

            this.blcCpsChannel = MinecraftChannelIdentifier.create("badlion", "cps");

            // Only register the listener if the config loads successfully
            this.proxy.getEventManager().register(this, new PlayerListener(this));

            this.logger.info("Successfully setup BadlionClientCPSAPI plugin.");
        } catch (Exception e) {
            this.logger.error("Error with config for BadlionClientCPSAPI plugin.");
            e.printStackTrace();
        }
    }

    private Conf loadConf(File file) throws IOException {

        try (FileReader fileReader = new FileReader(file)) {
            return BlcCpsApiVelocity.GSON_NON_PRETTY.fromJson(fileReader, Conf.class);
        } catch (FileNotFoundException ex) {
            this.logger.info("No Config Found: Saving default...");
            Conf conf = new Conf();
            this.saveConf(conf, file);
            return conf;
        }
    }

    private void saveConf(Conf conf, File file) {

        try (FileWriter fileWriter = new FileWriter(file)) {
            BlcCpsApiVelocity.GSON_PRETTY.toJson(conf, fileWriter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Conf getConf() {
        return this.conf;
    }

    public MinecraftChannelIdentifier getBlcCpsChannel() {
        return this.blcCpsChannel;
    }
}
