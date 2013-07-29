import groovy.transform.*;

@CompileStatic
public class ShowIntegrate {
  
  @Integrate
  public double x2(double x) {
    return x * x;
  }

  @Integrate
  public double sin2(double z) {
    double val = Math.sin(z);
    val *= val;
    return val;
  }

  @Integrate
  public double sin(double q) {
    return Math.sin(q);
  }

  @Integrate
  public double x3(double v) {
    return v * v * v;
  }

  public static void main(String[] args) {
    ShowIntegrate s = new ShowIntegrate();
    double lower = 0.0;
    double upper = 0.0;
    long start = System.currentTimeMillis();
    for(int i = 100; i < 1000; ++i) {
      for(int j = 0; j < 500; ++j) {
	lower = (double) j;
	upper = lower + 5;
	s.integrateX2(lower, upper, i);
	s.integrateSin2(lower, upper, i);
	s.integrateSin(lower, upper, i);
	s.integrateX3(lower, upper, i);
      }
    }

    long end = System.currentTimeMillis();
    println("Total time: " + ((double) (end - start) / 1000));
  }
}