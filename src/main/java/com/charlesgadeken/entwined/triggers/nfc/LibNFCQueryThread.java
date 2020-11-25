package com.charlesgadeken.entwined.triggers.nfc;

import java.util.Arrays;

public class LibNFCQueryThread extends Thread {
    LibNFC.Reader reader;
    boolean running;
    LibNFC.card_id old_cid;
    LibNFC.card_id zero_cid;
    NFCCardListener card_reader;

    public LibNFCQueryThread(LibNFC nfc, LibNFC.Reader r, NFCCardListener cr) {
        reader = r;
        zero_cid = old_cid = nfc.new card_id(0);
        card_reader = cr;
    }

    public void start() {
        running = true;
        super.start();
    }

    public void run() {
        while (running) {
            LibNFC.card_id cid = reader.get_card_id();

            if (!Arrays.equals(cid.id, old_cid.id)) {
                if (Arrays.equals(cid.id, zero_cid.id))
                    card_reader.onCardRemoved(reader.connstring, old_cid.toString());
                else card_reader.onCardAdded(reader.connstring, cid.toString());
                // println("Card changed:", reader, cid);
            }
            old_cid = cid;
        }
    }

    void quit() {
        running = false;
        interrupt();
    }
}
