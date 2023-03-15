package mGWO_WOA;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class mGWO_WOA_Implement {

  protected int popSize;
  protected int maxIter;

  protected int taskNum;
  protected int vmNum;

  protected int[][] wolfPositions;
  protected double[] wolfFitness;

  public int[] alpha_wolf;
  public int[] beta_wolf;
  public int[] delta_wolf;
  public int[] newPosition;

  public int alphaIndex = 0;
  public int betaIndex = 0;
  public int deltaIndex = 0;

  protected double a;
  protected double r1;
  protected double r2;
  protected double p;
  protected double b;
  protected double l;
  protected double A;
  protected double C;

  protected double[][] X1;
  protected double[][] X2;
  protected double[][] X3;

  Random rand = new Random();

  public Map<Integer,Integer> allocateTasks(List<Cloudlet> taskList,List<Vm> vmList){
    this.taskNum = taskList.size();
    this.vmNum = vmList.size();

    wolfPositions = new int[popSize][taskNum];
    wolfFitness = new double[popSize];
    alpha_wolf = new int[taskNum];
    beta_wolf = new int[taskNum];
    delta_wolf = new int[taskNum];
    newPosition = new int[taskNum];
    X1 = new double[popSize][taskNum];
    X2 = new double[popSize][taskNum];
    X3 = new double[popSize][taskNum];

    // Initialize the population
    for(int i = 0; i < popSize; i++) {
      for (int j = 0; j < taskNum; j++) {
        wolfPositions[i][j] = new Random().nextInt(vmNum);
      }
    }

    for (int i = 0; i < maxIter; i++) {
      // Calculate the fitness of each wolf
      for (int j = 0; j < popSize; j++) {
        wolfFitness[j] = calculateFitness(wolfPositions[j], taskList, vmList);
      }

      // Find the best, second best and third best wolf
      updateLeaderWolves();

      // Update the position of the wolves
      updatePosition(taskList, vmList);

    }

    // Return the best schedule
    updateLeaderWolves();
    Map<Integer,Integer> allocatedtasks = new HashMap<>();
    for (int i = 0; i < taskNum; i++) {
      allocatedtasks.put(i, alpha_wolf[i]);
    }

    return allocatedtasks;
  }

  private double calculateFitness(int[] schedule, List<Cloudlet> taskList,List<Vm> vmList){
    double makespan = calculateMakespan(schedule, taskList, vmList);
    double energy = calculateEnergy(schedule, taskList, vmList);
    double fitness = makespan + energy;
    return fitness;
  }

  // calculate the makespan of the schedule
  private double calculateMakespan(int[] schedule, List<Cloudlet> taskList,List<Vm> vmList){
    double makespan = 0;
    double[] vmTime = new double[vmNum];
    for (int i = 0; i < taskNum; i++) {
      vmTime[schedule[i]] += taskList.get(i).getCloudletLength() / vmList.get(schedule[i]).getMips();
      makespan = Math.max(makespan, vmTime[schedule[i]]);
    }
    return makespan;
  }

  private double calculateEnergy(int[] schedule, List<Cloudlet> taskList,List<Vm> vmList){
    // TODO: calculate the energy of the schedule
    double energy = 0;
    for (int i = 0; i < taskNum; i++) {
//      energy += taskList.get(i).getCloudletLength() * vmList.get(schedule[i]).getHost()
    }
    return energy;
  }

  // update alpha, beta and delta wolves
  private void updateLeaderWolves(){
    for (int i = 0; i < popSize; i++) {
      alphaIndex = getBestWolfIndex(-1, -1);
      alpha_wolf = wolfPositions[alphaIndex];

      betaIndex = getBestWolfIndex(alphaIndex, -1);
      beta_wolf = wolfPositions[betaIndex];

      deltaIndex = getBestWolfIndex(alphaIndex, betaIndex);
      delta_wolf = wolfPositions[deltaIndex];
    }
  }

  private int getBestWolfIndex(int exclude1, int exclude2) {
    int bestIndex = 0;
    double bestFitness = Double.MAX_VALUE;
    for (int i = 0; i < popSize; i++) {
      if (i == exclude1 || i == exclude2) {
        continue;
      }
      if (wolfFitness[i] < bestFitness) {
        bestIndex = i;
        bestFitness = wolfFitness[i];
      }
    }
    return bestIndex;
  }

  private void updatePosition(List<Cloudlet> taskList,List<Vm> vmList) {
    for (int i = 0; i < popSize; i++) {
      a = 2.0*(1-(Math.pow(i,2)/Math.pow(maxIter,2)));
      p = rand.nextDouble();
      l = rand.nextDouble() * 2 - 1;
      r1 = rand.nextDouble();
      r2 = rand.nextDouble();
      A = 2*a*r1-a;
      C = 2*r2;

      if (p < 0.5) {
        if (Math.abs(A) < 1) {
          for (int j = 0; j < taskNum; j++) {
            X1[i][j] = alpha_wolf[j] - A * Math.abs(C * alpha_wolf[j] - wolfPositions[i][j]);
            X1[i][j] = simplebounds(X1[i][j]);

            X2[i][j] = beta_wolf[j] - A * Math.abs(C * beta_wolf[j] - wolfPositions[i][j]);
            X2[i][j] = simplebounds(X2[i][j]);

            X3[i][j] = delta_wolf[j] - A * Math.abs(C * delta_wolf[j] - wolfPositions[i][j]);
            X3[i][j] = simplebounds(X3[i][j]);

            newPosition[j] = (int) simplebounds((X1[i][j] + X2[i][j] + X3[i][j]) / 3);
          }
        } else {
          int[] randomPosition = wolfPositions[rand.nextInt(popSize)];
          for (int j = 0; j < taskNum; j++) {
            double X = randomPosition[j] - A * Math.abs(C * randomPosition[j] - wolfPositions[i][j]);
            newPosition[j] = (int) simplebounds(X);
          }
        }
      } else {
        for (int j = 0; j < taskNum; j++) {
          double D = Math.abs(C * alpha_wolf[j] - wolfPositions[i][j]);
          X1[i][j] = D * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + alpha_wolf[j];
          X1[i][j] = simplebounds(X1[i][j]);

          D = Math.abs(C * beta_wolf[j] - wolfPositions[i][j]);
          X2[i][j] = D * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + beta_wolf[j];
          X2[i][j] = simplebounds(X2[i][j]);

          D = Math.abs(C * delta_wolf[j] - wolfPositions[i][j]);
          X3[i][j] = D * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + delta_wolf[j];
          X3[i][j] = simplebounds(X3[i][j]);

          newPosition[j] = (int) simplebounds((X1[i][j] + X2[i][j] + X3[i][j]) / 3);
        }
      }
//      for (int j = 0; j < taskNum; j++) {
//        X1[i][j] = alpha_wolf[j] - A * Math.abs(C * alpha_wolf[j] - wolfPositions[i][j]);
//        X1[i][j] = simplebounds(X1[i][j]);
//
//        X2[i][j] = beta_wolf[j] - A * Math.abs(C * beta_wolf[j] - wolfPositions[i][j]);
//        X2[i][j] = simplebounds(X2[i][j]);
//
//        X3[i][j] = delta_wolf[j] - A * Math.abs(C * delta_wolf[j] - wolfPositions[i][j]);
//        X3[i][j] = simplebounds(X3[i][j]);
//
//        newPosition[j] = (int) simplebounds((X1[i][j] + X2[i][j] + X3[i][j]) / 3);
//      }
      if (calculateFitness(newPosition, taskList, vmList) < wolfFitness[i]) {
        wolfPositions[i] = newPosition;
      }
    }
  }

  private double simplebounds(double val)
  {
    if(val < 0)
      return 0;

    if(val > vmNum - 1)
      return vmNum - 1;

    return val;
  }

  public mGWO_WOA_Implement(int popSize, int maxIter){
    this.popSize = popSize;
    this.maxIter = maxIter;
  }

}
