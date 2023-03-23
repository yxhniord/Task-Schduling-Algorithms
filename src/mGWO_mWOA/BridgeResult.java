package mGWO_mWOA;

public class BridgeResult {
    int[] bestPositions;
    double bestFitness;

    public BridgeResult(int[] bestPositions, double bestFitness) {
        this.bestPositions = bestPositions;
        this.bestFitness = bestFitness;
    }

    public int[] getBestPositions() {
        return bestPositions;
    }

    public double getBestFitness() {
        return bestFitness;
    }
}
