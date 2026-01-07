package net.dainplay.rpgworldmod.gui;


/*
    Class manages the calculations required to determine the correct color(s) to use
 */
public class HeartsBar
{

    public static HeartIcon[] calculateHeartIcons(int playerArmorValue)
    {
        HeartIcon[] armorIcons = new HeartIcon[playerArmorValue/2+playerArmorValue%2];

            int counter = playerArmorValue;
        for (int i = 0; i < playerArmorValue/2+playerArmorValue%2; i++)
        {
            armorIcons[i] = new HeartIcon();
            if (counter >= 2)
            {
                //We have at least a full icon to show
                armorIcons[i].heartIconType = HeartIcon.Type.FULL;
                counter -= 2;
            } else if (counter == 1)
            {
                //We have a half icon to show
                armorIcons[i].heartIconType = HeartIcon.Type.HALF;
                counter -= 1;
            } else
            {
                //Empty icon
                armorIcons[i].heartIconType = HeartIcon.Type.NONE;
            }
        }

        return armorIcons;
    }
}
