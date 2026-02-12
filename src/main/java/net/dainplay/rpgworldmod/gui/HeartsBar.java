package net.dainplay.rpgworldmod.gui;


/*
    Class manages the calculations required to determine the correct color(s) to use
 */
public class HeartsBar
{

    public static HeartIcon[] calculateHeartIcons(int value)
    {
        HeartIcon[] icons = new HeartIcon[value/2+value%2];

            int counter = value;
        for (int i = 0; i < value/2+value%2; i++)
        {
            icons[i] = new HeartIcon();
            if (counter >= 2)
            {
                //We have at least a full icon to show
                icons[i].heartIconType = HeartIcon.Type.FULL;
                counter -= 2;
            } else if (counter == 1)
            {
                //We have a half icon to show
                icons[i].heartIconType = HeartIcon.Type.HALF;
                counter -= 1;
            } else
            {
                //Empty icon
                icons[i].heartIconType = HeartIcon.Type.NONE;
            }
        }

        return icons;
    }
}
