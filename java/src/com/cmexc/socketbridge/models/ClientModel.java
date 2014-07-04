/********************************************************************************************************************
 * Copyright (c) 2014 CMEXC™ Customized Excellence Studio LCC.
 * CMJSocketBridge
 * ClientModel
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
package com.cmexc.socketbridge.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.cmexc.socketbridge.CMJSocketBridge;
import com.cmexc.socketbridge.enums.Events;
import com.cmexc.socketbridge.exceptions.ClientAlreadyConnectedException;
import com.cmexc.socketbridge.interfaces.ClientConnection;

/**
 * @author janosveres
 *
 */
public abstract class ClientModel implements Runnable, ClientConnection {
	protected boolean stopping = false; 
	
	protected String clientId;
	protected String host;
	protected int port;
	protected Socket socket;
	protected CMJSocketBridge bridge;
	
	protected PrintWriter out;
	protected BufferedReader in;

	protected boolean isConnected;

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	
	/**
	 * @param port 
	 * @param id 
	 * 
	 */
	public ClientModel(String id, String hostname, int port, CMJSocketBridge bridge) {
		this.clientId = id;
		this.host = hostname;
		this.port = port;
		this.bridge = bridge;
	}

	/* (non-Javadoc)
	 * @see com.cmexc.socketbridge.interfaces.ClientConnection#connect()
	 */
	@Override
	public boolean connect() throws ClientAlreadyConnectedException, IOException {
		if(this.socket != null && this.socket.isConnected() && isConnected)
			throw new ClientAlreadyConnectedException();
		
		this.socket = new Socket(host, port);
		out = new PrintWriter(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		isConnected = true;
		
		bridge.dispatchEvent(Events.Connected, clientId, null);
		
		return true; // if we get to this point, we're out of the woods
	}

	/* (non-Javadoc)
	 * @see com.cmexc.socketbridge.interfaces.ClientConnection#disconnect()
	 */
	@Override
	public boolean disconnect() {
		try {
			this.stopping = true;
			this.socket.close();
			this.in.close();
			this.out.close();
		} catch (IOException e) {
			bridge.dispatchEvent(Events.Error, clientId, "Failed to disconnect. " + e.getMessage());
		}

		return !isConnected;
	}

	/* (non-Javadoc)
	 * @see com.cmexc.socketbridge.interfaces.ClientConnection#sendMessage()
	 */
	@Override
	public synchronized boolean sendMessage(String message) {
		// for now
		out.println(message);
		
		boolean error = out.checkError();
		if(error)
			bridge.dispatchEvent(Events.FailedToWriteData, clientId, message);
		else
			bridge.dispatchEvent(Events.DataSent, clientId, message);
		
		return !error;
	}
}
