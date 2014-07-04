/********************************************************************************************************************
 * Copyright (c) 2014 CMEXC™ Customized Excellence Studio LCC.
 * CMJSocketBridge
 * ClientConnection
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

import java.io.IOException;

import com.cmexc.socketbridge.CMJSocketBridge;
import com.cmexc.socketbridge.enums.Events;

/**
 * @author janosveres
 *
 */
public class BaseClient extends ClientModel {

	/**
	 * @param id
	 * @param port
	 */
	public BaseClient(String id, String host, int port, CMJSocketBridge bridge) {
		super(id, host, port, bridge);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted() || !stopping) {
			
			try {
				// Only listen to message if we've been connected
				if(isConnected) {
					String message = in.readLine();
					bridge.clientReceivedMessage(this.clientId, message);
				} 
			} catch (IOException e) {
				if(!stopping)
					bridge.dispatchEvent(Events.Error, clientId, "IOException on client receive. " + e.getMessage());
			} 
			
			// if we're outside the here with stopping, we should change state
			if(stopping && isConnected)
			{
				// only send once
				isConnected = false;
				bridge.dispatchEvent(Events.Disconnected, clientId, null);
			}
			
			// anyhow, sleep here
			try {
				Thread.sleep(100); // sleep 100 ms
			} catch (InterruptedException e) {
				bridge.dispatchEvent(Events.Error, clientId, "Thread interrupted on client run." + e.getMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cmexc.socketbridge.models.ClientModel#sendMessage()
	 */
	@Override
	public synchronized boolean sendMessage(String message) {
		return super.sendMessage(message);
	}

}
