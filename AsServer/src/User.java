import java.util.Scanner;
import java.io.*;

public class User {
    Scanner scan = new Scanner(System.in);
    String name;
    String password;
    String mail;
    public User(String name, String password, String mail){
        this.name = name;
        this.password = password;
        this.mail = mail;
    }
    public User reg(){
        System.out.println("What is your mail ? :");
        this.mail = scan.next();

        System.out.println("What is your name ? :");
        this.name = scan.next();

        System.out.println("Create password : ");
        this.password = scan.next();

        try(FileWriter fileWriter = new FileWriter("users.txt", true)){
            fileWriter.write(this.mail + "|" + this.name + "|" + this.password);
            fileWriter.append("\n");
            fileWriter.flush();
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }

        return this;
    }
}

