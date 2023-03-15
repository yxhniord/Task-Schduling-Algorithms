package CO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class COImplement {

	private static Cheetah prey;

	public Map<Integer, Integer> allocateTasks(List<Cloudlet> taskList, List<Vm> vmList, int tmax) {
		// Step 1
		int D = 3; // Dimension
		int n = 5; // Population Size
		
		int vmListSize = vmList.size();
		int taskListSize = taskList.size();

		// Step 2
		// Create Initial Population
		List<Cheetah> X = generateInitialPopulation(vmListSize, taskListSize, n);
		
		// Step 3
		prey = findLeader(X);
		// Step 4
		int t = 0;

		// Step 5
		int it = 1;

		// Step 6
		int maxIt = D * tmax;

		// Step 7
		int T = (int) (60 * Math.ceil(D / 10.0));

		// Step 8
		while (it <= maxIt) {
			// Step 9
			List<Cheetah> m = selectRandomPop(X, new Random().nextInt(n - 2) + 2);

			// Step 10
			for (int i = 0; i < m.size(); i++) {
				// Step 11
				Cheetah neighbor;
				if (i == m.size() - 1) {
					neighbor = m.get(i - 1);
				} else {
					neighbor = m.get(i + 1);
				}

				// Step 12
				m.get(i).updateCheetah(vmListSize, taskListSize, t, T, neighbor);
			}
			// Step 28
			t = t + 1;
			if (t > T && t - Math.round(T) - 1 >= 1 && t > 2) {
				if (prey.getFitness() - findLeader(X).getFitness() < 0.001) {
					// Leave prey and go back home
					X = generateInitialPopulation(vmListSize, taskListSize, n);
					if (prey.getFitness() > findLeader(X).getFitness()) {
						X.set(0, prey);
					} else {
						prey = findLeader(X);
					}
					t = 0;
				}
			}
			it = it + 1;
			if (prey.getFitness() < findLeader(X).getFitness()) {
				prey = findLeader(X);
			}
		}
		return prey.getMap();
	}

	private List<Cheetah> selectRandomPop(List<Cheetah> x, int m) {
		Collections.shuffle(x);
		List<Cheetah> randomSeries = x.subList(0, m);
		return randomSeries;
	}

	private List<Cheetah> generateInitialPopulation(int numberofVM, int numberofJobs, int n) {
		List<Cheetah> population = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			population.add(Cheetah.getRandomCheetah(numberofVM, numberofJobs));
		}
		return population;
	}

	public static Cheetah getPrey() {
		return prey;
	}

	private Cheetah findLeader(List<Cheetah> X) {
		// Get Fitness for each Cheetah
		List<Double> fitness = X.stream().map(c -> c.getFitness()).collect(Collectors.toList());
		return (Cheetah) X.get(fitness.indexOf(Collections.max(fitness))).clone();
	}

}
