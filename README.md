CMJavaSocketBridge
==================

##Overview

##Installation
JSBHandler uses jQuery for event distribution, so the first order of business is to include jQuery latest in you html file. Right after jQuery be sure to reference the JSBHandler javascript.
``` html
<script src="http://code.jquery.com/jquery-1.11.1.min.js"></script> <!-- jQuery -->
<script src="../js/jsbhandler.js"></script> <!-- JSBHandler -->
```
The handler is looking for the Java Applet that comes with this library. Put it somewhere in the *body* of your html document, like this:
``` html
<applet id="CMJavaSocketBridge" archive="../path/to/CMJavaSocketBridge.jar" code=com.cmexc.socketbridge.CMJSocketBridge.class width="1" height="1" mayscript></applet>
```
That's it! :) Now you can start using the handler.
##Usage
Instantiate the handler:
``` javascript 
var handler = jsb.handler();
```

Setup a client:
``` javascript
var client = handler.registerClient("name", "host", port);
```
###Events
####JSBHandler events
- *ready* listen for callback:
``` javascript
$(handler).on('ready', function() { ... });
```
> **Cause**: 
> Ready event is triggered when the Java Applet finished initializing and is ready for registering clients and creating connections.

- *clientRegistered* listen for callback:
``` javascript
$(handler).on('clientRegistered', function(event, client) { ... });
```
> **Cause**: 
> Registered event is triggered when the Java Applet finished the setup of the client object and client thread. 
>
> **Parameters**:
> *event* - jQuery event Object
> *client* - a reference to the JSBClient Object that has been setup.

- *error* listen for callback:
``` javascript
$(handler).on('error', function(event, error) { ... });
```
> **Cause**:
> Error is triggered, whenever the Java Applet encounters an error.
>
> **Parameters**:
> *event* - jQuery event Object
> *error* - array with [ errMessage, data ]
>
> **NOTE**: *data* can be either a javascript literal object or the string representation of the message of an exception from the Java Applet. Value depends on origin of the error itself.
> Examples for error messages:
> - connecting
> - registering a client with duplicate ID

####JSBClient events
- *error* listen for callback:
``` javascript
$(handler).on('error', function(event, error) { ... });
```
> **Cause**:
> Error is triggered, whenever the dedicated client thread in the Java Applet fails in any way.
>
> **Parameters**:
> *event* - jQuery event Object
> *error* - array with [ errMessage, data ]
>
> **NOTE**: *data* can be either a javascript literal object or the string representation of the message of an exception from the Java Applet. Value depends on origin of the error itself.
> Examples for error messages:
> - IO exception during socket read/write
> - remote host closed connection
> - failed to connect to host
> - failed to send packet

- *connected* listen for callback:
``` javascript
$(handler).on('connected', function() { ... });
```
> **Cause**: 
> Connected event is triggered when the Java Applet's dedicated client thread successfully connected to the remote host. 

- *disconnected* listen for callback:
``` javascript
$(handler).on('disconnected', function() { ... });
```
> **Cause**: 
> Disconnected event is triggered when remote socket has reset the connection or a network failure has occurred, or even when the client has disconnected gracefully.

- *failedToConnect* listen for callback:
``` javascript
$(handler).on('failedToConnect', function(event, reason) { ... });
```
> **Cause**: 
> FailedToConnect event is triggered when a connect method in Java Applet fails. 
>
> **Parameters**:
> *event* - jQuery event Object
> *reason* - the reason why connection establishment failed

- *readData* listen for callback:
``` javascript
$(handler).on('readData', function(event, data) { ... });
```
> **Cause**: 
> ReadData event is triggered when the dedicated client thread in the Java Applet receives data from the remote host. 
>
> **Parameters**:
> *event* - jQuery event Object
> *data* - the data received from the remote host

- *sentData* listen for callback:
``` javascript
$(handler).on('sentData', function(event, data) { ... });
```
> **Cause**: 
> SentData event is triggered when the dedicated client thread in the Java Applet successfully sent the data to the remote host. 
>
> **Parameters**:
> *event* - jQuery event Object
> *data* - the data sent to the remote host

- *failedToWriteData* listen for callback:
``` javascript
$(handler).on('failedToWriteData', function(event, data) { ... });
```
> **Cause**: 
> FailedToWriteData event is triggered when the dedicated client thread in the Java Applet fails to write data to the remote host. 
>
> **Parameters**:
> *event* - jQuery event Object
> *data* - the data which has failed to be sent 

###Available Methods
####JSBHandler methods
- *getState()*
- *registerClient(clientID, host, port)*
- *getClient(clientID)*

####JSBClient methods
- *getInfo()*
- *connect()*
- *disconnect()*
- *send(data)*
- *connectionTime()*

> **NOTE:** TCP Send/Receive Behaviour Customization
Java Applet currently uses println() and .readLine() methods when sending packets to or receiving packet from the remote host. To change this behaviour, you have to edit the *BaseClient.java* and *ClientModel.java* files, then you have to compile and sign the Java Applet yourself.

##License
CMJavaSocketBridge is under MIT license so feel free to use it!

##Author
Made by Customized Excellence Studio LLC. If you have any question, feel free to drop us a line at [support@cmexc.com](mailto:support@cmexc.com)
