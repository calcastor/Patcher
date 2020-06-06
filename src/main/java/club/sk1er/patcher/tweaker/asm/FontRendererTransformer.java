/*
 * Copyright © 2020 by Sk1er LLC
 *
 * All rights reserved.
 *
 * Sk1er LLC
 * 444 S Fulton Ave
 * Mount Vernon, NY
 * sk1er.club
 */

package club.sk1er.patcher.tweaker.asm;

import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class FontRendererTransformer implements PatcherTransformer {

    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.gui.FontRenderer"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "patcherFontRenderer", "Lclub/sk1er/patcher/hooks/FontRendererHook;", null, null));
        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);

            if (methodNode.name.equals("<init>")) {
                methodNode.instructions.insertBefore(methodNode.instructions.getLast().getPrevious(), fontRendererHookInit());
            } else if (methodName.equals("getStringWidth") || methodName.equals("func_78256_a")) {
                clearInstructions(methodNode);
                methodNode.instructions.insert(getStringWidthHook());
            } else if (methodName.equals("renderStringAtPos") || methodName.equals("func_78255_a")) {
                clearInstructions(methodNode);
                methodNode.instructions.insert(renderStringAtPosHook());
            } else if ((methodName.equals("renderString") || methodName.equals("func_180455_b")) && methodNode.desc.equals("(Ljava/lang/String;FFIZ)I")) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), getShadowHook());
            }
        }
    }

    private InsnList fontRendererHookInit() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new TypeInsnNode(Opcodes.NEW, "club/sk1er/patcher/hooks/FontRendererHook"));
        list.add(new InsnNode(Opcodes.DUP));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "club/sk1er/patcher/hooks/FontRendererHook", "<init>", "(Lnet/minecraft/client/gui/FontRenderer;)V", false));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "patcherFontRenderer", "Lclub/sk1er/patcher/hooks/FontRendererHook;"));
        return list;
    }

    private InsnList renderStringAtPosHook() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "patcherFontRenderer", "Lclub/sk1er/patcher/hooks/FontRendererHook;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ILOAD, 2));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "club/sk1er/patcher/hooks/FontRendererHook", "renderStringAtPos", "(Ljava/lang/String;Z)V", false));
        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList getStringWidthHook() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "patcherFontRenderer", "Lclub/sk1er/patcher/hooks/FontRendererHook;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "club/sk1er/patcher/hooks/FontRendererHook", "getStringWidth", "(Ljava/lang/String;)I", false));
        list.add(new InsnNode(Opcodes.IRETURN));
        return list;
    }

    private InsnList getShadowHook() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, getPatcherConfigClass(), "disableShadowedText", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new LdcInsnNode(0));
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(ifeq);
        return list;
    }
}
