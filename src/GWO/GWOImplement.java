package GWO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import utils.Constants;

public class GWOImplement {
	public Map<Integer, Integer> allocateTasks(List<Cloudlet> taskList, List<Vm> vmList, int tmax) {

		int n = Constants.POPULATION;
		int taskListSize = taskList.size();
		int vmListSize = vmList.size();
		int t = 1;
		int maxIt = tmax;
		List<GreyWolf> X = generateInitialPopulation(vmListSize, taskListSize, n);
		Map<String, GreyWolf> agents = getBestSearchAgents(X, taskList, vmList);
		GreyWolf alpha = agents.get("alpha");
		GreyWolf beta = agents.get("beta");
		GreyWolf delta = agents.get("delta");
		GreyWolf bestSol = (GreyWolf) alpha.clone();
		while (t <= maxIt) {
			for (GreyWolf agent : X) {
				agent.updateGreyWolf(alpha, beta, delta, t, maxIt);
			}
			t = t + 1;
			agents = getBestSearchAgents(X, taskList, vmList);
			alpha = agents.get("alpha");
			beta = agents.get("beta");
			delta = agents.get("delta");
			if (bestSol.getFitness(taskList, vmList) > alpha.getFitness(taskList, vmList)) {
				bestSol = (GreyWolf) alpha.clone();
			}
		}
		return bestSol.getMap();
	}

	private Map<String, GreyWolf> getBestSearchAgents(List<GreyWolf> X, List<Cloudlet> taskList, List<Vm> vmList) {
		List<GreyWolf> newList = X.stream().sorted((x1, x2) -> {
			return (int) Math.signum(x1.getFitness(taskList, vmList) - x2.getFitness(taskList, vmList));
		}).collect(Collectors.toList());
		Map<String, GreyWolf> map = new HashMap<>();
		map.put("alpha", newList.get(0));
		map.put("beta", newList.get(1));
		map.put("delta", newList.get(2));
		return map;
	}

	private List<GreyWolf> generateInitialPopulation(int numberofVM, int numberofJobs, int n) {
		List<GreyWolf> population = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			population.add(GreyWolf.getRandomGreyWolf(numberofVM, numberofJobs));
		}
		return population;
	}

}
