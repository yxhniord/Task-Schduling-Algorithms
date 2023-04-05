package mGWO_WOA;

import HOA.Hoa;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class driver {
    public static void main(String[] args) throws Exception {
        File filePost = new File("./recordGWO.csv");
        FileWriter outputfilePost = new FileWriter(filePost);
        CSVWriter writerPost = new CSVWriter(outputfilePost);
        writerPost.writeNext(new String[] {"Nums of iteration", "Cost"});
        int itr = 0;
        int numJobs = 10;
        int numVms = 2;
        int population = 30;

        while(itr < 10000){
            mGWO m = new mGWO(itr, numJobs, numVms, population);
            double time = m.implement();
            writerPost.writeNext(new String[] {String.valueOf(itr), String.valueOf(time)});
            itr += 100;
        }
        writerPost.close();
    }
}
