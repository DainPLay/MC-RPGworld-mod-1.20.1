package net.dainplay.rpgworldmod.gui;

public class ManaIcon
{
    public Type manaIconType;


    public ManaIcon()
    {
        manaIconType = Type.NONE;
    }

    public enum Type
    {
        NONE,
        ONE,
        TWO,
        THREE,
        FOUR,
        FULL
    }
}
