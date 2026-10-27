import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModelParityCheck {
    private ModelParityCheck() {
    }

    private static String flags = "";

    private record Spec(ModelLayerLocation layer, String modelClass, String stateClass, Object... extra) {
    }

    private static Spec spec(String model) {
        String m = "net.minecraft.client.model.";
        String s = "net.minecraft.client.renderer.entity.state.";
        return switch (model) {
            case "pig" -> new Spec(ModelLayers.PIG, m + "animal.pig.PigModel", s + "PigRenderState");
            case "pig_saddle" -> new Spec(ModelLayers.PIG_SADDLE, m + "animal.pig.PigModel", s + "PigRenderState");
            case "cow" -> new Spec(ModelLayers.COW, m + "animal.cow.CowModel", s + "CowRenderState");
            case "sheep" -> new Spec(ModelLayers.SHEEP, m + "animal.sheep.SheepModel", s + "SheepRenderState");
            case "sheep_wool" -> new Spec(ModelLayers.SHEEP_WOOL, m + "animal.sheep.SheepFurModel", s + "SheepRenderState");
            case "creeper" -> new Spec(ModelLayers.CREEPER, m + "monster.creeper.CreeperModel", s + "CreeperRenderState");
            case "creeper_armor" -> new Spec(ModelLayers.CREEPER_ARMOR, m + "monster.creeper.CreeperModel", s + "CreeperRenderState");
            case "ghast" -> new Spec(ModelLayers.GHAST, m + "monster.ghast.GhastModel", s + "GhastRenderState");
            case "spider" -> new Spec(ModelLayers.SPIDER, m + "monster.spider.SpiderModel", s + "LivingEntityRenderState");
            case "zombie" -> new Spec(ModelLayers.ZOMBIE, m + "monster.zombie.ZombieModel", s + "ZombieRenderState");
            case "skeleton" -> new Spec(ModelLayers.SKELETON, m + "monster.skeleton.SkeletonModel", s + "SkeletonRenderState");
            case "chicken" -> new Spec(ModelLayers.CHICKEN, m + "animal.chicken.AdultChickenModel", s + "ChickenRenderState");
            case "squid" -> new Spec(ModelLayers.SQUID, m + "animal.squid.SquidModel", s + "SquidRenderState");
            case "slime" -> new Spec(ModelLayers.SLIME, m + "monster.slime.SlimeModel", s + "SlimeRenderState");
            case "slime_outer" -> new Spec(ModelLayers.SLIME_OUTER, m + "monster.slime.SlimeModel", s + "SlimeRenderState");
            case "wolf" -> new Spec(ModelLayers.WOLF, m + "animal.wolf.AdultWolfModel", s + "WolfRenderState");
            case "biped" -> new Spec(ModelLayers.PLAYER, m + "player.PlayerModel", s + "AvatarRenderState", false);
            case "minecart" -> new Spec(ModelLayers.MINECART, m + "object.cart.MinecartModel", s + "MinecartRenderState");
            case "armor_head" -> new Spec(ModelLayers.PLAYER_ARMOR.head(), m + "HumanoidModel", s + "HumanoidRenderState");
            case "armor_chest" -> new Spec(ModelLayers.PLAYER_ARMOR.chest(), m + "HumanoidModel", s + "HumanoidRenderState");
            case "armor_legs" -> new Spec(ModelLayers.PLAYER_ARMOR.legs(), m + "HumanoidModel", s + "HumanoidRenderState");
            case "armor_feet" -> new Spec(ModelLayers.PLAYER_ARMOR.feet(), m + "HumanoidModel", s + "HumanoidRenderState");
            case "pigzombie" -> new Spec(ModelLayers.ZOMBIFIED_PIGLIN, m + "monster.piglin.AdultZombifiedPiglinModel", s + "ZombifiedPiglinRenderState");
            case "giant" -> new Spec(ModelLayers.GIANT, m + "monster.zombie.GiantZombieModel", s + "ZombieRenderState");
            case "boat" -> new Spec(ModelLayers.OAK_BOAT, m + "object.boat.BoatModel", s + "BoatRenderState");
            case "chest_minecart" -> new Spec(ModelLayers.CHEST_MINECART, m + "object.cart.MinecartModel", s + "MinecartRenderState");
            default -> throw new IllegalArgumentException("unknown model " + model);
        };
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("construct")) {
            construct();
            return;
        }
        java.nio.file.Path outDir = java.nio.file.Path.of(args[0]);
        boolean beta = args[1].equals("beta");
        boolean flipBottoms = args[1].equals("vanilla-flipped");
        String[] parts = args[2].split(":", 2);
        String[] numbers = parts[0].split(",");
        flags = parts.length > 1 ? parts[1] : "";
        float[] pose = new float[5];
        for (int i = 0; i < 5 && i < numbers.length; i++) {
            pose[i] = Float.parseFloat(numbers[i]);
        }
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        Map<ModelLayerLocation, LayerDefinition> roots = roots(beta);
        java.nio.file.Files.createDirectories(outDir);
        for (int i = 3; i < args.length; i++) {
            List<String> lines = dumpModel(args[i], roots, pose, beta, flipBottoms);
            java.nio.file.Files.write(outDir.resolve(args[1] + "_" + args[i] + ".txt"), lines);
        }
    }

    private static void construct() throws ReflectiveOperationException {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        Map<ModelLayerLocation, LayerDefinition> roots = roots(true);
        String m = "net.minecraft.client.model.";
        Object[][] uses = {
                {ModelLayers.PLAYER, m + "player.PlayerModel"}, {ModelLayers.PLAYER_SLIM, m + "player.PlayerModel"},
                {ModelLayers.PLAYER_ARMOR, m + "player.PlayerModel"}, {ModelLayers.PLAYER_SLIM_ARMOR, m + "player.PlayerModel"},
                {ModelLayers.ZOMBIE, m + "monster.zombie.ZombieModel"}, {ModelLayers.ZOMBIE_ARMOR, m + "monster.zombie.ZombieModel"},
                {ModelLayers.GIANT, m + "monster.zombie.GiantZombieModel"}, {ModelLayers.GIANT_ARMOR, m + "monster.zombie.GiantZombieModel"},
                {ModelLayers.SKELETON, m + "monster.skeleton.SkeletonModel"}, {ModelLayers.SKELETON_ARMOR, m + "monster.skeleton.SkeletonModel"},
                {ModelLayers.ZOMBIFIED_PIGLIN, m + "monster.piglin.AdultZombifiedPiglinModel"},
                {ModelLayers.ZOMBIFIED_PIGLIN_ARMOR, m + "monster.piglin.AdultZombifiedPiglinModel"},
                {ModelLayers.PIG, m + "animal.pig.PigModel"}, {ModelLayers.PIG_SADDLE, m + "animal.pig.PigModel"},
                {ModelLayers.COW, m + "animal.cow.CowModel"}, {ModelLayers.SHEEP, m + "animal.sheep.SheepModel"},
                {ModelLayers.SHEEP_WOOL, m + "animal.sheep.SheepFurModel"},
                {ModelLayers.CREEPER, m + "monster.creeper.CreeperModel"}, {ModelLayers.CREEPER_ARMOR, m + "monster.creeper.CreeperModel"},
                {ModelLayers.GHAST, m + "monster.ghast.GhastModel"}, {ModelLayers.SPIDER, m + "monster.spider.SpiderModel"},
                {ModelLayers.CHICKEN, m + "animal.chicken.AdultChickenModel"}, {ModelLayers.SQUID, m + "animal.squid.SquidModel"},
                {ModelLayers.SLIME, m + "monster.slime.SlimeModel"}, {ModelLayers.SLIME_OUTER, m + "monster.slime.SlimeModel"},
                {ModelLayers.WOLF, m + "animal.wolf.AdultWolfModel"},
                {ModelLayers.MINECART, m + "object.cart.MinecartModel"}, {ModelLayers.CHEST_MINECART, m + "object.cart.MinecartModel"},
                {ModelLayers.FURNACE_MINECART, m + "object.cart.MinecartModel"}, {ModelLayers.OAK_BOAT, m + "object.boat.BoatModel"},
                {ModelLayers.BOAT_WATER_PATCH, null}
        };
        int built = 0;
        for (Object[] use : uses) {
            List<ModelLayerLocation> layers = new ArrayList<>();
            if (use[0] instanceof net.minecraft.client.renderer.entity.ArmorModelSet<?> set) {
                for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                        net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                        net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
                    layers.add((ModelLayerLocation) set.get(slot));
                }
            } else {
                layers.add((ModelLayerLocation) use[0]);
            }
            for (ModelLayerLocation layer : layers) {
                if (!dev.b173.client.model.BetaModels.isBeta(layer)) {
                    throw new IllegalStateException(layer + " is not a Beta layer");
                }
                ModelPart root = roots.get(layer).bakeRoot();
                dev.b173.client.model.BetaModels.finishBake(layer, root);
                if (use[1] == null) {
                    built++;
                    continue;
                }
                Class<?> type = Class.forName((String) use[1]);
                try {
                    if (type.getName().endsWith("PlayerModel")) {
                        type.getConstructor(ModelPart.class, boolean.class).newInstance(root, false);
                        type.getConstructor(ModelPart.class, boolean.class).newInstance(root, true);
                    } else {
                        type.getConstructor(ModelPart.class).newInstance(root);
                    }
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw new IllegalStateException(layer + " as " + type.getSimpleName() + ": " + e.getCause(), e.getCause());
                }
                built++;
            }
        }
        System.err.println("constructed " + built + " Beta models with the game's own model classes");
    }

    @SuppressWarnings("unchecked")
    private static Map<ModelLayerLocation, LayerDefinition> roots(boolean beta) throws ReflectiveOperationException {
        Map<ModelLayerLocation, LayerDefinition> roots = LayerDefinitions.createRoots();
        if (beta) {
            Class<?> models = Class.forName("dev.b173.client.model.BetaModels");
            roots = (Map<ModelLayerLocation, LayerDefinition>) models.getMethod("replace", Map.class).invoke(null, roots);
        }
        return roots;
    }

    @SuppressWarnings("unchecked")
    private static List<String> dumpModel(String name, Map<ModelLayerLocation, LayerDefinition> roots, float[] pose,
                                          boolean beta, boolean flipBottoms) throws ReflectiveOperationException {
        Spec spec = spec(name);
        LayerDefinition layer = roots.get(spec.layer);
        Field materialField = LayerDefinition.class.getDeclaredField("material");
        materialField.setAccessible(true);
        Object material = materialField.get(layer);
        int texWidth = intField(material, "xTexSize");
        int texHeight = intField(material, "yTexSize");
        ModelPart root = layer.bakeRoot();
        if (beta) {
            Class.forName("dev.b173.client.model.BetaModels").getMethod("finishBake", ModelLayerLocation.class, ModelPart.class)
                    .invoke(null, spec.layer, root);
        } else if (flipBottoms) {
            for (ModelPart part : root.getAllParts()) {
                flipBottoms(part);
            }
        }

        Class<?> modelClass = Class.forName(spec.modelClass);
        Model<Object> model = (Model<Object>) (spec.extra.length == 0
                ? modelClass.getConstructor(ModelPart.class).newInstance(root)
                : modelClass.getConstructor(ModelPart.class, boolean.class).newInstance(root, spec.extra[0]));
        Object state = Class.forName(spec.stateClass).getConstructor().newInstance();
        if (state instanceof LivingEntityRenderState living) {
            living.walkAnimationPos = pose[0];
            living.walkAnimationSpeed = pose[1];
            living.ageInTicks = pose[2];
            living.yRot = pose[3];
            living.xRot = pose[4];
            living.ageScale = 1.0F;
            living.scale = 1.0F;
        }
        for (String flag : flags.split("\\+")) {
            if (state instanceof net.minecraft.client.renderer.entity.state.HumanoidRenderState humanoid) {
                if (flag.startsWith("swing=")) {
                    humanoid.attackTime = Float.parseFloat(flag.substring(6));
                } else if (flag.equals("ride")) {
                    humanoid.isPassenger = true;
                } else if (flag.equals("sneak")) {
                    humanoid.isCrouching = true;
                } else if (flag.equals("item")) {
                    humanoid.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.ITEM;
                }
            }
            if (flag.equals("sit") && state instanceof net.minecraft.client.renderer.entity.state.WolfRenderState wolf) {
                wolf.isSitting = true;
            }
        }
        switch (name) {
            case "wolf" -> setFloat(state, "tailAngle", pose[2]);
            case "squid" -> setFloat(state, "tentacleAngle", pose[2]);
            case "sheep", "sheep_wool" -> setFloat(state, "headEatAngleScale", pose[4] * (float) (Math.PI / 180.0));
            case "chicken" -> {
                setFloat(state, "flap", (float) (Math.PI / 2));
                setFloat(state, "flapSpeed", pose[2] / 2.0F);
            }
            default -> {
            }
        }
        findSetupAnim(modelClass).invoke(model, state);
        if (beta) {
            mixinsWouldRun(name, model, state);
        }

        List<String> lines = new ArrayList<>();
        dump(model.root(), "", new PoseStack(), texWidth, texHeight, lines);
        return lines;
    }

    @SuppressWarnings("unchecked")
    private static void flipBottoms(ModelPart part) throws ReflectiveOperationException {
        Field cubesField = ModelPart.class.getDeclaredField("cubes");
        cubesField.setAccessible(true);
        for (ModelPart.Cube cube : (List<ModelPart.Cube>) cubesField.get(part)) {
            float bottom = Float.NEGATIVE_INFINITY;
            for (ModelPart.Polygon polygon : cube.polygons) {
                for (ModelPart.Vertex vertex : polygon.vertices()) {
                    bottom = Math.max(bottom, vertex.y());
                }
            }
            for (ModelPart.Polygon polygon : cube.polygons) {
                ModelPart.Vertex[] vertices = polygon.vertices();
                boolean underside = true;
                float minV = Float.POSITIVE_INFINITY;
                float maxV = Float.NEGATIVE_INFINITY;
                for (ModelPart.Vertex vertex : vertices) {
                    underside &= vertex.y() == bottom;
                    minV = Math.min(minV, vertex.v());
                    maxV = Math.max(maxV, vertex.v());
                }
                if (underside) {
                    for (int i = 0; i < vertices.length; i++) {
                        vertices[i] = vertices[i].remap(vertices[i].u(), minV + maxV - vertices[i].v());
                    }
                }
            }
        }
    }

    private static void mixinsWouldRun(String name, Model<?> model, Object state) {
        ModelPart root = model.root();
        if (!dev.b173.client.model.BetaModels.isBeta(root)) {
            return;
        }
        if (model instanceof net.minecraft.client.model.HumanoidModel<?> humanoid
                && state instanceof net.minecraft.client.renderer.entity.state.HumanoidRenderState humanoidState) {
            humanoid.resetPose();
            boolean player = model instanceof net.minecraft.client.model.player.PlayerModel || name.startsWith("armor_");
            dev.b173.client.model.BetaAnimations.biped(humanoid, humanoidState, player ? humanoidState.attackTime : 0.0F);
            if (model instanceof net.minecraft.client.model.monster.zombie.AbstractZombieModel<?>
                    || model instanceof net.minecraft.client.model.monster.skeleton.SkeletonModel<?>
                    || model instanceof net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel) {
                dev.b173.client.model.BetaAnimations.zombieArms(humanoid, humanoidState.ageInTicks);
            }
        } else if (model instanceof net.minecraft.client.model.QuadrupedModel<?>) {
            dev.b173.client.model.BetaAnimations.cowHorns(root, root.getChild("head"));
        } else if (model instanceof net.minecraft.client.model.animal.chicken.AdultChickenModel) {
            dev.b173.client.model.BetaAnimations.chickenHead(root.getChild("head"));
        } else if (model instanceof net.minecraft.client.model.animal.wolf.WolfModel
                && state instanceof net.minecraft.client.renderer.entity.state.WolfRenderState wolf) {
            dev.b173.client.model.BetaAnimations.wolf(root, wolf.isSitting);
        }
    }

    private static Method findSetupAnim(Class<?> type) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (method.getName().equals("setupAnim") && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == Object.class) {
                return method;
            }
        }
        throw new NoSuchMethodException(type + ".setupAnim");
    }

    private static void setFloat(Object owner, String name, float value) throws ReflectiveOperationException {
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.setFloat(owner, value);
                return;
            } catch (NoSuchFieldException e) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static int intField(Object owner, String name) throws ReflectiveOperationException {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(owner);
    }

    @SuppressWarnings("unchecked")
    private static void dump(ModelPart part, String path, PoseStack stack, int texWidth, int texHeight, List<String> lines)
            throws ReflectiveOperationException {
        if (!part.visible) {
            return;
        }
        Field cubesField = ModelPart.class.getDeclaredField("cubes");
        Field childrenField = ModelPart.class.getDeclaredField("children");
        cubesField.setAccessible(true);
        childrenField.setAccessible(true);
        List<ModelPart.Cube> cubes = (List<ModelPart.Cube>) cubesField.get(part);
        Map<String, ModelPart> children = (Map<String, ModelPart>) childrenField.get(part);
        if (cubes.isEmpty() && children.isEmpty()) {
            return;
        }
        stack.pushPose();
        part.translateAndRotate(stack);
        if (!part.skipDraw) {
            Matrix4f matrix = stack.last().pose();
            for (ModelPart.Cube cube : cubes) {
                for (ModelPart.Polygon polygon : cube.polygons) {
                    List<String> corners = new ArrayList<>();
                    for (ModelPart.Vertex vertex : polygon.vertices()) {
                        Vector4f p = matrix.transform(new Vector4f(vertex.worldX(), vertex.worldY(), vertex.worldZ(), 1.0F));
                        corners.add(String.format(Locale.ROOT, "%.3f %.3f %.3f %.2f %.2f",
                                p.x * 16.0F, p.y * 16.0F, p.z * 16.0F, vertex.u() * texWidth, vertex.v() * texHeight));
                    }
                    lines.add("Q " + (path.isEmpty() ? "root" : path) + " | " + String.join(" | ", corners));
                }
            }
        }
        for (Map.Entry<String, ModelPart> child : children.entrySet()) {
            dump(child.getValue(), path.isEmpty() ? child.getKey() : path + "/" + child.getKey(), stack, texWidth, texHeight, lines);
        }
        stack.popPose();
    }
}
