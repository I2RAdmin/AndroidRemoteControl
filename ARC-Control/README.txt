How to use the Android Remote Controller!

WARNING:
This application is still in development.  There may or may not be a small army of bugs hiding inside the code, and any established code is subject to change.  
As such, take this document with a grain of salt.  Commands may not work as specified.

QUICK REFERENCE:
(only send these commands once)

local connect 0 [Connect to remote device 0]
0 features camera [Check to make sure device 0 has a camera, and load it]

(this causes the phone to take a picture, pass it every time you want a picture taken)
0 picture -1 -1 1  [Take one picture]
...

stop (waits for any pending tasks to finish, and then kills the program)
PREAMBLE:

To run, just point your command prompt at the directory that contains the ARC.exe application, and run it.  This directory will be referred to as the "run" directory for the duration of this document.

The phrase "task" is used to specify some command that has been sent to a remote device to perform.  The phrase "command" either means a command that is being processed and performed localy, or the actual command string sent along with a "task".

All units of Time are in milliseconds, unless specified otherwise.

The basic program flow looks something like this:
Establish potential connections
Connect to one or more of those connections
Get sensor information about the device on the other side of the connection
Modify the sensor to get the data you want
Use the sensor

There are four main topics to cover:
	1) The Config.Properties file [DONE]
	2) The UI and Commands [IN PROGRESS]
	3) The Sensors [TO BE WRITTEN]
	4) Other files [TO BE WRITTEN]

0) BUILDING THE EXECUTEABLE
The Android Remote Controller uses a Launch4j to create a standalone executable to run the program.  The default ant build target creates the dist folder in the project directory.  When porting the application to another machine, you will want to move this entire folder, as it contains the logger properties file and the config properties file, as well as the .jar program archive.

In addition, it can not be assumed that machine we want to run the ARC controller on has Java, so the executable is instructed to look for the java JRE in dist/jre.  However, as one can not even assume that the jre will be in a standard location on the building machine, it is left up to the programmer to copy a jre and move it to the jre folder.

In the event that the target machine has Java, one can invoke Java from the command prompt and just run the .jar file
	ex: >java -jar ARC.jar

The project uses Ant to handle compilation and building.  The Main ant target compiles and builds all the project.

1) THE CONFIG.PROPERTIES FILE
This is the config.properties file you can see in the run directory.  In this file, three atributes are specified:
	Where user input comes from (UI_IN)
	Where to send output for the user (UI_OUT)
	What type of connections to search for (CONN_TYPE)
	
The general form of a property is
[attribute]=[value]

For the UI_IN/UI_OUT attributes, there are two types of values permitted:
	STANDARD_IN/STANDARD_OUT: tells the UI_IN/UI_OUT that user input/user messages should be directed through the terminal
	filename: a error-free file name in either UI_IN or UI_OUT tells the program to use that file for input or output.  Used primerally for scripting a batch of commands to run.

For the CONN_TYPE attribute, there are several values permitted:
	WIFI tells the program to look for a wifi connection on startup
	BLUETOOTH tells the program to look for a bluetooth connection on startup
	LOCAL tells the program to use a debuging configuration.  This allows a programmer to type out phone replies in a terminal.
	
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
	Pauses user interface execution until a remote device specified by [remote device index] no longer has any pending tasks.  The optional {task ID} argument pauses execution only until the specified task has finished execution.  This command is also primeraly used for scripting, as it allows for the device to wait until a particular task (or tasks) are done before moving on to the next ones.  This is particularly useful for waiting until a sensor has been discovered and its parameters loaded, before modifying or using said sensor in a script.
	
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
	Not currently completely implemented.

	_________________________________________________________
	
	KILL:
	use: [remote device index] kill [task ID]
	
	desciption:
	This command stops the currently running task specified by [task ID].  Used for when a task is behaving strangely, or to stop a Task that has been set to run indefinitely.
	
	NOTE:
	Task ID's are assigned randomly, which makes "kill" difficult to use in a script.  Look to future version to implement this feature better.
	
	__________________________________________________________
	
	GET SENSOR PARAMETERS:
	use: [remote device index] features [sensor type]
		There are 4 sensor types: camera, audio, environment, location
	
	description:
	This command checks to see if a remote device has a particular sensor type, and if it does, what parameters are on that remote sensor.  These are things like the 'flash mode' for the camera, or the amount of distance needed between updates for the GPS unit.  Not all features can be set, some only report values.  In edition, not all settable features will be set-- due to differing implementations on remote devices, some will be able to set features that others will not, but both will list the features as setable because of Google's API.
	
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
	
	Duration must be a valid positive integer, and specifies how long we want to record audio from the microphone.  The maximum duration can be set is specified in the return from the "features" command for the microphone sensor.  To record for longer than this, you can chain "record" commands together with "pause" commands to ensure that audio will be sequential.
	
	________________________________________________________________
	
	RECORD ENVIRONMENT SENSOR DATA:
	use: [remote device index] sense [duration]
	
	description:
	This command gets environment data from the sensor suite on the remote device.  Environment sensors are passive state sensors, such as an accelerometer, magnetometer, gyroscope, etc.  The sensors used for this sense are set wih a preceding modify command sent to the environmental sensors.  Available sensors to use with sense can be discovered with the "features" command.  Duration is how long we want to sense, with a duration of '-1' set to sense until this task is killed.
	
	Data from this command comes in as raw text and is stored in a file.  Each datum is a row, with columns being the elements in a data point.  Columns are labeled [CHECK IMPLEMENTATION].  
	
	_________________________________________________________________
	
	GET POSITION:
	(note: documentation was written late on locate.  I can't check the implementation right now, and I don't actually remember how the command works.  There were several settings-- it was pretty cool.)
	use: [remote device index] locate [duration]? (Something like that.)
	
	_________________________________________________________________
	
	STOPPING THE PROGRAM:
	use: stop OR quit OR exit OR close
	
	description:
	starts the shutdown process.  Closes the UI so the program can not get any new commands, and waits for any currently running tasks to end before shutting down.
	
	
	
	
	Part LAST: Current problems:
	
	The implementation is still fairly buggy.  Any use probably wants to keep output from the program going to Standard Out, which doesn't have rich error reporting, but will tell you when it failed.  The logs (in logger.log) are actually pretty verbose, and can shed light on problems as they occur.
	
	Data sent back across the link is saved by its TaskID (ditto for data saved on the phone with the 'save-to-sd' parameter set for any sensor catagory).  This is fine for when you don't care when a picture was taken, less fine for when you need some kind of order to the pictures.
		Going to hotfix and have files be named based on creation time, so that there is some correlation between when something happened and its save name.
	
	Known:
	Input from a file is still scary.  If the file is saved in some other encoding that isn't UTF-8 or ANSI, input will probably fail.  There is no set way to recover from this at the moment.  Malformed input files tend to make the program crash without a prayer of recovery.
	The phone side of the set-up can be shifty.  I'm not to certain about the bugs, but most of the time, totally killing the app and restarting it tend to fix problems
	Corrupt/missing data in pictures.  This has been supposedly fixed twice, but evil things come in threes.  I haven't seen this particular problem in a while, but if gray bands appear in imagry from the phone, that's out of order bytes being sent across the channel.
	