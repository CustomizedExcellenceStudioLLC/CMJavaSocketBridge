/********************************************************************************************************************
 * Copyright (c) 2014 CMEXC™ Customized Excellence Studio LCC.
 * CMJSocketBridge
 * CMJSocketBridge
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Customized Excellence Studio LLC.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * @author janosveres
 * @dept CMEXCM SDM SE
 *
 *******************************************************************************************************************/
package com.cmexc.socketbridge;

import java.applet.Applet;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.HashMap;

import com.cmexc.socketbridge.enums.Events;
import com.cmexc.socketbridge.enums.JSBCommand;
import com.cmexc.socketbridge.exceptions.ClientAlreadyConnectedException;
import com.cmexc.socketbridge.exceptions.ClientIdDuplicateException;
import com.cmexc.socketbridge.exceptions.ClientIdNotDefinedException;
import com.cmexc.socketbridge.exceptions.ClientIdNotRegistered;
import com.cmexc.socketbridge.factories.ClientFactory;
import com.cmexc.socketbridge.models.ClientModel;

import netscape.javascript.JSObject;

/**
 * @author janosveres
 *
 */
public class CMJSocketBridge extends Applet {
	final static int OnlineState = 1;
	final static int OfflineState = 0;
	final static int ErrorState = 2;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 735804524173385466L;
	private JSObject browser;
	private HashMap<String, ClientModel> clients;
	private final static boolean DEBUG = false;

	/**
	 * @see java.applet.Applet#init()
	 */
	public void init() {
		browser = JSObject.getWindow(this);
		
		// init fields and properties
		clients = new HashMap<String, ClientModel>();
	}

	/**
	 * @see java.applet.Applet#start()
	 */
	public void start() {
		if(this.browser == null) {
			log("[ERR] Application couldn't start because JSBHandler initialization is not finished");
			return;
		}
		
		this.browser.call("jaAppletReady", null);
		
		// Report status to handler
		this.browser.call("jaSetState", new Object[] { OnlineState });
	}

	/**
	 * @see java.applet.Applet#stop()
	 */
	public void stop() {
		// TODO: Tear down connections
	}

	/**
	 * @see java.applet.Applet#destroy()
	 */
	public void destroy() {
		// TODO: Kill threads then come to a full stop, cleanup
	}
	
	/**
	 * The method to invoke from JS to perform the privileged methods--which throw
	 * security errors if you try to access them directly.
	 * 
	 * @param who
	 * @param what  the command you want to perform (clipboard, screenshot, upload)
	 * @param data
	 */
	public boolean command(String who, String what, Object[] data){
		if(data != null){
			log("command received: " + what + " paramsCount: " + data.length + " from: " + who);
		for(int i = 0; i < data.length; i++)
	    	log("received data[" + i + "]: " + data[i]);
		} else {
			log("command received: " + what + " paramsCount: null from: " + who);
		}
		
	    final JSBCommand cmd = JSBCommand.commandByName(what);
	    if(cmd == JSBCommand.NonCommand)
	    {
	    	log("Invalid command received");
	    	return false;
	    }
	    
	    Boolean success = false;

	    // Decide whether we need elevated permissions or not
	    if(cmd == JSBCommand.Connect) {
	    	try {
	    		final String clientId = who;
	    		final Object[] dat = data;
				success = java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Boolean>(){
					@Override
					public Boolean run() throws Exception {
						// execute the privileged command
						return executeCommand(clientId, cmd, dat);
					}
				});
			} catch (PrivilegedActionException e) {
				log("Failed to execute command. PrivilegedActionException: " + e.getCause().getMessage());
				dispatchEvent(Events.Error, null, e.getCause().getMessage());
			} catch (Exception e) {
				log("Failed to execute command. " + e.getClass().getSimpleName() + ": " + e.getMessage());
				dispatchEvent(Events.Error, null, e.getClass().getSimpleName() + ": " + e.getMessage());
			}
	    } else {
	    	// simply dispatch command
	    	try {
				success = executeCommand(who, cmd, data);
			} catch (Exception e) {
				log("Failed to execute command. Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
				dispatchEvent(Events.Error, null, e.getMessage());
			}
	    }
	    
	    return success;
	}

	/**
	 * <p>Dispatching events for all threads</p>
	 * <i>Note: </i>
	 * @param event the type of event to dispatch to JSBHandler
	 * @param clientID the id of the client in JSBHandler, can be null for Bridge events
	 * @param data the error message itself, can be null for notification type events
	 */
	public void dispatchEvent(Events event, String clientID, String message) {
		String traceString = "";
		
		if(DEBUG) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		    for (StackTraceElement s : stacktrace) {
		        traceString = traceString + s.toString() + "\n\t\t";
		    }
		}
		log("dispatching event: " + event.toString() + " for client: " + clientID + "\n" + traceString);
		
