Gamma is an application for drawing Minkowski spacetime diagrams.

* INSTALLERS ARE AVAILABLE FOR WINDOWS AND DEBIAN-BASED LINUX DISTRIBUTIONS
* MACOS INSTALLERS ARE NOT CURRENTLY PLANNED
* THERE IS A UNIVERSAL ZIP FILE (SEE BELOW)

Known Problems:

  * Touch gestures for zoom/pan don't work well. Use Ctrl+0 to restore the
    original display. On my HP table, a two-finger zoom worked fine on the
    mouse pad.

********************************************************************
********************************************************************
********************************************************************

WINDOWS 7-11

* Download and run (double-click on) the Windows x64 installer (MSI)
  gamma-<version>.msi. For x32 systems, you will need to use the universal ZIP
  file. Once installed, you can run it from a desktop icon or from the Start
  menu.

DEBIAN-BASED LINUX DISTRIBUTIONS

* Download and run sudo dpkg -i gamma_<version>_amd64.deb. Once installed, you can
  run it from a desktop icon.

OTHER SYSTEMS

* Use the universal TAR.GZ file.

********************************************************************
********************************************************************
********************************************************************

INSTALLING THE UNIVERSAL TAR.GZ FILE

To install the universal tar.gz file, you will need to know how to:

* Enter DOS (Windows) or shell (Mac/Linux) commands.
* Define a persistent environment variable.
* Unpack a tar.gz file.
* Add a path to the PATH environment variable.

--------------------------------------------------------------------

INSTALL JAVA

Download and install Java 25 (or later). There are several versions and any of
them should work. The version used to build and test Gamma is the OpenJDK version
from

https://jdk.java.net/25/

You could also try the versions at

https://learn.microsoft.com/en-us/java/openjdk/download or
https://www.oracle.com/java/technologies/downloads/

The installer versions are the easiest to install. If you get a zip or tar file,
the general approach is to unpack it somewhere, set the JAVA_HOME environment
variable to point to this location and add the path to the bin folder inside
the unpacked Java JDK to your PATH.

To verify that Java is installed properly, bring up a new command (Windows)
or terminal (Mac/Linux) window and type:

  java --version

This should output something that includes:

  JDK 25 or OpenJDK 25

To make sure that the JAVA_HOME variable is set, enter:

  echo %JAVA_HOME% (Windows)
  echo $JAVA_HOME (Mac/Linux)

--------------------------------------------------------------------

INSTALL JAVAFX

Download and install JavaFX 25 [LTS] (or later) from

https://gluonhq.com/products/javafx/

(for Type, select SDK)

The download should be a ZIP file. You can place the included javafx-sdk-25
folder anywhere.

You will need to create a JAVAFX_HOME environment variable. The JAVAFX_HOME path
should point to the top of the unpacked ZIP file.

--------------------------------------------------------------------

INSTALL GAMMA

Unzip the Gamma installation package to any location. If you unpacked the file
into <some path>, then add <some path> to your PATH.

--------------------------------------------------------------------

RUN GAMMA

To run Gamma from a terminal window, enter:

  gamma.bat (Windows)
  gamma (Mac/Linux)

You may be able to link the command to a desktop icon to make it more
convenient to run. You may still see a brief appearance of a terminal window.