public class ShowAsyncGroovy {

  @AsyncPair
  public void showThread(String message) {
    println("Announcing ${message} from ${Thread.currentThread().id}");
  }
  
  public static void main(String[] args) {
    def me = new ShowAsyncGroovy();
    me.showThread("Call thread id: ${Thread.currentThread().id}");
  }
}