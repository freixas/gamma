Gamma is an application for drawing Minkowski spacetime diagrams.

To install:

(NOTE: I only have access to Windows 10 systems. Gamma should work on any
standard platform, but I am unable to test anything except Windows 10. Please
report any installation problems.)

Download and install Java JDK 17.0.2 (or later) from
https://www.oracle.com/java/technologies/downloads/

(NOTE: Future versions may not require installing the full Java JDK package.)

Unzip the Gamma installation package to any location. Standard locations would
be:

  * For Windows, C:\Program Files\gamma
  * For Macs, place it anywhere
  * For Linux, /opt/gamma (the standard Linux approach is to break up an
    application and place the parts in different folders--this will not work).

The Java JDK installation should have placed Java in your PATH. To verify that
Java is set up properly, bring up a terminal window (cmd.exe on Windows,
terminal on Macs and Linux) and type:

  java --version

This should output something like:

  openjdk 17.0.2 2022-01-18
  OpenJDK Runtime Environment (build 17.0.2+8-86)
  OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)

To run Gamma, enter the following command in a terminal window

  java -???

As this is cumbersome, you can create a desktop icon to do the work.

Windows 10:

  * Right-click on the desktop and select New > Shortcut
  * Browse to the location of Java (C:\Program Files\Java\bin\java.exe
  * Press Next and name the shortcut Gamma
  * Click Finish
  * Double-click on the created icon
  * Append ??? to the Target field
  * Click Change Icon
  * Browse to the location where Gamma is installed and then the icons folder
  * Select the gamma icon

  You can copy this icon to your start menu, if you don't want it on the
  desktop.

Mac:

  * ???

Linux

  You can create a desktop shortcut to run the command. The method varies
  depending on the version of Linux you are running.

  The command should look like ???

  An icon can be obtained by using an icon from the Gamma icons folder.







