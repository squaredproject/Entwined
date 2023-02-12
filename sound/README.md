# Sound for the sculpture

This should have been much simpler but it wasn't

Get a raspberry pi and a USB audio device.

## Todo

I'm leaving this in slightly bad shape. It's running on an RPI but not from the checked in version.

The service file has to be changed to point at the `Entwined/sound` directory.

## howto

### Use a RPI

This is running on a pi2 and doing fine. I have heard it glitch. An RPI3B might be better.

### Use USB audio device

Attach the USB audio device. The headset jack in the rpi itself is terrible and noisy.

The one we used 2023 is a UGREEN premium adapter available on Amazon. I used that for Serenity in 2019.

https://www.amazon.com/UGREEN-External-Headphone-Microphone-Desktops/dp/B01N905VOY

`aplay -L` will show you the names of the devices. Using the sysdefault seems like it works well.

Make sure the device you choose works from the command line. Modify the system service as necessary

### Set the audio level

By default, the audio level is set almost too low to hear. This can be done manually and only has to be done once.

`alsamixer` shows a visual interface over ncurses style. You can just raise the volume to top of yellow.

By command line `amixer sset 'Speaker' 90%` should do the same, but I'm not sure if the names of the variables
will be stable over time. `amixer scommands` will show what variables exist

### Create the system service

Create a soft link from /etc/systemd/services to this file. SHould work from there

### service to turn on and off

`crontab -e`

```
0 18 * * * systemctl restart sculpture_sound
0 22 * * * systemctl stop sculpture_sound
```

## Where to put new sound files

In this directory. They may have to be 16LE Stereo 48Kb WAV files. Not sure if MP3 files work.

## HDMI pain

Attempted to use the HDMI port, because it seemed nice.

The HDMI port on a raspberry pi has limitations. It only takes a format that is the raw format for HDMI.

This means there has to be format conversion to get there.

PulseAudio gets there. It becomes pretty easy to do from the command line, but hard from startup. This is called 'system wide' mode, and they make it hard to do that. Even though on a headless raspberry pi it's pretty hard.

Alternately, it should be possible with Aplay, because ALSA has libraries to do that. It seemed to me like the right
conf file for the HDMI driver wasn't been used. It could be that a later version will simply fix the issue
with getting the right ALSA file for the directory, in which case ALSA will figure out the conversion, and the HDMI
will work nicely with aplay.