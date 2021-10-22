package net.bteuk.uk121;

public class ConfigVariables {

    //Default values
    public static int yMin = -512;
    public static int height = 2544;
    public static int seaLevel = 0;

    public ConfigVariables(){
        Config config = new Config();
        config.load();
        yMin = config.yMin;
        height = config.height;
        seaLevel = config.seaLevel;
    }
}
