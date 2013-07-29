public class Integrate {

    public static double doIt(Calculate calculate, double lower, double upper, int steps) {
	double sum_pry1M5Tz90L1vHUehxHsxN7VefQ = 0.0;
        double delta_pry1M5Tz90L1vHUehxHsxN7VefQ = upper - lower / steps;
        double x = lower;
        for(int i_pry1M5Tz90L1vHUehxHsxN7VefQ = 0; i_pry1M5Tz90L1vHUehxHsxN7VefQ < steps ;++( i_pry1M5Tz90L1vHUehxHsxN7VefQ )) {
            sum_pry1M5Tz90L1vHUehxHsxN7VefQ += delta_pry1M5Tz90L1vHUehxHsxN7VefQ * calculate.calc(x);
            x += delta_pry1M5Tz90L1vHUehxHsxN7VefQ;
        }
        return sum_pry1M5Tz90L1vHUehxHsxN7VefQ;
    }

    public static void main(String[] args) {
	double lower = 0.0;
	double upper = 0.0;
	Calculate x2 = new Calculations.CalculateX2();
	Calculate x3 = new Calculations.CalculateX3();
	Calculate sin = new Calculations.CalculateSin();
	Calculate sin2 = new Calculations.CalculateSin2();
	long start = System.currentTimeMillis();
	for(int i = 100; i < 1000; ++i) {
	    for(int j = 0; j < 500; ++j) {
		lower = (double) j;
		upper = lower + 5;
		doIt(x2, lower, upper, i);
		doIt(sin2, lower, upper, i);
		doIt(sin, lower, upper, i);
		doIt(x3, lower, upper, i);
	    }
	}
	
	long end = System.currentTimeMillis();
	System.out.println("Total time: " + ((double) (end - start) / 1000));
    }
}
