package irc;

import java.util.Random;

public class DiceRoller {

    private final Random rand = new Random();

    private enum Dice {
        D4(4),
        D6(6),
        D8(8),
        D10(10),
        D12(12),
        D20(20),
        D100(100);

        private final int sides;

        Dice(int sides) {
            this.sides = sides;
        }

        private int getSides() {
            return sides;
        }
    }

    public int[] roll(int number, int sides) {
        Dice die;

        switch (sides) {
            case 4:
                die = Dice.D4;
                break;
            case 6:
                die = Dice.D6;
                break;
            case 8:
                die = Dice.D8;
                break;
            case 10:
                die = Dice.D10;
                break;
            case 12:
                die = Dice.D12;
                break;
            case 20:
                die = Dice.D20;
                break;
            case 100:
                die = Dice.D100;
                break;
            default:
                return new int[] {};
        }

        int[] rolls = new int[number];

        for (int i = 0; i < number; i++) {
            rolls[i] = rand.nextInt(die.getSides()) + 1;
        }

        return rolls;
    }
}
