#![Gamma Symbol][../src/main/icons/gamma-icon-48x48.png] Gamma - A Minkowski Spacetime Diagram Generator

Gamma is a  tool you can use to draw a wide variety of 2D Minkwoski spacetime diagrams.
In addition to the usual static diagrams, you can also create animated diagrams and
you can add toggles, choices, and sliders to manipulate the diagram.

You specify the problem using a special language designed specifically for the
task; the tool generates the diagram from your specification. For example, this 
code would create a simple diagram with two observers, one moving at 80% the 
speed of light relative to the other. The axes for the stationary observer drawn,
as are the worldlines for both observers.

```
observer1 = [observer ];
observer2 = [observer velocity .8];

axes ;
worldline observer1, style: "color: blue";
```
## Features

### Commands used for drawing 

 - Axes - relative to any inertial frame
 - Grids - relative to any inertial frame
 - Hypergrids
 - Events
 - Worldlines
 - Lines
 - Paths
 - Labels 

### Physics-based Objects

- Coordiantes
- Inertial frames
- Observers
- Lines
- Paths
- Bounds
- Intervals

### Expressions

- Floating point numbers and character strings
- Variables, including animated variables and variables controlled by the end user
- All common mathematical operators, including one especially for Lorentz transformations
- All common mathematical functions, as well as a number of relativity-related functions

### Other features

- Specify observers whose worldlines include multiple segments with varying acceleration
- Extract the instantaneous moving frame for any observer at any point along their worldline
- Create lines parallel or intersecting the axes of any inertial frame
- Find the intersection of lines or a line and a worldline
- Given a point on a worldline, get the matching velocity, x position, time, or tau
- Specify a problem relative to one inertial frame and draw it relative to another
- Completely customize the appearance of the diagram (color, line thickness, line style, fonts, etc.)

## Download and install

There are currently three installers:

- An [MSI installer](https://github.com/freixas/gamma/releases/download/1.0.0-alpha3/gamma-1.0.0.msi) for x64-based Windows 10 systems (likely to work on x64 Windows 7 and 8).
- A [DEB package](https://github.com/freixas/gamma/releases/download/1.0.0-alpha3/gamma_1.0.0-1_amd64.deb) for x64 Debian-based Linux distributions
- A [DMG package](https://github.com/freixas/gamma/releases/download/1.0.0-alpha3/gamma_1.0.0.dmg) for x64-based MacOS 11 systems (might work on MacOS 10.5)

For other systems (x32-based systems, Macs with M1, other Linux distributions), 
there is a universal tar.gz file which should work as long as you can find
suitable Java and JavaFX packages. Instructions for installing the universal 
tar.gz file is at the end of this document.

## Building

To build the application from source, you will needto get and install:

- Java 17.0.2 or later. The Java executables need to be on your path.
- Maven, version 3.8.4 or later
- Git, version 2.34.1 or later
- For Windows, [the latest WiX toolset](https://wixtoolset.org/releases/).

Using git, clone the branch or tag you are interested in:

- The latest branch is the highest numbered on in the form _n_._m_.x.
- To build version n.m, look for the tag _n_._m_._b_. If there are multiple tags, the one with the largest _b_ value is the latest.

In the top level folder, type `mvn clean package`

When the dust settles, The target folder will contain an installer for your system
as well as the universal ZIP file.

## Contributing

Currently, I'm looking for physicists willing to install and review the 
application. Open [an issue](https://github.com/freixas/gamma/issues) for bugs, 
but send general comments to [gamma@freixas.org](mailto://gamma@freixas.org).

If you'd like to contribute to the code, write to [gamma@freixas.org](mailto://gamma@freixas.org)
with your ideas for changes.

## License

Gamma is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html). 

## Contact

- Found a bug? Create [an issue](https://github.com/freixas/gamma/issues) on GitHub.
- Comments or questions? Start [a discussion](https://github.com/freixas/gamma/discussions) on GitHub.

Or write to [gamma@freixas.org](mailto://gamma@freixas.org).

Gamma was created and implemented by Antonio Freixas.