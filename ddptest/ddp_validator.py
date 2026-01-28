import argparse
from scapy.all import rdpcap, UDP, ARP

def is_valid_ddp_payload(data):
    """
    Validates a DDP payload based on specific color rules.

    Args:
        data (bytes): The UDP payload containing DDP data.

    Returns:
        tuple: (bool, str) where bool is True if valid, False otherwise,
               and str is a message explaining the result.
    """
    # DDP header is at least 10 bytes
    if len(data) < 10:
        return True, "Packet too short to be a DDP packet."

    # According to the spec, byte 0 has a version number in the top two bits.
    # We only accept version 1.
    if (data[0] & 0b11000000) >> 6 != 1:
        return True, f"DDP version is not 1. Got version {((data[0] & 0b11000000) >> 6)}"

    # Check if the timecode flag is set in the first byte.
    timecode_flag_set = (data[0] & 0b00000010) != 0
    header_size = 14 if timecode_flag_set else 10

    if len(data) < header_size:
        return True, "Packet too short for specified DDP header."

    pixel_data = data[header_size:]

    if len(pixel_data) % 3 != 0:
        return False, f"Pixel data length is {len(pixel_data)}, which is not a multiple of 3 (RGB)."

    if len(pixel_data) == 0:
        return True, "No pixel data in packet."

    first_pixel = pixel_data[0:3]

    # Check for black or white
    if first_pixel == b'\x00\x00\x00':
        return False, "First pixel is black."
    if first_pixel == b'\xff\xff\xff':
        return False, "First pixel is white."

    # Check if all pixels are the same color
    for i in range(3, len(pixel_data), 3):
        if pixel_data[i:i+3] != first_pixel:
            return False, f"Inconsistent pixel color at offset {i}. Expected {first_pixel.hex()}, got {pixel_data[i:i+3].hex()}"

    return True, "All pixels are the same, non-black, non-white color."

def main():
    parser = argparse.ArgumentParser(description="Analyze DDP packets in a pcap file.")
    parser.add_argument("pcap_file", help="The pcap file to analyze.")
    parser.add_argument("--ip", default="10.0.0.11", help="The destination IP address to filter by.")
    parser.add_argument("--debug", action="store_true", help="Enable debug mode to print all packet validation messages.")
    parser.add_argument("--strict", action="store_true", help="Report any non-DDP packets sent to the target IP.")
    args = parser.parse_args()

    try:
        packets = rdpcap(args.pcap_file)
    except FileNotFoundError:
        print(f"Error: File not found at {args.pcap_file}")
        return
    except Exception as e:
        print(f"Error reading pcap file: {e}")
        return

    failed_packets = 0
    processed_packets = 0
    for i, packet in enumerate(packets):
        # Strict mode checks for any packet to the destination, including broadcast and ARP
        if args.strict:
            if packet.haslayer(ARP) and (packet[ARP].pdst == args.ip or packet.dst == 'ff:ff:ff:ff:ff:ff'):
                print(f"Packet {i+1}: Non-DDP packet (ARP) found for {args.ip}")
            elif packet.haslayer('IP') and (packet['IP'].dst == args.ip or packet['IP'].dst == '255.255.255.255'):
                if not packet.haslayer(UDP):
                    print(f"Packet {i+1}: Non-DDP packet ({packet['IP'].proto}) found for {args.ip}")

        if UDP in packet and packet.haslayer('IP') and packet['IP'].dst == args.ip:
            processed_packets += 1
            udp_payload = packet[UDP].payload.load
            is_valid, message = is_valid_ddp_payload(udp_payload)
            if args.debug:
                if is_valid:
                    print(f"Packet {i+1}: OK - {message}")
                else:
                    print(f"Packet {i+1}: Validation Failed - {message}")
            if not is_valid:
                if not args.debug:
                    print(f"Packet {i+1}: Validation Failed - {message}")
                failed_packets += 1

    if not args.debug and failed_packets == 0 and processed_packets > 0:
        print("All processed packets passed validation.")
    elif processed_packets == 0 and not args.strict:
        print("No DDP packets found for the specified IP address.")

if __name__ == "__main__":
    main()
