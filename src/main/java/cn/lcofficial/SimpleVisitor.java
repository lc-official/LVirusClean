import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class SimpleVisitor extends ClassVisitor {
    // 定义一个ArrayList来存储操作码
    public ArrayList<Integer> opcodes = new ArrayList<>();
    // 定义一个字符串变量来存储类名
    private String lname;

    // 构造函数，传入ClassVisitor和类名
    public SimpleVisitor(ClassVisitor cv, String lname) {
        super(Opcodes.ASM5);
        this.cv = cv;
        this.lname = lname;
    }

    // 重写visitMethod方法，用于访问类中的方法
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // 调用父类的visitMethod方法
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        // 如果方法名为onEnable
        if (name.equals("onEnable")) {
            // 创建一个新的MethodVisitor
            visitor = new MethodVisitor(Opcodes.ASM5, visitor) {
                // 定义一个布尔变量，用于判断是否合法
                boolean ok = true;
                // 定义一个整数变量，用于记录步骤
                int step = 2;

                // 重写visitTypeInsn方法，用于访问类型指令
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    // 如果操作码为NEW且类型为类名，则不合法
                    if (opcode == Opcodes.NEW && type.equals(lname)) {
                        ok = false;
                        return;
                    }
                    // 调用父类的visitTypeInsn方法
                    super.visitTypeInsn(opcode, type);
                }

                // 重写visitVarInsn方法，用于访问局部变量指令
                @Override
                public void visitVarInsn(int opcode, int varIndex) {
                    // 如果不合法且操作码为ALOAD且变量索引为0，则不合法
                    if (!ok && opcode == Opcodes.ALOAD && varIndex == 0) {
                        return;
                    }
                    // 调用父类的visitVarInsn方法
                    super.visitVarInsn(opcode, varIndex);
                }

                // 重写visitInsn方法，用于访问指令
                @Override
                public void visitInsn(int opcode) {
                    // 如果不合法且操作码为DUP，则不合法
                    if (!ok && opcode == Opcodes.DUP) return;
                    // 调用父类的visitInsn方法
                    super.visitInsn(opcode);
                }

                // 重写visitMethodInsn方法，用于访问方法调用指令
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    // 如果不合法且操作码为INVOKESPECIAL且类名为类名且方法名为<init>，则不合法
                    if (!ok && opcode == Opcodes.INVOKESPECIAL && owner.equals(lname) && name.equals("<init>")) {
                        return;
                    }
                    // 如果不合法且操作码为INVOKEVIRTUAL
                    if (!ok && opcode == Opcodes.INVOKEVIRTUAL) {
                        // 如果类名为org.bukkit.plugin.java.JavaPlugin且方法名为getDataFolder且步骤为2，则步骤减1
                        if (owner.equals("org/bukkit/plugin/java/JavaPlugin") && name.equals("getDataFolder") && step == 2) {
                            step--;
                            return;
                        }
                        // 如果类名为java.io.File且方法名为getParent且步骤为1，则不合法
                        if (owner.equals("java/io/File") && name.equals("getParent") && step == 1) {
                            return;
                        }
                        // 如果类名为类名，则合法
                        if (owner.equals(lname)) {
                            ok = true;
                            return;
                        }
                    }
                    // 调用父类的visitMethodInsn方法
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }
        // 返回MethodVisitor
        return visitor;
    }
}