package net.minecraft.server;

import java.io.IOException;

public class ThreadCommandReader extends Thread {

    final MinecraftServer server;

    public ThreadCommandReader(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
    }

    public void run() {
        org.jline.reader.LineReader bufferedreader = this.server.reader; // Duckweed - jline update
        String s = null;

            while (!this.server.isStopped && MinecraftServer.isRunning(this.server)) {
                if (org.bukkit.craftbukkit.Main.useJline) {
                    s = bufferedreader.readLine(">", null);
                } else {
                    s = bufferedreader.readLine();
                }
                if (s != null) {
                    this.server.issueCommand(s, this.server);
                }
                // CraftBukkit end
            }
    }
}
