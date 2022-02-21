Gamma is an application for drawing Minkowski spacetime diagrams.

Known Problems:

  * Touch gestures for zoom/pan don't work well. Use Ctrl+0 to restore the
    original display. On my HP table, a two-finger zoom worked fine on the
    mouse pad.

********************************************************************
********************************************************************
********************************************************************

WINDOWS 10 INSTRUCTIONS

INSTALL JAVA

Download and install Java JDK 17.0.2 (or later) from

https://www.oracle.com/java/technologies/downloads/

To verify that it is installed properly:

The Java JDK installation should have placed Java in your PATH. Bring
up a terminal window (cmd.exe) and type:

  java --version

This should output something like:

  openjdk 17.0.2 2022-01-18
  OpenJDK Runtime Environment (build 17.0.2+8-86)
  OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)

--------------------------------------------------------------------

INSTALL JAVAFX

Download and install JavaFX 17.0.2 [LTS] (or later) from

https://gluonhq.com/products/javafx/

(for Type, select SDK)

The download should be a ZIP file. You can place the javafx-sdk-17.0.2
folder anywhere.

Open a terminal window (cmd.exe) and enter:

  setx JAVAFX_HOME="<JavaFX install location>\lib"

This only needs to be done once; the quotes are important if the path
has spaces. Note that only processes started in the future will know about this
new environment variable.

--------------------------------------------------------------------

INSTALL GAMMA

Unzip the Gamma installation package to any location.

Open a terminal window (cmd.exe) and enter:

  setx GAMMA_HOME="<Gamma install location>\lib"

This only needs to be done once; the quotes are important if the path
has spaces. Note that only processes started in the future will know about this
new environment variable.

--------------------------------------------------------------------

RUN GAMMA FROM A TERMINAL

Copy/paste this long command into a terminal window (cmd.exe):

  java -p %JAVAFX_HOME%;%GAMMA_HOME% --add-modules org.freixas.gamma,javafx.controls,javafx.fxml -jar %GAMMA_HOME%\gamma.jar

--------------------------------------------------------------------

RUN GAMMA FROM A SHORTCUT

You can create a desktop icon to to simplify running Gamma.

  * Right-click on the desktop and select New > Shortcut
  * Browse to the location of javaw (Usually,
    C:\Program Files\Java\bin\javaw.exe -- note the "w" at the end. Don't
    select java.exe)
  * Press Next and name the shortcut Gamma
  * Click Finish
  * Right-click on the created icon and select Properties
  * Append the arguments listed above (everything starting with "-p")
    to the Target field.
  * Click Change Icon
  * Browse to the location where Gamma is installed and then the icons folder
  * Select the gamma icon
  * Click OK until you're out

You can copy this shortcut to your start menu, if you don't want it on the
desktop. You can now double-click on the shortcut to start Gamma.


********************************************************************
********************************************************************
********************************************************************

MAC INSTRUCTIONS

I only have access to Windows 10 systems. Gamma should work on any
standard platform, but I am unable to test anything except Windows 10. Please
report any installation problems.

INSTALL JAVA

Download and install Java JDK 17.0.2 (or later) from

https://www.oracle.com/java/technologies/downloads/

To verify that it is installed properly:

The Java JDK installation should have placed Java in your PATH. Bring
up a terminal window and type:

  java --version

This should output something like:

  openjdk 17.0.2 2022-01-18
  OpenJDK Runtime Environment (build 17.0.2+8-86)
  OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)

--------------------------------------------------------------------

INSTALL JAVAFX

Download and install JavaFX 17.0.2 [LTS] (or later) from

https://gluonhq.com/products/javafx/

(for Type, select SDK)

The download should be a ZIP file. You can place the javafx-sdk-17.0.2
folder anywhere.

On a Mac, I can't say for sure how to set an environment variable
globally. It depends on the shell you run. The most common advice is to
edit ~/.bash_profile (for those running bash) or ~/.profile (for those running
sh and maybe other shells) and add this command:

  export JAVAFX_HOME="<JavaFX install location>\lib"

The quotes may be required if the path has spaces.

--------------------------------------------------------------------

INSTALL GAMMA

Unzip the Gamma installation package to any location.

Edit ~/.bash_profile or ~/.profile and add this command:

  export GAMMA_HOME="<Gamma install location>\lib"

The quotes may be required if the path has spaces.

--------------------------------------------------------------------

RUN GAMMA FROM A TERMINAL

Copy/paste this long command into a terminal window:

  java -p "$JAVAFX_HOME;$GAMMA_HOME" --add-modules org.freixas.gamma,javafx.controls,javafx.fxml -jar "$GAMMA_HOME\gamma.jar"

--------------------------------------------------------------------

RUN GAMMA FROM A SHORTCUT

Sorry, the process for creating Mac icons that execute commands is not
straight-forward and I have no way of testing any instructions.


********************************************************************
********************************************************************
********************************************************************

LINUX INSTRUCTIONS

I only have access to Windows 10 systems. Gamma should work on any
standard platform, but I am unable to test anything except Windows 10. Please
report any installation problems.

INSTALL JAVA

Download and install Java JDK 17.0.2 (or later) from

https://www.oracle.com/java/technologies/downloads/

To verify that it is installed properly:

The Java JDK installation should have placed Java in your PATH. Bring
up a terminal window and type:

  java --version

This should output something like:

  openjdk 17.0.2 2022-01-18
  OpenJDK Runtime Environment (build 17.0.2+8-86)
  OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)

--------------------------------------------------------------------

INSTALL JAVAFX

Download and install JavaFX 17.0.2 [LTS] (or later) from

https://gluonhq.com/products/javafx/

(for Type, select SDK)

The download should be a ZIP file. You can place the javafx-sdk-17.0.2
folder anywhere.

On Linux, I can't say for sure how to set an environment variable
globally. It depends on the shell you run. The most common advice is to
edit ~/.bash_profile (for those running bash) or ~/.profile (for those running
sh and maybe other shells) and add this command:

  export JAVAFX_HOME="<JavaFX install location>/lib"

The quotes may be required if the path has spaces.

--------------------------------------------------------------------

INSTALL GAMMA

Unzip the Gamma installation package to any location.

Edit ~/.bash_profile or ~/.profile and add this command:

  export GAMMA_HOME="<Gamma install location>/lib"

The quotes may be required if the path has spaces.

--------------------------------------------------------------------

RUN GAMMA FROM A TERMINAL

Copy/paste this long command into a terminal window:

  java -p "$JAVAFX_HOME;$GAMMA_HOME" --add-modules org.freixas.gamma,javafx.controls,javafx.fxml -jar "$GAMMA_HOME\gamma.jar"

--------------------------------------------------------------------

RUN GAMMA FROM A SHORTCUT

You can create a desktop shortcut to run the command. The method varies
depending on the version of Linux you are running.

See: https://www.xmodulo.com/create-desktop-shortcut-launcher-linux.html

An icon can be obtained by using an icon from the Gamma icons
folder.