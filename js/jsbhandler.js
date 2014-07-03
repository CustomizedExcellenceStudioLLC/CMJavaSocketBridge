//The MIT License (MIT)
//
//Copyright (c) 2014 Customized Excellence Studio LLC.
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

// Config
// 
var appletId = "CMJavaSocketBridge";

// Java Applet Accessor Methods
function jaAppletReady() {
    if(jsb.instance != null) {
        jsb.instance.jaAppletReady();
    } else {
        console.warn("JSBHandler instance not initialized");
    }
}
    
function jaSetState(state) {
    console.info('setting jsbhandler state: ' + state);
    jsb.instance.jaSetState(state); 
}

// Method is called when a client has connected to a remote socket
function jaConnected(clientID) {
    console.info(clientID + ' client connected to remote host');
    jsb.instance.jaConnected(clientID);
}

// Method is called when a client has disconnected
function jaDisconnected(clientID) {
    console.info(clientID + ' client disconnected from remote host');
    jsb.instance.jaDisconnected(clientID);
}

// Method is called when a client fails to connect initially
function jaFailedToConnect(clientID, reason) {
    console.warn(clientID + ' client failed to connect to remote host, because: '  + reason);
    jsb.instance.jaFailedToConnectfunction(clientID, reason);
}

// Method is called when a client has received data
function jaReceivedData(clientID, data) {
    console.info(clientID + ' received data from remote host. Data: ' + data);
    jsb.instance.jaReceivedData(clientID, data);
}
    
// Method is called when a client has sent data
function jaSentData(clientID, data) {
    console.info(clientID + ' client sent data to remote host. Data: ' + data);
    jsb.instance.jaSentData(clientID, data);

}
    
// Method is called when a client has failed to write data
function jaFailedToWriteData(clientID, data) {
    console.warn(clientID + ' client failed to send data to remote host. Data: ' + data);
    jsb.instance.jaFailedToWriteData(clientID, data);
}

// Method is called when a client or the applet has encountered an error 
function jaError(clientID, error) {
    console.warn(clientID + ' client hit error. Error: ' + error);
    jsb.instance.jaError(clientID, error);
}

function jaClientRegistered(clientID) {
    console.info(clientID + ' client registered with Applet.');
    jsb.instance.jaClientRegistered(clientID);
}

// Singleton object
function jsb(javaAppletID) {
    // documentation purposes
    this.version = "0.6";
    this.description = "Handler for Multithreaded Java Socket Bridge Applet";
    this.license = "The MIT License (MIT)";
    
    if(jsb.applet == null)
    {
        // cannot send error event from constructor.... so.
        alert('CMJavaSocketBridge applet cannot be found. Quitting script execution.' + jsb.applet);
        return; // quit
    }
    
    // Properties
    this.clients = {};
    this.state = 0;
}

jsb.constructor = jsb;
jsb.commands = {
    send : "sendmessage",
    connect : "connect",
    disconnect : "disconnect",
    register : "registerClient"
};
jsb.prototype = {
    // Reports an error to listeners of the Handler
    error : function (errMessage, data) {
        console.error("triggering error: " + errMessage + " data: " + data);
        $(this).trigger( "error", [ errMessage, data ] );  
    },
    
    // Returns the current State of the Java Applet as a String [offline, online, error]
    getState : function () {
        var state = 'undefined';
        if(this.state == 0)
            state = 'offline';
        else if (this.state == 1)
            state = 'online';
        else
            state = 'error';

        return state;  
    },
    
    // Registers a client if applet is in online state
    registerClient : function (clientID, host, port) {
        if(this.state == 0) 
        {
            this.error("Cannot register client when Java Applet is not ready", {
                id : clientID,
                host : host,
                port : port
            });
            return "Cannot register client when Java Applet is not ready";
        }

        // otherwise register client
        var client = new JSBClient(clientID, host, port);  
        if(client == null) 
        {
            this.error("Failed to initialize client", {
                id : clientID,
                host : host,
                port : port
            });
            return "Failed to initialize client";
        }

        // finally add created client to list
        this.clients.clientID = client;
        
        // dispatch client registration to Applet
        jsb.applet.command(null, jsb.commands.register, [clientID, host, port]);
        
        return client;
    },
    
    // Returns a client object with a specific id or null if there's no such client
    getClient : function (clientID) {
        // Find the appropriate client to trigger the event
        var client = this.clients.clientID;
        if(client == null)
        {
            // there's no such client --> report error
            this.error("Unregistered client requested.", clientID);
        } 

        return client;
    },
        
    //
    // Applet Methods - called directly from the Java Applet for interaction and updates
    //
    // These methods must only be called from the Java Applet directly !! 
    // Do NOT use them inside your javascript / Objective-J application
    //
    
    // Method is called when the Java Applet finished initializing and is ready for connections
    jaAppletReady : function () {
        // can be used for custom initialization
    },
    
    jaSetState : function (state) {
        this.state = state;  
        if(this.state == 1) // now online
            $(this).trigger("ready");
    },
    
    // Method is called when a client has connected to a remote socket
    jaConnected : function (clientID) {
        // Find the appropriate client to trigger the event
        var client = this.clients.clientID;
        if(client == null)
        {
            // there's no such client --> report error
            this.error("Unregistered client reported connection established.", clientID);
        } 
        else 
        {
            // there's a client, so go ahead
            client.connected();
        }
    },
    
    // Method is called when a client has disconnected
    jaDisconnected : function (clientID) {
        var client = this.getClient(clientID);
        if(client != null) 
        {
            // there's a client, so go ahead
            client.disconnected();
        }
    },
    
    // Method is called when a client fails to connect initially
    jaFailedToConnect : function (clientID, reason) {
        var client = this.getClient(clientID);
        if(client != null)
        {
            client.failedToConnect(reason);
        }
    },

    // Method is called when a client has received data
    jaReceivedData : function (clientID, data) {
        var client = this.getClient(clientID);
        if(client == null)
        {
            // there's no such client --> report error
            this.error("Unregistered client reported data received.", {
                "clientID" : clientID,
                "data" : data
            });
        } 
        else 
        {
            // client received data
            client.readData(data);
        }
    },
    
    // Method is called when a client has sent data
    jaSentData : function (clientID, data) {
        var client = this.getClient(clientID);
        if(client == null)
        {
            // there's no such client --> report error
            this.error("Unregistered client reported data sent.", {
                "clientID" : clientID,
                "data" : data
            });
        } 
        else 
        {
            // client sent data
            client.sentData(data);
        }
    },
    
    // Method is called when a client has failed to write data
    jaFailedToWriteData : function (clientID, data) {
        var client = this.getClient(clientID);
        if(client != null) 
        {
            // client received data
            client.failedToWrite(data);
        }
    },

    // Method is called when a client or the applet has encountered an error 
    jaError : function (clientID, error) {
        if(clientID == null)
        {
            // report java applet error directly
            this.error("[ERR] Java Applet has encountered an error. ", error);
        }
        else
        {
            // dispatch error to client directly
            var client = this.getClient(clientID);
            if(client != null)
            {
                client.error("[ERR] JSBClient error. ", error);
            }
        }
    },
    
    // Method is called when a client has finished the registration process
    jaClientRegistered : function (clientID) {
        if(clientID == null)
        {
            // report java applet error directly
            this.error("[ERR] Java Applet failed to register client. ");
        } else {
            // dispatch error to client directly
            var client = this.getClient(clientID);
            if(client != null)
            {
                $(this).trigger("clientRegistered", client);
            }
        }
    }
};

