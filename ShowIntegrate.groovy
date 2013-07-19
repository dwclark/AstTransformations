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

  public static void main(String[] args) {
    return;
  }
}