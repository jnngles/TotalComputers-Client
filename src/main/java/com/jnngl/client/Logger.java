package com.jnngl.client;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static String pattern = "HH:mm:ss";

    private static class LoggerInputStream extends InputStream {

        private final InputStream base;

        public LoggerInputStream(InputStream base) {
            this.base = base;
        }

        public void internalRead(int b) throws IOException {
            if(b == 10) LoggerOutputStream.newline = true;
            if(b != 0) ((LoggerPrintStream)System.out).getOut().logToFile(b);
        }

        @Override
        public int read() throws IOException {
            int b = base.read();
            internalRead(b);
            return b;
        }

        @Override
        public byte[] readAllBytes() throws IOException {
            byte[] b = base.readAllBytes();
            for(byte a : b) {
                internalRead(a);
            }
            return b;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int r = base.read(b, off, len);
            for(int i = off; i < off+r; i++) {
                internalRead(b[i]);
            }
            return r;
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            return base.read(b);
        }

        @Override
        public byte[] readNBytes(int len) throws IOException {
            byte[] b = base.readNBytes(len);
            for(byte a : b) internalRead(a);
            return b;
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            int r = base.readNBytes(b, off, len);
            for(int i = off; i < off+r; i++) internalRead(b[i]);
            return r;
        }

        @Override
        public synchronized void reset() throws IOException {
            base.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return base.skip(n);
        }

        @Override
        public void skipNBytes(long n) throws IOException {
            base.skipNBytes(n);
        }

        @Override
        public void close() throws IOException {
            base.close();
        }
    }

    private static class LoggerOutputStream extends FileOutputStream {

        private final String channel;
        private final PrintStream old;

        protected static boolean newline = true;

        public LoggerOutputStream(File file, String channel, PrintStream old)
                throws FileNotFoundException {
            super(file, true);
            this.channel = channel;
            this.old = old;
        }

        public void logToFile(int b) throws IOException {
            super.write(b);
        }

        @Override
        public void write(int b) throws IOException {
            if(newline) {
                newline = false;

                SimpleDateFormat format = new SimpleDateFormat(pattern);
                write(("["+format.format(new Date())+" "+Thread.currentThread().getName()+"/"+channel+"] ").getBytes(StandardCharsets.UTF_8));
            }
            if(b == 10) {
                flush();
                newline = true;
            }
            super.write(b);
            old.write(b);
            old.flush();
        }

        @Override
        public void write(byte @NotNull [] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            int prevX = off;
            for(int i = off; i < off+len; i++) {
                if(newline) {
                    newline = false;

                    SimpleDateFormat format = new SimpleDateFormat(pattern);
                    write(("["+format.format(new Date())+" "+Thread.currentThread().getName()+"/"+channel+"] ").getBytes(StandardCharsets.UTF_8));
                }
                if(b[i] == 10) {
                    super.write(b, prevX, i-prevX+1);
                    old.write(b, prevX, i-prevX+1);
                    old.flush();
                    prevX = i+1;
                    flush();
                    newline = true;
                }
            }
            super.write(b, prevX, len-prevX+off);
            old.write(b, prevX, len-prevX+off);
            old.flush();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            old.flush();
        }

    }

    private static class LoggerPrintStream extends PrintStream {

        public LoggerPrintStream(LoggerOutputStream out) {
            super(out, true, Charset.forName("cp1251"));
        }

        public LoggerOutputStream getOut() {
            return (LoggerOutputStream) out;
        }

    }

    public static void initializeLogger() throws IOException {
        File log = null;
        while(log == null || !log.createNewFile())
            log = new File("log_" +
                    new SimpleDateFormat("dd.MM.yyyy-HH.mm.ss").format(new Date()) + ".log");

        System.setOut(new LoggerPrintStream(new LoggerOutputStream(log, "INFO", System.out)));
        System.setErr(new LoggerPrintStream(new LoggerOutputStream(log,"ERR", System.err)));
        System.setIn(new LoggerInputStream(System.in));
    }

}
