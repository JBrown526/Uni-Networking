package irc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class IrcMain {
    private static String nick;
    private static String userName;
    private static String realName;
    private static String channel;

    private static PrintWriter out;
    private static Scanner in;

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        nick = "RambleBot";
        userName = "RamblingBot";
        realName = "The Rambling Bot";
        channel = "#ramblingbot";

        try (Socket socket = new Socket("selsey.nsqdc.city.ac.uk", 6667)) {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            writeCommand("NICK", nick);
            writeCommand("USER", userName + " ) * :" + realName);
            writeCommand("JOIN", channel);

            while (in.hasNext()) {
                String serverMessage = in.nextLine();
                System.out.println("<<< " + serverMessage);

                if (serverMessage.startsWith("PING")) {
                    String pingContents = serverMessage.split(" ", 2)[1];
                    writeCommand("PONG", pingContents);
                }

                if (serverMessage.contains("hello")) {
                    writeMessage("PRIVMSG #ramblingbot :Oh, hi!");
                }
            }

            in.close();
            out.close();

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCommand(String command, String message) {
        String fullMessage = command + " " + message + "\r\n";
        System.out.println(">>> " + fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    private static void writeMessage(String message) {
        String fullMessage = message + "\r\n";
        System.out.println(">>>" + fullMessage);
        out.print(fullMessage);
        out.flush();
    }
}
