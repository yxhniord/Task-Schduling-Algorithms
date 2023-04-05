package HOA;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import utils.HOAConstants;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HOAImplement {
    private int num_horses;
    private int num_iterations;
    private int num_tasks;
    private int num_vims;

    public HOAImplement(int num_horses, int num_iterations, int num_tasks, int num_vims){
        this.num_horses = num_horses;
        this.num_iterations = num_iterations;
        this.num_tasks = num_tasks;
        this.num_vims = num_vims;
    }

    private static double[][] initiate(int num_tasks, int num_vims){
        double[][] jobMapping = new double[num_tasks][num_vims];

        List<Integer> jobList = IntStream.rangeClosed(0, num_tasks - 1).boxed().collect(Collectors.toList());
        Collections.shuffle(jobList);
        List<List<Integer>> groups = IntStream.range(0, jobList.size()).boxed()
                .collect(Collectors.groupingBy(i -> i % num_vims)).values().stream()
                .map(il -> il.stream().map(jobList::get).collect(Collectors.toList())).collect(Collectors.toList());

        for (int i = 0; i < num_vims; i++) {
            List<Integer> jobs = groups.get(i);
            for (int j = 0; j < num_tasks; j++) {
                jobMapping[j][i] = jobMapping[j][i]==1.0 ? 1 : (jobs.contains(j) ? 1 : 0);
            }
        }
        return jobMapping;
    }


    public Map<Integer, Integer> implement(List<Cloudlet> taskList, List<Vm> vmList) {
        // initialize horse positions
        double[][][] positions = new double[num_horses][num_tasks][num_vims];
        for (int i = 0; i < num_horses; i++) {
            positions[i] = initiate(num_tasks, num_vims);
        }

        double[][] fitness = new double[num_horses][2];
        double[][] lead_position;
        int lead_horse;

        // best horse with the position information
        double[][] best_position = positions[0];
        double best_fitness = evaluateFitness(positions[0], taskList, vmList);

        int itr = 0;
        // perform horse herd optimization
        while (itr < num_iterations) {
            // compute alpha, beta, gamma, delta horses velocity and update positions
            double[][][] alpha_horse_velocity = new double[(int)(HOAConstants.ALPHA*num_horses)][num_tasks][num_vims];
            double[][][] beta_horse_velocity = new double[(int)Math.ceil((HOAConstants.BETA - HOAConstants.ALPHA)*num_horses)][num_tasks][num_vims];
            double[][][] gamma_horse_velocity = new double[(int)Math.ceil((HOAConstants.GAMMA - HOAConstants.BETA)*num_horses)][num_tasks][num_vims];
            double[][][] delta_horse_velocity = new double[(int)Math.ceil((HOAConstants.DELTA - HOAConstants.GAMMA)*num_horses)][num_tasks][num_vims];

            for (int i = 0; i < num_horses; i++) {
                if(i < HOAConstants.ALPHA*num_horses){
                    alpha_horse_velocity[i] = matrixSubtraction(matrixMultiply(HOAConstants.g_Alpha*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w, positions[i]),
                            matrixMultiply(HOAConstants.d_Alpha*HOAConstants.w, matrixSubtraction(badAvgPosition(HOAConstants.BAD*num_horses, positions),positions[i])));
                    positions[i] = matrixAddition(positions[i], alpha_horse_velocity[i]);
                }else if(i < HOAConstants.BETA*num_horses){
                    beta_horse_velocity[i - (int)Math.ceil(HOAConstants.ALPHA*num_horses)] = matrixAddition(
                            matrixAddition(matrixMultiply(HOAConstants.g_Beta*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w, positions[i]),
                                    matrixMultiply(HOAConstants.h_Beta*HOAConstants.w,matrixSubtraction(positions[0], positions[i]))),
                            matrixSubtraction(matrixMultiply(HOAConstants.s_Beta*HOAConstants.w,matrixSubtraction(goodAvgPosition(num_horses, positions),positions[i])),
                                    matrixMultiply(HOAConstants.d_Beta*HOAConstants.w,matrixSubtraction(badAvgPosition(HOAConstants.BAD*num_horses, positions),positions[i]))));
                    positions[i] = matrixAddition(positions[i], beta_horse_velocity[i - (int)Math.ceil(HOAConstants.ALPHA*num_horses)]);
                }else if(i < HOAConstants.GAMMA*num_horses){
                    gamma_horse_velocity[i - (int)Math.ceil(HOAConstants.BETA*num_horses)] = matrixAddition(
                            matrixAddition(
                                    matrixMultiply(HOAConstants.g_Gamma*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w, positions[i]),
                                    matrixMultiply(HOAConstants.h_Gamma*HOAConstants.w,matrixSubtraction(positions[0],positions[i]))),
                            matrixAddition(
                                    matrixAddition(
                                            matrixMultiply(HOAConstants.s_Gamma*HOAConstants.w, matrixSubtraction(goodAvgPosition(num_horses, positions),positions[i])),
                                            matrixMultiply(HOAConstants.i_Gamma*HOAConstants.w, matrixSubtraction(goodAvgPosition(HOAConstants.GOOD * num_horses, positions), positions[i]))),
                                    matrixSubtraction(
                                            matrixMultiply(HOAConstants.r_Gamma*HOAConstants.w*HOAConstants.p,positions[i]),
                                            matrixMultiply(HOAConstants.d_Gamma*HOAConstants.w,matrixSubtraction(badAvgPosition(HOAConstants.BAD*num_horses, positions), positions[i]))))
                    );
                    positions[i] = matrixAddition(positions[i], gamma_horse_velocity[i - (int)Math.ceil(HOAConstants.BETA*num_horses)]);
                }else if(i < HOAConstants.DELTA*num_horses){
                    delta_horse_velocity[i- (int)Math.ceil(HOAConstants.GAMMA*num_horses)] = matrixAddition(
                            matrixAddition(matrixMultiply(HOAConstants.g_Delta*(HOAConstants.g_u + HOAConstants.g_l*HOAConstants.p)*HOAConstants.w, positions[i]),
                                    matrixMultiply(HOAConstants.r_Delta*HOAConstants.w*HOAConstants.p, positions[i])),
                            matrixMultiply(HOAConstants.i_Delta*HOAConstants.w,
                                    matrixSubtraction(goodAvgPosition(HOAConstants.GOOD*num_horses,positions),positions[i])));
                    positions[i] = matrixAddition(positions[i], delta_horse_velocity[i- (int)Math.ceil(HOAConstants.GAMMA*num_horses)]);
                }
            }

            for (int i = 0; i < num_horses; i++){
                fitness[i][0] = evaluateFitness(positions[i], taskList, vmList);
                fitness[i][1] = i;
            }

            // sort by fitness score ascending
            Arrays.sort(fitness, Comparator.comparingDouble(o -> o[0]));

            // check the leader horse
            lead_horse = (int)Math.ceil(fitness[0][1]);
            lead_position = positions[lead_horse];
            if(fitness[0][0] < best_fitness){
                best_fitness = fitness[0][0];
                best_position = lead_position;
            }
            // sorted horses with fitness scores
            positions = sortedHorse(fitness, positions);
            itr ++;
        }

        return getMap(best_position);
    }

    private void displayMatrix(double[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
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

    private double[][] matrixSubtraction(double[][] matrix1, double[][] matrix2){
        double[][] result = new double[matrix1.length][matrix1[0].length];
        for(int i = 0; i < matrix1.length; i++) {
            for(int j = 0; j < matrix1[i].length; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
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
    private double[][] badAvgPosition(double scalar, double[][][] positions){
        double[][] matrix = new double[num_tasks][num_vims];
        for (int i = num_horses - 1; i > num_horses - (int)scalar; i--){
            matrix = matrixAddition(matrix, positions[i]);
        }
        return matrixMultiply(1/scalar, matrix);
    }

    // the best horses positions average
    private double[][] goodAvgPosition(double scalar, double[][][] positions){
        double[][] matrix = new double[num_tasks][num_vims];
        for (int i = 0; i < (int)scalar; i++){
            matrix = matrixAddition(matrix, positions[i]);
        }
        return matrixMultiply(1/scalar, matrix);
    }

    // evaluation function to calculate fitness of horses at given positions
    private double evaluateFitness(double[][] positions, List<Cloudlet> taskList, List<Vm> vmList) {
        return calculateMakespan(positions, taskList, vmList) + calculateCost(taskList);
    }

    private double calculateMakespan(double[][] positions, List<Cloudlet> taskList, List<Vm> vmList) {
        double makespan = 0;
        double[] vmTime = new double[num_vims];
        for (int i = 0; i < num_tasks; i++) {
            for (int j = 0; j < num_vims; j++) {
                if (positions[i][j] != 0) {
                    vmTime[j] += taskList.get(i).getCloudletLength() / vmList.get(j).getMips();
                    makespan = Math.max(makespan, vmTime[j]);
                }
            }
        }
        return makespan;
    }

    private double calculateCost(List<Cloudlet> taskList){
        double cost = 0;
        for (int i = 0; i < num_tasks; i++) {
            Cloudlet task = taskList.get(i);
            cost += task.getCostPerSec() * task.getActualCPUTime();
        }
        return cost;
    }

    private double[][][] sortedHorse(double[][] fitness, double[][][] positions){
        double[][][] tmp = new double[num_horses][num_tasks][num_vims];
        for(int i = 0; i < fitness.length; i++){
            int index = (int)fitness[i][1];
            tmp[i] = positions[index];
        }
        return tmp;
    };

    public Map<Integer, Integer> getMap(double[][] jobVMMapping) {
        Map<Integer, Integer> allocatedTasks = new HashMap<>();

        for (int i = 0; i < jobVMMapping.length; i++) {
            for (int j = 0; j < jobVMMapping[i].length; j++) {
                if (jobVMMapping[i][j] != 0) {
                    int currentVM = allocatedTasks.getOrDefault(i, -999);
                    if (currentVM != -999) {
                        if (jobVMMapping[i][currentVM] < jobVMMapping[i][j]) {
                            allocatedTasks.put(i, j);
                        }
                    } else {
                        allocatedTasks.put(i, j);
                    }

                }
            }
        }
        return allocatedTasks;
    }
}
