package br.com.pucrio.inf.biobd.outertuning.servlets;

import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Memory {

    private static ArrayList<String> agentsRunning;

    public Memory() {
        if (agentsRunning == null) {
            agentsRunning = new ArrayList<>();
        }
    }

    public void addAgent(String agentName) {
        agentsRunning.add(agentName);
    }

    public void removeAgent(String agentName) {
        agentsRunning.remove(agentName);
    }

    public boolean isRunning(String agentName) {
        return agentsRunning.contains(agentName);
    }

    public boolean isNotRunning(String agentName) {
        return !agentsRunning.contains(agentName);
    }
}
