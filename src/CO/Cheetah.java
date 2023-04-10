package CO;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import utils.Constants;

public class Cheetah {
	private double[][] jobVMMapping;
	private Random random;

	public Cheetah(double[][] jobVMMapping) {
		this.jobVMMapping = jobVMMapping;
		this.random = new Random(Constants.RANDOM_SEED);
	}

	public double getFitness(List<Cloudlet> taskList, List<Vm> vmList) {
		return calculateMakespan(taskList, vmList) + 0.001 * calculateCost(taskList, vmList);
	}

	public static Cheetah getRandomCheetah(int numberofVM, int numberofJobs) {
		double[][] jobMapping = new double[numberofVM][numberofJobs];

		List<Integer> jobList = IntStream.rangeClosed(0, numberofJobs - 1).boxed().collect(Collectors.toList());
		Collections.shuffle(jobList);
		List<List<Integer>> groups = IntStream.range(0, jobList.size()).boxed()
				.collect(Collectors.groupingBy(i -> i % numberofVM)).values().stream()
				.map(il -> il.stream().map(jobList::get).collect(Collectors.toList())).collect(Collectors.toList());

		for (int i = 0; i < numberofVM; i++) {
			List<Integer> jobs = groups.get(i);
			for (int j = 0; j < numberofJobs; j++) {
				jobMapping[i][j] = jobs.contains(j) ? 1 : 0;
			}
		}

		return new Cheetah(jobMapping);
	}

	@Override
	public Object clone() {
		return new Cheetah(Arrays.stream(jobVMMapping).map(double[]::clone).toArray(double[][]::new));
	}

	public void updateCheetah(int vmSize, int taskSize, int t, int T, Cheetah neighbor) {
		// Step 13
		double alpha = 0.001 * t / T;

		// Step 14
		double[][] r2 = generateUniformDistribution(vmSize, taskSize);
		double[][] r3 = generateUniformDistribution(vmSize, taskSize);

		for (int i = 0; i < vmSize; i++) {
			for (int j = 0; j < taskSize; j++) {
				// Step 15
				if (r2[i][j] <= r3[i][j]) {
					// Step 16
					double r4 = Math.max(0, Math.min(3, random.nextGaussian() * 0.45 + 1.5));
					double r1 = Math.max(0, Math.min(1, random.nextGaussian() * 0.15 + 0.5));
					double H = Math.pow(Math.E, 2 * (1 - t / T)) * (2 * r1 - 1);
					// Step 17
					if (H >= r4) {
						// Step 18 -- Search EQ1
						double r_hat = Math.max(0, Math.min(1, random.nextGaussian() * 0.15 + 0.5));
						jobVMMapping[i][j] = jobVMMapping[i][j] + r_hat * alpha;
					} else {
						// Step 20 -- Attack EQ3
						double r = random.nextGaussian();
						double r_dash = Math.pow(Math.abs(r), (r / 2)) * Math.sin(2 * Math.PI * r);
						double beta = neighbor.jobVMMapping[i][j] - jobVMMapping[i][j];
						jobVMMapping[i][j] = COImplement.getPrey().jobVMMapping[i][j] + r_dash * beta;
					}
				} else {
					// Step 23 -- Sit & Wait EQ2
					jobVMMapping[i][j] = jobVMMapping[i][j];
				}
			}
		}

	}

	private double[][] generateUniformDistribution(int rows, int cols) {
		double[][] rnd = new double[rows][cols];
		for (int i = 0; i < rnd.length; i++) {
			for (int j = 0; j < rnd[i].length; j++) {
				rnd[i][j] = random.nextDouble();
			}
		}
		return rnd;
	}

	public Map<Integer, Integer> getMap() {
		Map<Integer, Integer> allocatedTasks = new HashMap<>();
		for (int i = 0; i < jobVMMapping.length; i++) {
			for (int j = 0; j < jobVMMapping[i].length; j++) {
				if (jobVMMapping[i][j] != 0) {
					int currentVM = allocatedTasks.getOrDefault(j, -999);
					if (currentVM != -999) {
						if (jobVMMapping[currentVM][j] < jobVMMapping[i][j]) {
							allocatedTasks.put(j, i);
						}
					} else {
						allocatedTasks.put(j, i);
					}

				}
			}
		}
		return allocatedTasks;
	}

	@Override
	public String toString() {
		return this.getMap().toString();
	}

	private double calculateMakespan(List<Cloudlet> taskList, List<Vm> vmList) {
		double makespan = 0;
		double[] vmTime = new double[vmList.size()];
		for (int j = 0; j < taskList.size(); j++) {
			int assignment = -1;
			for (int i = 0; i < vmList.size(); i++) {
				if (jobVMMapping[i][j] != 0) {
					if (assignment == -1) {
						vmTime[i] += taskList.get(j).getCloudletLength() / vmList.get(i).getMips();
						assignment = i;
					} else if (jobVMMapping[i][j] >= jobVMMapping[assignment][j]) {
						vmTime[assignment] -= taskList.get(j).getCloudletLength() / vmList.get(assignment).getMips();
						vmTime[i] += taskList.get(j).getCloudletLength() / vmList.get(i).getMips();
						assignment = i;
					}
					makespan = Math.max(makespan, vmTime[i]);
				}
			}
		}
		return makespan;
	}

	private double calculateCost(List<Cloudlet> taskList, List<Vm> vmList) {
		double cost = 0;
		for (int i = 0; i < taskList.size(); i++) {
			Cloudlet task = taskList.get(i);
			cost += task.getCloudletTotalLength() * 0.1;
		}
		return cost;
	}

}
