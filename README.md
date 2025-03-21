# iflye

**ifyle** is an open-source framework for **I**ncremental **F**ast **L**ightweight (y) virtual network **E**mbedding.


## Installation (development)

* Install [Temurin JDK21](https://adoptium.net/temurin/releases/) or newer.
* Install [GIPS](https://gips.dev) as described [here](https://github.com/Echtzeitsysteme/gips#installation-development) or use the [pre-built Eclipse](https://github.com/Echtzeitsysteme/gips-eclipse-build).
* Install [Gurobi](https://www.gurobi.com/) in version `12.0.1` and activate a license for your computer.
    * Currently, Gurobi is the default ILP solver used in **iflye**.
* Install [IBM ILOG CPLEX](https://www.ibm.com/products/ilog-cplex-optimization-studio) in version `22.1.2`.
    * CPLEX is an alternative ILP solver in **iflye**. You do not need it explicitely, but if you did not install and configure it properly, at least one test case will fail.
    * Please notice: CPLEX does not support SOS1 constraints with equal weights (as usually desired by the PM-/ILP-based algorithms in this projects). Therefore, the adapter implementation ignores all SOS1 constraint creations.
* Launch a runtime workspace (while using a runtime Eclipse) as stated in the [eMoflon::IBeX installation steps](https://github.com/eMoflon/emoflon-ibex?tab=readme-ov-file#how-to-develop).
    * Additionally, the runtime workspace needs some environment variables to access the Gurobi and the CPLEX solver. Do not forget to adapt them to your individual setup:
```
# Linux/macOS
GRB_LICENSE_FILE=/home/mkratz/gurobi.lic
GUROBI_HOME=/opt/gurobi1201/linux64/
LD_LIBRARY_PATH=/opt/gurobi1201/linux64/lib/:/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux/
PATH=/opt/gurobi1201/linux64/bin/:/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux/:$PATH

# Windows
GRB_LICENSE_FILE=C:\Users\mkratz\gurobi.lic
GUROBI_HOME=C:\gurobi1201\win64
LD_LIBRARY_PATH=C:\gurobi1201\win64\lib;C:\Program Files\IBM\ILOG\CPLEX_Studio2212\cplex\bin\x64_win64\
PATH=C:\gurobi1201\win64\bin;C:\Program Files\IBM\ILOG\CPLEX_Studio2212\cplex\bin\x64_win64\
```

### Project setup (manual)

* Clone this Git repository to your local machine and import it into Eclipse: *File -> Import -> General -> Existing Projects into Workspace*. Import all projects.
* Clone the [GIPS examples repo](https://github.com/Echtzeitsysteme/gips-examples) to your local machine and import it into Eclipse: *File -> Import -> General -> Existing Projects into Workspace*. Import (at least) the following projects:
    * `gips.examples.dependencies`
    * `network.model`
    * `org.emoflon.gips.gipsl.examples.mdvne`
    * `org.emoflon.gips.gipsl.examples.mdvne.bwignore`
    * `org.emoflon.gips.gipsl.examples.mdvne.migration`
    * `org.emoflon.gips.gipsl.examples.mdvne.seq`
* Inside the runtime workspace, build all projects (*Project -> Clean... -> Clean all projects*) to trigger code generation.
    * Build the projects *network.model*, *network.model.rules*, *network.model.rules.racka*, *network.model.rules.rackb*, and *network.model.rules.vnet* with the black eMoflon hammer symbol.
    * Build the GIPS projects mentioned above with the black eMoflon hammer symbol.

A good start point to verify your installation is to run the included unit tests, refer to the [test section](#tests).

### Project setup (PSF)

* As an alternative to the previous project setup section, you can use this [PSF file](./projectSet.psf) for the import of all necessary projects.

### Code-Style

This project uses the built-in code-style and code-formatter of Eclipse.
Before contributing, please set-up your Eclipse code-style settings as follows:

* _Window_ -> _Preferences_ -> _Java_
    * -> _Code Style_ -> _Clean Up_ -> _Active profile:_ -> "Eclipse [built-in]" (default)
    * -> _Code Style_ -> _Formatter_ -> _Active profile:_ -> "Eclipse [built-in]" (default)
    * -> _Code Style_ -> _Organize Imports: -> "java, javax, org, com" (default)
    * -> _Editor_ -> _Save Actions:
        * Check "Perform the selected actions on save"
        * Check "Format source code"
        * Check "Format all lines"
        * Check "Organize imports"
        * Check "Additional actions"

By using this settings, you should be unable to commit unformatted code.


## Usage (running simulations)

After finishing the installation steps, you may run simulations, e.g., from the *examples* project.
There are some examples for network generators as well as embedding algorithms.
All examples contain a `public static void main(final String[] args)` method as entry point and can be run as *Java appication* from within the Eclipse workspace.

### CLI usage

You may want to run the whole program as one exported file, e.g., on a server via the CLI for measurement purposes.
To export the whole project as executable JAR file, follow this step:
* *File -> Export... -> Java/Runnable JAR file -> Next -> (Chose your launch configuration) -> (Chose the export destination) -> Library handling: Package required libraries into generated JAR -> Finish*

Depending on your launch configuration, you can start the JAR file with additional arguments.
Example:  
`$ java -jar iflye.jar --algorithm taf --objective total-taf-comm --snetfile resources/two-tier-12-pods/snet.json --vnetfile resources/two-tier-12-pods/vnets.json --csvpath metrics.csv`

For larger simulations, you may want to increase the Java heap space.
Example with 32 GiB:
`$ java -Xmx32g -jar iflye.jar $parameters`

In the subfolder [scripts/](scripts/) are some basic Bash scripts to run parameter sweeps as well as CLI argument parsing into the scenario.

### Scenario loader

As this project is the small sibling of the [iDyVE project](https://tubiblio.ulb.tu-darmstadt.de/124918/), you may want to run the same scenarios in both frameworks, e.g., to compare the performance.
For this purpose, **iflye** has a built in model converter which can read virtual and substrate networks from JSON export files (e.g., from iDyVE).

The chosen JSON format is loosly coupled with the used metamodel.
Therefore, it acts as a kind of abstract model representation to transfer models from one metamodel/framework to the other.

Feel free to check out some examples in [vne.scenarios/resources/*/](vne.scenarios/resources/).


## Tests

Various test cases to test the framework as well as some of the implemented VNE algorithms are implemented in the project [test.suite.iflye](test.suite.iflye/).
To start them, follow this step:
* *Right click on test.suite.iflye -> Run As... -> JUnit Test*

Please notice: The test [IlpSolverSetupTest](test.suite.iflye/src/test/ilp/IlpSolverSetupTest.java) will check your Gurobi/CPLEX installation and configuration. If this test fails, at least one of the two ILP solvers is not configured properly.


## Visualization

For easier debugging purposes, a basic GUI for visualizing networks is implemented in the project [network.visualization](network.visualization/) based on [GraphViz](http://www.graphviz.org/download/).
Currently, it can render tree-based networks as tree structures or use the automatic mode of GraphViz from a model file `model.xmi` in the [examples project](examples/).
Therefore, launch the class `Ui` with these arguments: `../examples/model.xmi sub 1`
* `../examples/model.xmi` is the path of the model to read.
* `sub` is the name of the (substrate) network to visualize.
* `1` configures the automatic layout. You can also chose `0` to use a tree-like layout.

![](gfx/gui-tree.png)
![](gfx/gui-auto.png)


## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for more details.
