package stze;

import java.util.ArrayList;

public final class Calculations {

    static double sum(ArrayList<Double> list) {

        double sum = 0;

        for(double l: list)
            sum += l;

        return sum;
    }

    static double average(ArrayList<Double> list) {
        return (sum(list) / list.size());
    }

    static double standardDeviation(ArrayList<Double> list) {

        double avg = average(list);

        double sum = 0;

        for(double l: list) {
            sum += (l - avg) * (l - avg);
        }

        double varianz = sum / list.size();

        return Math.sqrt(varianz);
    }

    static double min(ArrayList<Double> list) {

        double min = list.get(0);

        for(double l: list) {
            if(l < min)
                min = l;
        }

        return min;
    }

    static double max(ArrayList<Double> list) {

        double max = list.get(0);

        for(double l: list) {
            if(l > max)
                max = l;
        }

        return max;
    }


}
