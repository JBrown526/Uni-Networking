package irc;

import java.util.Random;

public class HigherOrLower {

    private final String channel;
    private final String player;
    private final int number;

    private int guesses;

    public HigherOrLower(String channel, String player) {
        this.channel = channel;
        this.player = player;

        Random rand = new Random();
        number = rand.nextInt(100);

        guesses = 0;
    }

    // processes a guess made by a player
    public String makeGuess(String channel, String player, int guess) {
        // makes sure it's being played in the correct channel
        if (!this.channel.equals(channel)) {
            return "A game is currently being played in #" + this.channel + " if you are the player, please play there";
        }
        // makes sure only the designated player can play
        if (!this.player.equals(player)) {
            return String.format("A game is currently being played by %s, please wait for them to finish first!", this.player);
        }
        // checks if the guess is greater than the number
        if (number < guess) {
            guesses++;
            return String.format("Incorrect! The number I'm thinking of is lower than %d. Guess again!", guess);
        }
        // checks if the guess is smaller than the number
        if (number > guess) {
            guesses++;
            return String.format("Incorrect! The number I'm thinking of is higher than %d. Guess again!", guess);
        }
        // if the player has made a correct guess
        return String.format("Correct! It took you %d to get it right. Thanks for playing!", guesses);
    }

    public String getPlayer() {
        return player;
    }

    public String getChannel() {
        return channel;
    }
}
