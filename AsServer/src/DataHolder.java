import java.util.Scanner;

public class DataHolder {
    Scanner scanner = new Scanner(System.in);
    private final  String userName = scanner.nextLine();

    public String getUserName(){
        return userName;
    }
}
