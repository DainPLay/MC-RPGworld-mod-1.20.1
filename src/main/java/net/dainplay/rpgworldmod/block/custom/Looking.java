package net.dainplay.rpgworldmod.block.custom;

import net.minecraft.util.StringRepresentable;

public enum Looking implements StringRepresentable {
   LEFT("left"),
   STRAIGHT("straight"),
   RIGHT("right");

   private final String name;

   private Looking(String pName) {
      this.name = pName;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}