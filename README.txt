Gamma is an application for drawing Minkowski spacetime diagrams.

To install:

(NOTE: I only have access to Windows 10 systems. Gamma should work on any
standard platform, but I am unable to test anything except Windows 10. Please
report any installation problems.)

To verify that it is installed properly:

The Java JDK installation should have placed Java in your PATH. Bring
up a terminal window (cmd.exe on Windows, terminal on Macs and Linux)
and type:

  java --version

This should output something like:

  openjdk 17.0.2 2022-01-18
  OpenJDK Runtime Environment (build 17.0.2+8-86)
  OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)

--------------------------------------------------------------------

Download and install Java JDK 17.0.2 (or later) from
https://www.oracle.com/java/technologies/downloads/

--------------------------------------------------------------------

Download and install JavaFX 17.0.2 [LTS] (or later) from
https://gluonhq.com/products/javafx/

(for Type, select SDK)

The download should be a ZIP file. You can place the javafx-sdk-17.0.2
folder anywhere.

On Windows, open a terminal window (cmd.exe) and enter:
  set JAVAFX_HOME="<JavaFX install location>\lib"

On Mac / Unix, open a terminal window (terminal) and enter:
  export JAVAFX_HOME=<JavaFX install location>/lib

--------------------------------------------------------------------

Unzip the Gamma installation package to any location. Standard locations would
be:

  * For Windows, C:\Program Files\gamma
  * For Macs, place it anywhere
  * For Linux, /opt/gamma (the standard Linux approach is to break up an
    application and place the parts in different folders--this will not work).

On Windows, to run Gamma, open a terminal window (cmd.exe) and enter:

  cd <Gamma install location>\lib
  java -p %PATH_TO_FX%;. ^
     --add-modules org.freixas.gamma,javafx.controls,javafx.fxml ^
     -jar gamma.jar

To run from anywhere:

  set PATH_TO_FX="<Gamma install location>\lib"
  (Only needs to be done once)

The comamnd becomes:

  java -p %JAVAFX_HOME%;%GAMMA_HOME% ^
     --add-modules org.freixas.gamma,javafx.controls,javafx.fxml ^
     -jar %GAMMA_HOME%\gamma.jar


On Mac / Unix, to run Gamma, open a terminal window (terminal) and
enter:

  cd <Gamma install location>/lib
  java -p $JAVAFX_HOME;. \
     --add-modules org.freixas.gamma,javafx.controls,javafx.fxml \
     -jar gamma.jar

To run from anywhere:

  export PATH_TO_FX="<Gamma install location>\lib"
  (Only needs to be done once)

The comamnd becomes:

  java -p $JAVAFX_HOME;$GAMMA_HOME \
     --add-modules org.freixas.gamma,javafx.controls,javafx.fxml \
     -jar $GAMMA_HOME/gamma.jar

--------------------------------------------------------------------

As this is cumbersome, you can create a desktop icon to do the work.

Windows 10:

  * Right-click on the desktop and select New > Shortcut
  * Browse to the location of Java (C:\Program Files\Java\bin\java.exe
  * Press Next and name the shortcut Gamma
  * Click Finish
  * Double-click on the created icon
  * Append the arguments listed above (everything starting with "-p"
    but all on one line) to the Target field (if you use the first
    form of the command, you will need to set the "Start in" field to
    "<Gamma install
    location>\lib"; if you use the second form, you will need to set
    location>the GAMMA_HOME envrionment variable).
  * Click Change Icon
  * Browse to the location where Gamma is installed and then the icons folder
  * Select the gamma icon

You can copy this icon to your start menu, if you don't want it on the
desktop.

Now, you can double-click on the icon to start Gamma.

Mac:

  Sorry, the process for creating Mac icons that execute commands is
  not straight-forward and I have no way of testing any instructions.

Linux

  You can create a desktop shortcut to run the command. The method varies
  depending on the version of Linux you are running.

  An icon can be obtained by using an icon from the Gamma icons
  folder.

  See: https://www.xmodulo.com/create-desktop-shortcut-launcher-linux.html
