package dev.b173.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class BetaModels {
    public static final String MARKER = "b173";
    public static final String HAT = "b173_hat";

    private static final float HALF_PI = (float) (Math.PI / 2);
    private static final float PI = (float) Math.PI;

    private static final Identifier PACK_MARKER = Identifier.fromNamespaceAndPath("b173", "beta.json");

    private static volatile Set<ModelLayerLocation> active = Set.of();

    private BetaModels() {
    }

    public static Map<ModelLayerLocation, LayerDefinition> replace(Map<ModelLayerLocation, LayerDefinition> vanilla) {
        Map<ModelLayerLocation, LayerDefinition> beta = layers();
        Map<ModelLayerLocation, LayerDefinition> merged = new HashMap<>(vanilla);
        merged.putAll(beta);
        active = Set.copyOf(beta.keySet());
        return merged;
    }

    public static boolean packLoaded() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.getResourceManager().getResource(PACK_MARKER).isPresent();
    }

    public static void clear() {
        active = Set.of();
    }

    public static boolean packActive() {
        return !active.isEmpty();
    }

    public static boolean isBeta(ModelLayerLocation layer) {
        return active.contains(layer);
    }

    public static boolean isBeta(ModelPart root) {
        return root.hasChild(MARKER);
    }

    static Map<ModelLayerLocation, LayerDefinition> layers() {
        Map<ModelLayerLocation, LayerDefinition> layers = new HashMap<>();
        layers.put(ModelLayers.PIG, LayerDefinition.create(pig(CubeDeformation.NONE), 64, 64));
        layers.put(ModelLayers.PIG_SADDLE, LayerDefinition.create(pig(new CubeDeformation(0.5F)), 64, 64));
        layers.put(ModelLayers.COW, LayerDefinition.create(cow(), 64, 64));
        layers.put(ModelLayers.SHEEP, LayerDefinition.create(sheep(), 64, 32));
        layers.put(ModelLayers.SHEEP_WOOL, LayerDefinition.create(sheepWool(), 64, 32));
        layers.put(ModelLayers.CREEPER, LayerDefinition.create(creeper(CubeDeformation.NONE), 64, 32));
        layers.put(ModelLayers.CREEPER_ARMOR, LayerDefinition.create(creeper(new CubeDeformation(2.0F)), 64, 32));
        layers.put(ModelLayers.GHAST, LayerDefinition.create(ghast(), 64, 32)
                .apply(mesh -> mesh.transformed(pose -> scaledFromFeet(pose, 4.5F))));
        layers.put(ModelLayers.SPIDER, LayerDefinition.create(spider(), 64, 32));
        layers.put(ModelLayers.ZOMBIE, LayerDefinition.create(biped(CubeDeformation.NONE), 64, 64));
        layers.put(ModelLayers.GIANT, LayerDefinition.create(biped(CubeDeformation.NONE), 64, 64)
                .apply(mesh -> mesh.transformed(pose -> scaledFromFeet(pose, 6.0F))));
        layers.put(ModelLayers.ZOMBIFIED_PIGLIN, LayerDefinition.create(pigZombie(), 64, 64));
        layers.put(ModelLayers.SKELETON, LayerDefinition.create(skeleton(), 64, 32));
        layers.put(ModelLayers.PLAYER, LayerDefinition.create(biped(CubeDeformation.NONE), 64, 64));
        layers.put(ModelLayers.PLAYER_SLIM, LayerDefinition.create(biped(CubeDeformation.NONE), 64, 64));
        layers.put(ModelLayers.CHICKEN, LayerDefinition.create(chicken(), 64, 32));
        layers.put(ModelLayers.SQUID, LayerDefinition.create(squid(), 64, 32));
        layers.put(ModelLayers.SLIME, LayerDefinition.create(slime(), 64, 32));
        layers.put(ModelLayers.SLIME_OUTER, LayerDefinition.create(slimeOuter(), 64, 32));
        layers.put(ModelLayers.WOLF, LayerDefinition.create(wolf(), 64, 32));
        layers.put(ModelLayers.MINECART, LayerDefinition.create(minecart(), 64, 32));
        layers.put(ModelLayers.CHEST_MINECART, LayerDefinition.create(minecart(), 64, 32));
        layers.put(ModelLayers.FURNACE_MINECART, LayerDefinition.create(minecart(), 64, 32));
        layers.put(ModelLayers.OAK_BOAT, LayerDefinition.create(boat(), 128, 64));
        layers.put(ModelLayers.BOAT_WATER_PATCH, LayerDefinition.create(withMarker(new MeshDefinition()), 0, 0));
        MeshTransformer giant = mesh -> mesh.transformed(pose -> scaledFromFeet(pose, 6.0F));
        armor(layers, ModelLayers.PLAYER_ARMOR, MeshTransformer.IDENTITY);
        armor(layers, ModelLayers.PLAYER_SLIM_ARMOR, MeshTransformer.IDENTITY);
        armor(layers, ModelLayers.ZOMBIE_ARMOR, MeshTransformer.IDENTITY);
        armor(layers, ModelLayers.SKELETON_ARMOR, MeshTransformer.IDENTITY);
        armor(layers, ModelLayers.ZOMBIFIED_PIGLIN_ARMOR, MeshTransformer.IDENTITY);
        armor(layers, ModelLayers.GIANT_ARMOR, giant);
        return layers;
    }

    private static void armor(Map<ModelLayerLocation, LayerDefinition> layers, ArmorModelSet<ModelLayerLocation> set,
                              MeshTransformer transform) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            CubeDeformation grow = new CubeDeformation(slot == EquipmentSlot.LEGS ? 0.5F : 1.0F);
            layers.put(set.get(slot), LayerDefinition.create(armorMesh(grow, slot), 64, 32).apply(transform));
        }
    }

    static MeshDefinition armorMesh(CubeDeformation grow, EquipmentSlot slot) {
        MeshDefinition mesh = biped(grow);
        PartDefinition root = mesh.getRoot();
        CubeListBuilder none = CubeListBuilder.create();
        if (slot != EquipmentSlot.HEAD) {
            root.addOrReplaceChild("head", none, PartPose.ZERO).addOrReplaceChild("hat", none, PartPose.ZERO);
            root.addOrReplaceChild(HAT, none, PartPose.ZERO);
        }
        if (slot != EquipmentSlot.CHEST && slot != EquipmentSlot.LEGS) {
            root.addOrReplaceChild("body", none, PartPose.ZERO).addOrReplaceChild("jacket", none, PartPose.ZERO);
        }
        if (slot != EquipmentSlot.CHEST) {
            root.addOrReplaceChild("right_arm", none, PartPose.offset(-5.0F, 2.0F, 0.0F))
                    .addOrReplaceChild("right_sleeve", none, PartPose.ZERO);
            root.addOrReplaceChild("left_arm", none, PartPose.offset(5.0F, 2.0F, 0.0F))
                    .addOrReplaceChild("left_sleeve", none, PartPose.ZERO);
        }
        if (slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) {
            root.addOrReplaceChild("right_leg", none, PartPose.offset(-2.0F, 12.0F, 0.0F))
                    .addOrReplaceChild("right_pants", none, PartPose.ZERO);
            root.addOrReplaceChild("left_leg", none, PartPose.offset(2.0F, 12.0F, 0.0F))
                    .addOrReplaceChild("left_pants", none, PartPose.ZERO);
        }
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("right_ear", none, PartPose.ZERO);
        head.addOrReplaceChild("left_ear", none, PartPose.ZERO);
        return mesh;
    }

    private static PartPose scaledFromFeet(PartPose pose, float scale) {
        return pose.scaled(scale).translated(0.0F, 24.016F - scale * 24.125F, 0.0F);
    }

    private static MeshDefinition withMarker(MeshDefinition mesh) {
        mesh.getRoot().addOrReplaceChild(MARKER, CubeListBuilder.create(), PartPose.ZERO);
        return mesh;
    }

    private static CubeListBuilder box(int u, int v, float x, float y, float z, float w, float h, float d,
                                       CubeDeformation grow) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d, grow);
    }

    private static CubeListBuilder box(int u, int v, float x, float y, float z, float w, float h, float d) {
        return box(u, v, x, y, z, w, h, d, CubeDeformation.NONE);
    }

    private static CubeListBuilder mirroredBox(int u, int v, float x, float y, float z, float w, float h, float d,
                                               CubeDeformation grow) {
        return CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, w, h, d, grow);
    }

    private static MeshDefinition quadruped(int legHeight, CubeDeformation grow) {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", box(0, 0, -4.0F, -4.0F, -8.0F, 8, 8, 8, grow),
                PartPose.offset(0.0F, 18 - legHeight, -6.0F));
        root.addOrReplaceChild("body", box(28, 8, -5.0F, -10.0F, -7.0F, 10, 16, 8, grow),
                PartPose.offsetAndRotation(0.0F, 17 - legHeight, 2.0F, HALF_PI, 0.0F, 0.0F));
        legs(root, box(0, 16, -2.0F, 0.0F, -2.0F, 4, legHeight, 4, grow), 3.0F, 24 - legHeight, 7.0F, -5.0F);
        return mesh;
    }

    private static void legs(PartDefinition root, CubeListBuilder leg, float x, float y, float hindZ, float frontZ) {
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-x, y, hindZ));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(x, y, hindZ));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-x, y, frontZ));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(x, y, frontZ));
    }

    private static MeshDefinition pig(CubeDeformation grow) {
        return quadruped(6, grow);
    }

    private static MeshDefinition cow() {
        MeshDefinition mesh = quadruped(12, CubeDeformation.NONE);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", box(0, 0, -4.0F, -4.0F, -6.0F, 8, 8, 6), PartPose.offset(0.0F, 4.0F, -8.0F));
        root.addOrReplaceChild("body", box(18, 4, -6.0F, -10.0F, -7.0F, 12, 18, 10),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, HALF_PI, 0.0F, 0.0F));
        legs(root, box(0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4), 4.0F, 12.0F, 7.0F, -6.0F);
        root.addOrReplaceChild("b173_right_horn", box(22, 0, -4.0F, -5.0F, -4.0F, 1, 3, 1), PartPose.offset(0.0F, 3.0F, -7.0F));
        root.addOrReplaceChild("b173_left_horn", box(22, 0, 3.0F, -5.0F, -4.0F, 1, 3, 1), PartPose.offset(0.0F, 3.0F, -7.0F));
        root.addOrReplaceChild("b173_udder", box(52, 0, -2.0F, -3.0F, 0.0F, 4, 6, 2),
                PartPose.offsetAndRotation(0.0F, 14.0F, 6.0F, HALF_PI, 0.0F, 0.0F));
        return mesh;
    }

    private static MeshDefinition sheep() {
        MeshDefinition mesh = quadruped(12, CubeDeformation.NONE);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", box(0, 0, -3.0F, -4.0F, -6.0F, 6, 6, 8), PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", box(28, 8, -4.0F, -10.0F, -7.0F, 8, 16, 6),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, HALF_PI, 0.0F, 0.0F));
        return mesh;
    }

    private static MeshDefinition sheepWool() {
        MeshDefinition mesh = quadruped(12, CubeDeformation.NONE);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", box(0, 0, -3.0F, -4.0F, -4.0F, 6, 6, 6, new CubeDeformation(0.6F)),
                PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", box(28, 8, -4.0F, -10.0F, -7.0F, 8, 16, 6, new CubeDeformation(1.75F)),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, HALF_PI, 0.0F, 0.0F));
        legs(root, box(0, 16, -2.0F, 0.0F, -2.0F, 4, 6, 4, new CubeDeformation(0.5F)), 3.0F, 12.0F, 7.0F, -5.0F);
        return mesh;
    }

    private static MeshDefinition creeper(CubeDeformation grow) {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", box(0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, grow), PartPose.offset(0.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("body", box(16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, grow), PartPose.offset(0.0F, 4.0F, 0.0F));
        CubeListBuilder leg = box(0, 16, -2.0F, 0.0F, -2.0F, 4, 6, 4, grow);
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(-2.0F, 16.0F, 4.0F));
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(2.0F, 16.0F, 4.0F));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(-2.0F, 16.0F, -4.0F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(2.0F, 16.0F, -4.0F));
        return mesh;
    }

    private static MeshDefinition ghast() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", box(0, 0, -8.0F, -8.0F, -8.0F, 16, 16, 16), PartPose.offset(0.0F, 8.0F, 0.0F));
        Random random = new Random(1660L);
        for (int i = 0; i < 9; i++) {
            float x = (((float) (i % 3) - (float) (i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float z = ((float) (i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int length = random.nextInt(7) + 8;
            root.addOrReplaceChild("tentacle" + i, box(0, 0, -1.0F, 0.0F, -1.0F, 2, length, 2), PartPose.offset(x, 15.0F, z));
        }
        return mesh;
    }

    private static MeshDefinition spider() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", box(32, 4, -4.0F, -4.0F, -8.0F, 8, 8, 8), PartPose.offset(0.0F, 15.0F, -3.0F));
        root.addOrReplaceChild("body0", box(0, 0, -3.0F, -3.0F, -3.0F, 6, 6, 6), PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("body1", box(0, 12, -5.0F, -4.0F, -6.0F, 10, 8, 12), PartPose.offset(0.0F, 15.0F, 9.0F));
        CubeListBuilder right = box(18, 0, -15.0F, -1.0F, -1.0F, 16, 2, 2);
        CubeListBuilder left = box(18, 0, -1.0F, -1.0F, -1.0F, 16, 2, 2);
        float quarter = PI / 4.0F;
        float eighth = PI / 8.0F;
        float middle = quarter * 0.74F;
        spiderLeg(root, "right_hind_leg", right, -4.0F, 2.0F, quarter, -quarter);
        spiderLeg(root, "left_hind_leg", left, 4.0F, 2.0F, -quarter, quarter);
        spiderLeg(root, "right_middle_hind_leg", right, -4.0F, 1.0F, eighth, -middle);
        spiderLeg(root, "left_middle_hind_leg", left, 4.0F, 1.0F, -eighth, middle);
        spiderLeg(root, "right_middle_front_leg", right, -4.0F, 0.0F, -eighth, -middle);
        spiderLeg(root, "left_middle_front_leg", left, 4.0F, 0.0F, eighth, middle);
        spiderLeg(root, "right_front_leg", right, -4.0F, -1.0F, -quarter, -quarter);
        spiderLeg(root, "left_front_leg", left, 4.0F, -1.0F, quarter, quarter);
        return mesh;
    }

    private static void spiderLeg(PartDefinition root, String name, CubeListBuilder leg, float x, float z, float yaw, float roll) {
        root.addOrReplaceChild(name, leg, PartPose.offsetAndRotation(x, 15.0F, z, 0.0F, yaw, roll));
    }

    private static MeshDefinition biped(CubeDeformation grow) {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", box(0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, grow), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(HAT, box(32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, grow.extend(0.5F)), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", box(16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, grow), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", box(40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, grow),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", mirroredBox(40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, grow),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", box(0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, grow),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", mirroredBox(0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, grow),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        body.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);
        rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        rightLeg.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
        leftLeg.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
        return mesh;
    }

    private static MeshDefinition pigZombie() {
        MeshDefinition mesh = biped(CubeDeformation.NONE);
        PartDefinition head = mesh.getRoot().getChild("head");
        head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.ZERO);
        return mesh;
    }

    private static MeshDefinition skeleton() {
        MeshDefinition mesh = biped(CubeDeformation.NONE);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_arm", box(40, 16, -1.0F, -2.0F, -1.0F, 2, 12, 2), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", mirroredBox(40, 16, -1.0F, -2.0F, -1.0F, 2, 12, 2, CubeDeformation.NONE),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", box(0, 16, -1.0F, 0.0F, -1.0F, 2, 12, 2), PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", mirroredBox(0, 16, -1.0F, 0.0F, -1.0F, 2, 12, 2, CubeDeformation.NONE),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        return mesh;
    }

    private static MeshDefinition chicken() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", box(0, 0, -2.0F, -6.0F, -2.0F, 4, 6, 3),
                PartPose.offset(0.0F, 15.0F, -4.0F));
        head.addOrReplaceChild("beak", box(14, 0, -2.0F, -4.0F, -4.0F, 4, 2, 2), PartPose.ZERO);
        head.addOrReplaceChild("red_thing", box(14, 4, -1.0F, -2.0F, -3.0F, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("body", box(0, 9, -3.0F, -4.0F, -3.0F, 6, 8, 6),
                PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, HALF_PI, 0.0F, 0.0F));
        CubeListBuilder leg = box(26, 0, -1.0F, 0.0F, -3.0F, 3, 5, 3);
        root.addOrReplaceChild("right_leg", leg, PartPose.offset(-2.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("left_leg", leg, PartPose.offset(1.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("right_wing", box(24, 13, 0.0F, 0.0F, -3.0F, 1, 4, 6), PartPose.offset(-4.0F, 13.0F, 0.0F));
        root.addOrReplaceChild("left_wing", box(24, 13, -1.0F, 0.0F, -3.0F, 1, 4, 6), PartPose.offset(4.0F, 13.0F, 0.0F));
        return mesh;
    }

    private static MeshDefinition squid() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", box(0, 0, -6.0F, -8.0F, -6.0F, 12, 16, 12), PartPose.offset(0.0F, 8.0F, 0.0F));
        CubeListBuilder tentacle = box(48, 0, -1.0F, 0.0F, -1.0F, 2, 18, 2);
        for (int i = 0; i < 8; i++) {
            double angle = (double) i * Math.PI * 2.0D / 8.0D;
            float x = (float) Math.cos(angle) * 5.0F;
            float z = (float) Math.sin(angle) * 5.0F;
            float yaw = (float) ((double) i * Math.PI * -2.0D / 8.0D + Math.PI / 2.0D);
            root.addOrReplaceChild("tentacle" + i, tentacle, PartPose.offsetAndRotation(x, 15.0F, z, 0.0F, yaw, 0.0F));
        }
        return mesh;
    }

    private static MeshDefinition slime() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", box(0, 16, -3.0F, 17.0F, -3.0F, 6, 6, 6), PartPose.ZERO);
        root.addOrReplaceChild("right_eye", box(32, 0, -3.25F, 18.0F, -3.5F, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("left_eye", box(32, 4, 1.25F, 18.0F, -3.5F, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("mouth", box(32, 8, 0.0F, 21.0F, -3.5F, 1, 1, 1), PartPose.ZERO);
        return mesh;
    }

    private static MeshDefinition slimeOuter() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        mesh.getRoot().addOrReplaceChild("cube", box(0, 0, -4.0F, 16.0F, -4.0F, 8, 8, 8), PartPose.ZERO);
        return mesh;
    }

    private static MeshDefinition wolf() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.5F, -7.0F));
        head.addOrReplaceChild("real_head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4)
                .texOffs(16, 14).addBox(-3.0F, -5.0F, 0.0F, 2, 2, 1)
                .texOffs(16, 14).addBox(1.0F, -5.0F, 0.0F, 2, 2, 1), PartPose.ZERO);
        root.addOrReplaceChild("b173_nose", CubeListBuilder.create(), PartPose.offset(-0.5F, 13.5F, -7.0F))
                .addOrReplaceChild("b173_nose_roll", box(0, 10, -2.0F, 0.0F, -5.0F, 3, 3, 4), PartPose.ZERO);
        root.addOrReplaceChild("body", box(18, 14, -4.0F, -2.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, HALF_PI, 0.0F, 0.0F));
        root.addOrReplaceChild("upper_body", box(21, 0, -4.0F, -3.0F, -3.0F, 8, 6, 7),
                PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, HALF_PI, 0.0F, 0.0F));
        CubeListBuilder leg = box(0, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-2.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(0.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-2.5F, 16.0F, -4.0F));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(0.5F, 16.0F, -4.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, PI / 5.0F, 0.0F, 0.0F))
                .addOrReplaceChild("real_tail", box(9, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2), PartPose.ZERO);
        return mesh;
    }

    private static MeshDefinition minecart() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", box(0, 10, -10.0F, -8.0F, -1.0F, 20, 16, 2),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, HALF_PI, 0.0F, 0.0F));
        CubeListBuilder side = box(0, 0, -8.0F, -9.0F, -1.0F, 16, 8, 2);
        root.addOrReplaceChild("front", side, PartPose.offsetAndRotation(-9.0F, 4.0F, 0.0F, 0.0F, PI * 3.0F / 2.0F, 0.0F));
        root.addOrReplaceChild("back", side, PartPose.offsetAndRotation(9.0F, 4.0F, 0.0F, 0.0F, HALF_PI, 0.0F));
        root.addOrReplaceChild("left", side, PartPose.offsetAndRotation(0.0F, 4.0F, -7.0F, 0.0F, PI, 0.0F));
        root.addOrReplaceChild("right", side, PartPose.offset(0.0F, 4.0F, 7.0F));
        root.addOrReplaceChild("b173_floor", box(44, 10, -9.0F, -7.0F, -1.0F, 18, 14, 1),
                PartPose.offsetAndRotation(0.0F, 4.1F, 0.0F, -HALF_PI, 0.0F, 0.0F));
        return mesh;
    }

    private static MeshDefinition boat() {
        MeshDefinition mesh = withMarker(new MeshDefinition());
        PartDefinition root = mesh.getRoot();
        float y = 4.0F + 1.2F;
        root.addOrReplaceChild("bottom", box(0, 8, -12.0F, -8.0F, -3.0F, 24, 16, 4),
                PartPose.offsetAndRotation(0.0F, y, 0.0F, HALF_PI, 0.0F, 0.0F));
        CubeListBuilder side = box(0, 0, -10.0F, -7.0F, -1.0F, 20, 6, 2);
        root.addOrReplaceChild("back", side, PartPose.offsetAndRotation(-11.0F, y, 0.0F, 0.0F, PI * 3.0F / 2.0F, 0.0F));
        root.addOrReplaceChild("front", side, PartPose.offsetAndRotation(11.0F, y, 0.0F, 0.0F, HALF_PI, 0.0F));
        root.addOrReplaceChild("right", side, PartPose.offsetAndRotation(0.0F, y, -9.0F, 0.0F, PI, 0.0F));
        root.addOrReplaceChild("left", side, PartPose.offset(0.0F, y, 9.0F));
        root.addOrReplaceChild("left_paddle", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_paddle", CubeListBuilder.create(), PartPose.ZERO);
        return mesh;
    }

    public static void finishBake(ModelLayerLocation layer, ModelPart root) {
        if (!isBeta(layer)) {
            return;
        }
        flipUndersides(root);
    }

    static void flipUndersides(ModelPart root) {
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
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
        });
    }
}
