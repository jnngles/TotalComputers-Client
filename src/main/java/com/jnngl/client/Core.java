package com.jnngl.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class Core {

    private ClassLoader child = this.getClass().getClassLoader();

    public void loadCore(File file) throws IOException {
        if(!file.exists()) throw new FileNotFoundException("Unable to load core file");
        child = new URLClassLoader(new URL[] { file.toURI().toURL() }, Core.class.getClassLoader());
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, child);
    }

}
