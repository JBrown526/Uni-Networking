package irc;

import javax.swing.*;
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

    public enum Command {
        NICK("NICK"), USER("USER"), JOIN("JOIN"), PING("PING"), PONG("PONG"), PRIVMSG("PRIVMSG");

        public final String label;

        Command(String label) {
            this.label = label;
        }

        private String getLabel() {
            return label;
        }
    }

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        nick = "RambleBot";
        userName = "RamblingBot";
        realName = "The Rambling Bot";
        channel = "#ramblingbot";

        try (Socket socket = new Socket("selsey.nsqdc.city.ac.uk", 6667)) {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            writeCommand(Command.NICK, nick);
            writeCommand(Command.USER, userName + " ) * :" + realName);
            writeCommand(Command.JOIN, channel);

            while (in.hasNext()) {
                String serverMessage = in.nextLine();
                String message = serverMessage.toLowerCase();
                System.out.println("<<< " + serverMessage);

                if (serverMessage.startsWith(Command.PING.getLabel())) {
                    String pingContents = serverMessage.split(" ", 2)[1];
                    writeCommand(Command.PONG, pingContents);
                }

                if (message.contains("hello there")) {
                    writeMessage("General Kenobi! You are a bold one");
                }
            }

            in.close();
            out.close();

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCommand(Command command, String message) {
        String fullMessage = command.getLabel() + " " + message + "\r\n";
        System.out.println(">>> " + fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    private static void writeMessage(String message) {
        String fullMessage = Command.PRIVMSG.getLabel() + " " + channel + " :" + message + "\r\n";
        System.out.println(">>>" + fullMessage);
        out.print(fullMessage);
        out.flush();
    }
}
