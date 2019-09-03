package org.salex.robocc;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is implemented by the roboter control center and
 * will be used by the agents to send signals about events.
 *  
 * @author Sascha Gärtner
 */
public interface ICallback extends Remote {
	/**
	 * Signals, that the agent has been shut down.
	 * 
	 * @param agent The affected agent.
	 */
	public void unsubscribeAgent(IAgent agent) throws RemoteException;
}
