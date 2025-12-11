package net.dainplay.rpgworldmod.gui;


/*
    Class manages the calculations required to determine the correct color(s) to use
 */
public class MossBar
{

    public static MossIcon[] calculateMossIcons(int playerArmorValue)
    {
        MossIcon[] armorIcons = new MossIcon[playerArmorValue/2+playerArmorValue%2];

            int counter = playerArmorValue;
        for (int i = 0; i < playerArmorValue/2+playerArmorValue%2; i++)
        {
            armorIcons[i] = new MossIcon();
            if (counter >= 2)
            {
                //We have at least a full icon to show
                armorIcons[i].mossIconType = MossIcon.Type.FULL;
                counter -= 2;
            } else if (counter == 1)
            {
                //We have a half icon to show
                armorIcons[i].mossIconType = MossIcon.Type.HALF;
                counter -= 1;
            } else
            {
                //Empty icon
                armorIcons[i].mossIconType = MossIcon.Type.NONE;
            }
        }

        return armorIcons;
    }
}
