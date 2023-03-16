package mGWO_mWOA;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import utils.Constants;

import java.util.List;

public class mGWO_mWOA_simpleAlgo {
    static int N; // population (number of search agents)
    static int D; // Dimension
    static int n_vm = Constants.NO_OF_VMS;
    static int n_task = Constants.NO_OF_TASKS;
    static int maxIter; // iteration
    static double a; // a is in range [0,2]
    static double r1;
    static double r2;
    static double[][] alfa;
    static double[][] beta;
    static double[][] delta;
    static double X1, X2, X3;
    static double A1, C1, A2, C2, A3, C3;
    static double Lower, Upper;
    static double[][][] positions; // population position
    static double[] BestVal;
    static double fitness, alfaScore, betaScore, deltaScore;
    static boolean flag = false;

    public mGWO_mWOA_simpleAlgo(int iter, int UpLevel, int LowLevel, int searchAgent, int dimension) {
        maxIter = iter;
        Upper = UpLevel;
        Lower = LowLevel;
        N = searchAgent;
        fitness = Double.MAX_VALUE;
        alfaScore = Double.MAX_VALUE;
        betaScore = Double.MAX_VALUE;
        deltaScore = Double.MAX_VALUE;
        positions = new double[N][n_vm][n_task];
        alfa = new double[n_vm][n_task];
        beta = new double[n_vm][n_task];
        delta = new double[n_vm][n_task];
        BestVal = new double[maxIter];
    }

    static void init(List<Cloudlet> cloudletList, List<Vm> vmList) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < n_vm; j++) {
                for (int k = 0; k < n_task; k++) {
                    positions[i][j][k] = cloudletList.get(i).getCloudletLength() /
                            vmList.get(j).getMips();
                }
            }
        }

        sortAndIndex(positions);
        BestVal[0] = benchmark(alfa);
    }

    static double benchmark(double[][] position) {
        double makespan = 0;
        double[] dcWorkTime = new double[n_vm];
        for (int i = 0; i < n_task; i++) {
            for (int j = 0; j < n_vm; j++) {
                dcWorkTime[j] += position[j][i];
                makespan = Math.max(makespan, dcWorkTime[j]);
            }
        }
//  use for print
//        if (flag) {
//            for (int i = 0; i < n_vm; i++) {
//                for (int j = 0; j < n_task; j++) {
//                    Log.print(position[i][j] + " ");
//                }
//                Log.printLine();
//            }
//        }
//        flag = false;

        return makespan;
    }

    static void sortAndIndex(double[][][] positions) {
        double score;
        for (int i = 0; i < N; i++) {
            score = benchmark(positions[i]);
            if (score < alfaScore) {
                alfaScore = score;
                for (int j = 0; j < n_vm; j++) {
                    for (int k = 0; k < n_task; k++) {
                        alfa[j][k] = positions[i][j][k];
                    }
                }
            }
            if (score > alfaScore && score < betaScore) {
                betaScore = score;
                for (int j = 0; j < n_vm; j++) {
                    for (int k = 0; k < n_task; k++) {
                        beta[j][k] = positions[i][j][k];
                    }
                }
            }
            if (score > betaScore && score < deltaScore) {
                deltaScore = score;
                for (int j = 0; j < n_vm; j++) {
                    for (int k = 0; i < n_task; k++) {
                        delta[j][k] = positions[i][j][k];
                    }
                }
            }
        }
    }

    public double[][][] solution(List<Cloudlet> cloudletList, List<Vm> vmList) {
        init(cloudletList, vmList);
        for (int iter = 1; iter < maxIter; iter++) {
            a = 2.0 * (1.0 - (Math.pow(iter, 2) / Math.pow(maxIter, 2)));

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < n_vm; j++) {
                    for (int k = 0; k < n_task; k++) {
                        // update values for alfa
                        r1 = Math.random();
                        r2 = Math.random();
                        A1 = (2.0 * a * r1) - a;
                        C1 = 2.0 * r2;
                        // update position by alfa
                        X1 = alfa[j][k] - A1 * (Math.abs(C1 * alfa[j][k] - positions[i][j][k]));

                        // update values for beta
                        r1 = Math.random();
                        r2 = Math.random();
                        A2 = (2.0 * a * r1) - a;
                        C2 = 2.0 * r2;
                        // update position by beta
                        X2 = beta[j][k] - A2 * (Math.abs(C2 * beta[j][k] - positions[i][j][k]));

                        // update values for beta
                        r1 = Math.random();
                        r2 = Math.random();
                        A3 = (2.0 * a * r1) - a;
                        C3 = 2.0 * r2;
                        // update position by beta
                        X3 = delta[j][k] - A3 * (Math.abs(C3 * delta[j][k] - positions[i][j][k]));

                        positions[i][j][k] = simpleBounds((X1 + X2 + X3) / 3.0);
                    }
                }
            }
            sortAndIndex(positions);
            BestVal[iter] = benchmark(alfa);
        }
        double[][][] out = new double[2][n_vm][n_task];
        double testSum = 0;
        double testMakespan = 0;
        for (int i = 0; i < n_vm; i++) {
            for (int j = 0; j < n_task; j++) {
                out[1][i][j] = alfa[i][j];
                testSum += alfa[i][j];
                testMakespan += Math.abs(alfa[i][j]);
            }
        }
        out[0][0][0] = benchmark(alfa);
        Log.printLine("Test sum: " + testSum);
        Log.printLine("Test makespan: " + testMakespan);
        return out;
    }

    static double simpleBounds(double s) {
        if (s < Lower) {
            s = Lower;
        }
        if (s > Upper) {
            s = Upper;
        }
        return s;
    }
}
