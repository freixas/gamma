Gamma is an application for drawing Minkowski spacetime diagrams.

* INSTALLERS ARE AVAILABLE FOR WINDOWS AND DEBIAN-BASED LINUX DISTRIBUTIONS
* MACOS INSTALLERS ARE PLANNED
* THERE IS A UNIVERSAL ZIP FILE (SEE BELOW)

Known Problems:

  * Touch gestures for zoom/pan don't work well. Use Ctrl+0 to restore the
    original display. On my HP table, a two-finger zoom worked fine on the
    mouse pad.

********************************************************************
********************************************************************
********************************************************************

WINDOWS 7-10

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

Download and install Java JDK 17.0.2 (or later) from

https://www.oracle.com/java/technologies/downloads/

This is also called "Java SE Development Kit 17.0.2". The page includes a link
to installation instructions in the section on "Release Information".

There are also open source versions at

https://jdk.java.net/17/

These versions don't include installers or even installation instructions.

To verify that Java is installed properly, type:

  java --version

into a cmd.exe or terminal window. This should output something starting with:

  java version "17.0.2"

Make sure that the JAVA_HOME variable is set. Using a terminal window, enter:

  echo %JAVA_HOME% (Windows)
  echo $JAVA_HOME (Mac/Linux)

If this does not display path, set JAVA_HOME to the path where Java
was installed. On Windows, locate the top of the Java installation (usually
something like C:\Program Files\Java\<version>). Then, in a terminal window,
type:

  setx JAVA_HOME ""

On Mac/Linux, use the "which" command in a terminal window to find the java
executable:

  which java

Remove "/bin" from the path. This should be your JAVA_HOME value. You will need
to set it in whatever profile file you are using: ~/.bash_profile, ~/.profile,
or others. In this file, enter:

  JAVA_HOME="<path>"

You may have to log out/in to make this effective.

--------------------------------------------------------------------

INSTALL JAVAFX

Download and install JavaFX 17.0.2 [LTS] (or later) from

https://gluonhq.com/products/javafx/

(for Type, select SDK)

The download should be a ZIP file. You can place the javafx-sdk-17.0.2
folder anywhere.

You will need to create a JAVAFX_HOME environment variable. The JAVAFX_HOME path
should point to the top of the unpacked ZIP file.

--------------------------------------------------------------------

INSTALL GAMMA

Unzip the Gamma installation package to any location. If you unpacked the file
into <some path>, then add <some path>\app (Windows) or <some path>/app (Mac/
Linux) to your PATH.

--------------------------------------------------------------------

RUN GAMMA

To run Gamma from a terminal window, enter:

  gamma.bat (Windows)
  gamma (Mac/Linux)

You may be able to link the command to a desktop icon to make it more
convenient to run. You may still see a brief appearance of a terminal window.