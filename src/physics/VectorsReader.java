package physics;


import java.io.*;
import java.util.*;

import utility.math.Vector2;

public class VectorsReader {

    /*Take input from a csv file, and put it in 2d vectors*/

    public static final String delimiter = ";";
    public static ArrayList<String> finalArr = new ArrayList<>();
    public static double velocity;



    public static Queue<Vector2> read(String csvFile) {
        Queue<Vector2> q
                = new LinkedList<>();
        Vector2 vector = new Vector2();

        try {
            File file = new File(System.getProperty("user.dir") + csvFile);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] tempArr;
            String temp="";
            String temp2="";
            double temp3=0.0;
            double temp4=0.0;




            while((line = br.readLine()) != null){
                tempArr = line.split(delimiter); //turn each line into an array index
                for(String tempStr : tempArr) {
                    temp=(tempStr.substring(tempStr.indexOf("(")+1, tempStr.indexOf(",")));
                    temp2=(tempStr.substring(tempStr.indexOf(",")+1, tempStr.indexOf(")")));
                    temp3=Double.parseDouble(temp);
                    temp4=Double.parseDouble(temp2);
                    vector = new Vector2(temp3,temp4);
                    q.add(vector);

                }
            }
            br.close();

        }
        catch(IOException ioe) {
            ioe.printStackTrace();

        }
        return q;
    }
}

