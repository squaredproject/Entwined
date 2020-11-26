package com.charlesgadeken.entwined.triggers.nfc;

import java.util.ArrayList;

class LibNFCMainThread extends Thread {
    LibNFC nfc;
    int start;
    ArrayList<LibNFCQueryThread> threads;
    NFCCardListener card_reader;

    public LibNFCMainThread(LibNFC n, NFCCardListener c) {
        nfc = n;
        card_reader = c;
        threads = new ArrayList<>();
    }

    public void run() {
        while (true) {
            try {
                nfc.connect_to_readers();
            } catch (Exception e) {
                System.out.println("Lib error connecting to readers: " + e);
                break;
            }
            // start threads that need to start
            for (LibNFC.Reader r : nfc.nd_list) {
                boolean found = false;

                for (LibNFCQueryThread t : threads) {
                    if (t.reader == r) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    LibNFCQueryThread t = new LibNFCQueryThread(nfc, r, card_reader);
                    t.start();
                    threads.add(t);
                    System.out.println("Added thread for " + r);
                    card_reader.onReaderAdded(r.connstring);
                }
            }
            // stop threads that need to stop
            for (LibNFCQueryThread t : threads) {
                if (!nfc.nd_list.contains(t.reader) && t.running) {
                    System.out.println("Quit thread for " + t.reader);
                    t.quit();
                    // remove t from threads
                    card_reader.onReaderRemoved(t.reader.connstring);
                }
            }
            try {
                sleep(5000);
            } catch (Exception e) {
                System.out.println("Sleep in LibNFCMainThread failed.");
            }
        }
    }
}
