package net.dainplay.rpgworldmod.gui;


/*
    Class manages the calculations required to determine the correct color(s) to use
 */
public class MossBar
{

    public static MossIcon[] calculateMossIcons(int playerArmorValue)
    {
        MossIcon[] armorIcons = new MossIcon[10];

        //Calculate which color scale to use
        int scale = playerArmorValue / 20;

        //Scale the value down for each position
        int counter = playerArmorValue - (scale * 20);

        //Handle exact wrap around situation
        if(scale > 0 && counter == 0)
        {
            counter = 20;
        }

        for (int i = 0; i < 10; i++)
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
