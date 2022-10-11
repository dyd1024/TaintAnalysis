import boomerang.scene.WrappedClass;
import de.fraunhofer.iem.secucheck.FluentTQLClassLoader.JarClassLoaderUtils;
import de.fraunhofer.iem.secucheck.InternalFluentTQL.dsl.exception.DuplicateTaintFlowQueryIDException;
import de.fraunhofer.iem.secucheck.InternalFluentTQL.fluentInterface.SpecificationInterface.FluentTQLUserInterface;
import de.fraunhofer.iem.secucheck.SecuCheckCoreQueryUtility;
import de.fraunhofer.iem.secucheck.analysis.query.*;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;

import java.io.File;
import java.util.*;

public class Util {
    public Util() {
    }

    public static void initializeSootWithEntryPoints(String sootClassPath, List<EntryPoint> entryPoints) throws Exception {
        G.v();
        G.reset();
        Options.v().set_keep_line_number(true);
        Options.v().setPhaseOption("cg.cha", "on");
        Options.v().setPhaseOption("cg.cha", "apponly:true");
        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().set_output_format(12);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_exclude(excludedPackages());
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Scene.v().addBasicClass("java.lang.StringBuilder", 3);
        Scene.v().addBasicClass("java.lang.System", 3);
        Scene.v().addBasicClass("java.lang.ThreadGroup", 3);
        Scene.v().addBasicClass("java.lang.ClassLoader", 3);
        Scene.v().addBasicClass("java.security.PrivilegedActionException", 3);
        Scene.v().addBasicClass("java.lang.Thread", 3);
        Scene.v().addBasicClass("java.lang.AbstractStringBuilder", 3);
        Runnable runnable = () -> {
            List<SootMethod> entries = new ArrayList();
            Iterator var2 = entryPoints.iterator();

            while(var2.hasNext()) {
                EntryPoint entry = (EntryPoint)var2.next();
                SootClass sootClass = Scene.v().forceResolve(entry.getCanonicalClassName(), 3);
                sootClass.setApplicationClass();
                if (entry.isAllMethods()) {
                    entries.addAll(sootClass.getMethods());
                } else {
                    entry.getMethods().forEach((y) -> {
                        entries.add(sootClass.getMethodByName(y));
                    });
                }
            }

            Scene.v().setEntryPoints(entries);
        };
        executeSootRunnable(runnable, "Could not find entry point.");
        Scene.v().forceResolve("java.lang.Thread", 3).setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }

    public static List<String> excludedPackages() {
        List<String> excludedPackages = new LinkedList();
        excludedPackages.add("sun.*");
        excludedPackages.add("javax.*");
        excludedPackages.add("com.sun.*");
        excludedPackages.add("com.ibm.*");
        excludedPackages.add("org.xml.*");
        excludedPackages.add("org.w3c.*");
        excludedPackages.add("apple.awt.*");
        excludedPackages.add("com.apple.*");
        return excludedPackages;
    }

    private static void drawCallGraph(CallGraph callGraph) {
        DotGraph dot = new DotGraph("callgraph");
        Iterator<Edge> iteratorEdges = callGraph.iterator();
        System.out.println("Call Graph size : " + callGraph.size());

        while(iteratorEdges.hasNext()) {
            Edge edge = (Edge)iteratorEdges.next();
            String node_src = edge.getSrc().toString();
            String node_tgt = edge.getTgt().toString();
            dot.drawEdge(node_src, node_tgt);
        }

        dot.plot("<file-path>");
    }

    private static void executeSootRunnable(Runnable runable, String message) throws Exception {
        try {
            runable.run();
        } catch (Exception | Error var3) {
            throw new Exception(message, var3);
        }
    }

    public static String getCombinedSootClassPath(OS os, String appClassPath, String sootClassPath) {
        String separator = os == OS.WINDOWS ? ";" : ":";
        return sootClassPath + separator + appClassPath;
    }

    public static List<Method> getMethods(SecucheckTaintFlowQuery flowQuery) {
        List<Method> methods = new ArrayList();
        Iterator var2 = flowQuery.getTaintFlows().iterator();

        while(var2.hasNext()) {
            TaintFlow singleFlow = (TaintFlow)var2.next();
            methods.addAll(getMethods(singleFlow));
        }

        return methods;
    }

