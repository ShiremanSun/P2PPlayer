package com.example.sunday.p2pplayer.bittorrent;

import java.io.File;

/**
 * Created by sunday on 19-4-25.
 */

public final class BTContext {
    public File homeDir;
    public File torrentsDir;
    public File dataDir;
    public String interfaces;
    public int retries;
    public boolean optimizeMemory;
    public final int[] version = {0,0,0,0};

    /**
     * Indicates if the engine starts with the DHT enable.
     */
    public boolean enableDht = true;
}
