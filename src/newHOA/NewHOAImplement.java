package newHOA;

import CO.Cheetah;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import utils.Constants;
import utils.HOAConstants;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewHOAImplement {

	private static Horse horse;
	
	public Map<Integer, Integer> allocateTasks(List<Cloudlet> taskList, List<Vm> vmList, int n, int maxIt) {

		int vmListSize = vmList.size();
		int taskListSize = taskList.size();

		// Step 2
		// Create Initial Population
		List<Horse> X = generateInitialPopulation(vmListSize, taskListSize, n);

		horse = findLeader(X, taskList, vmList);
		double bestScore = horse.getFitness(taskList, vmList);
		int t = 1;

		while (t <= maxIt) {
			for (int i = 0; i < n; i++) {
				if(i < (int)Math.ceil(HOAConstants.ALPHA*n)){
					double scalar = HOAConstants.BAD*n;
					double[][] bad = badAvgPosition(scalar, X, n, vmListSize, taskListSize);
					double a = HOAConstants.g_Alpha*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w + HOAConstants.d_Alpha*HOAConstants.w + 1;
					X.get(i).updateHorse(vmListSize,taskListSize, a, -1, matrixMultiply(HOAConstants.d_Alpha*HOAConstants.w, bad));
				}else if(i < (int)Math.ceil(HOAConstants.BETA*n)){
					double a = HOAConstants.g_Beta*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w - HOAConstants.h_Beta*HOAConstants.w - HOAConstants.s_Beta*HOAConstants.w + HOAConstants.d_Beta*HOAConstants.w + 1;
					double scalar = HOAConstants.BAD*n;
					double[][] bad = badAvgPosition(scalar, X, n, vmListSize, taskListSize);
					double goodScalar = n;
					double[][] avg = goodAvgPosition(goodScalar, X, n, vmListSize, taskListSize);
					double[][] cur = matrixMultiply(HOAConstants.h_Beta*HOAConstants.w, horse.getJobVMMapping());
					X.get(i).updateHorse(vmListSize,taskListSize, a, 1, matrixAddition(matrixAddition(matrixMultiply(-HOAConstants.d_Beta*HOAConstants.w, bad), matrixMultiply(HOAConstants.s_Beta*HOAConstants.w, avg)), cur));
				}else if(i < (int)Math.ceil(HOAConstants.GAMMA*n)){
					double a = HOAConstants.g_Gamma*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w - HOAConstants.h_Gamma*HOAConstants.w - HOAConstants.s_Gamma*HOAConstants.w - HOAConstants.i_Gamma*HOAConstants.w + HOAConstants.d_Gamma*HOAConstants.w + HOAConstants.r_Gamma*HOAConstants.w*HOAConstants.p + 1;
					double[][] cur = matrixMultiply(HOAConstants.h_Gamma*HOAConstants.w, horse.getJobVMMapping());
					double avgScalar = n;
					double goodScalar = HOAConstants.GOOD * n;
					double[][] avg = goodAvgPosition(avgScalar, X, n, vmListSize, taskListSize);
					double[][] good = goodAvgPosition(goodScalar, X, n, vmListSize, taskListSize);
					double badScalar = HOAConstants.BAD*n;
					double[][] bad = badAvgPosition(badScalar, X, n, vmListSize, taskListSize);
					X.get(i).updateHorse(vmListSize,taskListSize, a, 1, matrixAddition(matrixAddition(matrixAddition(matrixMultiply(- HOAConstants.d_Gamma*HOAConstants.w, bad), matrixMultiply(HOAConstants.s_Gamma*HOAConstants.w, avg)), cur),matrixMultiply(HOAConstants.i_Gamma*HOAConstants.w, good)));
				}else if(i < (int)Math.ceil(HOAConstants.DELTA*n)){
					double a = HOAConstants.g_Delta*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w - HOAConstants.i_Delta*HOAConstants.w + HOAConstants.r_Delta*HOAConstants.w*HOAConstants.p + 1;
					double goodScalar = HOAConstants.GOOD * n;
					double[][] good = goodAvgPosition(goodScalar, X, n, vmListSize, taskListSize);
					X.get(i).updateHorse(vmListSize,taskListSize, a, 1, matrixMultiply(HOAConstants.i_Delta*HOAConstants.w, good));
				}
			}
//			List<Double> fitness = X.stream().map(c -> c.getFitness(taskList, vmList)).collect(Collectors.toList());
			sortHorsesByFitness(X, taskList, vmList);

			Horse lead_horse = X.get(0);
			if(lead_horse.getFitness(taskList, vmList) < bestScore){
				horse = lead_horse;
				bestScore = horse.getFitness(taskList, vmList);
			}
			if(bestScore == 0.0){
				return horse.getMap();
			}
			t ++;
		}

		return horse.getMap();
	}

	private List<Horse> selectRandomPop(List<Horse> x, int m) {
		Collections.shuffle(x);
		List<Horse> randomSeries = x.subList(0, m);
		return randomSeries;
	}

	private List<Horse> generateInitialPopulation(int numberofVM, int numberofJobs, int n) {
		List<Horse> population = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			population.add(Horse.getRandomHorse(numberofVM, numberofJobs));
		}
		return population;
	}

	private Horse findLeader(List<Horse> X, List<Cloudlet> taskList, List<Vm> vmList) {
		// Get Fitness for each Cheetah
		List<Double> fitness = X.stream().map(c -> c.getFitness(taskList, vmList)).collect(Collectors.toList());
		return (Horse) X.get(fitness.indexOf(Collections.min(fitness))).clone();
	}

	private double[][] matrixAddition(double[][] matrix1, double[][] matrix2){
		double[][] result = new double[matrix1.length][matrix1[0].length];
		for(int i = 0; i < matrix1.length; i++) {
			for(int j = 0; j < matrix1[i].length; j++) {
				result[i][j] = matrix1[i][j] + matrix2[i][j];
			}
		}
		return result;
	}

	private double[][] matrixMultiply(double scalar, double[][] matrix){
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] *= scalar;
			}
		}
		return matrix;
	}

	// the worst horses positions average
	private double[][] badAvgPosition(double scalar, List<Horse> X, int n, int vmSize, int taskSize){
		double[][] matrix = new double[vmSize][taskSize];
		for (int k = n - 1; k > n - (int)scalar; k--){
			for (int i = 0; i < vmSize; i++) {
				for (int j = 0; j < taskSize; j++) {
					matrix[i][j] += X.get(k).getJobVMMapping()[i][j]/scalar;
				}
			}
		}
		return matrix;
	}

	// the best horses positions average
	private double[][] goodAvgPosition(double scalar, List<Horse> X, int n, int vmSize, int taskSize){
		double[][] matrix = new double[vmSize][taskSize];
		for (int k = 0; k < (int)Math.ceil(scalar); k++){
			for (int i = 0; i < vmSize; i++) {
				for (int j = 0; j < taskSize; j++) {
					matrix[i][j] += X.get(k).getJobVMMapping()[i][j]/scalar;
				}
			}
		}
		return matrix;
	}

	private static void sortHorsesByFitness(List<Horse> horses, List<Cloudlet> taskList, List<Vm> vmList) {
		List<Horse> sortedHorses = horses.stream()
				.sorted(
						Comparator.comparingDouble(h -> h.getFitness(taskList, vmList))
				)
				.collect(Collectors.toList());

		horses.clear();
		horses.addAll(sortedHorses);
	}
}
