package irc;

import java.util.Random;

public class RandomStoryGen {

    // types of quests
    private enum QuestType {
        SOLO, MULTI_MALE, MULTI_FEMALE
    }

    // genders
    private enum CharacterGender {
        MALE, FEMALE
    }

    private final Random rand = new Random();

    // arrays of all interchangeable elements for the story
    private final String[] maleNames = {"Callum", "Reginald", "Richard", "James", "John", "Alexander", "Harold", "Russell",
            "Myles", "Dominic", "Tommy", "Tommen", "Jamal", "Clark", "Ronald", "Ron", "Nicolas", "Jeremy", "Alistair",
            "Luke", "Han", "Anakin", "Haris", "Ibrahim", "Abdullah", "Kyle", "Gary", "Finn", "Hugh", "Theo", "Mike"};
    private final String[] femaleNames = {"Emma", "Olivia", "Ava", "Isabella", "Sophia", "Charlotte", "Mia", "Amelia",
            "Evelyn", "Abigail", "Emily", "Elizabeth", "Mila", "Ella", "Sofia", "Harriett", "Aria", "Brie", "Scarlett",
            "Victoria", "Luna", "Hermione", "Leia", "Katie", "Cho", "Ginny", "Ashley", "Megan", "Maya", "Simone", "Liv"};
    private final String[] surnames = {"Sharpe", "Solo", "Skywalker", "Duststrider", "Tailor", "Thatcher", "Smith",
            "Highwatcher", "Wazowski", "Buckbard", "Weasley", "Urbet", "Potter", "Morales", "Bercow", "Lovegood",
            "Fletcher", "Flynn", "Stark", "Dashwood", "O'Hara", "Trask", "Jones", "Hamilton", "Price", "Kent", "Bond"};
    private final String[] places = {"Hogwarts", "The Emerald City", "Metropolis", "Asgard", "The Shire", "Rivendell",
            "Elfinwood", "Pantora", "Kanto", "Sinnoh", "Dragonstone", "Rylath", "Ithoria", "Aeos", "Chandrilla", "Illandia",
            "Iridonia", "Griffonia", "Mimbana", "Pandora", "Hadfield", "The lonely Mountain", "Mount Doom", "Castle Blackfrost"};
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
            "Captain", "Archpriest", "Minister", "Cardinal", "Chief", "Director", "Patriarch", "Professor", "Chairman",
            "Chancellor", "Marquis", "Count", "Earl", "Duke", "Baron", "Viscount"};
    private final String[] femaleTitles = {"Queen", "Princess", "Dame", "Archmage", "Lady", "Mayor", "General", "Admiral",
            "Captain", "Archpriest", "Minister", "Cardinal", "Chief", "Director", "Matriarch", "Professor", "Chairman",
            "Chancellor", "Marquess", "Countess", "Duchess", "Baroness", "Viscountess"};
    private final String[] maleJobs = {"Nurse", "Knight", "Blacksmith", "Mage", "Wizard", "Doctor", "Hunter", "Rogue",
            "Thief", "Warrior", "Ogre", "Fisherman", "Butcher", "Carpenter", "Chef", "Priest", "Monk", "Detective",
            "Soldier", "Alchemist", "Warlock", "Archer", "Musketeer", "Lumberjack", "Mercenary", "Cook", "Gardener"};
    private final String[] femaleJobs = {"Nurse", "Knight", "Blacksmith", "Mage", "Witch", "Doctor", "Hunter", "Rogue",
            "Thief", "Warrior", "Ogre", "Fisherman", "Butcher", "Carpenter", "Chef", "Priestess", "Nun", "Detective",
            "Soldier", "Alchemist", "Warlock", "Archer", "Musketeer", "Lumberjack", "Mercenary", "Cook", "Gardener"};
    private final String[] tragicBackstory = {"villain raped and pillaged their way through their village",
            "a dragon burnt down their home", "villain abducted them for evil science experiments", "their best friend called them smelly",
            "villain killed their parents but failed to kill them due to a prophecy", "they were sold into slavery to villain",
            "villain tricked them into doing something dangerous, leaving them hideously deformed", "they contracted a rare and untreatable disease"};

    // generates a short story synopsis
    public String makeStory() {
        CharacterGender gender = randomiseGender();
        QuestType quest = randomiseQuestType();

        String story;
        String characterSegment;
        String questSegment;

        // fills the main character section based on their randomised gender
        if (gender == CharacterGender.MALE) {
            // selects random details for a male character
            characterSegment = String.format("%s %s, a %s", getRandom(maleNames), getRandom(surnames), getRandom(maleJobs));
        } else {
            // selects random details for a female character
            characterSegment = String.format("%s %s, a %s", getRandom(femaleNames), getRandom(surnames), getRandom(femaleJobs));
        }
        // selects a random origin location for the character
        characterSegment = String.format("%s from %s", characterSegment, getRandom(places));

        // selects a random destination for the character
        questSegment = String.format("%s where they plan to", getRandom(places));
        // selects random details for a target character
        String genderedEnd = String.format("%s of %s", getRandom(surnames), getRandom(places));
        // fills the quest segment based on the randomised quest type
        switch (quest) {
            case MULTI_MALE:
                // selects random details for a quest with a male target character
                questSegment = String.format("%s %s %s %s %s", questSegment, getRandom(multiMaleQuests), getRandom(maleTitles), getRandom(maleNames), genderedEnd);
                break;
            case MULTI_FEMALE:
                // selects random details for a quest with a female target character
                questSegment = String.format("%s %s %s %s %s", questSegment, getRandom(multiFemaleQuests), getRandom(femaleTitles), getRandom(femaleNames), genderedEnd);
                break;
            default:
                // selects random details for a quest with no target character
                questSegment = String.format("%s %s", questSegment, getRandom(quests));
        }

        // finalises the story
        story = String.format("Our story begins with %s on their journey to %s...", characterSegment, questSegment);
        story = insertMacguffin(story);
        return story;
    }

    // generates a tragic backstory
    public String makeTragicBackstory() {
        CharacterGender characterGender = randomiseGender();
        CharacterGender villainGender = randomiseGender();
        int age = randomChildAge();

        String story;
        String characterSegment;
        String villainSegment;

        // selects a random name for the main depending on their gender
        characterSegment = (characterGender == CharacterGender.MALE) ? getRandom(maleNames) : getRandom(femaleNames);
        characterSegment = String.format("%s %s", characterSegment, getRandom(surnames));

        // populates random details for a villain
        if (villainGender == CharacterGender.MALE) {
            // random details for a male villain
            villainSegment = String.format("%s %s", getRandom(maleTitles), getRandom(maleNames));
        } else {
            // random details for a female villain
            villainSegment = String.format("%s %s", getRandom(femaleTitles), getRandom(femaleNames));
        }
        villainSegment = String.format("%s %s of %s", villainSegment, getRandom(surnames), getRandom(places));

        // finalises the backstory
        story = String.format("At the tender age of %d, %s was ripped from their comfortable life when %s...", age, characterSegment, getRandom(tragicBackstory));
        story = insertDastardlyVillain(story, villainSegment);
        return story;
    }

    // randomises the gender for the main character
    private CharacterGender randomiseGender() {
        int percentage = rand.nextInt(100);
        return percentage < 50 ? CharacterGender.MALE : CharacterGender.FEMALE;
    }

    // randomises the type of quest the main character is going on
    private QuestType randomiseQuestType() {
        int percentage = rand.nextInt(100);
        if (percentage < 50) return QuestType.SOLO;
        return percentage < 75 ? QuestType.MULTI_MALE : QuestType.MULTI_FEMALE;
    }

    // selects a random entry from the give array
    private String getRandom(String[] strings) {
        int selected = rand.nextInt(strings.length);
        return strings[selected];
    }

    private int randomChildAge() {
        return rand.nextInt(14) + 1;
    }

    // chooses a random macguffin for the story
    private String insertMacguffin(String story) {
        return story.replace("macguffin", getRandom(macguffins));
    }

    // inserts the villains details
    private String insertDastardlyVillain(String story, String villain) {
        return story.replace("villain", villain);
    }
}
