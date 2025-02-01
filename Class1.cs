using System;
using StardewModdingAPI;

namespace WaidFirstMod
{
    public class ModEntry : Mod
    {
        public override void Entry(IModHelper helper)
        {
            this.Monitor.Log("Марина привет я мод я тебя люблю йоу!", LogLevel.Info);
        }
    }
}