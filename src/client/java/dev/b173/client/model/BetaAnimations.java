package dev.b173.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public final class BetaAnimations {
    private static final float PI = (float) Math.PI;
    private static final float DEGREES = 57.295776F;

    private static final float[][] FLEECE = {
            {1.0F, 1.0F, 1.0F}, {0.95F, 0.7F, 0.2F}, {0.9F, 0.5F, 0.85F}, {0.6F, 0.7F, 0.95F},
            {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.7F, 0.8F}, {0.3F, 0.3F, 0.3F},
            {0.6F, 0.6F, 0.6F}, {0.3F, 0.6F, 0.7F}, {0.7F, 0.4F, 0.9F}, {0.2F, 0.4F, 0.8F},
            {0.5F, 0.4F, 0.3F}, {0.4F, 0.5F, 0.2F}, {0.8F, 0.3F, 0.3F}, {0.1F, 0.1F, 0.1F}
    };

    private BetaAnimations() {
    }

    public static int fleeceColor(DyeColor color) {
        float[] rgb = FLEECE[color.getId() & 15];
        return ARGB.colorFromFloat(1.0F, rgb[0], rgb[1], rgb[2]);
    }

    public static void biped(HumanoidModel<?> model, HumanoidRenderState state, float swing) {
        float walk = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float age = state.ageInTicks;
        ModelPart head = model.head;
        ModelPart body = model.body;
        ModelPart rightArm = model.rightArm;
        ModelPart leftArm = model.leftArm;
        ModelPart rightLeg = model.rightLeg;
        ModelPart leftLeg = model.leftLeg;

        head.yRot = state.yRot / DEGREES;
        head.xRot = state.xRot / DEGREES;
        ModelPart root = model.root();
        if (root.hasChild(BetaModels.HAT)) {
            ModelPart hat = root.getChild(BetaModels.HAT);
            hat.yRot = head.yRot;
            hat.xRot = head.xRot;
            hat.visible = model.hat.visible;
        }
        rightArm.xRot = Mth.cos(walk * 0.6662F + PI) * 2.0F * speed * 0.5F;
        leftArm.xRot = Mth.cos(walk * 0.6662F) * 2.0F * speed * 0.5F;
        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.4F * speed;
        leftLeg.xRot = Mth.cos(walk * 0.6662F + PI) * 1.4F * speed;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
        rightLeg.zRot = 0.0F;
        leftLeg.zRot = 0.0F;
        if (state.isPassenger) {
            rightArm.xRot += -0.62831855F;
            leftArm.xRot += -0.62831855F;
            rightLeg.xRot = -1.2566371F;
            leftLeg.xRot = -1.2566371F;
            rightLeg.yRot = 0.31415927F;
            leftLeg.yRot = -0.31415927F;
        }
        if (state.leftArmPose != HumanoidModel.ArmPose.EMPTY) {
            leftArm.xRot = leftArm.xRot * 0.5F - 0.31415927F;
        }
        if (state.rightArmPose != HumanoidModel.ArmPose.EMPTY) {
            rightArm.xRot = rightArm.xRot * 0.5F - 0.31415927F;
        }
        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;

        body.yRot = Mth.sin(Mth.sqrt(swing) * PI * 2.0F) * 0.2F;
        rightArm.z = Mth.sin(body.yRot) * 5.0F;
        rightArm.x = -Mth.cos(body.yRot) * 5.0F;
        leftArm.z = -Mth.sin(body.yRot) * 5.0F;
        leftArm.x = Mth.cos(body.yRot) * 5.0F;
        rightArm.yRot += body.yRot;
        leftArm.yRot += body.yRot;
        leftArm.xRot += body.yRot;
        float eased = 1.0F - swing;
        eased *= eased;
        eased *= eased;
        eased = 1.0F - eased;
        float lift = Mth.sin(eased * PI);
        float reach = Mth.sin(swing * PI) * -(head.xRot - 0.7F) * 0.75F;
        rightArm.xRot = (float) ((double) rightArm.xRot - ((double) lift * 1.2D + (double) reach));
        rightArm.yRot += body.yRot * 2.0F;
        rightArm.zRot = Mth.sin(swing * PI) * -0.4F;

        if (state.isCrouching) {
            body.xRot = 0.5F;
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
            rightLeg.z = 4.0F;
            leftLeg.z = 4.0F;
            rightLeg.y = 9.0F;
            leftLeg.y = 9.0F;
            head.y = 1.0F;
        } else {
            body.xRot = 0.0F;
            rightLeg.z = 0.0F;
            leftLeg.z = 0.0F;
            rightLeg.y = 12.0F;
            leftLeg.y = 12.0F;
            head.y = 0.0F;
        }
        bobArms(rightArm, leftArm, age);
    }

    public static void zombieArms(HumanoidModel<?> model, float ageInTicks) {
        ModelPart rightArm = model.rightArm;
        ModelPart leftArm = model.leftArm;
        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightArm.yRot = -0.1F;
        leftArm.yRot = 0.1F;
        rightArm.xRot = -1.5707964F;
        leftArm.xRot = -1.5707964F;
        bobArms(rightArm, leftArm, ageInTicks);
    }

    public static void cowHorns(ModelPart root, ModelPart head) {
        if (!root.hasChild("b173_right_horn")) {
            return;
        }
        for (String name : new String[]{"b173_right_horn", "b173_left_horn"}) {
            ModelPart horn = root.getChild(name);
            horn.xRot = head.xRot;
            horn.yRot = head.yRot;
        }
    }

    public static void chickenHead(ModelPart head) {
        head.xRot = -head.xRot;
    }

    public static void wolf(ModelPart root, boolean sitting) {
        if (!root.hasChild("b173_nose")) {
            return;
        }
        ModelPart head = root.getChild("head");
        ModelPart nose = root.getChild("b173_nose");
        nose.xRot = head.xRot;
        nose.yRot = head.yRot;
        nose.getChild("b173_nose_roll").zRot = head.getChild("real_head").zRot;
        if (sitting) {
            root.getChild("right_hind_leg").y -= 0.7F;
            root.getChild("left_hind_leg").y -= 0.7F;
            root.getChild("left_front_leg").x += 0.02F;
        }
    }

    private static void bobArms(ModelPart rightArm, ModelPart leftArm, float age) {
        rightArm.zRot += Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot -= Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        rightArm.xRot += Mth.sin(age * 0.067F) * 0.05F;
        leftArm.xRot -= Mth.sin(age * 0.067F) * 0.05F;
    }
}