    public static List<Method> getMethods(TaintFlow flowQuery) {
        List<Method> methods = new ArrayList();
        flowQuery.getFrom().forEach((y) -> {
            methods.add(y);
        });
        flowQuery.getTo().forEach((y) -> {
            methods.add(y);
        });
        if (flowQuery.getNotThrough() != null) {
            flowQuery.getNotThrough().forEach((y) -> {
                methods.add(y);
            });
        }

        if (flowQuery.getThrough() != null) {
            flowQuery.getThrough().forEach((y) -> {
                methods.add(y);
            });
        }

        return methods;
    }

    public static SootMethod getSootMethod(boomerang.scene.Method method) {
        WrappedClass wrappedClass = method.getDeclaringClass();
        SootClass clazz = (SootClass)wrappedClass.getDelegate();
        return clazz.getMethod(method.getSubSignature());
    }

    public static SootMethod getSootMethod(Method method) {
        String[] signatures = method.getSignature().split(":");
        SootClass sootClass = Scene.v().forceResolve(signatures[0], 3);
        return sootClass != null && signatures.length >= 2 ? sootClass.getMethodUnsafe(signatures[1].trim()) : null;
    }

    public static SootMethod findSourceMethodDefinition(TaintFlow partialFlow, SootMethod method, Stmt actualStatement) {
        Iterator var3 = partialFlow.getFrom().iterator();

        String sourceSootSignature;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            Method sourceMethod = (Method)var3.next();
            sourceSootSignature = "<" + sourceMethod.getSignature() + ">";
            if (method.getSignature().equals(sourceSootSignature)) {
                return method;
            }
        } while(!actualStatement.containsInvokeExpr() || !actualStatement.toString().contains(sourceSootSignature));

        return actualStatement.getInvokeExpr().getMethodRef().tryResolve();
    }

    public static SootMethod findSinkMethodDefinition(TaintFlow partialFlow, SootMethod method, Stmt actualStatement) {
        Iterator var3 = partialFlow.getTo().iterator();

        String sinkSootSignature;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            Method sinkMethod = (Method)var3.next();
            sinkSootSignature = "<" + sinkMethod.getSignature() + ">";
        } while(!actualStatement.containsInvokeExpr() || !actualStatement.toString().contains(sinkSootSignature));

        return actualStatement.getInvokeExpr().getMethodRef().tryResolve();
    }

    public static void loadAllParticipantMethods(TaintFlowImpl singleFlow) {
        Iterator var1 = getMethods((TaintFlow)singleFlow).iterator();

        while(var1.hasNext()) {
            Method method = (Method)var1.next();
            getSootMethod(method);
        }

    }

    public static String wrapInAngularBrackets(String value) {
        return "<" + value + ">";
    }

    public static boolean toStringEquals(Object object1, Object object2) {
        return object1.toString().equals(object2.toString());
    }

    public static List<MethodImpl> set_gen_pro() throws DuplicateTaintFlowQueryIDException {
//        File out = new File("\\Propagators\\target");
        File out = new File("Propagators\\target");
        HashSet<de.fraunhofer.iem.secucheck.InternalFluentTQL.fluentInterface.MethodPackage.Method> generalPropagators = new HashSet<>();
        JarClassLoaderUtils jarClassLoaderUtils = new JarClassLoaderUtils();
        HashMap<String, FluentTQLUserInterface> specs = jarClassLoaderUtils.loadAppAndGetFluentTQLSpecification(out.getAbsolutePath());
        generalPropagators.addAll(jarClassLoaderUtils.getGeneralPropagators());

        List<MethodImpl> generalPropagators_re = new ArrayList<>();

        for (de.fraunhofer.iem.secucheck.InternalFluentTQL.fluentInterface.MethodPackage.Method method : generalPropagators) {
            generalPropagators_re.add(SecuCheckCoreQueryUtility.getMethodImpl(method));
        }
        return generalPropagators_re;
    }

}
