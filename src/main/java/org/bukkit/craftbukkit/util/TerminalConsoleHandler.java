package org.bukkit.craftbukkit.util;

import org.bukkit.craftbukkit.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalConsoleHandler extends ConsoleHandler {
    // Duckweed start - jline update
    private final org.jline.reader.LineReader reader;

    public TerminalConsoleHandler(org.jline.reader.LineReader reader) {
        super();
        if (Main.useJline) {
            this.setOutputStream(new JLineOutputStream());
        }
        this.reader = reader;
    }

    private class JLineOutputStream extends java.io.ByteArrayOutputStream {
        private JLineOutputStream() {
            super(1024);
        }

        @Override
        public synchronized void flush() throws IOException {
            reader.printAbove(this.toString("UTF-8"));
            this.count = 0;
        }
    }
    // Duckweed end
}
