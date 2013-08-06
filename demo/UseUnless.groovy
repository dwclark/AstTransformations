@EnableUnless
public class UseUnless {

  public void methodOne() {
    println("Inside methodOne");
  }

  public void methodTwo() {
    println("Inside methodTwo");
  }

  public void methodThree(boolean arg) {
    println("Inside methodThree, arg is ${arg}");
    unless(arg) {
      println("Hit the 'unless' statement");
      return;
    }

    println("If arg was false, you shouldn't see this");
  }

  public static void main(String[] args) {
    println("Inside UseUnless.main()");
    def use = new UseUnless();
    use.methodThree(true);
    use.methodThree(false);
  }

}