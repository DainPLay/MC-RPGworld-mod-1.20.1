package net.dainplay.rpgworldmod.gui;


/*
    Class manages the calculations required to determine the correct color(s) to use
 */
public class StarsBar
{

    public static ManaIcon[] calculateStarsIcons(int playerManaValue)
    {
        ManaIcon[] armorIcons = new ManaIcon[playerManaValue/5+playerManaValue%5];

            int counter = playerManaValue;
        for (int i = 0; i < playerManaValue/5+playerManaValue%5; i++)
        {
            armorIcons[i] = new ManaIcon();

            if (counter >= 5)
            {
                armorIcons[i].manaIconType = ManaIcon.Type.FULL;
                counter -= 5;
            } else
            if (counter >= 4)
            {
                armorIcons[i].manaIconType = ManaIcon.Type.FOUR;
                counter -= 4;
            } else
            if (counter >= 3)
            {
                armorIcons[i].manaIconType = ManaIcon.Type.THREE;
                counter -= 3;
            } else if (counter >= 2)
            {
                armorIcons[i].manaIconType = ManaIcon.Type.TWO;
                counter -= 2;
            } else if (counter == 1)
            {
                armorIcons[i].manaIconType = ManaIcon.Type.ONE;
                counter -= 1;
            } else
            {
                armorIcons[i].manaIconType = ManaIcon.Type.NONE;
            }
        }

        return armorIcons;
    }
}
