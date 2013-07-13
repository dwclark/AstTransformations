public class UseRename {

  @Rename
  public void doStuff() {
    println("I can confidently assert I am the only statement in the doStuff() method");
  }

  @WrapMethod
  public int myInt() {
    return 1;
  }
  
  public static void main(String[] args) {
    def obj = new UseRename();
    obj.doStuff();
    println(obj.myInt());
    println(obj."${WrapMethodTransformation.PREFIX}myInt"());
  }

}