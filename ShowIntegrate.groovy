import groovy.transform.*;

@CompileStatic
public class ShowIntegrate {
  
  @Integrate
  public double x2(double x) {
    double foo = x*x;
    foo = foo + 1.0d;
    return foo;
  }

  @Integrate
  public double blah(double y) {
    if(y == 2) {
      println "blah";
    }
    else {
      println "poo";
    }

    return y;
  }

  public static void main(String[] args) {
    double d = 100.00D;
    println(d);
    ShowIntegrate me = new ShowIntegrate();
    println(me.integrateX2(1d,1d,1));
  }
}