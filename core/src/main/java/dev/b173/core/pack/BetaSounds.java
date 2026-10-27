package dev.b173.core.pack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class BetaSounds {
    public interface Resources {
        List<String> list() throws IOException;

        byte[] read(String path) throws IOException;
    }

    public static final List<String> MUSIC = List.of(
            "music/calm1.ogg", "music/calm2.ogg", "music/calm3.ogg",
            "newmusic/hal1.ogg", "newmusic/hal2.ogg", "newmusic/hal3.ogg", "newmusic/hal4.ogg",
            "newmusic/nuance1.ogg", "newmusic/nuance2.ogg",
            "newmusic/piano1.ogg", "newmusic/piano2.ogg", "newmusic/piano3.ogg");

    private static final String SILENT = "-";
    private static final String MUSIC_POOL = "@music";

    private static final String[] EVENTS = buildEvents();

    private static String[] buildEvents() {
        List<String> out = new ArrayList<>();
        String[][] types = {
                {"stone", "step.stone", "step.stone"}, {"wood", "step.wood", "step.wood"},
                {"gravel", "step.gravel", "step.gravel"}, {"grass", "step.grass", "step.grass"},
                {"wool", "step.cloth", "step.cloth"}, {"sand", "step.sand", "step.gravel"},
                {"glass", "step.stone", "random.glass"}, {"metal", "step.stone", "step.stone"},
                {"snow", "step.cloth", "step.cloth"}, {"ladder", "step.wood", "step.wood"},
                {"netherrack", "step.stone", "step.stone"}, {"soul_sand", "step.sand", "step.gravel"},
                {"sponge", "step.grass", "step.grass"}, {"cobweb", "step.cloth", "step.cloth"}
        };
        for (String[] type : types) {
            out.add("block." + type[0] + ".break " + type[2]);
            for (String use : new String[]{"step", "place", "hit", "fall"}) {
                out.add("block." + type[0] + "." + use + " " + type[1]);
            }
        }
        out.add("block.spawner.break step.stone 1.5");
        for (String use : new String[]{"step", "place", "hit", "fall"}) {
            out.add("block.spawner." + use + " step.stone 1.5");
        }
        out.add("block.crop.break step.grass");
        out.add("block.sand.idle -");

        String[] doors = {"block.wooden_door", "block.iron_door", "block.wooden_trapdoor", "block.iron_trapdoor"};
        for (String door : doors) {
            out.add(door + ".open random.door_open");
            out.add(door + ".close random.door_close");
        }
        for (String click : new String[]{"block.lever.click", "block.stone_button.click_on", "block.stone_button.click_off",
                "block.wooden_button.click_on", "block.wooden_button.click_off",
                "block.stone_pressure_plate.click_on", "block.stone_pressure_plate.click_off",
                "block.wooden_pressure_plate.click_on", "block.wooden_pressure_plate.click_off",
                "block.dispenser.dispense", "block.dispenser.fail", "ui.button.click"}) {
            out.add(click + " random.click");
        }
        out.add("block.dispenser.launch random.bow");
        out.add("block.piston.extend tile.piston.out");
        out.add("block.piston.contract tile.piston.in");
        out.add("block.note_block.harp note.harp");
        out.add("block.note_block.basedrum note.bd");
        out.add("block.note_block.snare note.snare");
        out.add("block.note_block.hat note.hat");
        out.add("block.note_block.bass note.bassattack");
        for (String instrument : new String[]{"banjo", "bell", "bit", "chime", "cow_bell", "didgeridoo", "flute", "guitar",
                "iron_xylophone", "pling", "xylophone"}) {
            out.add("block.note_block." + instrument + " note.harp");
        }
        out.add("block.portal.ambient portal.portal");
        out.add("block.portal.trigger portal.trigger");
        out.add("block.portal.travel portal.travel");
        out.add("block.fire.ambient fire.fire");
        out.add("block.fire.extinguish random.fizz");
        out.add("block.lava.extinguish random.fizz");
        out.add("block.lava.ambient -");
        out.add("block.lava.pop -");
        out.add("block.water.ambient liquid.water");
        out.add("block.redstone_torch.burnout random.fizz");
        out.add("block.chest.open -");
        out.add("block.chest.close -");
        out.add("block.chest.locked -");
        out.add("block.furnace.fire_crackle -");

        out.add("weather.rain ambient.weather.rain");
        out.add("weather.rain.above ambient.weather.rain");
        out.add("entity.lightning_bolt.thunder ambient.weather.thunder");
        out.add("entity.lightning_bolt.impact random.explode");
        out.add("ambient.cave ambient.cave.cave");
        for (String place : new String[]{"basalt_deltas", "crimson_forest", "nether_wastes", "soul_sand_valley", "warped_forest"}) {
            for (String part : new String[]{"additions", "loop", "mood"}) {
                out.add("ambient." + place + "." + part + " -");
            }
        }
        for (String part : new String[]{"enter", "exit", "loop", "loop.additions", "loop.additions.rare",
                "loop.additions.ultra_rare"}) {
            out.add("ambient.underwater." + part + " -");
        }

        for (String hurt : new String[]{"entity.player.hurt", "entity.player.hurt_drown", "entity.player.hurt_freeze",
                "entity.player.hurt_on_fire", "entity.player.hurt_sweet_berry_bush", "entity.player.death",
                "entity.generic.hurt", "entity.generic.death", "entity.hostile.hurt", "entity.hostile.death"}) {
            out.add(hurt + " random.hurt");
        }
        for (String quiet : new String[]{"entity.player.attack.crit", "entity.player.attack.knockback",
                "entity.player.attack.nodamage", "entity.player.attack.strong", "entity.player.attack.sweep",
                "entity.player.attack.weak", "entity.player.big_fall", "entity.player.small_fall", "entity.player.breath",
                "entity.player.burp", "entity.player.levelup", "entity.player.swim", "entity.player.teleport",
                "entity.generic.big_fall", "entity.generic.small_fall", "entity.generic.swim", "entity.generic.eat",
                "entity.generic.drink", "entity.hostile.big_fall", "entity.hostile.small_fall", "entity.hostile.swim",
                "entity.item.break", "entity.experience_orb.pickup", "entity.arrow.hit_player",
                "entity.fishing_bobber.retrieve", "entity.minecart.riding", "entity.minecart.inside",
                "entity.minecart.inside.underwater", "entity.boat.paddle_land", "entity.boat.paddle_water",
                "entity.painting.place", "entity.painting.break", "item.bucket.fill", "item.bucket.fill_lava",
                "item.bucket.empty", "item.bucket.empty_lava", "item.bone_meal.use", "item.crop.plant",
                "item.armor.equip_generic", "item.armor.equip_leather", "item.armor.equip_chain",
                "item.armor.equip_iron", "item.armor.equip_gold", "item.armor.equip_diamond", "ui.toast.in",
                "ui.toast.out", "ui.toast.challenge_complete", "ui.hud.bubble_pop"}) {
            out.add(quiet + " -");
        }
        for (String splash : new String[]{"entity.player.splash", "entity.player.splash.high_speed",
                "entity.generic.splash", "entity.hostile.splash", "entity.fishing_bobber.splash"}) {
            out.add(splash + " random.splash");
        }
        out.add("entity.generic.explode random.explode");
        out.add("entity.generic.extinguish_fire random.fizz");
        out.add("entity.generic.burn random.fizz");
        out.add("entity.item.pickup random.pop");
        out.add("entity.arrow.shoot random.bow");
        out.add("entity.arrow.hit random.drr");
        out.add("entity.snowball.throw random.bow");
        out.add("entity.egg.throw random.bow");
        out.add("entity.fishing_bobber.throw random.bow");
        out.add("entity.tnt.primed random.fuse");
        out.add("item.flintandsteel.use fire.ignite");
        out.add("item.hoe.till step.gravel");

        out.add("entity.zombie.ambient mob.zombie");
        out.add("entity.zombie.hurt mob.zombiehurt");
        out.add("entity.zombie.death mob.zombiedeath");
        out.add("entity.skeleton.ambient mob.skeleton");
        out.add("entity.skeleton.hurt mob.skeletonhurt");
        out.add("entity.skeleton.death mob.skeletonhurt");
        out.add("entity.skeleton.shoot random.bow");
        out.add("entity.spider.ambient mob.spider");
        out.add("entity.spider.hurt mob.spider");
        out.add("entity.spider.death mob.spiderdeath");
        out.add("entity.creeper.hurt mob.creeper");
        out.add("entity.creeper.death mob.creeperdeath");
        out.add("entity.creeper.primed random.fuse");
        for (String pig : new String[]{"entity.pig", "entity.pig_big", "entity.pig_mini"}) {
            out.add(pig + ".ambient mob.pig");
            out.add(pig + ".hurt mob.pig");
            out.add(pig + ".death mob.pigdeath");
            out.add(pig + ".eat -");
        }
        for (String cow : new String[]{"entity.cow", "entity.cow_moody"}) {
            out.add(cow + ".ambient mob.cow");
            out.add(cow + ".hurt mob.cowhurt");
            out.add(cow + ".death mob.cowhurt");
            out.add(cow + ".step -");
        }
        out.add("entity.sheep.ambient mob.sheep");
        out.add("entity.sheep.hurt mob.sheep");
        out.add("entity.sheep.death mob.sheep");
        for (String chicken : new String[]{"entity.chicken", "entity.chicken_picky"}) {
            out.add(chicken + ".ambient mob.chicken");
            out.add(chicken + ".hurt mob.chickenhurt");
            out.add(chicken + ".death mob.chickenhurt");
            out.add(chicken + ".step -");
        }
        out.add("entity.chicken.egg mob.chickenplop");
        for (String wolf : new String[]{"entity.wolf", "entity.wolf_angry", "entity.wolf_big", "entity.wolf_cute",
                "entity.wolf_grumpy", "entity.wolf_puglin", "entity.wolf_sad"}) {
            out.add(wolf + ".ambient mob.wolf.bark");
            out.add(wolf + ".growl mob.wolf.growl");
            out.add(wolf + ".whine mob.wolf.whine");
            out.add(wolf + ".pant mob.wolf.panting");
            out.add(wolf + ".hurt mob.wolf.hurt");
            out.add(wolf + ".death mob.wolf.death");
        }
        out.add("entity.wolf.shake mob.wolf.shake");
        out.add("entity.ghast.ambient mob.ghast.moan");
        out.add("entity.ghast.hurt mob.ghast.scream");
        out.add("entity.ghast.scream mob.ghast.scream");
        out.add("entity.ghast.death mob.ghast.death");
        out.add("entity.ghast.warn mob.ghast.charge");
        out.add("entity.ghast.shoot mob.ghast.fireball");
        for (String slime : new String[]{"jump", "jump_small", "squish", "squish_small", "hurt", "hurt_small", "death",
                "death_small"}) {
            out.add("entity.slime." + slime + " mob.slime");
        }
        out.add("entity.slime.attack mob.slimeattack");
        out.add("entity.zombified_piglin.ambient mob.zombiepig.zpig");
        out.add("entity.zombified_piglin.angry mob.zombiepig.zpigangry");
        out.add("entity.zombified_piglin.hurt mob.zombiepig.zpighurt");
        out.add("entity.zombified_piglin.death mob.zombiepig.zpigdeath");
        for (String squid : new String[]{"ambient", "hurt", "death", "squirt"}) {
            out.add("entity.squid." + squid + " -");
        }
        for (String quiet : new String[]{"entity.zombie.step", "entity.zombie.attack_wooden_door",
                "entity.zombie.attack_iron_door", "entity.zombie.break_wooden_door", "entity.skeleton.step",
                "entity.spider.step", "entity.pig.step", "entity.pig.saddle", "entity.sheep.step", "entity.sheep.shear",
                "entity.cow.milk", "entity.wolf.step"}) {
            out.add(quiet + " -");
        }

        for (String music : new String[]{"music.game", "music.creative", "music.under_water", "music.end",
                "music.dragon", "music.credits", "music.nether.basalt_deltas", "music.nether.crimson_forest",
                "music.nether.nether_wastes", "music.nether.soul_sand_valley", "music.nether.warped_forest",
                "music.overworld.badlands", "music.overworld.bamboo_jungle", "music.overworld.cherry_grove",
                "music.overworld.deep_dark", "music.overworld.desert", "music.overworld.dripstone_caves",
                "music.overworld.flower_forest", "music.overworld.forest", "music.overworld.frozen_peaks",
                "music.overworld.grove", "music.overworld.jagged_peaks", "music.overworld.jungle",
                "music.overworld.lush_caves", "music.overworld.meadow", "music.overworld.old_growth_taiga",
                "music.overworld.snowy_slopes", "music.overworld.sparse_jungle", "music.overworld.stony_peaks",
                "music.overworld.swamp"}) {
            out.add(music + " " + MUSIC_POOL);
        }
        out.add("music.menu -");
        out.add("music_disc.13 streaming/13.ogg");
        out.add("music_disc.cat streaming/cat.ogg");
        return out.toArray(String[]::new);
    }

    private static final Set<String> SILENCED = silencedEvents();

    private static Set<String> silencedEvents() {
        Set<String> out = new java.util.HashSet<>();
        for (String entry : EVENTS) {
            String[] parts = entry.split(" ");
            if (parts[1].equals(SILENT)) {
                out.add(parts[0]);
            }
        }
        return Set.copyOf(out);
    }

    public static boolean silenced(String event) {
        return SILENCED.contains(event);
    }

    private BetaSounds() {
    }

    public record Result(int events, int files, List<String> missing) {
    }

    public static Result write(Resources resources, BetaPackBuilder.Sink sink) throws IOException {
        List<String> listing = resources.list();
        Map<String, List<String>> variants = new LinkedHashMap<>();
        for (String entry : EVENTS) {
            String source = entry.split(" ")[1];
            if (!source.equals(SILENT) && !variants.containsKey(source)) {
                variants.put(source, files(source, listing));
            }
        }

        Set<String> copied = new LinkedHashSet<>();
        List<String> missing = new ArrayList<>();
        Map<String, byte[]> loaded = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> source : variants.entrySet()) {
            List<String> present = new ArrayList<>();
            for (String file : source.getValue()) {
                byte[] data = loaded.containsKey(file) ? loaded.get(file) : resources.read(file);
                if (data == null) {
                    missing.add(file);
                    continue;
                }
                loaded.put(file, data);
                present.add(file);
            }
            if (present.isEmpty()) {
                missing.add(source.getKey());
            }
            source.setValue(present);
        }
        for (Map.Entry<String, byte[]> file : loaded.entrySet()) {
            sink.write("assets/b173/sounds/" + file.getKey(), file.getValue());
            copied.add(file.getKey());
        }

        StringBuilder json = new StringBuilder("{\n");
        int events = 0;
        for (String entry : EVENTS) {
            String[] parts = entry.split(" ");
            String event = parts[0];
            String source = parts[1];
            List<String> files = source.equals(SILENT) ? List.of() : variants.get(source);
            if (!source.equals(SILENT) && files.isEmpty()) {
                continue;
            }
            boolean stream = source.equals(MUSIC_POOL) || source.startsWith("streaming/");
            json.append(events == 0 ? "" : ",\n").append("  \"").append(event).append("\": {\"replace\": true, \"sounds\": [");
            for (int i = 0; i < files.size(); i++) {
                String name = files.get(i).substring(0, files.get(i).length() - ".ogg".length());
                json.append(i == 0 ? "" : ", ").append("{\"name\": \"b173:").append(name).append('"');
                if (stream) {
                    json.append(", \"stream\": true");
                }
                if (parts.length > 2) {
                    json.append(", \"pitch\": ").append(parts[2]);
                }
                json.append('}');
            }
            json.append("]}");
            events++;
        }
        json.append("\n}\n");
        sink.write("assets/minecraft/sounds.json", json.toString().getBytes(StandardCharsets.UTF_8));
        return new Result(events, copied.size(), List.copyOf(missing));
    }

    static List<String> files(String source, List<String> listing) {
        if (source.equals(MUSIC_POOL)) {
            return MUSIC.stream().filter(listing::contains).toList();
        }
        if (source.startsWith("streaming/")) {
            return listing.contains(source) ? List.of(source) : List.of();
        }
        String path = source.replace('.', '/');
        Pattern variant = Pattern.compile("(sound|newsound)/" + Pattern.quote(path) + "[0-9]*\\.ogg");
        return listing.stream().filter(name -> variant.matcher(name).matches()).sorted().toList();
    }

    public static boolean wanted(String path) {
        if (MUSIC.contains(path) || path.equals("streaming/13.ogg") || path.equals("streaming/cat.ogg")) {
            return true;
        }
        for (String entry : EVENTS) {
            String source = entry.split(" ")[1];
            if (!source.equals(SILENT) && !source.startsWith("@") && !source.startsWith("streaming/")
                    && files(source, List.of(path)).contains(path)) {
                return true;
            }
        }
        return false;
    }
}
