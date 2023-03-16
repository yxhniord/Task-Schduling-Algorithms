package HOA;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HOAImplement {
    private static final double ALPHA = 0.1;
    private static final double BETA = 0.3;
    private static final double GAMMA = 0.6;
    private static final double DELTA = 1;
    private static final double g_Alpha = 1.50;
    private static final double d_Alpha = 0.5;

    private static final double g_Beta = 1.5;
    private static final double h_Beta = 0.9;
    private static final double s_Beta = 0.2;
    private static final double d_Beta = 0.2;

    private static final double g_Gamma = 1.5;
    private static final double h_Gamma = 0.5;
    private static final double s_Gamma = 0.1;
    private static final double i_Gamma = 0.3;
    private static final double d_Gamma = 0.1;
    private static final double r_Gamma = 0.05;

    private static final double g_Delta = 1.5;
    private static final double i_Delta = 0.3;
    private static final double r_Delta = 0.1;

    private static final double w = 0.95;
    private static final double g_l = 0.95;
    private static final double g_u = 1.05;
    private static final double p = Math.random();
    private static final double BAD = 0.2;
    private static final double GOOD = 0.1;

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


    public Map<Integer, Integer> implement() {
        // initialize horse positions
        double[][][] positions = new double[num_horses][num_tasks][num_vims];
        for (int i = 0; i < num_horses; i++) {
            positions[i] = initiate(num_tasks, num_vims);
        }

        double[][] fitness = new double[num_horses][2];
        double[][] lead_position;
        int lead_horse;

        // perform horse herd optimization
        for (int iteration = 0; iteration < num_iterations; iteration++) {
            for (int i = 0; i < num_horses; i++){
                fitness[i][0] = evaluateFitness(positions[i]);
                fitness[i][1] = i;
            }

            // sort by fitness score ascending
            Arrays.sort(fitness, Comparator.comparingDouble(o -> o[0]));

            // check the leader horse
            lead_horse = (int)(fitness[0][1]);
            lead_position = positions[lead_horse];
            if(fitness[0][0] == 0.0 ){
                return getMap(lead_position);
            }

            // sorted horses with fitness scores
            positions = sortedHorse(fitness, positions);

            // compute alpha, beta, gamma, delta horses velocity and update positions
            double[][][] alpha_horse_velocity = new double[(int)(ALPHA*num_horses)][num_tasks][num_vims];
            double[][][] beta_horse_velocity = new double[(int)Math.ceil((BETA - ALPHA)*num_horses)][num_tasks][num_vims];
            double[][][] gamma_horse_velocity = new double[(int)Math.ceil((GAMMA - BETA)*num_horses)][num_tasks][num_vims];
            double[][][] delta_horse_velocity = new double[(int)Math.ceil((DELTA - GAMMA)*num_horses)][num_tasks][num_vims];

            for (int i = 0; i < num_horses; i++) {
                if(i < ALPHA*num_horses){
                    alpha_horse_velocity[i] = matrixSubtraction(matrixMultiply(g_Alpha*(g_u + g_l*p)*w, positions[i]),
                            matrixMultiply(d_Alpha*w, matrixSubtraction(badAvgPosition(BAD*num_horses, positions),positions[i])));
                    positions[i] = matrixAddition(positions[i], alpha_horse_velocity[i]);
                }else if(i < BETA*num_horses){
                    beta_horse_velocity[i - (int)Math.ceil(ALPHA*num_horses)] = matrixAddition(
                            matrixAddition(matrixMultiply(g_Beta*(g_u + g_l*p)*w, positions[i]),
                                    matrixMultiply(h_Beta*w,matrixSubtraction(positions[0], positions[i]))),
                            matrixSubtraction(matrixMultiply(s_Beta*w,matrixSubtraction(goodAvgPosition(num_horses, positions),positions[i])),
                                    matrixMultiply(d_Beta*w,matrixSubtraction(badAvgPosition(BAD*num_horses, positions),positions[i]))));
                    positions[i] = matrixAddition(positions[i], beta_horse_velocity[i - (int)Math.ceil(ALPHA*num_horses)]);
                }else if(i < GAMMA*num_horses){
                    gamma_horse_velocity[i - (int)Math.ceil(BETA*num_horses)] = matrixAddition(
                            matrixAddition(
                                    matrixMultiply(g_Gamma*(g_u + g_l*p)*w, positions[i]),
                                    matrixMultiply(h_Gamma*w,matrixSubtraction(positions[0],positions[i]))),
                            matrixAddition(
                                    matrixAddition(
                                            matrixMultiply(s_Gamma*w, matrixSubtraction(goodAvgPosition(num_horses, positions),positions[i])),
                                            matrixMultiply(i_Gamma*w, matrixSubtraction(goodAvgPosition(GOOD * num_horses, positions), positions[i]))),
                                    matrixSubtraction(
                                            matrixMultiply(r_Gamma*w*p,positions[i]),
                                            matrixMultiply(d_Gamma*w,matrixSubtraction(badAvgPosition(BAD*num_horses, positions), positions[i]))))
                    );
                    positions[i] = matrixAddition(positions[i], gamma_horse_velocity[i - (int)Math.ceil(BETA*num_horses)]);
                }else if(i < DELTA*num_horses){
                    delta_horse_velocity[i- (int)Math.ceil(GAMMA*num_horses)] = matrixAddition(
                            matrixAddition(matrixMultiply(g_Delta*(g_u + g_l*p)*w, positions[i]),
                                    matrixMultiply(r_Delta*w*p, positions[i])),
                            matrixMultiply(i_Delta*w,
                                    matrixSubtraction(goodAvgPosition(GOOD*num_horses,positions),positions[i])));
                    positions[i] = matrixAddition(positions[i], delta_horse_velocity[i- (int)Math.ceil(GAMMA*num_horses)]);
                }
            }

        }

        // print final horse positions
        System.out.println("Final horse positions:");
        for (int i = 0; i < num_horses; i++) {
            System.out.println("Horse " + (i+1) + ": " + positions[i]);
        }

        // return the lead horse
        return getMap(positions[0]);
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
    private double evaluateFitness(double[][] positions) {
        double fitness = 0.0;
        for (int i = 0; i < num_tasks; i++) {
            for (int j = 0; j < num_vims; j++) {
                // calculate fitness based on position (e.g. distance from optimal position)
                fitness += Math.sqrt(positions[i][j]);
            }
        }
        return fitness;
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
