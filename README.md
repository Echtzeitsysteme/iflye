# iflye

**ifyle** is an open-source framework for **I**ncremental **F**ast **L**ightweight (y) virtual network **E**mbedding.


## Installation (development)

* Install [AdoptOpenJDK 13 (HotSpot JVM)](https://adoptopenjdk.net/releases.html?variant=openjdk13&jvmVariant=hotspot).
* Install eMoflon::IBeX as described [here](https://github.com/eMoflon/emoflon-ibex#how-to-develop).
* Install [Gurobi](https://www.gurobi.com/) in version `8.1.1` and activate a license for your computer.
* Launch a runtime workspace (while using a runtime Eclipse) as stated in the eMoflon::IBeX installation steps.
    * Additionally, the runtime workspace needs some environment variables to access the Gurobi solver (Do not forget to adapt them to your individual setup):
```
GRB_LICENSE_FILE=/home/maxkratz/gruobi.lic
GUROBI_HOME=/opt/gurobi811/linux64/
GUROBI_JAR_PATH=/opt/gurobi811/linux64/lib/gurobi.jar
LD_LIBRARY_PATH=/opt/gurobi811/linux64/lib/
PATH=/opt/gurobi811/linux64/bin/:$PATH
```
* Clone this Git repository to your local machine and import it: *File -> Import -> General -> Existing Projects into Workspace*. Import all projects.
* Inside the runtime workspace, build all projects (Project -> Clean... -> Clean all projects) to trigger code generation.
    * Build the projects *network.model* and *model.rules* with the black eMoflon hammer symbol.


## Usage (running simulations)

After finishing the installation steps, one may run simulations e.g. from the *examples* project.
There are some examples for network generators as well as embedding algorithms.
All examples contain a `public static void main(final String[] args)` method as entry point and can be run as *Java appication* from within the eclipse workspace.


### CLI usage

One may want to run the whole program as one exported file e.g. on the CLI for measurement purposes.
To export the whole project as executable JAR file, follow this step:
* *File -> Export... -> Java/Runnable JAR file -> Next -> (Chose your launch configuration) -> (Chose the export destination) -> Library handling: Package required libraries into generated JAR -> Finish*

Depending on your launch configuration, you can start the JAR file with additional arguments.
Example:
`$ java -jar iflye.jar`

For larger simulations, you may want to increase the java heap space.
Example with 32 GiB::
`$ java -Xmx32g -jar iflye.jar`

In the subfolder [vne.scenarios/scripts/](vne.scenarios/scripts/) are some basic bash scripts to run parameter sweeps as well as CLI argument parsing into the scenario.


### Scenario loader

As this project is the small sibling of the [idyve project](https://tubiblio.ulb.tu-darmstadt.de/124918/), one may want to run the same scenarios in both frameworks.
For this purpose, **iflye** has a built in model converter which can read virtual and substrate networks from JSON export files (e.g. from idyve).

The chosen JSON format is loosly coupled with the used metamodel.
Therefore, it acts as a kind of abstract model representation.

Feel free to check out some examples in [vne.scenarios/resources/*/](vne.scenarios/resources/).


## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for more details.
