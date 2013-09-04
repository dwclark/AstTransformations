@EnableUnless
public class UseUnless {

  public void myMethod(boolean arg) {
    println("Inside myMethod, arg is ${arg}");
    unless(arg) {
      println("Hit the 'unless' statement");
      return;
    }

    assert(arg);
    println("If arg was false, you shouldn't see this");
  }

  public static void main(String[] args) {
    println("Inside UseUnless.main()");
    def use = new UseUnless();
    use.myMethod(true);
    use.myMethod(false);
  }

}