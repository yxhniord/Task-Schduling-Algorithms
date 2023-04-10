package GWO;

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

public class GreyWolf {
	private double[][] jobVMMapping;

	public GreyWolf(double[][] jobVMMapping) {
		this.jobVMMapping = jobVMMapping;
	}

	public double getFitness(List<Cloudlet> taskList, List<Vm> vmList) {
		return calculateMakespan(taskList, vmList) + 0.001 * calculateCost(taskList, vmList);
	}

	public static GreyWolf getRandomGreyWolf(int numberofVM, int numberofJobs) {
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
		return new GreyWolf(jobMapping);
	}

	@Override
	public Object clone() {
		return new GreyWolf(Arrays.stream(jobVMMapping).map(double[]::clone).toArray(double[][]::new));
	}

	public void updateGreyWolf(GreyWolf alpha, GreyWolf beta, GreyWolf delta, int t, int maxIt) {
		Random random = new Random(Constants.RANDOM_SEED);
		double a = generate_a(t, maxIt);

		for (int i = 0; i < jobVMMapping.length; i++) {
			for (int j = 0; j < jobVMMapping[i].length; j++) {
				double a1 = 2 * a * random.nextDouble() - a;
				double a2 = 2 * a * random.nextDouble() - a;
				double a3 = 2 * a * random.nextDouble() - a;
				double c1 = 2 * random.nextDouble();
				double c2 = 2 * random.nextDouble();
				double c3 = 2 * random.nextDouble();
				double dAlpha = Math.abs(c1 * alpha.jobVMMapping[i][j] - jobVMMapping[i][j]);
				double dBeta = Math.abs(c2 * beta.jobVMMapping[i][j] - jobVMMapping[i][j]);
				double dDelta = Math.abs(c3 * delta.jobVMMapping[i][j] - jobVMMapping[i][j]);
				double x1 = alpha.jobVMMapping[i][j] - a1 * dAlpha;
				double x2 = beta.jobVMMapping[i][j] - a2 * dBeta;
				double x3 = delta.jobVMMapping[i][j] - a3 * dDelta;
				jobVMMapping[i][j] = (x1 + x2 + x3) / 3.0;
			}
		}
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
			cost += 0.1 * task.getCloudletTotalLength();
		}
		return cost;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(jobVMMapping);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GreyWolf other = (GreyWolf) obj;
		return Arrays.deepEquals(jobVMMapping, other.jobVMMapping);
	}

	private double generate_a(int t, int maxIt) {
		return 2 - 2 * (t / (double) maxIt);
	}

}
