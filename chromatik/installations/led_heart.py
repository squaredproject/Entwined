#!/usr/bin/env python3
"""
Generate a Chromatik fixture file (.lxf) from LED heart pixel data CSV.

This script reads pixel coordinate data from a CSV file and generates
a Chromatik-compatible fixture definition file (.lxf) for LED control.

Supports two CSV formats:
1. New format with header: Pixel #,Original Facet ID,X,Z,Y,...
   - Columns determined dynamically from header
   - Pixels sorted by "Pixel #" column
   
2. Legacy format (no header): pixel_id,x,y,z
   - Fixed column positions
"""

import argparse
import csv
import json
import os


def read_pixel_data(csv_filename):
    """Read coordinates from CSV file
    
    Supports two formats:
    1. New format with header: Pixel #,Original Facet ID,X,Z,Y,...
    2. Legacy format without header: pixel_id,x,y,z
    """
    coords = []
    
    with open(csv_filename, 'r') as f:
        reader = csv.reader(f)
        rows = list(reader)
    
    if not rows:
        return coords
    
    # Check if first row is a header by looking for "Pixel" in the first cell
    header = rows[0]
    has_header = header[0].strip().lower().startswith('pixel')
    
    if has_header:
        # New format with header
        # Find column indices from header
        header_lower = [h.strip().lower() for h in header]
        
        # Find the column indices
        pixel_col = None
        x_col = None
        y_col = None
        z_col = None
        
        for i, h in enumerate(header_lower):
            if h.startswith('pixel'):
                pixel_col = i
            elif h == 'x':
                x_col = i
            elif h == 'y':
                y_col = i
            elif h == 'z':
                z_col = i
        
        if pixel_col is None or x_col is None or y_col is None or z_col is None:
            raise ValueError(f"Missing required columns in header. Found: {header}")
        
        # Parse data rows
        pixel_data = []
        for row in rows[1:]:  # Skip header
            if len(row) > max(pixel_col, x_col, y_col, z_col):
                try:
                    pixel_num = int(row[pixel_col])
                    x = float(row[x_col])
                    y = float(row[y_col])
                    z = float(row[z_col])
                    pixel_data.append((pixel_num, x, y, z))
                except (ValueError, IndexError):
                    continue  # Skip invalid rows
        
        # Sort by pixel number to ensure correct ordering
        pixel_data.sort(key=lambda p: p[0])
        
        # Extract coordinates in sorted order
        coords = [{"x": x, "y": y, "z": z} for _, x, y, z in pixel_data]
    else:
        # Legacy format: pixel_id, x, y, z (no header)
        for row in rows:
            if len(row) >= 4:
                try:
                    x = float(row[1])
                    y = float(row[2])
                    z = float(row[3])
                    coords.append({"x": x, "y": y, "z": z})
                except (ValueError, IndexError):
                    continue
    
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

This script reads a CSV file containing pixel coordinates and generates
a Chromatik-compatible fixture definition file for DDP LED control.

CSV Formats Supported:
  1. New format with header:
     Pixel #,Original Facet ID,X,Z,Y
     1,136,-87.8,111.1,-24.3
     
  2. Legacy format (no header):
     pixel_id,x,y,z
     0,1.5,2.0,0.0
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
    default_csv = os.path.join(default_subdir, "led_heart_data.csv")
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
