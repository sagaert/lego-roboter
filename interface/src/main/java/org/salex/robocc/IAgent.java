package org.salex.robocc;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is implemented by the agents and
 * will be used by the roboter control center to
 * send comands and request state.
 * 
 * @author Sascha Gärtner
 */
public interface IAgent extends Remote {
	public static final String RMI_NAME = "org.salex.robocc.agent";
	
	/**
	 * The default port the agent will listen for connections from a roboter control center. 
	 * 
	 * Inventors year of birth :-)
	 */
	public static final int DEFAULT_PORT = 1976;
		
	public void registerCallback(ICallback callback) throws RemoteException;
	public void unregisterCallback(ICallback callback) throws RemoteException;
}
