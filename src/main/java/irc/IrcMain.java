package irc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcMain {

    // =============================================================================
    // FIELDS
    // =============================================================================

    // TODO: set at runtime
    private static final String DEFAULT_HOSTNAME = "selsey.nsqdc.city.ac.uk";
    private static final int DEFAULT_PORT = 6667;
    private static final String DEFAULT_USERNAME = "RamblingBot";
    private static final String DEFAULT_REAL_NAME = "The Rambling Bot";
    private static final String DEFAULT_NICKNAME = "RambleBot";
    private static final String DEFAULT_CHANNEL = "ramblingbot";

    private static String nick;

    private static ArrayList<String> channels;
    private static String currentChannel;
    private static boolean commandIssued;
    private static boolean timeRequested;

    private static final RandomStoryGen randomStoryGen = new RandomStoryGen();
    private static HigherOrLower higherOrLower;

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
        TIME("TIME"),
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

    public static void main(String[] args) {
        String hostname;
        int port;
        String username;
        String realName;

        Scanner sysIn = new Scanner(new InputStreamReader(System.in));

        // option to skip manual entry of connection parameters
        System.out.println("would you like to connect with the default settings? (Y/N)");
        String input = sysIn.nextLine();
        if (input.contains("Y")) {
            hostname = DEFAULT_HOSTNAME;
            port = DEFAULT_PORT;
            username = DEFAULT_USERNAME;
            realName = DEFAULT_USERNAME;
            nick = DEFAULT_NICKNAME;
            currentChannel = DEFAULT_CHANNEL;
        } else {
            // manual entry for hostname
            System.out.printf("please enter the host you would like to connect to and then press enter (if left blank this will default to %s):%n", DEFAULT_HOSTNAME);
            input = sysIn.nextLine();
            hostname = input.equals("") ? DEFAULT_HOSTNAME : input;

            // manual entry for port
            System.out.printf("please enter the port you would like to connect on and then press enter (if left blank this will default to %d):%n", DEFAULT_PORT);
            input = sysIn.nextLine();
            port = input.equals("") ? DEFAULT_PORT : Integer.parseInt(input);

            // manual entry for username
            System.out.printf("please enter the username you would like to use and then press enter (if left blank this will default to %s):%n", DEFAULT_USERNAME);
            input = sysIn.nextLine();
            username = input.equals("") ? DEFAULT_USERNAME : input;

            // manual entry for real name
            System.out.printf("please enter the real name you would like to use and then press enter (if left blank this will default to %s):%n", DEFAULT_REAL_NAME);
            input = sysIn.nextLine();
            realName = input.equals("") ? DEFAULT_REAL_NAME : input;

            // manual entry for nickname
            System.out.printf("please enter the nickname you would like to use and then press enter (if left blank this will default to %s):%n", DEFAULT_NICKNAME);
            input = sysIn.nextLine();
            nick = input.equals("") ? DEFAULT_NICKNAME : input;

            // manual entry for channel
            System.out.printf("please enter the first channel you would like to join and then press enter (if left blank this will default to %s):%n", DEFAULT_CHANNEL);
            input = sysIn.nextLine();
            currentChannel = input.equals("") ? DEFAULT_CHANNEL : input;
        }

        channels = new ArrayList<>();
        commandIssued = false;

        higherOrLower = null;

        // opens a connection to the IRC server given in HOSTNAME
        try (Socket socket = new Socket(hostname, port)) {

            // gets the input and output streams of the socket
            out = new PrintWriter(socket.getOutputStream(), true);
            Scanner in = new Scanner(socket.getInputStream());

            // provides credentials for the IRC server and joins the default channel
            writeCommand(Command.NICK, nick);
            writeCommand(Command.USER, String.format("%s ) * :%s", username, realName));
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

                // checks if a command has been sent by the server
                helpCommand(serverMessage);
                joinCommand(serverMessage);
                leaveCommand(serverMessage);
                renameCommand(serverMessage);
                setTopicCommand(serverMessage);
                timeCommand(serverMessage);
                storyCommand(serverMessage);
                backstoryCommand(serverMessage);
                higherOrLowerCommand(serverMessage);
                higherOrLowerGuess(serverMessage);
                higherOrLowerStopCommand(serverMessage);
                quitCommand(serverMessage);

                if (timeRequested) {
                    timeOutput(serverMessage);
                }

                if (!commandIssued) {
                    easterEggs(serverMessage);
                }
                commandIssued = false;
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

    // formats the channel name for the server
    private static String channelSignatureFormatter(String channel) {
        return String.format("#%s", channel);
    }

    // extracts the user a message was sent from
    private static String getUser(String serverMessage) {
        String nameSegment = serverMessage.split("!~")[1];
        return nameSegment.split("@")[0];
    }

    // checks if the message is returning a timestamp
    private static boolean containsTime(String serverMessage) {
        Pattern p = Pattern.compile(".*\\d{2}[ ]\\d{4}[ ]--[ ]\\d{2}[:]\\d{2}.*");
        Matcher m = p.matcher(serverMessage);
        return m.matches();
    }

    // extracts the time signature from the message
    private static String[] getTime(String serverMessage) {
        String fullTimeSegment = serverMessage.split(" :", 2)[1];
        String timeSegment = fullTimeSegment.replace("-- ", "");

        // {day, month, date, year, time, region}
        return timeSegment.split(" ");
    }

    // outputs the time to the server
    private static void timeOutput(String serverMessage) {
        // checks if the server message contains a time code
        if (containsTime(serverMessage)) {
            String[] time = getTime(serverMessage);
            writeTextCommand(Command.PRIVMSG, String.format("It is %s %s on %s the %s%s of %s %s", time[4], time[5], time[0], time[2], ordinalSuffix(Integer.parseInt(time[2])), time[1], time[3]));
            timeRequested = false;
        }
    }

    // determines the correct ordinal suffix for the day
    private static String ordinalSuffix(int number) {
        int tenRemainder = number % 10;
        String suffix;

        switch (tenRemainder) {
            case 1:
                suffix = "st";
                break;
            case 2:
                suffix = "nd";
                break;
            case 3:
                suffix = "rd";
                break;
            default:
                suffix = "th";
        }

        // edge cases
        switch (number) {
            case 11:
            case 12:
            case 13:
                suffix = "th";
            default:
        }

        return suffix;
    }

    // joins the given channel
    private static void joinChannel(String channel) {
        String channelSignature = channelSignatureFormatter(channel);

        // checks if the channel has already been joined
        if (channels.contains(channelSignature)) {
            writeTextCommand(Command.PRIVMSG, String.format("I'm already in %s!", channelSignature));
        } else {
            currentChannel = channel;
            writeCommand(Command.JOIN, channelSignature);
            channels.add(channelSignature);
            writeTextCommand(Command.PRIVMSG, String.format("Hi! I'm %s, but you can call me %s. If you want to learn more about what I can do type \"!help\". Let's get along!", DEFAULT_REAL_NAME, nick));
        }
    }

    // leaves the given channel
    private static void leaveChannel(String channel) {
        String channelSignature = channelSignatureFormatter(channel);

        // checks the bot is in the channel it's being asked to leave
        if (channels.contains(channelSignature)) {
            writeCommand(Command.PART, channelSignature);
            channels.remove(channelSignature);
        } else {
            writeTextCommand(Command.PRIVMSG, String.format("I am not in %s", channelSignature));
        }
    }

    // disconnects from the server
    private static void quitServer() {
        writeCommand(Command.QUIT);
    }

    // sends a command to the server
    private static void writeCommand(Command command, String message) {
        String fullMessage = String.format("%s %s\r%n", command.getLabel(), message);
        System.out.printf(">>> %s%n", fullMessage);
        out.print(fullMessage);
        out.flush();
    }

    // sends a command to the server with no attached text
    private static void writeCommand(Command command) {
        writeCommand(command, "");
    }

    // sends a command to the server on the current channel
    private static void writeTextCommand(Command command, String message) {
        writeCommand(command, String.format("#%s :%s", currentChannel, message));
    }

    // =============================================================================
    // COMMAND METHODS
    // =============================================================================

    // lists all the available commands
    private static void helpCommand(String serverMessage) {
        if (serverMessage.contains("!help")) {
            getChannel(serverMessage);
            writeTextCommand(Command.PRIVMSG, "Here's a list of my commands:");
            writeTextCommand(Command.PRIVMSG, "!join <channel> - this will make me join a channel of your choosing! Yay new friends!");
            writeTextCommand(Command.PRIVMSG, "!leave <channel> - this will remove me from a channel of your choosing, if no channel is given it will remove me from the current channel");
            writeTextCommand(Command.PRIVMSG, "!rename <newname> - this will rename me to whatever you choose, please be nice!");
            writeTextCommand(Command.PRIVMSG, "!settopic <topic> - this will change the channel's topic to your input");
            writeTextCommand(Command.PRIVMSG, "!time - this will return the current time on the server");
            writeTextCommand(Command.PRIVMSG, "!story - this will make me generate a random short story idea");
            writeTextCommand(Command.PRIVMSG, "!backstory - this will make me generate a random tragic backstory");
            writeTextCommand(Command.PRIVMSG, "!holstart - this will start a game of higher or lower! I can only run one game at a time so if another person is playing please wait your turn :)");
            writeTextCommand(Command.PRIVMSG, "!holguess <number> - this will make a guess in a game of higher or lower if the current player enters a positive whole number");
            writeTextCommand(Command.PRIVMSG, "!holstop - this can be used by the current player to stop a game of higher or lower prematurely");
            writeTextCommand(Command.PRIVMSG, "!quit - this will kick me from the server");
            commandIssued = true;
        }
    }

    // tells the bot to join the given channel
    private static void joinCommand(String serverMessage) {
        if (serverMessage.contains("!join ")) {
            // extracts the channel to join from the server message
            String channelSegment = serverMessage.split("!join ", 2)[1];
            String channelFullName = channelSegment.split(" ")[0];
            String channelName = channelFullName.replace("#", "");

            getChannel(serverMessage);
            joinChannel(channelName);
            commandIssued = true;
        }
    }

    // tells the bot to leave the given channel
    private static void leaveCommand(String serverMessage) {
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
            commandIssued = true;
        }
    }

    // changes the bots nickname to the provided field
    private static void renameCommand(String serverMessage) {
        if (serverMessage.contains("!rename ")) {
            // extracts the new nickname from the server message
            String nameSegment = serverMessage.split("!rename ", 2)[1];
            nick = nameSegment.split(" ")[0];

            writeCommand(Command.NICK, nick);
            commandIssued = true;
        }
    }

    // tells the bot to update the channel topic
    private static void setTopicCommand(String serverMessage) {
        if (serverMessage.contains("!settopic ")) {
            getChannel(serverMessage);

            // splits out the new topic
            String topicSegment = serverMessage.split("!settopic ", 2)[1];

            writeTextCommand(Command.TOPIC, topicSegment);
            commandIssued = true;
        }
    }

    // asks the bot for the time
    private static void timeCommand(String serverMessage) {
        if (serverMessage.contains("!time")) {
            getChannel(serverMessage);
            writeCommand(Command.TIME);
            timeRequested = true;
            commandIssued = true;
        }
    }

    // tells the bot to generate a short story synopsis
    private static void storyCommand(String serverMessage) {
        if (serverMessage.contains("!story")) {
            getChannel(serverMessage);
            writeTextCommand(Command.PRIVMSG, randomStoryGen.makeStory());
            commandIssued = true;
        }
    }

    // tells the bot to generate a tragic backstory
    private static void backstoryCommand(String serverMessage) {
        if (serverMessage.contains("!backstory")) {
            getChannel(serverMessage);
            writeTextCommand(Command.PRIVMSG, randomStoryGen.makeTragicBackstory());
            commandIssued = true;
        }
    }

    // starts a game of higher or lower
    private static void higherOrLowerCommand(String serverMessage) {
        if (serverMessage.contains("!holstart")) {
            // checks a game isn't already ongoing
            if (higherOrLower == null) {
                getChannel(serverMessage);
                String user = getUser(serverMessage);
                higherOrLower = new HigherOrLower(currentChannel, user);

                writeTextCommand(Command.PRIVMSG, "I'm thinking of a whole number between 1 and 100. Guess what it is!");
            } else {
                writeTextCommand(Command.PRIVMSG, String.format("%s is currently playing a game in #%s, please wait for them to finish or quit", higherOrLower.getPlayer(), higherOrLower.getChannel()));
            }
            commandIssued = true;
        }
    }

    // makes a guess in a game of higher or lower
    private static void higherOrLowerGuess(String serverMessage) {
        if (serverMessage.contains("!holguess ")) {
            // checks a game is running
            if (higherOrLower != null) {
                getChannel(serverMessage);
                String player = getUser(serverMessage);
                String numString = serverMessage.split("!holguess ", 2)[1];

                // ensures a valid number has been input
                try {
                    int guess = Integer.parseInt(numString);

                    if (guess >= 1 && guess <= 100) {
                        String guessResponse = higherOrLower.makeGuess(currentChannel, player, guess);
                        writeTextCommand(Command.PRIVMSG, guessResponse);

                        // ends the game if player makes a successful guess
                        if (guessResponse.contains("Thanks for playing!")) {
                            higherOrLower = null;
                        }
                    } else {
                        writeTextCommand(Command.PRIVMSG, String.format("Guesses must be between 1 and 100, %d is not between 1 and 100, please guess again", guess));
                    }
                } catch (NumberFormatException nfe) {
                    writeTextCommand(Command.PRIVMSG, String.format("%s is not a valid whole number format, please guess again", numString));
                }
            } else {
                writeTextCommand(Command.PRIVMSG, "No game of higher or lower is being played");
            }
            commandIssued = true;
        }
    }

    // stops the currently running game of higher or lower
    private static void higherOrLowerStopCommand(String serverMessage) {
        if (serverMessage.contains("!holstop")) {
            // checks a game is running
            if (higherOrLower != null) {
                // checks the player is the one wanting to stop the game
                if (getUser(serverMessage).equals(higherOrLower.getPlayer())) {
                    writeTextCommand(Command.PRIVMSG, String.format("%s has stopped their game of higher or lower", higherOrLower.getPlayer()));
                    higherOrLower = null;
                } else {
                    writeTextCommand(Command.PRIVMSG, String.format("Only the current player, %s, can end their game", higherOrLower.getPlayer()));
                }
            } else {
                writeTextCommand(Command.PRIVMSG, "No game of higher or lower is being played");
            }
            commandIssued = true;
        }
    }

    // tells the bot to disconnect from the server
    private static void quitCommand(String serverMessage) {
        if (serverMessage.contains("!quit")) {
            quitServer();
            commandIssued = true;
        }
    }

    // =============================================================================
    // EASTER EGGS
    // =============================================================================

    private static void easterEggs(String serverMessage) {
        String message = serverMessage.toLowerCase();
        String dadJokeFormat = "Hi %s! I'm %s";

        if (message.contains("hello there")) {
            getChannel(serverMessage);
            writeTextCommand(Command.PRIVMSG, "General Kenobi! You are a bold one");
        }

        if (message.contains("crusade") || message.contains("crusading")) {
            getChannel(serverMessage);
            writeTextCommand(Command.PRIVMSG, "DEUS VULT! DEUS VULT! DEUS VULT! DEUS VULT!");
        }

        if (message.contains("begone bot")) {
            getChannel(serverMessage);
            writeTextCommand(Command.PRIVMSG, "You don't have to be so mean about it. Goodbye :(");
            quitServer();
        }

        if (message.contains(" i'm ")) {
            getChannel(serverMessage);
            String objectSegment = message.split(" i'm ")[1];
            String object = objectSegment.split(" ", 2)[0];
            writeTextCommand(Command.PRIVMSG, String.format(dadJokeFormat, object, nick));
        } else if (message.contains(":i'm ")) {
            getChannel(serverMessage);
            String objectSegment = message.split(":i'm ")[1];
            String object = objectSegment.split(" ", 2)[0];
            writeTextCommand(Command.PRIVMSG, String.format(dadJokeFormat, object, nick));
        } else if (message.contains(" im ")) {
            getChannel(serverMessage);
            String objectSegment = message.split(" im ")[1];
            String object = objectSegment.split(" ", 2)[0];
            writeTextCommand(Command.PRIVMSG, String.format(dadJokeFormat, object, nick));
        } else if (message.contains(":im ")) {
            getChannel(serverMessage);
            String objectSegment = message.split(":im ")[1];
            String object = objectSegment.split(" ", 2)[0];
            writeTextCommand(Command.PRIVMSG, String.format(dadJokeFormat, object, nick));
        }
    }
}
