package newHOA;

import CO.COImplement;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import utils.Constants;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Horse {
	private double[][] jobVMMapping;
	private Random random;

	public Horse(double[][] jobVMMapping) {
		this.jobVMMapping = jobVMMapping;
		this.random = new Random(Constants.RANDOM_SEED);
	}

	public double getFitness(List<Cloudlet> taskList, List<Vm> vmList) {
		return calculateMakespan(taskList, vmList) + 0.001*calculateCost(taskList, vmList);
	}

	public static Horse getRandomHorse(int numberofVM, int numberofJobs) {
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

		return new Horse(jobMapping);
	}

	@Override
	public Object clone() {
		return new Horse(Arrays.stream(jobVMMapping).map(double[]::clone).toArray(double[][]::new));
	}

	public void updateHorse(int vmSize, int taskSize, double a, double b, double[][] vmapping) {
		for (int i = 0; i < vmSize; i++) {
			for (int j = 0; j < taskSize; j++) {
				jobVMMapping[i][j] = jobVMMapping[i][j]*a + b*vmapping[i][j];
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
//			Cloudlet task = taskList.get(i);
//			cost += task.getCostPerSec() * task.getActualCPUTime();
			cost += taskList.get(i).getCloudletTotalLength()*0.1;
		}
		return cost;
	}

	public double[][] getJobVMMapping() {
		return jobVMMapping;
	}
}
