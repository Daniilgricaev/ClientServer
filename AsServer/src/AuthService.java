import java.io.*;
import java.util.Scanner;


public class AuthService {
    public static synchronized boolean check(String name, String password){
      try{
          File readFile = new File("users.txt");
          Scanner scanner = new Scanner(readFile);
          while(scanner.hasNext()){
              String data = scanner.nextLine();
              String[] info = data.split("\\|");
              if (info[1].equals(name) && info[2].equals(password)) {
                  return true;
              }
          }
      }catch (FileNotFoundException ex){
          System.out.println("File not found");
          ex.getMessage();
      }
      return false;
    }
}
