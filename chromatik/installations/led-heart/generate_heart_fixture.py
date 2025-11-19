#!/usr/bin/env python3
"""
Generate a Chromatik fixture file (.lxf) from PIXEL-HEART-DATA.csv
"""

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


def main():
    # Get the directory where this script is located
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Input and output files
    csv_file = os.path.join(script_dir, "PIXEL-HEART-DATA.csv")
    lxf_file = os.path.join(script_dir, "PixelHeart.lxf")
    
    # Read pixel coordinates
    print(f"Reading coordinates from {csv_file}...")
    coords = read_pixel_data(csv_file)
    print(f"Found {len(coords)} points")
    
    # Generate fixture file
    print(f"Generating fixture file {lxf_file}...")
    fixture = generate_lxf(coords, lxf_file)
    
    print(f"✓ Successfully created {lxf_file}")
    print(f"  - Points: {len(coords)}")
    print(f"  - Protocol: DDP")
    print(f"  - Default host: {fixture['parameters']['host']['default']}")


if __name__ == "__main__":
    main()
