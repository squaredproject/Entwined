#!/usr/bin/env python3
"""
Generate a Chromatik fixture file (.lxf) from PIXEL-HEART-DATA.csv

This script reads pixel coordinate data from a CSV file and generates
a Chromatik-compatible fixture definition file (.lxf) for LED control.
"""

import argparse
import csv
import json
import os


def read_pixel_data(csv_filename):
    """Read coordinates from CSV file"""
    coords = []
    
    with open(csv_filename, 'r') as f:
        reader = csv.reader(f)
        for row in reader:
            if len(row) == 4:
                # id, x, y, z
                pixel_id = int(row[0])
                x = float(row[1])
                y = float(row[2])
                z = float(row[3])
                coords.append({"x": x, "y": y, "z": z})
    
    return coords


def generate_lxf(coords, output_filename):
    """Generate .lxf fixture file"""
    
    fixture = {
        "label": "Pixel Heart",
        "parameters": {
            "host": {
                "type": "string",
                "default": "127.0.0.1",
                "label": "DDP Host",
                "description": "DDP destination hostname"
            }
        },
        "components": [
            {
                "type": "points",
                "coords": coords
            }
        ],
        "outputs": [
            {
                "protocol": "ddp",
                "host": "$host",
                "dataOffset": 0
            }
        ]
    }
    
    with open(output_filename, 'w') as f:
        json.dump(fixture, f, indent=2)
    
    return fixture


def parse_args():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(
        description="""
Generate a Chromatik fixture file (.lxf) from pixel coordinate CSV data.

This script reads a CSV file containing pixel ID and X,Y,Z coordinates,
then generates a Chromatik-compatible fixture definition file for DDP
LED control.

CSV Format:
  Each row should contain: pixel_id, x, y, z
  Example: 0, 1.5, 2.0, 0.0
        """,
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s -f led-heart/
      Generate fixture in led-heart/ folder

  %(prog)s -c my_pixels.csv -f output/
      Specify custom input CSV and output folder

  %(prog)s -f led-heart/ --host 192.168.1.100
      Set custom DDP host IP
        """
    )
    
    # Get script directory for default paths
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_subdir = os.path.join(script_dir, "led-heart")
    default_csv = os.path.join(default_subdir, "PIXEL-HEART-DATA.csv")
    default_lxf = os.path.join(default_subdir, "PixelHeart.lxf")
    
    parser.add_argument(
        '-c', '--csv',
        dest='csv_file',
        default=default_csv,
        metavar='FILE',
        help='Input CSV file with pixel coordinates (default: %(default)s)'
    )
    
    parser.add_argument(
        '-f', '--fixtures_folder',
        type=str,
        required=True,
        metavar='FOLDER',
        help='Folder to write .lxf fixture file (e.g., led-heart/)'
    )
    
    parser.add_argument(
        '--host',
        default='127.0.0.1',
        metavar='IP',
        help='Default DDP host IP address (default: %(default)s)'
    )
    
    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Show detailed output'
    )
    
    return parser.parse_args()


def generate_lxf_data(coords, label, host):
    """Generate fixture data structure"""
    return {
        "label": label,
        "parameters": {
            "host": {
                "type": "string",
                "default": host,
                "label": "DDP Host",
                "description": "DDP destination hostname"
            }
        },
        "components": [
            {
                "type": "points",
                "coords": coords
            }
        ],
        "outputs": [
            {
                "protocol": "ddp",
                "host": "$host",
                "dataOffset": 0
            }
        ]
    }


def main():
    args = parse_args()
    
    # Read pixel coordinates
    print(f"Reading coordinates from {args.csv_file}...")
    
    if not os.path.exists(args.csv_file):
        print(f"Error: CSV file not found: {args.csv_file}")
        return 1
    
    coords = read_pixel_data(args.csv_file)
    print(f"Found {len(coords)} points")
    
    if len(coords) == 0:
        print("Error: No valid coordinates found in CSV file")
        return 1
    
    # Generate fixture data
    fixture = generate_lxf_data(coords, "Pixel Heart", args.host)
    
    if args.verbose:
        print(f"\nFixture configuration:")
        print(f"  Host: {args.host}")
        print(f"  Points: {len(coords)}")
        if len(coords) <= 5:
            for i, c in enumerate(coords):
                print(f"    [{i}] x={c['x']}, y={c['y']}, z={c['z']}")
        else:
            for i in range(3):
                c = coords[i]
                print(f"    [{i}] x={c['x']}, y={c['y']}, z={c['z']}")
            print(f"    ... ({len(coords) - 5} more points)")
            for i in range(len(coords) - 2, len(coords)):
                c = coords[i]
                print(f"    [{i}] x={c['x']}, y={c['y']}, z={c['z']}")
    
    # Ensure output directory exists
    fixtures_folder = args.fixtures_folder
    if not fixtures_folder.endswith('/') and not fixtures_folder.endswith('\\'):
        fixtures_folder += '/'
    
    if not os.path.exists(fixtures_folder):
        os.makedirs(fixtures_folder)
    
    # Write fixture file
    output_file = fixtures_folder + "PixelHeart.lxf"
    print(f"Generating fixture file {output_file}...")
    
    with open(output_file, 'w') as f:
        json.dump(fixture, f, indent=4)
    
    print(f"✓ Successfully created {output_file}")
    print(f"  - Label: {fixture['label']}")
    print(f"  - Points: {len(coords)}")
    print(f"  - Protocol: DDP")
    print(f"  - Default host: {fixture['parameters']['host']['default']}")
    
    return 0


if __name__ == "__main__":
    exit(main())
