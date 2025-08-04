package net.bteuk.uk121.mod.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.bteuk.uk121.mod.ConfigVariables;
import net.bteuk.uk121.util.CoordinateParseUtils;
import net.bteuk.uk121.util.LatLng;
import net.bteuk.uk121.Projections.ModifiedAirocean;
import net.bteuk.uk121.minecraft.surfacebuilder.BlockAPICall;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Tpll {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tpll")
                .then(argument("coordinates", greedyString())
                        .executes(ctx -> run(ctx.getSource(), getString(ctx, "coordinates"))))
                .executes(ctx -> run(ctx.getSource(), null)));
    }

    public static int run(ServerCommandSource source, String coordinates) {

        //If the command sender is not a player it will throw an exception.
        final PlayerEntity self;
        try {
            self = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendError(Text.of("This command can only be executed by a player!"));
            return 1;
        }

        if (coordinates == null) {
            usage(self);
            return 1;
        }

        String[] args = coordinates.split(" ");

        switch (args.length)
        {
            case 0:
            case 1:
                usage(self);
                return 1;
        }

        double altitude = Double.NaN;
        LatLng defaultCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(args).trim());

        if (defaultCoords == null) {
            LatLng possiblePlayerCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(selectArray(args, 1)));
            if (possiblePlayerCoords != null) {
                defaultCoords = possiblePlayerCoords;
            }
        }

        LatLng possibleHeightCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(inverseSelectArray(args, args.length - 1)));
        if (possibleHeightCoords != null) {
            defaultCoords = possibleHeightCoords;
            try {
                altitude = Double.parseDouble(args[args.length - 1]);
            } catch (Exception e) {
                e.printStackTrace();
                altitude = Double.NaN;
            }
        }

        LatLng possibleHeightNameCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(inverseSelectArray(selectArray(args, 1), selectArray(args, 1).length - 1)));
        if (possibleHeightNameCoords != null) {
            defaultCoords = possibleHeightNameCoords;
            try {
                altitude = Double.parseDouble(selectArray(args, 1)[selectArray(args, 1).length - 1]);
            } catch (Exception e) {
                e.printStackTrace();
                altitude = Double.NaN;
            }
        }

        if (defaultCoords == null) {
            usage(self);
            return 1;
        }

        double[] proj;

        ModifiedAirocean projection = new ModifiedAirocean();
        proj = projection.fromGeo(defaultCoords.getLng(), defaultCoords.getLat());

        if (Double.isNaN(altitude)) {
            //Get world from registry
            World world = MinecraftClient.getInstance().world;
            BlockPos loc = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(proj[0], 0, proj[1]));
            altitude = loc.getY();

            if (altitude == ConfigVariables.yMin){
                altitude = BlockAPICall.getTileAndHeightForXZ((int) proj[0], (int) proj[1]);
            }
        }

        LatLng finalDefaultCoords = defaultCoords;

        self.sendMessage(Text.of("Teleporting to " + formatDecimal(finalDefaultCoords.getLat()) + ", " + formatDecimal(finalDefaultCoords.getLng())), false);
        self.teleport(proj[0], altitude, proj[1]);

        return 1;

    }

    private static void usage(PlayerEntity self) {
        self.sendMessage(Text.of("/tpll <latitude> <longitude> [altitude]"), false);
    }

    private static String getRawArguments(String[] args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1) {
            return args[0];
        }

        StringBuilder arguments = new StringBuilder(args[0].replace((char) 176, (char) 32).trim());

        for (int x = 1; x < args.length; x++) {
            arguments.append(" ").append(args[x].replace((char) 176, (char) 32).trim());
        }

        return arguments.toString();
    }

    private static String formatDecimal(double val) {
        return new DecimalFormat("##.#####").format(val);
    }

    /**
     * Gets all objects in a string array above a given index
     *
     * @param args  Initial array
     * @param index Starting index
     * @return Selected array
     */
    private static String[] selectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for (int i = index; i < args.length; i++) {
            array.add(args[i]);
        }

        return array.toArray(array.toArray(new String[array.size()]));
    }

    private static String[] inverseSelectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            array.add(args[i]);
        }

        return array.toArray(array.toArray(new String[array.size()]));

    }
}
