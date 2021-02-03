package com.down.ui;

import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class DownloadHistory {
    private static final String historyInfo = "Xdownloader.info";

    public static synchronized String[] getHistory() {
        File file = new File(System.getProperty("user.dir"), historyInfo);
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                List<String> list = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        list.add(line);
                    }
                }
                reader.close();
                String[] strings = new String[list.size()];
                list.toArray(strings);
                return strings;
            } catch (Exception e) {
                return new String[0];
            }
        } else {
            return new String[0];
        }
    }

    public static synchronized void addHistory(String url) {
        File file = new File(System.getProperty("user.dir"), historyInfo);
        try {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true));
            printWriter.println(url);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void clearHistory() {
        File file = new File(System.getProperty("user.dir"), historyInfo);
        file.delete();
    }
}
