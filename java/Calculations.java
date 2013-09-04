public class Calculations {
    
    public static class CalculateSin implements Calculate {
	public double calc(double x) {
	    return Math.sin(x);
	}
    }

    public static class CalculateSin2 implements Calculate {
	public double calc(double z) {
	    double val = Math.sin(z);
	    val *= val;
	    return val;
	}
    }

    public static class CalculateX2 implements Calculate {
	public double calc(double x) {
	    return x * x;
	}
    }

    public static class CalculateX3 implements Calculate {
	public double calc(double x) {
	    return x * x * x;
	}
    }
    
}
