public class ShowAsyncGroovy {

  public void doThread() {
    Thread.start {
      println("I'm here");
    };
  }

  @AsyncPair
  public void showThread(long id) {
    println("Caller id: ${id}, My id: ${Thread.currentThread().id}");
  }
  
  public static void main(String[] args) {
    def me = new ShowAsyncGroovy();
    me.showThread(Thread.currentThread().id);
    me.asyncShowThread(Thread.currentThread().id).join();
  }
}