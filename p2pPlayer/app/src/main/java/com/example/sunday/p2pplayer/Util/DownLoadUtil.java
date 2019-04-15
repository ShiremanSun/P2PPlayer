package com.example.sunday.p2pplayer.Util;

import com.turn.ttorrent.client.SimpleClient;
import com.turn.ttorrent.tracker.Tracker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Sunday on 2019/4/11
 */
public class DownLoadUtil {
    SimpleClient client = new SimpleClient();
    InetAddress address;

    Tracker tracker;
    {
        try {
            tracker = new Tracker(6969);
            FilenameFilter filenameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return false;
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            address = InetAddress.getByName("188.131.249.47");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
