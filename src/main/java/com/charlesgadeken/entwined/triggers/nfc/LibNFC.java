package com.charlesgadeken.entwined.triggers.nfc;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class LibNFC {
    static final int NFC_BUFSIZE_CONNSTRING = 1024;
    static final int DEVICE_NAME_LENGTH = 256;
    static final int DEVICE_PORT_LENGTH = 64;
    static final int MAX_READERS = 10;
    static final int NFC_ENOTSUCHDEV = -4;
    static final int NFC_EIO = -1;
    static final int NFC_PROPERTY_INFINITE_SELECT = 7;

    static final long DEFAULT_MODULATION = 0x100000001l;

    private PointerByReference context;
    List<Reader> nd_list;
    private int num_readers;
    private int max_num_readers;

    public class nfc_target extends Structure {
        public byte head[];
        public byte size;
        public byte padding[];
        public byte serial[];
        public byte tail[];

        public nfc_target() {
            head = new byte[3];
            padding = new byte[7];
            serial = new byte[10];
            tail = new byte[280];
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"head", "size", "padding", "serial", "tail"});
        }
    }

    public class Reader {
        public Pointer pnd;
        public String connstring;

        public Reader(Pointer pnd, String connstring) {
            this.pnd = pnd;
            this.connstring = connstring;
        }

        public LibNFC.card_id get_card_id() {
            LibNFC.nfc_target nt = new LibNFC.nfc_target();

            Memory m = new Memory(100);
            m.setByte((long) 0, (byte) 1);
            m.setByte((long) 4, (byte) 1);
            long before = System.currentTimeMillis();
            // int err = NFCLib.INSTANCE.nfc_initiator_poll_target(pnd, m.share(0), 1, 1, 1, nt);
            int err =
                    NFCLib.INSTANCE.nfc_initiator_select_passive_target(
                            pnd, DEFAULT_MODULATION, Pointer.NULL, 0, nt);
            //            println("Poll time:", System.currentTimeMillis() - before);

            if (err > 0) {
                // println("card", new LibNFC.card_id(nt));
                return new LibNFC.card_id(nt);
            }
            return new LibNFC.card_id(0);
        }

        public String toString() {
            return connstring;
        }
    }

    public class card_id {
        byte id[];

        public card_id(nfc_target nt) {
            id = new byte[nt.size];
            System.arraycopy(nt.serial, 0, id, 0, nt.size);
        }

        public card_id(int i) {
            id = new byte[i];
        }

        public String toString() {
            String s = "";
            for (byte b : id) {
                s += String.format("%02x", b);
            }
            return s;
        }
    }

    @SuppressWarnings("unchecked")
    public LibNFC() throws Exception {
        num_readers = 0;
        max_num_readers = 10;
        context = new PointerByReference();
        NFCLib.INSTANCE.nfc_init(context);
        if (context.getValue() == Pointer.NULL) {
            throw new Exception("nfc_init failed.");
        }
        nd_list = new ArrayList<>();

        Memory connstrings = new Memory(MAX_READERS * NFC_BUFSIZE_CONNSTRING);
        num_readers =
                NFCLib.INSTANCE.nfc_list_devices(
                        context.getValue(), connstrings.share(0), max_num_readers);
    }

    private LibNFC.Reader connect_reader(String connstring) throws Exception {
        Pointer pnd = NFCLib.INSTANCE.nfc_open(context.getValue(), connstring);
        if (Pointer.nativeValue(pnd) == 0l) { // if (pnd == NULL)
            throw new Exception("Problem opening NFC reader. Another program accessing NFC maybe?");
        }
        if (NFCLib.INSTANCE.nfc_initiator_init(pnd) < 0) {
            throw new Exception("Failed initiator init.");
        }
        NFCLib.INSTANCE.nfc_device_set_property_bool(pnd, NFC_PROPERTY_INFINITE_SELECT, false);
        return new LibNFC.Reader(pnd, connstring);
    }

    public void add_unconnected_readers(int current_num_readers, String[] connstrings)
            throws Exception {
        for (String conn : connstrings) {
            boolean found = false;
            for (LibNFC.Reader r : nd_list) {
                if (conn.equals(r.connstring)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                nd_list.add(connect_reader(conn));
            }
        }
    }

    public void disconnect_unconnected_readers(int current_num_readers, String[] connstrings) {
        Iterator<Reader> loop = nd_list.iterator();
        while (loop.hasNext()) {
            LibNFC.Reader r = loop.next();
            boolean found = false;
            for (String conn : connstrings) {
                if (conn.equals(r.connstring)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                loop.remove();
            }
        }
    }

    public void connect_to_readers() throws Exception {
        Memory m_connstrings = new Memory(MAX_READERS * NFC_BUFSIZE_CONNSTRING);
        int current_num_readers =
                NFCLib.INSTANCE.nfc_list_devices(
                        context.getValue(), m_connstrings.share(0), max_num_readers);

        String[] connstrings = new String[current_num_readers];
        for (int i = 0; i < current_num_readers; ++i) {
            connstrings[i] = m_connstrings.getString(i * NFC_BUFSIZE_CONNSTRING);
        }

        add_unconnected_readers(current_num_readers, connstrings);
        disconnect_unconnected_readers(current_num_readers, connstrings);

        num_readers = current_num_readers;
        if (num_readers != nd_list.size())
            System.out.println(
                    "Error - num_readers != nd_list.size(): "
                            + num_readers
                            + ", "
                            + nd_list.size());
    }

    public int get_num_readers() {
        return num_readers;
    }

    public void close() {
        for (int i = 0; i < num_readers; ++i) NFCLib.INSTANCE.nfc_close(nd_list.get(i).pnd);
        NFCLib.INSTANCE.nfc_exit(context.getValue());
    }
}
