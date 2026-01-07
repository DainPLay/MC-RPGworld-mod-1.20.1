package net.dainplay.rpgworldmod.gui;

/*
    Class wraps the information required to draw an individual armor icon
 */
public class HeartIcon
{
    public Type heartIconType;


    public HeartIcon()
    {
        heartIconType = Type.NONE;
    }

    /*
        The type of armor icon to show.
     */
    public enum Type
    {
        NONE,
        HALF,
        FULL
    }
}
