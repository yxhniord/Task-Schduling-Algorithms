package GWO;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class GreyWolf {
	private RealMatrix jobVMMapping;

	public GreyWolf(RealMatrix jobVMMapping) {
		this.jobVMMapping = jobVMMapping;
	}

	public double getFitness(List<Cloudlet> taskList, List<Vm> vmList) {
		double w1 = 0.5;
		double w2 = 0.5;
		return w1 * calculateMakespan(taskList, vmList) + w2 * calculateEnergy(taskList, vmList);
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

		return new GreyWolf(new Array2DRowRealMatrix(jobMapping));
	}

	@Override
	public Object clone() {
		return new GreyWolf(jobVMMapping.copy());
	}

	public void updateGreyWolf(GreyWolf alpha, GreyWolf beta, GreyWolf delta, RealMatrix a1, RealMatrix a2,
			RealMatrix a3, RealMatrix c1, RealMatrix c2, RealMatrix c3) {
		RealMatrix dAlpha = abs(c1.multiply(alpha.jobVMMapping).subtract(jobVMMapping));
		RealMatrix dBeta = abs(c2.multiply(beta.jobVMMapping).subtract(jobVMMapping));
		RealMatrix dDelta = abs(c3.multiply(delta.jobVMMapping).subtract(jobVMMapping));
		RealMatrix x1 = alpha.jobVMMapping.subtract(a1.multiply(dAlpha));
		RealMatrix x2 = beta.jobVMMapping.subtract(a2.multiply(dBeta));
		RealMatrix x3 = delta.jobVMMapping.subtract(a3.multiply(dDelta));
		jobVMMapping = x1.add(x2).add(x3).scalarMultiply(1 / 3.0);
	}

	public Map<Integer, Integer> getMap() {
		Map<Integer, Integer> allocatedTasks = new HashMap<>();
		double[][] jobMapping = jobVMMapping.getData();
		for (int i = 0; i < jobMapping.length; i++) {
			for (int j = 0; j < jobMapping[i].length; j++) {
				if (jobMapping[i][j] != 0) {
					int currentVM = allocatedTasks.getOrDefault(j, -999);
					if (currentVM != -999) {
						if (jobMapping[currentVM][j] < jobMapping[i][j]) {
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

	private RealMatrix abs(RealMatrix a) {
		double[][] data = a.getData();
		double[][] doubleArray = Arrays.stream(data)
                .map(arr -> Arrays.stream(arr).map(d -> Math.abs(d))
                		.toArray())
                .toArray(double[][]::new);
		return new Array2DRowRealMatrix(doubleArray);
	}
	
	private double calculateMakespan(List<Cloudlet> taskList, List<Vm> vmList) {
		double makespan = 0;
		double[] vmTime = new double[vmList.size()];
		for (int i = 0; i < vmList.size(); i++) {
			for (int j = 0; j < taskList.size(); j++) {
				if (jobVMMapping.getEntry(i, j) != 0) {
					// TODO Update
					vmTime[i] += taskList.get(j).getCloudletLength() / vmList.get(i).getMips();
					makespan = Math.max(makespan, vmTime[i]);
				}
			}
		}
		return makespan;
	}

	private double calculateEnergy(List<Cloudlet> taskList, List<Vm> vmList) {
		// TODO: calculate the energy of the schedule
		double energy = 0;
		for (int i = 0; i < taskList.size(); i++) {
			// energy += taskList.get(i).getCloudletLength() * vmList.get(schedule[i]).getHost()
		}
		return energy;
	}

}
