package irc;

import java.io.PrintWriter;
import java.util.Random;

public class RandomStoryGen {

    private enum QuestType {
        SOLO, MULTI_MALE, MULTI_FEMALE
    }

    private enum CharacterGender {
        MALE, FEMALE
    }

    private final String[] maleNames = {"Callum", "Reginald", "Richard", "James", "John", "Alexander", "Harold", "Russell",
            "Myles", "Dominic", "Tommy", "Tommen", "Jamal", "Clark", "Ronald", "Ron", "Nicolas", "Jeremy", "Alistair",
            "Luke", "Han", "Anakin", "Haris", "Ibrahim", "Abdullah", "Kyle", "Gary", "Finn", "Hugh", "Theo", "Mike"};
    private final String[] femaleNames = {"Emma", "Olivia", "Ava", "Isabella", "Sophia", "Charlotte", "Mia", "Amelia",
            "Evelyn", "Abigail", "Emily", "Elizabeth", "Mila", "Ella", "Sofia", "Harriett", "Aria", "Brie", "Scarlett",
            "Victoria", "Luna", "Hermione", "Leia", "Katie", "Cho", "Ginny", "Ashley", "Megan", "Maya", "Simone", "Liv"};
    private final String[] surnames = {"Sharpe", "Solo", "Skywalker", "Duststrider", "Tailor", "Thatcher", "Smith",
            "Highwatcher", "Hunter", "Buckbard", "Weasley", "Urbet", "Potter", "Morales", "Bercow", "Lovegood",
            "Fletcher", "Flynn", "Stark", "Dashwood", "O'Hara", "Trask", "Jones", "Hamilton", "Price", "Kent", "Bond"};
    private final String[] places = {"Hogwarts", "The Emerald City", "Metropolis", "Asgard", "The Shire", "Rivendell",
            "Elfinwood", "Pantora", "Kanto", "Sinnoh", "Dragonstone", "Rylath", "Ithoria", "Aeos", "Chandrilla", "Illandia",
            "Iridonia", "Griffonia", "Mimbana", "Pandora", "Hadfield", "The lonely Mountain", "Mount Ruen", "Castle Blackfrost"};
    private final String[] quests = {"overthrow The Empire", "seek the macguffin", "slay the dragon", "steal from the rich to give to the poor",
            "hide the macguffin", "destroy the macguffin", "steal the macguffin"};
    private final String[] multiMaleQuests = {"rescue", "elope with their husband", "guard", "extort", "blackmail",
            "kill their nemesis", "give the macguffin to", "save", "use the macguffin on", "seek revenge for the murder of"};
    private final String[] multiFemaleQuests = {"rescue the", "elope with their wife", "guard", "extort", "blackmail",
            "kill their nemesis", "give the macguffin to", "save", "use the macguffin on", "seek revenge for the murder of"};
    private final String[] macguffins = {"Holy Grail", "Magical Sword", "One Ring", "Ancient Sarcophagus", "Gem of Power",
            "Infinity Stones", "Book of the Dead", "Briefcase", "Ark of the Covenant", "Rogue Construct", "Secret Plans",
            "Egg", "Suitcase", "Sacred Texts", "Cursed Chalice", "Magic Staff"};
    private final String[] maleTitles = {"King", "Prince", "Sir", "Archmage", "Lord", "Mayor", "General", "Admiral",
            "Captain", "Archpriest", "Minister", "Cardinal", "Chief", "Director", "Patriarch", "Professor", "Chairman"};
    private final String[] femaleTitles = {"Queen", "Princess", "Dame", "Archmage", "Lady", "Mayor", "General", "Admiral",
            "Captain", "Archpriest", "Minister", "Cardinal", "Chief", "Director", "Matriarch", "Professor", "Chairman"};
    private final String[] maleJobs = {"Nurse", "Knight", "Blacksmith", "Mage", "Wizard", "Doctor", "Hunter", "Rogue",
            "Thief", "Warrior", "Ogre", "Fisherman", "Butcher", "Carpenter", "Chef", "Priest", "Monk", "Detective",
            "Soldier", "Alchemist", "Warlock", "Archer", "Musketeer", "Lumberjack", "Mercenary", "Cook", "Gardener"};
    private final String[] femaleJobs = {"Nurse", "Knight", "Blacksmith", "Mage", "Witch", "Doctor", "Hunter", "Rogue",
            "Thief", "Warrior", "Ogre", "Fisherman", "Butcher", "Carpenter", "Chef", "Priestess", "Nun", "Detective",
            "Soldier", "Alchemist", "Warlock", "Archer", "Musketeer", "Lumberjack", "Mercenary", "Cook", "Gardener"};
    private final Random rand = new Random();

    private String story;

    public RandomStoryGen() {
        CharacterGender gender = randomiseGender();
        QuestType quest = randomiseQuestType();
        String characterSegment;
        String questSegment;

        if (gender == CharacterGender.MALE) {
            characterSegment = String.format("%s %s, a %s", getRandom(maleNames), getRandom(surnames), getRandom(maleJobs));
        } else {
            characterSegment = String.format("%s %s, a %s", getRandom(femaleNames), getRandom(surnames), getRandom(femaleJobs));
        }

        characterSegment = String.format("%s from %s", characterSegment, getRandom(places));
        questSegment = String.format("%s where they plan to", getRandom(places));

        String genderedEnd = String.format("%s of %s", getRandom(surnames), getRandom(places));
        switch (quest) {
            case MULTI_MALE:
                questSegment = String.format("%s %s %s %s %s", questSegment, getRandom(multiMaleQuests), getRandom(maleTitles), getRandom(maleNames), genderedEnd);
                break;
            case MULTI_FEMALE:
                questSegment = String.format("%s %s %s %s %s", questSegment, getRandom(multiFemaleQuests), getRandom(femaleTitles), getRandom(femaleNames), genderedEnd);
                break;
            default:
                questSegment = String.format("%s %s", questSegment, getRandom(quests));
        }

        questSegment = String.format("%s...", questSegment);

        story = String.format("Our story begins with %s on their journey to %s", characterSegment, questSegment);
        story = changeMacguffin(story);
    }

    public String getStory() {
        return story;
    }

    private CharacterGender randomiseGender() {
        int percentage = rand.nextInt(100);
        return percentage < 50 ? CharacterGender.MALE : CharacterGender.FEMALE;
    }

    private QuestType randomiseQuestType() {
        int percentage = rand.nextInt(100);
        if (percentage < 50) return QuestType.SOLO;
        return percentage < 75 ? QuestType.MULTI_MALE : QuestType.MULTI_FEMALE;
    }

    private String getRandom(String[] strings) {
        int selected = rand.nextInt(strings.length);
        return strings[selected];
    }

    private String changeMacguffin(String story) {
        return story.replace("macguffin", getRandom(macguffins));
    }
}
