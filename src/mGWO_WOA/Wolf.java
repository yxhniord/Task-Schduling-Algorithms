package mGWO_WOA;


import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class Wolf {

  public double[][] mapping;
  public List<Cloudlet> taskList;
  public List<Vm> vmList;
  protected int taskNum;
  protected int vmNum;
  public double fitness;

  public Wolf (List<Cloudlet> taskList, List<Vm> vmList, double[][] mapping) {
    this.taskList = taskList;
    this.vmList = vmList;
    this.taskNum = taskList.size();
    this.vmNum = vmList.size();
    this.mapping = mapping;
    calculateFitness();
  }

  public static Wolf generateRandomWolf(List<Cloudlet> taskList, List<Vm> vmList) {
    double[][] mapping = new double[taskList.size()][vmList.size()];
    Random rand = new Random();
    for (int i = 0; i < taskList.size(); i++) {
      int randomVm = rand.nextInt(vmList.size());
      mapping[i][randomVm] = 1;
    }
    return new Wolf(taskList, vmList, mapping);
  }

  public void normalizeWolf () {
    for (int i = 0; i < taskNum; i++) {
      int maxVmIndex = 0;
      double maxValue = mapping[i][0];

      for (int j = 1; j < vmNum; j++) {
        if (mapping[i][j] > maxValue) {
          maxValue = mapping[i][j];
          maxVmIndex = j;
        }
      }

      for (int j = 0; j < vmNum; j++) {
        if (j == maxVmIndex) {
          mapping[i][j] = 1;
        } else {
          mapping[i][j] = 0;
        }
      }
    }
    calculateFitness();
  }

  public double calculateFitness(){
    this.fitness = calculateMakespan() + calculateCost();
    return this.fitness;
  }

  // calculate the makespan of the schedule
  private double calculateMakespan(){
    double makespan = 0;
    double[] vmTime = new double[vmNum];
    for (int i = 0; i < taskNum; i++) {
      for (int j = 0; j < vmNum; j++) {
        if (mapping[i][j] == 1) {
          vmTime[j] += taskList.get(i).getCloudletLength() / vmList.get(j).getMips();
          makespan = Math.max(makespan, vmTime[j]);
        }
      }
    }
    return makespan;
  }

  private double calculateCost(){
    double cost = 0;
    for (int i = 0; i < taskNum; i++) {
      Cloudlet task = taskList.get(i);
      cost += task.getCostPerSec() * task.getActualCPUTime();
    }
    return cost;
  }

}
