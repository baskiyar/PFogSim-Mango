/**
 * 
 */
package edu.auburn.pFogSim.orchestrator;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;

/**
 * @author Qian
 *
 */
public class ServiceReplaceOrchestrator extends EdgeOrchestrator{

	/**
	 * @param _policy
	 * @param _simScenario
	 */
	public ServiceReplaceOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @author Qian
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		EdgeHost host = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId()).getHost();
		if (host != null) {
			return host.getId();
		}
		else {
			return -1;
		}
	}
	/**
	 * @author Qian
	 */
	@Override
	public EdgeVM getVmToOffload(Task task) {
		EdgeHost host = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId()).getHost();
		if (host != null) {
			return (EdgeVM) host.getVmList().get(0);
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#initialize()
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

}
