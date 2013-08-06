import java.util.concurrent.*;

public class ShowAsyncPair {

  private static ExecutorService executorService = Executors.newCachedThreadPool();

  @MakeAsyncPair
  public void showThread(long id) {
    println("[PLAIN JAVA THREAD] Caller Thread id: ${id}, My Thread id: ${Thread.currentThread().id}");
  }
  
  @MakeAsyncPair("executorService")
  public void showExecutorThread(long id) {
    println("[EXECUTOR SERVICE] Caller Thread id: ${id}, My Thread id: ${Thread.currentThread().id}");
  }

  @MakeAsyncPair("executorService")
  public String toUpper(String str) {
    return str.toUpperCase();
  }

  @MakeAsyncPair("executorService")
  public int addNumbers(int one, int two, int three) {
    return one + two + three;
  }
  
  public static void main(String[] args) {
    println();
    def me = new ShowAsyncPair();
    println("****Test sync vs async calls, thread ids should be different****");
    me.asyncShowThread(Thread.currentThread().id).join();
    println();

    //try the executor now
    Future<String> toUpperFut = me.asyncToUpper("blah blah");
    println("Should be BLAH BLAH: " + toUpperFut.get());
    println();

    //try the executor with a void returning method
    println("****Test sync vs async calls using executor, thread ids should be different****");
    me.asyncShowExecutorThread(Thread.currentThread().id).get();
    println();

    //See if synchronous arithmetic works like asynchronous arithmetic
    println("Synchronous: ${me.addNumbers(1, 5, 12)}, Asynchronous: ${me.asyncAddNumbers(1, 5, 12).get()}");
    assert(me.addNumbers(1, 5, 12) == me.asyncAddNumbers(1, 5, 12).get());

    executorService.shutdown();
  }
}