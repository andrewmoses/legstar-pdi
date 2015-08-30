# Pre-requisites #

LegStar for PDI requires [PDI version 5.0.1](http://sourceforge.net/projects/pentaho/files/Data%20Integration/) or later release.

# Get from Marketplace #

On the PDI Spoon interface select Help -> Marketplace then pick up the legstar.pdi.zosfile plugin.

# Download/Unzip #

Alternatively, you can download the latest LegStar for PDI release from [this link](Downloads.md).

For the rest of this document, we refer to the folder where you installed PDI as $KETTLE\_DIR.

Unzip the archive you downloaded to $KETTLE\_DIR/plugins. This should create a folder named **legstar.pdi.zosfile** under $KETTLE\_DIR/plugins.

# Check #

Start or restart the PDI spoon UI ($KETTLE\_DIR/spoon.sh or %KETTLE\_DIR%/Spoon.bat).

Create a new PDI Transformation. You should now have a Design tab with all the available steps.

Open the Input category and drag/drop the **z/OS File Input** step onto the canvas.

Congratulations, LegStar for PDI is now installed. You can now start building your [first transformation](GetStarted.md).