		String fnToCall = null;
		switch(event) {
		case Connected:
			// call directly due to parameter count difference
			this.browser.call("jaConnected", new String[] {clientID});
			break;
		case Disconnected:
			// call directly due to parameter count difference
			this.browser.call("jaDisconnected", new String[] {clientID});
			break;
		case ClientRegistered:
			// call directly due to parameter count difference
			this.browser.call("jaClientRegistered", new String[] {clientID});
			break;
		case DataReceived:
			fnToCall = "jaReceivedData";
			break;
		case DataSent:
			fnToCall = "jaSentData";
			break;
		case Error:
			fnToCall = "jaError";
			break;
		case FailedToConnect:
			fnToCall = "jaFailedToConnect";
			break;
		case FailedToWriteData:
			fnToCall = "jaFailedToWriteData";
			break;
		default:
			break;
		}
		
		// if function was determined, only then
		if(fnToCall != null)
			this.browser.call(fnToCall, new String[] {clientID, message});
	}

	/**
	 * <p>Called directly from the javascript command dispatcher</p>
	 * <i>Note: </i>
	 * @param who The Thread/Socket who has to perform the command
	 * @param cmd
	 * @param data 
	 * @throws Exception 
	 */
	protected boolean executeCommand(String who, JSBCommand cmd, Object[] data) throws Exception {
		boolean retVal = false;
		switch(cmd){
		case RegisterClient:
			retVal = createClient(data);
			break;
		case Connect:
			retVal = connect(who);
			break;
		case Disconnect: {
			retVal = disconnect(who);
			break;
		}
		case NonCommand:
			throw new Exception("Cannot perform non-defined commands. Oops.");
		case SendMessage: {
			if(data == null || data.length < 1)
				throw new Exception("Missing Data message");
			retVal = sendMessage(who, (String) data[0]);
			break;
		}
		default:
			break;
		}
		return retVal;
	}

	/**
	 * <p></p>
	 * <i>Note: </i>
	 * @param who
	 * @param data
	 * @return
	 * @throws ClientIdNotRegistered
	 */
	private boolean sendMessage(String who, String message)
			throws ClientIdNotRegistered {
		boolean retVal;
		ClientModel client = this.clients.get(who);
		if(client == null)
			throw new ClientIdNotRegistered(who);
		retVal = client.sendMessage(message);
		return retVal;
	}

	/**
	 * <p></p>
	 * <i>Note: </i>
	 * @param who
	 * @return
	 * @throws ClientIdNotRegistered
	 */
	private boolean disconnect(String who) throws ClientIdNotRegistered {
		boolean retVal;
		ClientModel client = this.clients.get(who);
		if(client == null)
			throw new ClientIdNotRegistered(who);
		retVal = client.disconnect();
		return retVal;
	}
	
	/**
	 * <p>Tell a client to connect to its remote end</p>
	 * <i>Note: Host and port is predefined at init</i>
	 * @param who
	 * @return
	 * @throws ClientIdNotRegistered 
	 * @throws IOException 
	 * @throws ClientAlreadyConnectedException 
	 */
	private boolean connect(String who) throws ClientIdNotRegistered, ClientAlreadyConnectedException {
		if(!this.clients.containsKey(who))
			throw new ClientIdNotRegistered(who);
		
		ClientModel cm = this.clients.get(who);

		boolean success;
		try {
			success = cm.connect();
		} catch (IOException e) {
			success = false; 
			// report failed to connect
			dispatchEvent(Events.FailedToConnect, who, e.getMessage());
		}
		
		return success;
	}

	/**
	 * <p>Creates a new ClientConnection with a remote end.</p>
	 * <i>Note: Connection is not started automatically, however its own thread is initialized right away. For connecting call {@link #connect(String) #connect(String)}</i>
	 * @param who
	 * @param data contains constructor info for client in the following format: { host : "hostname", port : "port" }
	 * @return
	 * @throws Exception 
	 */
	private boolean createClient(Object[] data) throws Exception {
		// Translate JSObject to Java
		String clientName = (String) data[0];
		String hostname = (String) data[1];
		Double port = (Double) data[2];
		
		log("creating new clientmodel with client: " + clientName + " host: " + hostname + " port: " + port);
		
		if(data.length < 3)
			throw new Exception("Parameter array's Length is incorrect. Specify the following: [clientID, hostname, port]");
		else if(clientName == null)
			throw new ClientIdNotDefinedException(data.toString());
		else if(this.clients.containsKey(clientName))
			throw new ClientIdDuplicateException(clientName);
		
		// fire up new client thread
		ClientModel client = ClientFactory.makeClient(clientName, hostname, port, this);
		Thread t = new Thread(client); // name the thread after the client automatically
		this.clients.put(clientName, client);
		t.start(); // kick off - but we don't retain the thread
		
		boolean success = this.clients.containsKey(clientName);
		if(success) 
			this.dispatchEvent(Events.ClientRegistered, (String) data[0], null);
		
		return success; // if clients contains the clientId, we most probably succeeded
	}

	// Log something to console
	public void log(String message){
		System.out.println(message);
	}

	/**
	 * <p></p>
	 * <i>Note: </i>
	 * @param clientId
	 * @param message
	 */
	public synchronized void clientReceivedMessage(String clientId, String message) {
		// just dispatch it
		dispatchEvent(Events.DataReceived, clientId, message);
	}
}
