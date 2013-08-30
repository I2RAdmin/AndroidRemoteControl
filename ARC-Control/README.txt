How to use the Android Remote Controller!

WARNING:
This application is still in development.  There may or may not be a small army of bugs hiding inside the code, and any established code is subject to change.  
As such, take this document with a grain of salt.  Commands may not work as specified.

PREAMBLE:

To run, just point your command prompt at the directory that contains the ARC.exe application, and run it.  This directory will be refered to as the "run" directory for the duration of this document.

The phrase "task" is used to specify some command that has been sent to a remote device to perform.  The phrase "command" either means a command that is being processed and performed localy, or the actual command string sent along with a "task".

All units of Time are in milliseconds, unless specified otherwise.

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
	
	FREEZE:
	use: local freeze [pause time]
	
	description:
	Stops execution of the user interface for the number of milliseconds specified in [pause time].  This command should really only be called when running the program from an input file, and only after
	the connect command to give the program enough time to establish a connection before executing the next set of lines
	
	_________________________________________________
	
	LIST REMOTE DEVICES:
	use: local devices
	
	description:
	Returns a list of potential connections to the user to use.  This list can change at runtime, if a connection is lost from a remote device, then that particular connection is removed from the valid connection list.
	
	__________________________________________________
	
	LIST REMOTE DEVICE SENSORS:
	use: local sensors [remote connection index]
	
	description:
	After a remote device has been successfully connected to, we can start discovering what sensors that remote device has.  This command return a list of senors that have been discovered so far on the remote device specified by the [remote connection index].  More sensors can be discovered with the "features" command on a particular remote device.
	
	___________________________________________________
	
	PAUSE:
	use: local pause [remote device index] {task ID}
	
	desciption:
	Pauses user interface execution until a remote device specified by [remote device index] no longer has any pending tasks.  The optional {task ID} argument pauses execution only until the speceified task has finished execution.  This command is also primeraly used for scripting, as it allows for the device to wait until a particular task (or tasks) are done before moving on to the next ones.  This is particularly useful for waiting until a sensor has been discovered and its parameters loaded, before modifying or using said sensor in a script.
	
	NOTE:
	Task ID's, at the time of writing, are randomly generated.  As such, the {task ID} argument is pretty useless.  Look to future releases for a way to support this particular feature.
	
	____________________________________________________

	
2B) REMOTE COMMANDS
Commands that deal with a remote device are of the general form:
[remote device index] [command] [arguments]

The various remote commands are:
	
	DO NOTHING
	use: [remote device index] ping
	
	description:
	This command is not designed to be performed by the user.  The program pings each remote connection to ensure liveliness.  If there is an error sending a ping over the connection, then the the program starts attempting to reconnect to the remote device.
	
	As such, a ping task never gets saved to the list of tasks sent to a remote device, and is generally handled without saying anything to the end user.  However, the command is exposed for use, although it is recommended that it is not used.
	
	_______________________________________________________
	
	KILL ALL:
	use [remote device index] nuke
	
	desciption:
	This command removes all tasks that the PC client has sent to a remote device, and tells the remote device to flush its own task list.  Use was designed for killing the program mid processing, or other unusual shutdown routines.  Does not close the connection after being used.
	
	NOTE:
	Not currently completely impemented.

	_________________________________________________________
	
	KILL:
	use: [remote device index] kill [task ID]
	
	desciption:
	This command stops the currently  running task specified by [task ID].  Used for when a task is behaving strangely, or to stop a Task that has been set to run indefinantely.
	
	NOTE:
	Task ID's are assigned randomly, which makes "kill" difficult to use in a script.  Look to future version to implement this feature better.
	
	__________________________________________________________
	
	MODIFY SENSOR:
	use: [remote device index] modify [sensor name] [feature name 1] [new feature value 1] [feature name 2] [new feature value 2]...
	
	desciption:
	This command changes the values stored on [sensor name]'s [feature name]s to the [new feature value]s.  Several sensors require a modify command to be performed before they can be used.  A sensor's features (and the current values and acceptable values) can be retrieved with a "features" command.  For other sensors, this command can change some part of a sensor, for instance, the camera's flash can be turned on or off with a modify command for the camera.
	
	Senors that require a modify before use:
		Environment (used with the "sense" command): The exact sensors we want to record from in the Environment sensor suite need to have an update timer set with the modify command before they will return data with a sense command
		Location (used with the "locate" command): The location provider we want to get position data from needs to be set before data will be returned from a "locate" command
		
	___________________________________________________________
	
	TAKE PICTURE:
	use: {remote device index] picture [delay between pictures] [duration to continuously take pictures] [number of pictures to take]
	
	desciption:
	This command takes a picture with a remote device's camera sensor.  The settings used are those specified with the last "modify" command sent to the camera, along with any settings set with the take picture command.
	
	-1 can be set for any of the arguments passed to the take picture command, in which case, the default value on the remote device is used in its place.  If both [duration to continuously take pictures] and [number of pictures to take] are -1, pictures are taken continously until a "kill" command is sent for this task.
	
	_____________________________________________________________
	
	RECORD AUDIO:
	use: [remote device index] record [duration]
	
	description:
	This command records audio from a remote device's microphone sensor.  The settings used are those specified with this command along with the last "modify" command sent to the microphone.
	
	Duration must be a valid positive integer, and specifies how long we want to record audio from the microphone.  The maximum duration can be set is specified in the return from the "features" command for the microphone sensor.  To record for longer than this, you can chain "record" commands together with "pause" commands to ensure that audio will be sequental.
	
	________________________________________________________________
	
	RECORD ENVIRONMENT SENSOR DATA:
	use: [remote device index] sense [duration]
	
	description:
	This command gets environment 