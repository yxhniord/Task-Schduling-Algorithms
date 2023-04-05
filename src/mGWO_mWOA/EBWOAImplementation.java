package mGWO_mWOA;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EBWOAImplementation {
    public int taskNum;
    public int vmNum;
    public int popSize = 30;
    public int[][] whalePositions;
    public double[] whaleFitness;
    public double bestFitness;
    public int bestAgentIndex;
    private int[] bestPosition;
    public int[] newPosition;
    private double[][] X;
    public double a;
    public double A;
    public double C;
    public double l;
    public double p;
    private double r;
    private double b = 1.0;

    private int currentIter;
    private int maxIter;
    private List<Cloudlet> taskList;
    private List<Vm> vmList;
    Random random = new Random(Constants.RANDOM_SEED);

    public Map<Integer, Integer> allocateTasksWithBest(List<Cloudlet> taskList, List<Vm> vmList, int iter, BridgeResult bridgeResult) {
        taskNum = taskList.size();
        vmNum = vmList.size();
        maxIter = iter;
        this.taskList = taskList;
        this.vmList = vmList;

        whalePositions = new int[popSize][taskNum];
        whaleFitness = new double[popSize];
        newPosition = new int[taskNum];
        X = new double[popSize][taskNum];

        whalePositions[0] = bridgeResult.getBestPositions();
        bestPosition = bridgeResult.getBestPositions();
        bestAgentIndex = 0;
        this.bestFitness = bridgeResult.getBestFitness();

        for (int i = 1; i < popSize; i++) {
            for (int j = 0; j < taskNum; j++) {
                whalePositions[i][j] = random.nextInt(vmNum);
            }
        }

        for (currentIter = 0 ; currentIter < maxIter; currentIter++) {
            for (int j = 0; j < popSize; j++) {
                whaleFitness[j] = calculateFitness(whalePositions[j]);
            }
            updateWhales();
        }
        Map<Integer, Integer> res = new HashMap<>();
        for (int i = 0; i < taskNum; i++) {
            res.put(i, bestPosition[i]);
        }
        return res;
    }

    private double calculateMakespan(int[] schedule) {
        double makespan = 0;
        // calculation time for every vm
        double[] vmTime = new double[vmNum];
        for (int i = 0; i < taskNum; i++) {
            vmTime[schedule[i]] += taskList.get(i).getCloudletLength() / vmList.get(schedule[i]).getMips();
            makespan = Math.max(makespan, vmTime[schedule[i]]);
        }
        return makespan;
    }

    private double calculateCost(List<Cloudlet> taskList) {
        double cost = 0.0;
        for (Cloudlet task : taskList) {
            cost += task.getCostPerSec() * task.getActualCPUTime();
        }
        return cost;
    }

    private double calculateFitness(int[] schedule) {
        double makespan = calculateMakespan(schedule);
        double cost = calculateCost(taskList);
        return makespan + cost;
    }

    public void updateWhales() {
        for (int i = 0; i < popSize; i++) {
            a = 2.0 * (1 - currentIter * 1.0 / maxIter);
            r = random.nextDouble();
            A = 2 * a * r - a;
            C = 2 * r;
            l = random.nextDouble(-1, 1);
            p = random.nextDouble();

            if (p < 0.5) {
                if (Math.abs(A) < 1) {
                    for (int j = 0; j < taskNum; j++) {
                        X[i][j] = bestPosition[j] - A * Math.abs(C * bestPosition[j] - bestPosition[j]);
                        X[i][j] = simpleBounds(X[i][j]);
                    }
                } else {
                    int[] randomPosition = whalePositions[random.nextInt(popSize)];
                    for (int j = 0; j < taskNum; j++) {
                        X[i][j] = randomPosition[j] - A * Math.abs(C * randomPosition[j] - X[i][j]);
                        X[i][j] = simpleBounds(X[i][j]);
                    }
                }
            } else {
                for (int j = 0; j < taskNum; j++) {
                    double D = Math.abs(bestPosition[j] - X[i][j]);
                    X[i][j] = D * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + bestPosition[j];
                    X[i][j] = simpleBounds(X[i][j]);
                }
            }
            whaleFitness[i] = calculateFitness(whalePositions[i]);
            if (whaleFitness[i] < bestFitness) {
                bestPosition = whalePositions[i];
                bestAgentIndex = i;
                bestFitness = whaleFitness[i];
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
