#! /bin/bash

cd /home/eldermother/nfc_controller/nfc-ocs-client/
source .venv/bin/activate
python ./nfc_osc_client.py --ip=10.0.0.10 --port=7777
