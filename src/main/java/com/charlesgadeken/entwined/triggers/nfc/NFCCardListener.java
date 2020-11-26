package com.charlesgadeken.entwined.triggers.nfc;

interface NFCCardListener {
    void onReaderAdded(String reader);

    void onReaderRemoved(String reader);

    void onCardAdded(String reader, String cardId);

    void onCardRemoved(String reader, String cardId);
}
