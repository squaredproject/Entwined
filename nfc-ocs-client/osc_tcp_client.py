""" TCP Client for OSC messages"""

import socket
from pythonosc.osc_message_builder import OscMessageBuilder
from pythonosc.osc_message import OscMessage
from pythonosc.osc_bundle import OscBundle

from typing import Union
from collections.abc import Iterable

class OscTcpClient:
    """OSC TCP Client"""

    def __init__(self, ip, port):
        self.osc_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.osc_socket.connect((ip, port))
        self._address = ip
        self._port = port

    def send(self, content: Union[OscMessage, OscBundle]) -> None:
        """Sends an :class:`OscMessage` or :class:`OscBundle` via tcp

        Args:
            content: Message or bundle to be sent
        """
        self.osc_socket.sendto(content.dgram, (self._address, self._port))


    def send_message(self, address: str, value) -> None:
        """Build :class:`OscMessage` from arguments and send to server

        Args:
            address: OSC address the message shall go to
            value: One or more arguments to be added to the message
        """
        builder = OscMessageBuilder(address=address)
        if value is None:
            values = []
        elif not isinstance(value, Iterable) or isinstance(value, (str, bytes)):
            values = [value]
        else:
            values = value
        for val in values:
            builder.add_arg(val)
        msg = builder.build()
        self.send(msg)
