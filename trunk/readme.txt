1) About
============
The Phosphorus project is addressing some of the key technical issues to enable on-demand, 
end-to-end network services across multiple domains in an optical Grid environment. 
In our work we have focused on the design and evaluation of innovative architectures 
and algorithms to efficiently manage optical Grid infrastructures. 
Since the solutions obtained in our studies can not be easily deployed in the testbed, 
a simulation environment is developed.

This simulator allows to make comparisons between different architectural and algorithmic 
proposals and assists in evaluating them in a rapid and straightforward way.


2) License
=============
The Simulation Framework for Optical Grid Infrastructure is licensed under the GNU General 
Public License v3.0 (GPLv3).


3) Installation/usage how to
===============================
The software consists out of several java files which need to be included in your IDE of choice. 
In order to be able to run the software, the JUNG library <http://jung.sourceforge.net/> must
be included in your project. 

All the specifics of the Simulation environment are described in "D5.6:Grid Simulation environment"
but a basic setup is given below.

1. Create your configuration file which contains all the basic parameters of your simulation i.e. defaultJobIAT,
defaultFlopSize etc.

2. Create a java file with a static main procedure.

3. Create the SimulationInstance variable and the simulator object and link them together.

4. Create your entities by calling the appropriate Static method of the Grid.Utilities.Util class.

5. Link your entities together with a network by creating links between the enitites.

6. If desired, adjust some of the parameters of the entites which should not have the default values.

7. Route the simulation.

8. Initialise the enitities

9. Run the simulation, print our your results and sit back and relax.  


4) Documents
===============
The deliverables which are concerned with this simulation environment are:

* D5.6: Grid Simulation environment
* D5.7: Grid Network Design
* D5.9: Extended Simulation Environment


5) Notes (if necessary)
=========================
Any bugs or comments can be sent to jens.buysse@intec.ugent.be.