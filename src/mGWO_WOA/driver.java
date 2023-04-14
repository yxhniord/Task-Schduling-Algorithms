package mGWO_WOA;

import HOA.Hoa;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class driver {
    public static void main(String[] args) throws Exception {
        File filePost = new File("./recordGWOpop.csv");
        FileWriter outputfilePost = new FileWriter(filePost);
        CSVWriter writerPost = new CSVWriter(outputfilePost);
        writerPost.writeNext(new String[] {"Nums of iteration", "Cost"});
        int itr = 100;
        int numJobs = 1000;
        int numVms = 30;
        int population = 4;

        while(population <= 30){
            mGWO m = new mGWO(itr, numJobs, numVms, population);
            double time = m.implement();
            writerPost.writeNext(new String[] {String.valueOf(itr), String.valueOf(time)});
            population += 2;
        }
        writerPost.close();
    }
}
