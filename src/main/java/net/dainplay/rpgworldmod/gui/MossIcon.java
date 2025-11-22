package net.dainplay.rpgworldmod.gui;

/*
    Class wraps the information required to draw an individual armor icon
 */
public class MossIcon
{
    public Type mossIconType;


    public MossIcon()
    {
        mossIconType = Type.NONE;
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
