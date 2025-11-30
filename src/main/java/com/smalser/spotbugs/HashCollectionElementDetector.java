package com.smalser.spotbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTypeTable;
import org.apache.bcel.classfile.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HashCollectionElementDetector extends BytecodeScanningDetector implements Detector {
    private static final Logger log = LoggerFactory.getLogger(HashCollectionElementDetector.class);

    private final BugReporter bugReporter;
    private JavaClass currentClass;

    public HashCollectionElementDetector(BugReporter reporter) {
        log.info("Initialized!");
        this.bugReporter = reporter;
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        this.currentClass = classContext.getJavaClass();
        super.visitClassContext(classContext);
    }

    @Override
    public void sawOpcode(int seen) {
        if (seen == Const.NEW) {
            handleNewOperation();
        } else if (seen == Const.INVOKESTATIC) {
            handleInvokeStaticOperation();
        }
    }

    private void handleNewOperation() {
        String className = getClassConstantOperand();
        if ("java/util/HashSet".equals(className) || "java/util/HashMap".equals(className)) {
            checkLocalVariableAssignment();
        }
    }

    private void handleInvokeStaticOperation() {
        String className = getClassConstantOperand();
        String methodName = getNameConstantOperand();
        if ("java/util/stream/Collectors".equals(className) && "toSet".equals(methodName)) {
            checkLocalVariableAssignment();

            // Case 2: assignment to field
            for (org.apache.bcel.classfile.Field f : currentClass.getFields()) {
                String sig = f.getSignature();
                String genericSig = f.getGenericSignature();
                if (genericSig != null) {
                    checkSignatureAndReport(genericSig);
                } else {
                    checkSignatureAndReport(sig);
                }
            }

            // Case 3: return value from method
            checkMethodReturnType();
        }
    }

    private void checkLocalVariableAssignment() {
        LocalVariableTypeTable lvtt = getCode().getLocalVariableTypeTable();
        if (lvtt != null) {
            // Find the local variable signature
            for (LocalVariable lv : lvtt.getLocalVariableTypeTable()) {
                String signature = lv.getSignature();
                checkSignatureAndReport(signature);
            }
        }
    }

    private void checkMethodReturnType() {
        Method m = getMethod();
        String genericSig = m.getGenericSignature();
        if (genericSig != null) {
            checkSignatureAndReport(genericSig);
        } else {
            checkSignatureAndReport(m.getSignature());
        }
    }

    private void checkSignatureAndReport(String signature) {
        log.info("signature={}", signature);
        if (signature != null && signature.contains("java/util/Set")) {
            // Example: Ljava/util/Set<Lcom/smalser/spotbugs/Invoice;>;
            int start = signature.indexOf('<');
            int end = signature.indexOf('>');
            if (start >= 0 && end > start) {
                String elementSig = signature.substring(start + 1, end);
                // Strip leading L and trailing ;
                if (elementSig.startsWith("L") && elementSig.endsWith(";")) {
                    String genericClass = elementSig.substring(1, elementSig.length() - 1).replace('/', '.');
                    if (!hasEqualsAndHashCode(genericClass)) {
                        bugReporter.reportBug(new BugInstance(this,
                                "HC_MISSING_EQUALS_HASHCODE", HIGH_PRIORITY)
                                .addClass(currentClass)
                                .addString("Element type " + genericClass +
                                        " used in hash-based collection without equals/hashCode"));
                    }
                }
            }
        }
    }

    /**
     * Utility: check if a class defines both equals and hashCode
     */
    private boolean hasEqualsAndHashCode(String className) {
        try {
            JavaClass jc = Repository.lookupClass(className);
            boolean hasEq = false, hasHash = false;
            for (Method m : jc.getMethods()) {
                if (m.getName().equals("equals") && m.getSignature().equals("(Ljava/lang/Object;)Z")) hasEq = true;
                if (m.getName().equals("hashCode") && m.getSignature().equals("()I")) hasHash = true;
            }
            log.info(">>>> genericClass={} hasEquals={} hasHashCode={}", className, hasEq, hasHash);
            return hasEq && hasHash;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}

