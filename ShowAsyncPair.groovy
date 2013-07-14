public class ShowAsyncGroovy {

  @MakeAsyncPair
  public void showThread(long id) {
    println("Caller id: ${id}, My id: ${Thread.currentThread().id}");
  }

  @MakeAsyncPair
  public String toUpper(String str) {
    return str.toUpperCase();
  }
  
  public static void main(String[] args) {
    def me = new ShowAsyncGroovy();
    me.showThread(Thread.currentThread().id);
    me.asyncShowThread(Thread.currentThread().id).join();
  }
}