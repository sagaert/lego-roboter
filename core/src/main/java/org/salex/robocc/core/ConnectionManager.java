package org.salex.robocc.core;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

import org.salex.robocc.IAgent;
import org.salex.robocc.ICallback;

public class ConnectionManager implements ICallback {
	private ICallback callbackServiceStub;
	private final Set<IAgent> agents = new HashSet<IAgent>();
	
	public void start() {
		try {
			this.callbackServiceStub = (ICallback) UnicastRemoteObject.exportObject(this, 0);
			System.out.println("Callback-Service started successfully.");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			for(IAgent agent : this.agents) {
				disconnect(agent);
			}
			UnicastRemoteObject.unexportObject(this, false);
			System.out.println("Callback-Service stopped successfully.");
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}
	
	public IAgent connect(final String host, final int port) {
		try {
			final Registry registry = LocateRegistry.getRegistry(host, port);
			final IAgent agent = (IAgent) registry.lookup(IAgent.RMI_NAME);
			agent.registerCallback(this.callbackServiceStub);
			this.agents.add(agent);
			System.out.println("Connection to agent established (Host: " + host + ", Port: " + port + ").");
			return agent;
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		} catch (NotBoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void disconnect(final IAgent agent) {
		try {
			agent.unregisterCallback(this.callbackServiceStub);
			this.agents.remove(agent);
			System.out.println("Connection to agent closed by roboter control center");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsubscribeAgent(IAgent agent) {
		this.agents.remove(agent);
		System.out.println("Connection to agent closed by agent");
	}
}
