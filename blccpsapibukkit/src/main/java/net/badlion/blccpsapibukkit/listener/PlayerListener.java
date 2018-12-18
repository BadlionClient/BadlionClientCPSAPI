package net.badlion.blccpsapibukkit.listener;

import net.badlion.blccpsapibukkit.BlcCpsApiBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class PlayerListener implements Listener {

    private BlcCpsApiBukkit plugin;
    private String versionSuffix;

    // Reflection stuff
    private Class<?> craftPlayerClass;
    private Method getHandleMethod;

    private Class<?> nmsPlayerClass;
    private Field playerConnectionField;

    private Class<?> playerConnectionClass;
    private Method sendPacketMethod;

    private Class<?> packetPlayOutCustomPayloadClass;
    private Constructor<?> packetPlayOutCustomPayloadConstructor;

    // Bukkit 1.8+ support
    private Class<?> packetDataSerializerClass;
    private Constructor<?> packetDataSerializerConstructor;

    // Netty classes used by newer 1.8 and newer
    private Class<?> byteBufClass;
    private Class<?> unpooledClass;
    private Method wrappedBufferMethod;

    public PlayerListener(BlcCpsApiBukkit plugin) {
        this.plugin = plugin;

        // Get the v1_X_Y from the end of the package name, e.g. v_1_7_R4 or v_1_12_R1
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");

        if (parts.length > 0) {
            String suffix = parts[parts.length - 1];
            if (!suffix.startsWith("v")) {
                throw new RuntimeException("Failed to find version for running Minecraft server, got suffix " + suffix);
            }

            this.versionSuffix = suffix;

            this.plugin.getLogger().info("Found version " + this.versionSuffix);
        }

        // We need to use reflection because Bukkit by default handles plugin messages in a really silly way
        this.craftPlayerClass = this.getClass("org.bukkit.craftbukkit." + this.versionSuffix + ".entity.CraftPlayer");
        if (this.craftPlayerClass == null) {
            throw new RuntimeException("Failed to find CraftPlayer class");
        }

        this.nmsPlayerClass = this.getClass("net.minecraft.server." + this.versionSuffix + ".EntityPlayer");
        if (this.nmsPlayerClass == null) {
            throw new RuntimeException("Failed to find EntityPlayer class");
        }

        this.playerConnectionClass = this.getClass("net.minecraft.server." + this.versionSuffix + ".PlayerConnection");
        if (this.playerConnectionClass == null) {
            throw new RuntimeException("Failed to find PlayerConnection class");
        }

        this.packetPlayOutCustomPayloadClass = this.getClass("net.minecraft.server." + this.versionSuffix + ".PacketPlayOutCustomPayload");
        if (this.packetPlayOutCustomPayloadClass == null) {
            throw new RuntimeException("Failed to find PacketPlayOutCustomPayload class");
        }

        this.packetPlayOutCustomPayloadConstructor = this.getConstructor(this.packetPlayOutCustomPayloadClass, String.class, byte[].class);
        if (this.packetPlayOutCustomPayloadConstructor == null) {
            // Newer versions of Minecraft use a different custom packet system
            this.packetDataSerializerClass = this.getClass("net.minecraft.server." + this.versionSuffix + ".PacketDataSerializer");
            if (this.packetDataSerializerClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer class");
            }

            this.byteBufClass = this.getClass("io.netty.buffer.ByteBuf");
            if (this.byteBufClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or ByteBuf class");
            }

            this.packetDataSerializerConstructor = this.getConstructor(this.packetDataSerializerClass, this.byteBufClass);
            if (this.packetDataSerializerConstructor == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer constructor");
            }

            this.unpooledClass = this.getClass("io.netty.buffer.Unpooled");
            if (this.unpooledClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or Unpooled class");
            }

            this.wrappedBufferMethod = this.getMethod(this.unpooledClass, "wrappedBuffer", byte[].class);
            if (this.wrappedBufferMethod == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or wrappedBuffer()");
            }

            // If we made it this far in theory we are on at least 1.8
            this.packetPlayOutCustomPayloadConstructor = this.getConstructor(this.packetPlayOutCustomPayloadClass, String.class, this.packetDataSerializerClass);
            if (this.packetPlayOutCustomPayloadConstructor == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor 2x");
            }
        }

        this.getHandleMethod = this.getMethod(this.craftPlayerClass, "getHandle");
        if (this.getHandleMethod == null) {
            throw new RuntimeException("Failed to find CraftPlayer.getHandle()");
        }

        this.playerConnectionField = this.getField(this.nmsPlayerClass, "playerConnection");
        if (this.playerConnectionField == null) {
            throw new RuntimeException("Failed to find EntityPlayer.playerConnection");
        }

        this.sendPacketMethod = this.getMethod(this.playerConnectionClass, "sendPacket");
        if (this.sendPacketMethod == null) {
            throw new RuntimeException("Failed to find PlayerConnection.sendPacket()");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Create data we need for packet;
        String channel = "BLC|C";
        byte[] message = BlcCpsApiBukkit.GSON_NON_PRETTY.toJson(this.plugin.getConf()).getBytes();

        try {
            Object packet;

            // Newer MC version, setup ByteBuf object
            if (this.packetDataSerializerClass != null) {
                Object byteBuf = this.wrappedBufferMethod.invoke(null, message);
                Object packetDataSerializer = this.packetDataSerializerConstructor.newInstance(byteBuf);

                packet = this.packetPlayOutCustomPayloadConstructor.newInstance(channel, packetDataSerializer);
            } else {
                // Work our magic to make the packet
                packet = this.packetPlayOutCustomPayloadConstructor.newInstance(channel, message);
            }

            // Work our magic to send the packet
            Object nmsPlayer = this.getHandleMethod.invoke(player);
            Object playerConnection = this.playerConnectionField.get(nmsPlayer);
            this.sendPacketMethod.invoke(playerConnection, packet);
        } catch (IllegalAccessException e) {
            this.plugin.getLogger().severe("Failed to send BLC CPS packet");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            this.plugin.getLogger().severe("Failed to send BLC CPS packet");
            e.printStackTrace();
        } catch (InstantiationException e) {
            this.plugin.getLogger().severe("Failed to send BLC CPS packet");
            e.printStackTrace();
        }
    }

    public Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Constructor<?> getConstructor(Class<?> clazz, Class<?> ...params) {
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }

        return null;
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (params.length > 0) {
                    if (Arrays.equals(method.getParameterTypes(), params)) {
                        method.setAccessible(true);
                        return method;
                    }
                } else {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

    public Field getField(Class<?> clazz, String fieldName) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }

        return null;
    }

}
