How to use the Android Remote Controller!

WARNING:
This application is still in development.  There may or may not be a small army of bugs hiding inside the code, and any established code is subject to change.  
As such, take this document with a grain of salt.  Commands may not work as specified.

To run, just point your command prompt at the directory that contains the ARC.exe application, and run it.  This directory will be refered to as the "run" directory for the duration of this document.

The basic program flow looks something like this:
Establish potential connections
Connect to one or more of those connections
Get sensor information about the device on the other side of the connection
Modify the sensor to get the data you want
Use the sensor

There are four main topics to cover:
	1) The Config.Properties file
	2) The UI and Commands
	3) The Sensors
	4) Other files

	
1) THE CONFIG.PROPERTIES FILE
This is the config.properties file you can see in the run directory.  In this file, three atributes are specified:
	Where user input comes from (UI_IN)
	Where to send output for the user (UI_OUT)
	What type of connections to search for (CONN_TYPE)
	
The general form of a property is
[attribute]=[value]

For the pair of UI attributes, there are two types of values permitted:
	STANDARD_IN/STANDARD_OUT tells the UI_IN/UI_OUT that user input/user messages should be directed through the terminal
	filename: a error-free file name in either UI_IN or UI_OUT tells the program to use that file for input or output.  Used primerally for scripting a batch of commands to run.

For the CONN_TYPE attribute, there are several values permitted:
	WIFI tells the program to look for a wifi connection on startup
	BLUETOOTH tells the program to look for a bluetooth connection on startup

More than one CONN_TYPE can be set as a comma seperated list:
	ex: WIFI,BLUETOOTH searches for both wifi connections and bluetooth connections


	
2) THE UI AND COMMANDS
This section deals with the various commands that the Android Remote Controller supports and how to use them.  
This is dividied into three parts:
	2A) local commands
	2B) remote commands
	2C) some examples
	
2A) LOCAL COMMANDS
local commands follow the form:
local [command] [arguments]

the local device index can also be used:
-1 [command] [arguments]

The various local commands are:
	
	CONNECT:
	use: local connect [remote connection index]
	
	description:
	Connect connects the PC side of the Android Remote Controller to a remote device running the remote side.  This command must be called before any remote commands can be sent to that device.
	During the search step at start up, the PC side will report potential connections it has found, and what index those connections have been assigned.  Those integers are the valid arguments
	for [remote connection index].
	When a connection has been successfully established, a message is displayed back to the UI, which indicates that the remote device at the specified index is ready to recieve commands.
	
	_______________________________________________
	
	HELP:
	use: local help
	
	description:
	Tells the user to look at this document.
	
	_______________________________________________
	
	