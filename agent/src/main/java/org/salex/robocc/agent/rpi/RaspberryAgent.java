package org.salex.robocc.agent.rpi;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.salex.robocc.IAgent;
import org.salex.robocc.ICallback;

/**
 * A roboter control center agent for the raspberry pi.
 * Uses the pi4j library to control the hardware through the gpio port of the raspberry pi.
 * 
 * @author Sascha Gärtner
 */
public class RaspberryAgent implements IAgent {
	/**
	 * The program itselt will instantiate the agent, call the start-method and
	 * registers a shutdown-hook to call the stop-method. Waiting for SIGNINT (Ctrl-C).
	 * 
	 * @param args
	 * 	First argument is the port, the agent will listen for connections from a roboter control center.
	 */
	public static void main(String[] args) {
		// guess pid
		final String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
		final int pid = Integer.parseInt(runtimeName.split("@")[0]);
		
		// determine port
		final int port = args.length>0?Integer.parseInt(args[0]):DEFAULT_PORT;
		
		final RaspberryAgent agent = new RaspberryAgent(port, pid);
		try {
			agent.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						agent.stop();
					} catch(Exception e) {
						System.err.println("Exception on shutdown: " + e.getMessage());
						e.printStackTrace();
					}
				}
			});
			while(true) {} // Wait for Ctrl+C :-)
		} catch(Exception e) {
			System.err.println("Exception on startup: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Constructs a new agent instance.
	 * 
	 * @param port The port, the agent will listen for connections from a roboter control center.
	 * @param pid The pid of the agents process.
	 */
	private RaspberryAgent(final int port, final int pid) {
		this.port = port;
		this.pid = pid;
	}

	/**
	 * The port, the agent will listen for connections from a roboter control center.
	 */
	private final int port;
	
	/**
	 * The pid of the agents process.
	 */
	private final int pid;

	/**
	 * The callback stub for the currently connected roboter control center.
	 * Will be <code>null</code>, if no roboter control center is connected. 
	 */
	private ICallback currentCallback;
	
	/**
	 * The stub of this agent.
	 */
	private IAgent agentStub;
	
	/**
	 * The startup process of the agent. Starts an rmi registry on the specified port
	 * creates the agents stub an binds it to {@link Agent#RMI_NAME}.
	 * 
	 * @throws Exception Throws any exception occured during the startup process. 
	 */
	public void start() throws Exception {
		final Registry registry = LocateRegistry.createRegistry(this.port);
		this.agentStub = (IAgent) UnicastRemoteObject.exportObject(this, 0);
		registry.rebind(RMI_NAME, this.agentStub);
		System.out.println("Raspberry RoboCC Agent started (PID: " + this.pid + ", Port: " + this.port + ")");
	}

	/**
	 * The shutdown process of the agent. Inform a conncted roboter contorl center
	 * about the stopping of the agent. Unbinds the agent from the rmi registry.
	 * 
	 * @throws Exception Throws any exception occured during the shutdown process. 
	 */
	public void stop() throws Exception {
		if(this.currentCallback != null) {
			this.currentCallback.unsubscribeAgent(this.agentStub);
		}
		final Registry registry = LocateRegistry.getRegistry(this.port);
		registry.unbind(RMI_NAME);
		System.out.println("Raspberry RoboCC Agent stopped (PID: " + this.pid + ", Port: " + this.port + ")");
	}
	
	/**
	 * @see IAgent#registerCallback(ICallback)
	 */
	@Override
	public void registerCallback(ICallback callback) {
		currentCallback = callback;
		System.out.println("RoboCC connected.");
	}

	/**
	 * @see IAgent#unregisterCallback(ICallback)
	 */
	@Override
	public void unregisterCallback(ICallback callback) {
		if(currentCallback != null) {
			if(currentCallback.equals(callback)) {
				this.currentCallback = null;
				System.out.println("RoboCC disconnected.");
			} else {
				System.err.println("Wrong RoboCC tried to disconnect!");
			}
		} else {
			System.err.println("RoboCC tried to disconnect while not connected!");
		}
	}

}
