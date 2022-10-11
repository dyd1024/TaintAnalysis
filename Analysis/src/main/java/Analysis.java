import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.QueryGraph;
import boomerang.scene.*;
import boomerang.scene.jimple.BoomerangPretransformer;
import boomerang.scene.jimple.SootCallGraph;
import de.fraunhofer.iem.secucheck.analysis.query.EntryPoint;
import de.fraunhofer.iem.secucheck.analysis.query.OS;
import soot.*;
import wpds.impl.Weight;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Analysis {

    public void run() throws Exception {
        String appClassPath = ".\\DemoTests\\target\\classes";
        String classPath = Util.getCombinedSootClassPath(OS.WINDOWS, appClassPath, "");
        Util.initializeSootWithEntryPoints(classPath,getEntryPoints(appClassPath));
//        System.out.println("class_num: "+ Scene.v().getClassNumberer().size());
//        for (SootMethod m : Scene.v().getEntryPoints()){
//            System.out.println("method: " + m);
//            Body jimpleBody = m.retrieveActiveBody();
//            System.out.println("-------------------------------"+m.getName()+"-------------------------------");
//            System.out.println(jimpleBody.toString());
//        }

        Transform transform = new Transform("wjtp.ifds", this.createAnalysisTransformer());
        PackManager.v().getPack("wjtp").add(transform);
        PackManager.v().getPack("cg").apply();
        BoomerangPretransformer.v().apply();
        PackManager.v().getPack("wjtp").apply();
        BoomerangPretransformer.v().reset();
    }

    private static Transformer createAnalysisTransformer() {
        return new SceneTransformer() {
            protected void internalTransform(
                    String phaseName, @SuppressWarnings("rawtypes") Map options) {
                SootCallGraph sootCallGraph = new SootCallGraph();
//                System.out.println("callgraph.size: "+sootCallGraph.size());
//                System.out.println("soot_callgraph: "+ Scene.v().getCallGraph().size());
                AnalysisScope scope =
                        new AnalysisScope(sootCallGraph) {
                            @Override
                            protected Collection<? extends Query> generate(ControlFlowGraph.Edge cfgEdge) {
                                Statement statement = cfgEdge.getStart();
                                if (statement.toString().contains("secret")){
                                    return Collections.singleton(
                                            new ForwardQuery(
                                                    cfgEdge,
                                                    new AllocVal(statement.getLeftOp(), statement, statement.getLeftOp())));
                                }
                                return Collections.emptySet();
                            }
                        };

                //  Submit a query to the solver.
                Collection<Query> sources = scope.computeSeeds();
                Iterator<Query> iterator_query = sources.iterator();

                while (iterator_query.hasNext()){
                    ForwardQuery source = (ForwardQuery)iterator_query.next();
                    SimpleSpecificationGuide simpleSpecificationGuide = new SimpleSpecificationGuide();
                    SimpleBoomerangOptions simpleBoomerangOptions = new SimpleBoomerangOptions();

                    //写一个specification,options前向分析,需要搞定一个customDataFlowScope
                    //source从要分析的代码中指定
                    DemandDrivenGuidedAnalysis_n demandDrivenGuidedAnalysis = new DemandDrivenGuidedAnalysis_n(
                            simpleSpecificationGuide,
                            simpleBoomerangOptions,
                            SootDataFlowScope.make(Scene.v()));

                    Set<Statement> result = new HashSet();
                    QueryGraph<Weight.NoWeight> queryGraph = demandDrivenGuidedAnalysis.run(source,result);
//                    Iterator it = queryGraph.getNodes().iterator();
//                    while (it.hasNext()){
//                        Query query = (Query) it.next();
//                        System.out.println(query.toString());
//                    }
                    Iterator it_set = result.iterator();
                    while (it_set.hasNext()){
                        System.out.println("stmt: " + it_set.next().toString());
                    }

                }
            }
        };
    }

    private List<EntryPoint> getEntryPoints(String classPath) {

        List<EntryPoint> entryPoints = new ArrayList<EntryPoint>();
        List<String> typeNames = Arrays.asList("taint.analysis.taintAnalysis");

//        List<String> typeNames = Arrays.asList();
//        try {
//            List<String> classes = Files.walk(Paths.get(classPath))
//                    .filter(Files::isRegularFile)
//                    .map(Path::toAbsolutePath)
//                    .map(Path::toString)
//                    .filter(a -> a.endsWith(".class"))
//                    .map(a -> a.replace(classPath, "").replace(File.separator, ".").replaceAll("^\\.", "").replaceAll("\\.class$", ""))
//                    .collect(Collectors.toList());
//            typeNames = classes;
//        } catch ( IOException e) {
//            System.err.println("Something went wrong.\n" + e.getMessage());
//            System.exit(-1);
//        }

        for (String typeName : typeNames) {
            EntryPoint entryPoint = new EntryPoint();
            entryPoint.setCanonicalClassName(typeName);
            entryPoint.setAllMethods(true);
            entryPoints.add(entryPoint);
        }
//        System.out.println("entryPoints: " + entryPoints);
        return entryPoints;
    }

}