// Singleton instance holder
jsb.instance = null;

// Applet holder
jsb.applet = null;

// Static Singleton accessor for the jsbhandler JSObject for the Java Applet and other javascript functions
jsb.handler = function () {
    if(jsb.instance == null) {
        // load applet the first time the instance is created
        jsb.applet = document.getElementById(appletId);
        jsb.instance = new jsb(appletId);
    }

    return jsb.instance; // singleton object
};


// Client Class for handling threads
function JSBClient (clientID, host, port) {
    this.clientID = clientID;
    this.isConnected = false;
    this.host = host;
    this.port = port;
    this.connectionTimestamp;
}

JSBClient.constructor = JSBClient;
JSBClient.prototype = {
    getInfo : function() {
        return this.clientID + ' is connected: ' + this.isConnected;    
    },
    
    // Triggers a connect method in the Java Applet
    connect : function() {
        if(jsb.instance.state == 1 && !this.isConnected) {
            // execute command through applet
            jsb.applet.command(this.clientID, jsb.commands.connect, null);
        }
        else {
            this.error("Cannot connect when Java Bridge is not ready or Client is not connected. Data: [Applet State, Client isConnected]", [jsb.instance.getState(), this.isConnected ]);
        }
    },
    
    // Triggers a disconnect method in the Java Applet
    disconnect : function () {
        if(jsb.instance.state == 1 && this.isConnected) {
            // execute command through applet
            jsb.applet.command(this.clientID, jsb.commands.disconnect, null);
        }
        else {
            this.error("Cannot disconnect when Java Bridge is not ready or Client is not connected. Data: [Applet State, Client isConnected]", [jsb.instance.getState(), this.isConnected ]);
        }
    },
    
    // Triggers a send data method in the Java Applet
    send : function (data) {
        if(jsb.instance.state == 1 && this.isConnected) {
            // execute command through applet
            jsb.applet.command(this.clientID, jsb.commands.send, [data]);
        }
        else {
            this.error("Cannot send data when Java Bridge is not ready or Client is not connected. Data: [Applet State, Client isConnected]", [jsb.instance.getState(), this.isConnected ]);
        }
    },
        
    // Method tells how much time has been spent in connection with the remote end
    connectionTime : function () {
        var time = (Date.now() - this.connectionTimestamp) / 1000;
        if (time < 0)
            time = 0;
        return time;
    },
    
    //
    // Callback Methods
    // Do NOT call these methods directly
    //
    
    // Method is called when java applet reports this client connected to a remote socket through handler
    connected : function () {
        // connect logically and trigger connect event
        this.isConnected = true;
        this.connectionTimestamp = Date.now;
        
        $(this).trigger( "connected" );  
        
    },
    
    // Method is called when remote socket has reset the connection or a network failure has occured, or even when the client has disconnected gracefully
    disconnected : function () {
        // disconnect logically and trigger disconnect event
        this.isConnected = false;
        $(this).trigger( "disconnected" );  
    },
    
    // Method is called when initial connect failed to establish
    failedToConnect : function (reason) {
        $(this).trigger("failedToConnect", reason);  
    },
    
    // Method is called when data is received on socket
    readData : function (data) {
        // trigger read data event
        $(this).trigger( "readData", data);  
    },
    
    // Method is called when data has been successfully written to remote socket
    sentData : function (data) {
        // trigger sent data event
        $(this).trigger( "sentData", data);    
    },
    
    // Method is called when socket failed to write to remote end
    failedToWrite : function (data) {
        // trigger failed data send event
        $(this).trigger( "failedToWriteData", data);  
    },
    
    // Method is called when an error has occoured
    error : function (errMessage, data) {
        // simply trigger error event
        $(this).trigger( "error", [ errMessage, data ] );
    }
}