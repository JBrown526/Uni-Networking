package irc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class IrcMain {
    //TODO: COMMENTS

    // =============================================================================
    // FIELDS
    // =============================================================================

    // TODO: set these at runtime through commandline inputs
    private static final String HOSTNAME = "selsey.nsqdc.city.ac.uk";
    private static final String USER_NAME = "RamblingBot";
    private static final String REAL_NAME = "The Rambling Bot";
    private static String nick;

    private static ArrayList<String> channels;
    private static String currentChannel;

    private static PrintWriter out;

    // enum containing implemented IRC commands
    private enum Command {
        NICK("NICK"),
        USER("USER"),
        JOIN("JOIN"),
        PART("PART"),
        QUIT("QUIT"),
        PING("PING"),
        PONG("PONG"),
        PRIVMSG("PRIVMSG"),
        TOPIC("TOPIC");

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
        nick = "RambleBot";
        currentChannel = "ramblingbot";
        channels = new ArrayList<>();

        // opens a connection to the IRC server given in HOSTNAME
        try (Socket socket = new Socket(HOSTNAME, 6667)) {

            // gets the input and output streams of the socket
            out = new PrintWriter(socket.getOutputStream(), true);
            Scanner in = new Scanner(socket.getInputStream());

            // provides credentials for the IRC server and joins the default channel
            writeCommand(Command.NICK, nick);
            writeCommand(Command.USER, String.format("%s ) * :%s", USER_NAME, REAL_NAME));
            joinChannel(currentChannel);

            // loops through the responses received from the input stream
            while (in.hasNext()) {
                // stores the message from the server and prints it to the console
                String serverMessage = in.nextLine();
                System.out.printf("<<< %s%n", serverMessage);

                // responds to any pings sent out by the server
                if (serverMessage.startsWith(Command.PING.getLabel())) {
                    String pingContents = serverMessage.split(" ", 2)[1];
                    writeCommand(Command.PONG, pingContents);
                }

                // =============================================================================
                // COMMANDS
                // =============================================================================

                // lists all the available commands
                if (serverMessage.contains("!help")) {
                    getChannel(serverMessage);
                    writeMessage("Here's a list of my commands:");
                    writeMessage("!join <channel> - this will make me join a channel of your choosing! Yay new friends!");
                    writeMessage("!leave <channel> - this will remove me from a channel of your choosing, if no channel is given it will remove me from the current channel");
                    writeMessage("!rename <newname> - this will rename me to whatever you choose, please be nice!");
                    writeMessage("!settopic <topic> - this will change the channel's topic to your input");
                    writeMessage("!quit - this will kick me from the server");
                    //TODO: channel description command?
                    //TODO: time command (timezones?)
                }

                // tells the bot to join the given channel
                if (serverMessage.contains("!join ")) {
                    // extracts the channel to join from the server message
                    String channelSegment = serverMessage.split("!join ", 2)[1];
                    String channelFullName = channelSegment.split(" ")[0];
                    String channelName = channelFullName.replace("#", "");

                    getChannel(serverMessage);
                    joinChannel(channelName);
                }

                // tells the bot to leave the given channel
                if (serverMessage.contains("!leave")) {
                    // defaults to leaving the current channel
                    getChannel(serverMessage);
                    String channel = currentChannel;

                    // checks if an input channel has been given
                    if (serverMessage.contains("!leave ")) {
                        // extracts the channel to leave from the server message
                        String channelSegment = serverMessage.split("!leave ", 2)[1];
                        String channelName = channelSegment.split(" ")[0];
                        channel = channelName.replace("#", "");
                    }

                    leaveChannel(channel);

                    // quits the server if the bot is not in any channels
                    if (channels.isEmpty()) {
                        quitServer();
                    }
                }

                // changes the bots nickname to the provided field
                if (serverMessage.contains("!rename ")) {
                    // extracts the new nickname from the server message
                    String nameSegment = serverMessage.split("!rename ", 2)[1];
                    nick = nameSegment.split(" ")[0];

                    writeCommand(Command.NICK, nick);
                }

                // tells the bot to disconnect from the server
                if (serverMessage.contains("!quit")) {
                    quitServer();
                }

                easterEggs(serverMessage);
            }

            in.close();
            out.close();

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    // extracts the channel a message was sent from
    private static void getChannel(String serverMessage) {
        String channelSegment = serverMessage.split("#")[1];
        currentChannel = channelSegment.split(" ")[0];
    }

    // sends a command to the server
    private static void writeCommand(Command command, String message) {
        String fullMessage = String.format("%s %s\r%n", command.getLabel(), message);
        System.out.printf(">>> %s%n", fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    // sends a message to the server on the current channel
    private static void writeMessage(String message) {
        String fullMessage = String.format("%s #%s :%s\r%n", Command.PRIVMSG.getLabel(), currentChannel, message);
        System.out.printf(">>> %s%n", fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    // =============================================================================
    // COMMAND METHODS
    // =============================================================================

    // joins the given channel
    private static void joinChannel(String channel) {
        String channelSignature = String.format("#%s", channel);

        // checks if the channel has already been joined
        if (channels.contains(channelSignature)) {
            writeMessage(String.format("I'm already in %s!", channelSignature));
        } else {
            currentChannel = channel;
            writeCommand(Command.JOIN, channelSignature);
            channels.add(channelSignature);
            writeMessage(String.format("Hi! I'm %s, but you can call me %s. If you want to learn more about what I can do type \"!help\". Let's get along!", REAL_NAME, nick));
        }
    }

    // leaves the given channel
    private static void leaveChannel(String channel) {
        String channelSignature = String.format("#%s", channel);

        if (channels.contains(channelSignature)) {
            writeCommand(Command.PART, channelSignature);
            channels.remove(channelSignature);
        } else {
            writeMessage(String.format("I am not in %s", channelSignature));
        }
    }

    // disconnects from the server
    private static void quitServer() {
        writeCommand(Command.QUIT, "");
    }

    // =============================================================================
    // EASTER EGGS
    // =============================================================================

    private static void easterEggs(String serverMessage) {
        String message = serverMessage.toLowerCase();

        if (message.contains("hello there")) {
            getChannel(serverMessage);
            writeMessage("General Kenobi! You are a bold one");
        }

        while (message.contains("crusade") || message.contains("crusading")) {
            getChannel(serverMessage);
            writeMessage("DEUS VULT! DEUS VULT! DEUS VULT! DEUS VULT!");
        }

        if (message.contains("begone bot")) {
            getChannel(serverMessage);
            writeMessage("You don't have to be so mean about it. Goodbye :(");
            quitServer();
        }
    }
}
