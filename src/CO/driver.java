package CO;

import HOA.Hoa;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class driver {
    public static void main(String[] args) throws Exception {
        File filePost = new File("./recordComakespan.csv");
        FileWriter outputfilePost = new FileWriter(filePost);
        CSVWriter writerPost = new CSVWriter(outputfilePost);
        writerPost.writeNext(new String[] {"Nums of iteration", "Cost"});
        int itr = 5;
        int numJobs = 10;
        int numVms = 30;
        int population = 30;

        while(itr <= 100){
            Co c = new Co(itr, numJobs, numVms, population);
            double time = c.implement();
            writerPost.writeNext(new String[] {String.valueOf(itr), String.valueOf(time)});
            itr += 5;
        }
        writerPost.close();
    }
}
