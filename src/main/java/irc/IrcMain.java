package irc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class IrcMain {
    private static String nick;
    private static String userName;
    private static String realName;
    private static PrintWriter out;
    private static Scanner in;

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        nick = "RambleBot";
        userName = "RamblingBot";
        realName = "The Rambling Bot";

        try (Socket socket = new Socket("selsey.nsqdc.city.ac.uk", 6667)) {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            write("NICK", nick);
            write("USER", userName + " ) * :" + realName);
            write("JOIN", "#ramblingbot");

            while (in.hasNext()) {
                String serverMessage = in.nextLine();
                System.out.println("<<< " + serverMessage);

                if (serverMessage.startsWith("PING")) {
                    String pingContents = serverMessage.split(" ", 2)[1];
                    write("PONG", pingContents);
                }
            }

            in.close();
            out.close();

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(String command, String message) {
        String fullMessage = command + " " + message + "\r\n";
        System.out.println(">>> " + message);
        out.print(fullMessage);
        out.flush();
    }
}
