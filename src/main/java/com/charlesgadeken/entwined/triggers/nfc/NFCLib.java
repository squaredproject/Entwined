package com.charlesgadeken.entwined.triggers.nfc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

interface NFCLib extends Library {
    NFCLib INSTANCE = (NFCLib) Native.loadLibrary("nfc", NFCLib.class);

    void nfc_init(PointerByReference context);

    Pointer nfc_open(Pointer context, String connstring);

    int nfc_initiator_init(Pointer p);

    int nfc_list_devices(Pointer context, Pointer connstring, int connstring_len);

    int nfc_device_set_property_bool(Pointer pnd, int property, boolean value);

    // list_passive just calls select_passive
    int nfc_initiator_select_passive_target(
            Pointer pnd, long nm, Pointer pbtInitData, int szInitData, LibNFC.nfc_target nt);

    int nfc_initiator_list_passive_targets(
            Pointer pnd, long nm, LibNFC.nfc_target ant[], long szTargets);

    // list/select_passive blocks for 5 seconds if there is no card.
    int nfc_initiator_poll_target(
            Pointer pnd, Pointer pnm, int size_pnm, int PollNr, int uiPeriod, LibNFC.nfc_target nt);

    String nfc_device_get_name(Pointer p);

    void nfc_close(Pointer pnd);

    void nfc_exit(Pointer context);
}
