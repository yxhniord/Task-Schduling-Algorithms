package mGWO_WOA;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import utils.Constants;

public class mGWO_WOA_Implement {

  protected int popSize;
  protected int maxIter;

  protected int taskNum;
  protected int vmNum;

  protected List<Wolf> wolves;
  protected Wolf alpha_wolf;
  protected Wolf beta_wolf;
  protected Wolf delta_wolf;
  protected Wolf newPosition;

  protected double a;
  protected double r1;
  protected double r2;
  protected double p;
  protected double b;
  protected double l;
  protected double A;
  protected double C;

  Random rand = new Random(Constants.RANDOM_SEED);

  public Map<Integer,Integer> allocateTasks(List<Cloudlet> taskList,List<Vm> vmList){
    this.taskNum = taskList.size();
    this.vmNum = vmList.size();

    wolves = new ArrayList<Wolf>();
    newPosition = Wolf.generateRandomWolf(taskList, vmList);

    // Initialize the population
    for(int i = 0; i < popSize; i++) {
      wolves.add(Wolf.generateRandomWolf(taskList, vmList));
    }

    for (int i = 0; i < maxIter; i++) {
      // Calculate the fitness of each wolf
      wolves.forEach(Wolf::calculateFitness);

      // Find the best, second best and third best wolf
      updateLeaderWolves();

      // Update the position of the wolves
      updatePosition();

    }

    // Return the best schedule
    updateLeaderWolves();
    Map<Integer,Integer> allocatedtasks = new HashMap<>();
    for (int i = 0; i < taskNum; i++) {
      for (int j = 0; j < vmNum; j++) {
        if (alpha_wolf.mapping[i][j] == 1) {
          allocatedtasks.put(i, j);
        }
      }
    }

    return allocatedtasks;
  }

  private void updateLeaderWolves(){
    // Sort the wolves by fitness
    wolves = wolves.stream()
        .sorted(Comparator.comparingDouble(w -> w.fitness))
        .collect(Collectors.toList());

    alpha_wolf = wolves.get(0);
    beta_wolf = wolves.get(1);
    delta_wolf = wolves.get(2);
  }

  private void updatePosition() {
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
            for (int k = 0; k < vmNum; k++) {
              double X1 = alpha_wolf.mapping[j][k] - A * Math.abs(C * alpha_wolf.mapping[j][k] - wolves.get(i).mapping[j][k]);
              double X2 = beta_wolf.mapping[j][k] - A * Math.abs(C * beta_wolf.mapping[j][k] - wolves.get(i).mapping[j][k]);
              double X3 = delta_wolf.mapping[j][k] - A * Math.abs(C * delta_wolf.mapping[j][k] - wolves.get(i).mapping[j][k]);

              newPosition.mapping[j][k] = (X1 + X2 + X3) / 3;
            }
          }
        } else {
          Wolf randomWolf = wolves.get(rand.nextInt(popSize));
          for (int j = 0; j < taskNum; j++) {
            for (int k = 0; k < vmNum; k++) {
              double X = randomWolf.mapping[j][k] - A * Math.abs(C * randomWolf.mapping[j][k] - wolves.get(i).mapping[j][k]);
              newPosition.mapping[j][k] = X;
            }
          }
        }
      } else {
        for (int j = 0; j < taskNum; j++) {
          for (int k = 0; k < vmNum; k++) {
            double D1 = Math.abs(C * alpha_wolf.mapping[j][k] - wolves.get(i).mapping[j][k]);
            double X1 = D1 * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + alpha_wolf.mapping[j][k];

            double D2 = Math.abs(C * beta_wolf.mapping[j][k] - wolves.get(i).mapping[j][k]);
            double X2 = D2 * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + beta_wolf.mapping[j][k];

            double D3 = Math.abs(C * delta_wolf.mapping[j][k] - wolves.get(i).mapping[j][k]);
            double X3 = D3 * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + delta_wolf.mapping[j][k];

            newPosition.mapping[j][k] = (X1 + X2 + X3) / 3;
          }
        }
      }

      newPosition.normalizeWolf();
      if (newPosition.calculateFitness() < wolves.get(i).fitness) {
        wolves.set(i, newPosition);
      }
    }
  }

  public mGWO_WOA_Implement(int popSize, int maxIter){
    this.popSize = popSize;
    this.maxIter = maxIter;
  }

}
