import java.util.List;

public class MathMethods {
    public static double sum(List<Double> a){
        if (a.size() > 0) {
            int sum = 0;

            for (Double i : a) {
                sum += i;
            }
            return sum;
        }
        return 0;
    }
    public static double mean(List<Double> a){
        double sum = sum(a);
        double mean = 0;
        mean = sum / (a.size() * 1.0);
        return mean;
    }
    public static double stddev(List<Double> a){
        double sum = 0;
        double mean = mean(a);

        for (Double i : a)
            sum += Math.pow((i - mean), 2);
        return Math.sqrt( sum / ( a.size() - 1 ) ); // sample
    }
}
