package net.dainplay.rpgworldmod.block.custom;

import net.minecraft.util.StringRepresentable;

public enum Form implements StringRepresentable {
   SINGLE("single"),
   DOUBLE("double"),
   TRIPLE("triple");

   private final String name;

   private Form(String pName) {
      this.name = pName;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}