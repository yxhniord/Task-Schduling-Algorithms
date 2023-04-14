package HOA;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class driver {
    public static void main(String[] args) throws Exception {
        File filePost = new File("./recordreverse.csv");
        FileWriter outputfilePost = new FileWriter(filePost);
        CSVWriter writerPost = new CSVWriter(outputfilePost);
        writerPost.writeNext(new String[] {"Nums of iteration", "Cost"});
        int itr = 5;
        int numJobs = 1000;
        int numVms = 30;
        int population = 30;

        while(itr <= 150){
            Hoa h = new Hoa(itr, numJobs, numVms, population);
            double time = h.implement();
            writerPost.writeNext(new String[] {String.valueOf(itr), String.valueOf(time)});
            itr += 5;
        }
        writerPost.close();
    }
}
