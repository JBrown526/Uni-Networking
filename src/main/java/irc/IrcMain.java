package irc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class IrcMain {
    //TODO: COMMENTS

    // =============================================================================
    // FIELDS
    // =============================================================================

    private static String hostname;
    private static String nick;
    private static String userName;
    private static String realName;
    private static String channel;
    private static int channelCount;

    private static PrintWriter out;
    private static Scanner in;

    // Enum containing implemented IRC commands
    private enum Command {
        NICK("NICK"),
        USER("USER"),
        JOIN("JOIN"),
        PART("PART"),
        PING("PING"),
        PONG("PONG"),
        PRIVMSG("PRIVMSG");

        public final String label;

        Command(String label) {
            this.label = label;
        }

        private String getLabel() {
            return label;
        }
    }

    // =============================================================================
    // MAIN
    // =============================================================================

    // TODO: Refactor into methods/class
    public static void main(String[] args) {
//        Scanner console = new Scanner(System.in);
        hostname = "selsey.nsqdc.city.ac.uk";
        nick = "RambleBot";
        userName = "RamblingBot";
        realName = "The Rambling Bot";
        channel = "ramblingbot";
        channelCount = 0;

        try (Socket socket = new Socket(hostname, 6667)) {

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            writeCommand(Command.NICK, nick);
            writeCommand(Command.USER, String.format("%s ) * :%s", userName, realName));

            //TODO: split into separate method
            writeCommand(Command.JOIN, channel);
            joinChannel(channel);

            while (in.hasNext()) {
                String serverMessage = in.nextLine();
                String message = serverMessage.toLowerCase();
                System.out.printf("<<< %s%n", serverMessage);

                if (serverMessage.startsWith(Command.PING.getLabel())) {
                    String pingContents = serverMessage.split(" ", 2)[1];
                    writeCommand(Command.PONG, pingContents);
                }

                if (serverMessage.contains("!help")) {
                    writeMessage("Here's a list of my commands:");
                    writeMessage("!rename <newname> this will rename me to whatever you choose, please be nice! :)");
                    writeMessage("!join <channel> this will make me join a channel of your choosing! Yay new friends!");
                    writeMessage("!leave <channel> this will remove me from a channel of you're choosing");
                    // TODO: leave channel - decrement channel count, if last channel dc from server
                    //TODO: disconnect command
                    //TODO: channel description command?
                    //TODO: time command (timezones?)
                }

                if (serverMessage.contains("!rename ")) {
                    String nameSegment = serverMessage.split("!rename ", 2)[1];
                    nick = nameSegment.split(" ")[0];
                    writeCommand(Command.NICK, nick);
                }

                if (serverMessage.contains("!join ")) {
                    String channelSegment = serverMessage.split("!join ")[1];
                    String channelName = channelSegment.split(" ")[0];
                    String channel = channelName.replace("#", "");
                    joinChannel(channel);
                }

                if (serverMessage.contains("!leave ")) {
                    String channelSegment = serverMessage.split("!leave ")[1];
                    String channelName = channelSegment.split(" ")[0];
                    String channel = channelName.replace("#", "");
                    leaveChannel(channel);
                }

                // TODO only send in received channel
                if (message.contains("hello there")) {
                    writeMessage("General Kenobi! You are a bold one");
                }

                if (message.contains("crusade") || message.contains("crusading")) {
                    writeMessage("DEUS VULT! DEUS VULT! DEUS VULT! DEUS VULT!");
                }

                if (message.contains("begone bot")) {
                    writeMessage("You don't have to be so mean about it. Goodbye :(");
                    break;
                }
            }

            in.close();
            out.close();

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =============================================================================
    // METHODS
    // =============================================================================

    private static void writeCommand(Command command, String message) {
        String fullMessage = String.format("%s %s\r%n", command.getLabel(), message);
        System.out.printf(">>> %s%n", fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    private static void writeMessage(String message) {
        String fullMessage = String.format("%s %s :%s\r%n", Command.PRIVMSG.getLabel(), channel, message);
        System.out.printf(">>>%s%n", fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    private static void joinChannel(String channel) {
        String channelSignature = String.format("#%s", channel);
        writeCommand(Command.JOIN, channelSignature);
        channelCount++;
    }

    private static void leaveChannel(String channel) {
        String channelSignature = String.format("#%s", channel);
        writeCommand(Command.PART, channelSignature);
        channelCount--;
    }
}
