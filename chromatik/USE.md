# Using Entwined and Chromatik

## Please add to this evolving document!

Submit a pull request, or write an issue tag documentation.

## LX Studio documentation

After you've got LX Studio running, [please peruse the documentation](https://github.com/heronarts/LXStudio/wiki). However, you'll see a wealth of info. We'll specifically focus on using Entwined.

## The main window

The window has the following elements. 

The left bar tends to have output information.

The lower bar has active channel entries that you can manipulate.

The right bar controls interaction, such as the MIDI map, recording inputs, the pattern entry for a channel, the OSC entries.

There is a small upper ribbon that allows saving and restoring projects.

The lower left had a tip bar. If you're unsure what a button does, hover over, and read the tip.

Unlike Processing based LX Studio, the window can be resized. Moving the sculpture
around is best done with a trackpad. Pinch to zoom, drag to rotate, and CTL-drag to
shift.

### Channels and Channel names

The name of the channel can be set to a string - like "channel 1" which isn't very
descriptive - or can be set to something more descriptive like "TREES" (a channel
which only effects the trees), or can be set to the currently playing pattern.

You can change this yourself by clicking on the name and using the pulldown.

### What if I can't see all the channels?

The left most channel, channel 1, and the right most channel is Master.

There are 8 channels, but there are also 3 visible channels that are used by the Ipad.
In the old interface we had these channels, but they were invisible. Now you have to scroll over them to get to the master channel. Don't touch the ipad channels.

If you can't see both the master channel and channel 1, then you have to scroll. There
is no scroll bar in this interface, so you have to use a trackpad with a swipe interface. Possibly selecting a channel and using the arrow keys will work also.

### Channel controls

The channels have a mini mode, and a full mode. In the mini mode, you'll be able to see
and switch channels easily, in the full mode you can set capabilities like
a channel which only applies to some structure elements. 

Changing mode is in a small triangle just to the right bottom of the master channel, and you have to be scrolled all the way over.

### Selecting a channel

In order to change the pattern running on a channel, you'll have to select it.

The only way to select on a channel with the visual interface is to click in the channel name (at the top of a channel).

If you have an APC40, the 'Track Selection' row will allow selecting the first 8 channels.

### Superpower: channel target!

In the expanded view of a channel, toward the left bottom, is a small button like a group of boxes in a pyramid.

This allows a channel to be filtered and effect a particular set of fixtures. They
are set in the fixture files. If you view the fixtures on the left side, select one,
and look down, you see a set of tags for each fixture. For example, a Shrub will have "SHRUB" and its peice name 'shrub-01' or similar.

If you wish a channel to control only the shrubs, or only a certain shrub, or a group
of shrubs and circles, you can do so with a comma separated list. 

I am uncertain, as of writing this guide, whether those changes are saved as a part
of automation. I would guess not, so you should set up channels that effect parts of
the sculpture.

## Fixtures

The fixtures are the elements in the left bar. The correct fixtures for this
installation will have already been saved for you. If you have to create a new
installation, you are best off creating the new fixtures - which will be copied
to the `Chromatik/Fixtures` directory. Use a project file that you like, delete
all the existing features, add the correct set of features, *enable them*, and you'll
be done.

If you have to change the IP address of a fixture, you don't have to do this, but
you might have to hit the friendly "reload fixture files" button.

## Output

If your sculpture is not lighting up, try the following.

* Make sure all individual fixtures are enabled, and none are 'soloed'. There is a button to the left of each fixture which is whether it is enabled, and adding a new fixture will add it disabled to the end of the list, which is easy to mix.

* Make sure output is enabled. This is a large friendly button on the master, and
it needs to be red to actually output to the sculpture. When running in headless
mode, output is enabled by default and through a command line switch.

* You don't have a license! One of the main differences between the free version and
the licensed version is only the licensed version can be used to run a sculpture.
If you have a red "license" button in the upper right, then you're unlicensed and need
to request one from Mark. Please contact Entwined staff. 


## Recording a set and autoplay

The upper left corner has a play, stop, record button. You'll want to play your set, then record it. The file has to be copied somewhere to auto-play. If you want to list files and play them, that's simple.

[TODO: add more specific instructions]

### Some events aren't intended to record

The recording automation system doesn't record several things:
- the master brightness
- changes to the pattern list on a channel
- trigger events (or any midi event)

1. *Master brightness* The purpose of not recording the master brightness has a purpose: you can change the
overall running brightness on the sculpture, and not have that be recorded. Therefore,
when you are playing in a different sitaution, or on a different sculpture, you should set this brightness and not worry about it being recorded.

2. *Some channel changes* The purpose of not recording changes to the pattern list is due to the difficulty of uniquely naming patterns (essentially effect rack entries). The reality, though, is
this normally does what you want: you want to select or act on the only pattern with
a given name. This means that practically you can add a pattern to a channel and use it.

However, if you play that file back *without saving a project with that pattern on that channel*, it won't work right. You'll need to add it.

Realistically, don't add patterns to channels during recording.

3. *Trigger events* If you hit a trigger pad on the APC40, the trigger action is not
saved, its result is saved. This means, under the covers, that all triggers have
to change parameters or other similar elements that will be saved by an automation.

This ends up "doing the right thing" because you didn't really want to record pressing
the button (which might do a different thing with a different program file later),
you wanted to record the action that button took.

## Using a midi controler

This installation tends to use the APC40 and is tuned with that in mind. The APC40 MK1
has a row of knobs called "track control" in the upper right which are visibile also
in the interfaces, as well as "device control" for the currently playing pattern in the selected channel.

You just attach an APC40. The checked in project file has an APC40 already selected
as a controller.

You can also attach any other Midi device! LX Studio has rich midi control in the
style of Ableton. There is a midi map mode, and you assign any midi control to any on-screen visible function. 

For example, you can attach a Midifighter Twister, and have knobs assigned to specific
functions. Unfortunately you'll have to set these up every time, as they're saved
with the project file, and different people might have different favorite buttons.

## Licenses

Entwined has an LX Studio plugin. This is how we make interactions work and a few 
other things. If you are running an unlicensed version, you'll still see the sculpture,
and you'll be able to develop and run new patterns.
