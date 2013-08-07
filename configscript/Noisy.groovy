public class Noisy {

  public void programming() {
    println("programming: Bargle Bargle!");
  }

  public void sales() {
    println("sales: I can't shut up");
    println("sales: I'm in sales");
    println("sales: Can I give you my business card?");
    println("sales: It was nice meeting you");
  }

  public static void main(String[] args) {
    def noisy = new Noisy();
    noisy.programming();
    noisy.sales();
  }
}