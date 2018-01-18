package rs.expand.pixelupgrade.utilities;

// Remote imports.
import java.util.ArrayList;

// Local imports.
import static rs.expand.pixelupgrade.PixelUpgrade.*;

public class GetPokemonInfo
{
    public static String getGenderCharacter(int genderNum)
    {
        switch (genderNum)
        {
            case 0: return "♂";
            case 1: return "♀";
            case 2: return "⚥";
            default: return "?";
        }
    }

    public static String getGender(int genderNum)
    {
        switch (genderNum)
        {
            case 0: return "male ";
            case 1: return "female ";
            default: return "";
        }
    }

    public static String getGrowthName(int growthNum)
    {
        switch (growthNum)
        {
            case 0: return "Pygmy";
            case 1: return "Runt";
            case 2: return "Small";
            case 3: return "Ordinary";
            case 4: return "Huge";
            case 5: return "Giant";
            case 6: return "Enormous";
            case 7: return "§cGinormous"; // Now with fancy red color!
            case 8: return "§aMicroscopic"; // Now with fancy green color!
            default: return "?";
        }
    }

    public static ArrayList<String> getNatureStrings(int natureNum)
    {
        String natureName = "", plusVal = "", minusVal = "";

        switch (natureNum)
        {
            case 0:
                natureName = "Hardy";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 1:
                natureName = "Serious";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 2:
                natureName = "Docile";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 3:
                natureName = "Bashful";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 4:
                natureName = "Quirky";
                plusVal = "+None";
                minusVal = "-None";
                break;
            case 5:
                natureName = "Lonely";
                plusVal = "+Atk";
                minusVal = "-Def";
                break;
            case 6:
                natureName = "Brave";
                plusVal = "+Atk";
                minusVal = "-" + shortenedSpeed;
                break;
            case 7:
                natureName = "Adamant";
                plusVal = "+Atk";
                minusVal = "-" + shortenedSpecialAttack;
                break;
            case 8:
                natureName = "Naughty";
                plusVal = "+Atk";
                minusVal = "-" + shortenedSpecialDefense;
                break;
            case 9:
                natureName = "Bold";
                plusVal = "+Def";
                minusVal = "-Atk";
                break;
            case 10:
                natureName = "Relaxed";
                plusVal = "+Def";
                minusVal = "-" + shortenedSpeed;
                break;
            case 11:
                natureName = "Impish";
                plusVal = "+Def";
                minusVal = "-" + shortenedSpecialAttack;
                break;
            case 12:
                natureName = "Lax";
                plusVal = "+Def";
                minusVal = "-" + shortenedSpecialDefense;
                break;
            case 13:
                natureName = "Timid";
                plusVal = "+" + shortenedSpeed;
                minusVal = "-Atk";
                break;
            case 14:
                natureName = "Hasty";
                plusVal = "+" + shortenedSpeed;
                minusVal = "-Def";
                break;
            case 15:
                natureName = "Jolly";
                plusVal = "+" + shortenedSpeed;
                minusVal = "-" + shortenedSpecialAttack;
                break;
            case 16:
                natureName = "Naive";
                plusVal = "+" + shortenedSpeed;
                minusVal = "-" + shortenedSpecialDefense;
                break;
            case 17:
                natureName = "Modest";
                plusVal = "+" + shortenedSpecialAttack;
                minusVal = "-Atk";
                break;
            case 18:
                natureName = "Mild";
                plusVal = "+" + shortenedSpecialAttack;
                minusVal = "-Def";
                break;
            case 19:
                natureName = "Quiet";
                plusVal = "+" + shortenedSpecialAttack;
                minusVal = "-" + shortenedSpeed;
                break;
            case 20:
                natureName = "Rash";
                plusVal = "+" + shortenedSpecialAttack;
                minusVal = "-" + shortenedSpecialDefense;
                break;
            case 21:
                natureName = "Calm";
                plusVal = "+" + shortenedSpecialDefense;
                minusVal = "-Atk";
                break;
            case 22:
                natureName = "Gentle";
                plusVal = "+" + shortenedSpecialDefense;
                minusVal = "-Def";
                break;
            case 23:
                natureName = "Sassy";
                plusVal = "+" + shortenedSpecialDefense;
                minusVal = "-" + shortenedSpeed;
                break;
            case 24:
                natureName = "Careful";
                plusVal = "+" + shortenedSpecialDefense;
                minusVal = "-" + shortenedSpecialAttack;
                break;
        }

        ArrayList<String> returnString = new ArrayList<>();
        returnString.add(natureName);
        returnString.add(plusVal);
        returnString.add(minusVal);
        return returnString;
    }
}
