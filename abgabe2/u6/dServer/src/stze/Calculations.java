package stze;

import java.util.ArrayList;

public class Calculations {

    static double sum(ArrayList<Double> list) {

        double sum = 0;

        for(double l: list)
            sum += l;

        return sum;
    }

    double average(ArrayList<Double> list) {
        return (sum(list) / list.size());
    }

    double standardDeviation(ArrayList<Double> list) {

        double avg = average(list);

        double sum = 0;

        for(double l: list) {
            sum += (l - avg) * (l - avg);
        }

        double varianz = sum / list.size();

        return Math.sqrt(varianz);
    }


}
