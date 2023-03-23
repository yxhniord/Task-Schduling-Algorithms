package GWO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class GWOImplement {
	public Map<Integer, Integer> allocateTasks(List<Cloudlet> taskList, List<Vm> vmList, int tmax) {
		
		int n = 5;
		int taskListSize = taskList.size();
		int vmListSize = vmList.size();
		int t = 1;
		int maxIt = tmax;
		List<GreyWolf> X = generateInitialPopulation(vmListSize, taskListSize, n);
		double a = generate_a(t, maxIt);
		RealMatrix A1 = generateA(vmListSize, a);
		RealMatrix A2 = generateA(vmListSize, a);
		RealMatrix A3 = generateA(vmListSize, a);
		RealMatrix C1 = generateC(vmListSize);
		RealMatrix C2 = generateC(vmListSize);
		RealMatrix C3 = generateC(vmListSize);
		Map<String, GreyWolf> agents = getBestSearchAgents(X, taskList, vmList);
		GreyWolf alpha = agents.get("alpha");
		GreyWolf beta = agents.get("beta");
		GreyWolf delta = agents.get("delta");
		while (t <= maxIt) {
			for (GreyWolf agent : X) {
				agent.updateGreyWolf(alpha, beta, delta, A1, A2, A3, C1, C2, C3);
			}
			t = t + 1;
			a = generate_a(t, maxIt);
			A1 = generateA(vmListSize, a);
			A2 = generateA(vmListSize, a);
			A3 = generateA(vmListSize, a);
			C1 = generateC(vmListSize);
			C2 = generateC(vmListSize);
			C3 = generateC(vmListSize);
			agents = getBestSearchAgents(X, taskList, vmList);
			alpha = agents.get("alpha");
			beta = agents.get("beta");
			delta = agents.get("delta");
		}
		return alpha.getMap();
	}

	private double generate_a(int t, int maxIt) {
		return 2 - 2 * (t / (double) maxIt);
	}

	private RealMatrix generateA(int vmListSize, double a) {
		RealMatrix r1 = generateUniformDistribution(vmListSize, vmListSize);
		return r1.scalarMultiply(2 * a).scalarAdd(-1 * a);
	}

	private RealMatrix generateC(int vmListSize) {
		RealMatrix r2 = generateUniformDistribution(vmListSize, vmListSize);
		return r2.scalarMultiply(2);
	}

	private Map<String, GreyWolf> getBestSearchAgents(List<GreyWolf> X, List<Cloudlet> taskList, List<Vm> vmList) {
		List<GreyWolf> newList = X.stream().sorted((x1, x2) -> {
			return (int) (x2.getFitness(taskList, vmList) - x1.getFitness(taskList, vmList));
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

	private RealMatrix generateUniformDistribution(int rows, int cols) {
		Random random = new Random();
		double[][] rnd = new double[rows][cols];
		for (int i = 0; i < rnd.length; i++) {
			for (int j = 0; j < rnd[i].length; j++) {
				rnd[i][j] = random.nextDouble();
			}
		}
		return new Array2DRowRealMatrix(rnd);
	}

}
