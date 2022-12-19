# New WEC+2 Wifi controllers

These small controllers have no ethernet and only one output, and they have a small button on them.

The user guide is here: https://minleonusa.com/wp-content/uploads/2021/05/RGBWEC2-User-Guide.pdf

## How ours are configured

In order to not be dependant on an access point (with the small antenna), each is configured as an access point.

There is a number "ent-01", "ent-02", etc. This is the access point name for you to connect to the controller

The password is `theshrub!`

The IP address is 192.168.2.2 for the web page

You can also use the Phone app if you like but the web page is good

To connect to a particular one, power it up, and connect to the access point

## Set the default behavior

The key is the power up behavior

In order to set a new behavior, use the `effects` tab. The highlighted value is the current value.

The effect will use the values in the `color` tab

The default startup effect is set by hitting the `on\off` tab *twice* . THIS IS THE MYSTERIOUS PART THAT'S
DEEP IN THE SLIDE DECK OF THE MANUAL.

If you want to set the default behavior to a new color, go into color, click the first color, set to a new value,
then press `on/off` tab twice. The default effect is already `solid color` so that's all you need.

The button on the wec will still cycle through all the effects.

## Set up a new one

The default connection is found by looking for an address `wec[number]` like `wec98023`. They are set up with no password.

Go in and reset the wec name, the AP name, the password type to encrypted, and the password.

## The shows tab

You don't really need to do this; skip

Using the `shows` interface is only somewhat intuitive. There is a video online by Minleon.

In short, the `set` button takes the values from the `effects` and `colors` tab. Thus, you can set an effect
to something, and the colors, and the amount of time, and press set, and it'll fill it in.

It seems you don't need to press save? Whenever you hit `set` it seems to update the page.

However, if you change one of the shows, and you want that show to be default, you have to select
that behavior in `effects` then press `on\off` tab twice

## Note

We could configure these to be on the main 'entwined' network which means it could accept DDP packets,
be available for DDPtest, and allow us to use the scripts that turn on and off the lights.

However, we are uncertain how well they will work through the metal.

To do this, put the WEC into `network` mode (what a normal company would call `client`), and set the SSID and Password correctly. 

Set the ip address for the same as other ndb: 10.0.0.x : and write that value
on the WEC. 

Misko says this will work although I'm not so sure. It's also hard to set off-site, you'd need an AP
with the same setup.
