# Installing an Installation

After placing the trees and shrubs according to the guide in the Trees/data directory,
and setting the Config.java as per the requirements of the installation,
use the 'capture.sh' to create a new subdirectory here - and check the files into
github.

When you want to capture again, use `recapture.sh` and it will make sure the directory
already exists.

When you want to install to your local directory for testing with processing, use `release.sh`.
I'm sorry for the poor naming it is the opposite of capture.

When you need to do a true install in the field, use `install.sh`. This will run a script
to install necessary cron jobs and monitoring.

# older installations

Over time, older installations can become "broken" because new features are required.
In Nov '21 I added a bunch of features around support for different cube sizes,
and fairy circles, and installations, and we didn't maintain backward compatiblity,
so the older installations don't work. I have thus moved them into an archive directory
so we could look at them later, but one would have to go in and update elements of
the config to make them work right (use the new shrubs config system, etc)