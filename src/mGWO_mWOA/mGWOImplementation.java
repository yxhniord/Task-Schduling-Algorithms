package mGWO_mWOA;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public class mGWOImplementation {
    public static int taskNum;
    public static int vmNum;
    public static int popSize = 30;
    public static int[][] wolfPositions;
    public static double[] wolfFitness;
    public static double[][] X1;
    public static double[][] X2;
    public static double[][] X3;
    public static double a;
    public static double A;
    public static double C;
    public static int alphaIndex = 0;
    public static int betaIndex = 0;
    public static int deltaIndex = 0;
    public static double r1;
    public static double r2;

    public static int[] alpha_wolf;
    public static int[] beta_wolf;
    public static int[] delta_wolf;
    public static int[] newPosition;

    public static int maxIter;
    public static int current_iteration = 0;

    public static double gbest_fitness = Double.MAX_VALUE;
    public static int[] gbest_schedule;
    public static List<int[]> schedules = new ArrayList<int[]>();

    public void init(int jobNum, int maxVmNum) {
        taskNum = jobNum;
        vmNum = maxVmNum;

        wolfPositions = new int[popSize][taskNum];  // [i][j] = ith search agent, assign task j to vm [i][j].
        wolfFitness = new double[popSize];
        alpha_wolf = new int[taskNum];
        beta_wolf = new int[taskNum];
        delta_wolf = new int[taskNum];
        X1 = new double[popSize][taskNum];
        X2 = new double[popSize][taskNum];
        X3 = new double[popSize][taskNum];

        gbest_schedule = new int[taskNum];

        for(int i = 0; i < popSize; i++) {
            for (int j = 0; j < taskNum; j++) {
                // assign task j to a random vm
                wolfPositions[i][j] = new Random().nextInt(vmNum);
            }
            schedules.add(wolfPositions[i]);
        }
    }

    public Map<Integer, Integer> allocateTasks(List<Cloudlet> taskList, List<Vm> vmList, int iter) {
        taskNum = taskList.size();
        vmNum = vmList.size();
        maxIter = iter;

        wolfPositions = new int[popSize][taskNum];  // [i][j] = ith search agent, assign task j to vm [i][j].
        wolfFitness = new double[popSize];
        alpha_wolf = new int[taskNum];
        beta_wolf = new int[taskNum];
        delta_wolf = new int[taskNum];
        X1 = new double[popSize][taskNum];
        X2 = new double[popSize][taskNum];
        X3 = new double[popSize][taskNum];
        newPosition = new int[taskNum];

        for (int i = 0; i < popSize; i++) {
            for (int j = 0; j < taskNum; j++) {
                wolfPositions[i][j] = new Random().nextInt(vmNum);
            }
//            schedules.add(wolfPositions[i]);
        }

        for (int i = 0; i < maxIter; i++) {
            for (int j = 0; j < popSize; j++) {
                wolfFitness[j] = calculateFitness(wolfPositions[j], taskList, vmList);
//                wolfFitness[j] = calculateFitness(schedules.get(j), taskList, vmList);
            }
            updateWolves(taskList, vmList);
        }


        Map<Integer, Integer> res = new HashMap<>();
        for (int i = 0; i < taskNum; i++) {
            res.put(i, alpha_wolf[i]);
        }
        gbest_fitness = calculateFitness(alpha_wolf, taskList, vmList);
        return res;
    }

    public BridgeResult allocateTasksHybrid(List<Cloudlet> taskList, List<Vm> vmList, int iter) {
        taskNum = taskList.size();
        vmNum = vmList.size();
        maxIter = iter;

        wolfPositions = new int[popSize][taskNum];  // [i][j] = ith search agent, assign task j to vm [i][j].
        wolfFitness = new double[popSize];
        alpha_wolf = new int[taskNum];
        beta_wolf = new int[taskNum];
        delta_wolf = new int[taskNum];
        X1 = new double[popSize][taskNum];
        X2 = new double[popSize][taskNum];
        X3 = new double[popSize][taskNum];

        newPosition = new int[taskNum];

        for (int i = 0; i < popSize; i++) {
            for (int j = 0; j < taskNum; j++) {
                wolfPositions[i][j] = new Random().nextInt(vmNum);
            }
//            schedules.add(wolfPositions[i]);
        }

        for (int i = 0; i < maxIter; i++) {
            for (int j = 0; j < popSize; j++) {
                wolfFitness[j] = calculateFitness(wolfPositions[j], taskList, vmList);
//                wolfFitness[j] = calculateFitness(schedules.get(j), taskList, vmList);
            }
            updateWolves(taskList, vmList);
        }


//        Map<Integer, Integer> res = new HashMap<>();
//        for (int i = 0; i < taskNum; i++) {
//            res.put(i, alpha_wolf[i]);
//        }
        gbest_fitness = calculateFitness(alpha_wolf, taskList, vmList);
        BridgeResult bridgeResult = new BridgeResult(alpha_wolf, gbest_fitness);
        return bridgeResult;
    }

    /**
     *
     * @param schedule Every element stands for a vm
     * @param taskList
     * @param vmList
     * @return
     */
    private double calculateMakespan(int[] schedule, List<Cloudlet> taskList, List<Vm> vmList) {
        double makespan = 0;
        // calculation time for every vm
        double[] vmTime = new double[vmNum];
        for (int i = 0; i < taskNum; i++) {
            vmTime[schedule[i]] += taskList.get(i).getCloudletLength() / vmList.get(schedule[i]).getMips();
            makespan = Math.max(makespan, vmTime[schedule[i]]);
        }
        return makespan;
    }

    private double calculateFitness(int[] schedule, List<Cloudlet> taskList, List<Vm> vmList) {
        double makespan = calculateMakespan(schedule, taskList, vmList);
        double fitness = makespan;
        return fitness;
    }

    public void updateWolves(List<Cloudlet> taskList, List<Vm> vmList) {
        // select top 3 smallest fitness index, which are alpha, beta, and delta.
        alphaIndex = 0;
        for(int i = 0; i < popSize; i++) {
            if(wolfFitness[i] < wolfFitness[alphaIndex]) {
                alphaIndex = i;
            }
        }

        betaIndex = 0;
        double temp = Double.MAX_VALUE;
        for(int i = 0;i < popSize; i++) {
            if(i != alphaIndex && wolfFitness[i] < temp){
                temp = wolfFitness[i];
                betaIndex = i;
            }
        }

        deltaIndex = 0;
        temp = Double.MAX_VALUE;
        for(int i = 0; i < popSize; i++) {
            if(i != alphaIndex && i != betaIndex && wolfFitness[i] < temp) {
                temp = wolfFitness[i];
                deltaIndex = i;
            }
        }

        // update the assigning strategy
        alpha_wolf = wolfPositions[alphaIndex];
        beta_wolf = wolfPositions[betaIndex];
        delta_wolf = wolfPositions[deltaIndex];




        //Update position of all wolves
        for(int i = 0; i < popSize; i++) {
            // update a
            a = 2.0 * (1 - ((Math.pow(current_iteration, 2)) / (Math.pow(maxIter, 2))));
            r1 = new Random().nextDouble();
            r2 = new Random().nextDouble();

            A = 2.0 * a * r1 - a;
            C = 2.0 * r2;

            for(int j = 0; j < taskNum; j++) {
                X1[i][j] = alpha_wolf[j] - A * Math.abs(C * alpha_wolf[j] - wolfPositions[i][j]);
                X1[i][j] = simpleBounds(X1[i][j]);

                X2[i][j] = beta_wolf[j] - A * Math.abs(C * beta_wolf[j] - wolfPositions[i][j]);
                X2[i][j] = simpleBounds(X2[i][j]);

                X3[i][j] = delta_wolf[j] - A * Math.abs(C * delta_wolf[j] - wolfPositions[i][j]);
                X3[i][j] = simpleBounds(X3[i][j]);

                newPosition[j] = (int) simpleBounds((X1[i][j] + X2[i][j] + X3[i][j]) / 3.0);

//                wolfPositions[i][j] = (int) ((X1[i][j] + X2[i][j] + X3[i][j]) / 3.0);
//                wolfPositions[i][j] = (int) simpleBounds(wolfPositions[i][j]);
            }
//            schedules.set(i, wolfPositions[i]);
            if (calculateFitness(newPosition, taskList, vmList) < wolfFitness[i]) {
                wolfPositions[i] = newPosition;
            }
        }
    }

    public double simpleBounds(double val) {
        if (val < 0) {
            return 0;
        }
        if (val > vmNum-1) {
            return vmNum-1;
        }
        return val;
    }

}
