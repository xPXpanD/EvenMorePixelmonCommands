package rs.expand.pixelupgrade.utilities;

import java.util.ArrayList;

public class GetPokemonInfo
{
    private static GetPokemonInfo instance = new GetPokemonInfo();
    public static GetPokemonInfo getInstance()
    {   return instance;    }

    public static String getGenderCharacter(int genderNum)
    {
        String genderName = "";

        switch (genderNum)
        {
            case 0: genderName = "\u2642"; break;
            case 1: genderName = "\u2640"; break;
            case 2: genderName = "\u26A5"; break;
        }

        return genderName;
    }

    public static String getGrowthName(int growthNum)
    {
        String growthName = "";

        switch (growthNum)
        {
            case 0: growthName = "PygmyY"; break;
            case 1: growthName = "Runt"; break;
            case 2: growthName = "Small"; break;
            case 3: growthName = "Ordinary"; break;
            case 4: growthName = "Huge"; break;
            case 5: growthName = "Giant"; break;
            case 6: growthName = "Enormous"; break;
            case 7: growthName = "\u00A7cGinormous"; break;
            case 8: growthName = "\u00A7aMicroscopic"; break;
        }

        return growthName;
    }

    public static ArrayList<String> getNatureStrings(int natureNum, String configSpAtk, String configSpDef, String configSpeed)
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
                minusVal = "-" + configSpeed;
                break;
            case 7:
                natureName = "Adamant";
                plusVal = "+Atk";
                minusVal = "-" + configSpAtk;
                break;
            case 8:
                natureName = "Naughty";
                plusVal = "+Atk";
                minusVal = "-" + configSpDef;
                break;
            case 9:
                natureName = "Bold";
                plusVal = "+Def";
                minusVal = "-Atk";
                break;
            case 10:
                natureName = "Relaxed";
                plusVal = "+Def";
                minusVal = "-" + configSpeed;
                break;
            case 11:
                natureName = "Impish";
                plusVal = "+Def";
                minusVal = "-" + configSpAtk;
                break;
            case 12:
                natureName = "Lax";
                plusVal = "+Def";
                minusVal = "-" + configSpDef;
                break;
            case 13:
                natureName = "Timid";
                plusVal = "+" + configSpeed;
                minusVal = "-Atk";
                break;
            case 14:
                natureName = "Hasty";
                plusVal = "+" + configSpeed;
                minusVal = "-Def";
                break;
            case 15:
                natureName = "Jolly";
                plusVal = "+" + configSpeed;
                minusVal = "-" + configSpAtk;
                break;
            case 16:
                natureName = "Naive";
                plusVal = "+" + configSpeed;
                minusVal = "-" + configSpDef;
                break;
            case 17:
                natureName = "Modest";
                plusVal = "+" + configSpAtk;
                minusVal = "-Atk";
                break;
            case 18:
                natureName = "Mild";
                plusVal = "+" + configSpAtk;
                minusVal = "-Def";
                break;
            case 19:
                natureName = "Quiet";
                plusVal = "+" + configSpAtk;
                minusVal = "-" + configSpeed;
                break;
            case 20:
                natureName = "Rash";
                plusVal = "+" + configSpAtk;
                minusVal = "-" + configSpDef;
                break;
            case 21:
                natureName = "Calm";
                plusVal = "+" + configSpDef;
                minusVal = "-Atk";
                break;
            case 22:
                natureName = "Gentle";
                plusVal = "+" + configSpDef;
                minusVal = "-Def";
                break;
            case 23:
                natureName = "Sassy";
                plusVal = "+" + configSpDef;
                minusVal = "-" + configSpeed;
                break;
            case 24:
                natureName = "Careful";
                plusVal = "+" + configSpDef;
                minusVal = "-" + configSpAtk;
                break;
        }

        ArrayList<String> returnString = new ArrayList<>();
        returnString.add(natureName);
        returnString.add(plusVal);
        returnString.add(minusVal);
        return returnString;
    }
}
