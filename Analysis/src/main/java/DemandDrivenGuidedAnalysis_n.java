import boomerang.BackwardQuery;
import boomerang.Boomerang;
import boomerang.BoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.QueryGraph;
import boomerang.guided.Specification.QueryDirection;
import boomerang.results.AbstractBoomerangResults.Context;
import boomerang.results.BackwardBoomerangResults;
import boomerang.results.ForwardBoomerangResults;
import boomerang.scene.ControlFlowGraph.Edge;
import boomerang.scene.DataFlowScope;
import boomerang.scene.SootDataFlowScope;
import boomerang.scene.Statement;
import boomerang.scene.Val;
import boomerang.scene.jimple.SootCallGraph;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import soot.Scene;
import sync.pds.solver.nodes.Node;
import wpds.impl.Weight.NoWeight;

public class DemandDrivenGuidedAnalysis_n {
    private final BoomerangOptions customBoomerangOptions;
    private final IDemandDrivenGuidedManager_n spec;
    private final DataFlowScope scope;
    private final SootCallGraph callGraph;
    private final LinkedList<DemandDrivenGuidedAnalysis_n.QueryWithContext> queryQueue = Lists.newLinkedList();
    private final Set<Query> visited = Sets.newHashSet();
    private final Boomerang solver;
    private boolean triggered;

    public DemandDrivenGuidedAnalysis_n(
            IDemandDrivenGuidedManager_n specification,
            BoomerangOptions options,
            DataFlowScope dataFlowScope) {
        spec = specification;
        callGraph = new SootCallGraph();
        scope = dataFlowScope;
        if (!options.allowMultipleQueries()) {
            throw new RuntimeException(
                    "Boomerang options allowMultipleQueries is set to false. Please enable it.");
        }
        customBoomerangOptions = options;
        solver = new Boomerang(callGraph, scope, customBoomerangOptions);
    }

    public DemandDrivenGuidedAnalysis_n(
            IDemandDrivenGuidedManager_n specification, BoomerangOptions options) {
        this(specification, options, SootDataFlowScope.make(Scene.v()));
    }

    /**
     * The query graph takes as input an initial query from which all follow up computations are
     * computed. Based on the specification provided. It returns the QueryGraph which is a graph whose
     * nodes are Boomerang Queries, there is an edge between the queries if there node A triggered a
     * subsequent query B.
     *
     * <p>Important note: Ensure to call cleanUp() after finishing the analysis.
     *
     * @param query The initial query to start the analysis from.
     * @return a query graph containing all queries triggered.
     */
    public QueryGraph<NoWeight> run(Query query, Set<Statement> result) {
        if (triggered) {
            throw new RuntimeException(
                    DemandDrivenGuidedAnalysis_n.class.getName()
                            + " must be instantiated once per query. Use a different instance.");
        }
        triggered = true;
        queryQueue.add(new DemandDrivenGuidedAnalysis_n.QueryWithContext(query));
        while (!queryQueue.isEmpty()) {
            DemandDrivenGuidedAnalysis_n.QueryWithContext pop = queryQueue.pop();
            if (pop.query instanceof ForwardQuery) {
                ForwardBoomerangResults<NoWeight> results;
                ForwardQuery currentQuery = (ForwardQuery) pop.query;
                if (pop.parentQuery == null) {
                    results = solver.solve(currentQuery);
                } else {
                    results = solver.solveUnderScope(currentQuery, pop.triggeringNode, pop.parentQuery);
                }

                Table<Edge, Val, NoWeight> forwardResults =
                        results.asStatementValWeightTable((ForwardQuery) pop.query);
                // Any ForwardQuery may trigger additional ForwardQuery under its own scope.
                triggerNewBackwardQueries(forwardResults, currentQuery, QueryDirection.FORWARD, result);
            } else {
                BackwardBoomerangResults<NoWeight> results;
                if (pop.parentQuery == null) {
                    results = solver.solve((BackwardQuery) pop.query);
                } else {
                    results =
                            solver.solveUnderScope(
                                    (BackwardQuery) pop.query, pop.triggeringNode, pop.parentQuery);
                }
                Table<Edge, Val, NoWeight> backwardResults =
                        solver.getBackwardSolvers().get(query).asStatementValWeightTable();

                triggerNewBackwardQueries(backwardResults, pop.query, QueryDirection.BACKWARD, result);
                Map<ForwardQuery, Context> allocationSites = results.getAllocationSites();

                for (Entry<ForwardQuery, Context> entry : allocationSites.entrySet()) {
                    triggerNewBackwardQueries(
                            results.asStatementValWeightTable(entry.getKey()),
                            entry.getKey(),
                            QueryDirection.FORWARD, result);
                }
            }
        }

        QueryGraph<NoWeight> queryGraph = solver.getQueryGraph();
        return queryGraph;
    }

    /**
     * Ensure to call cleanup to detach all listeners from the solver, otherwise the analysis may run
     * into a Memory issues.
     */
    public void cleanUp() {
        solver.unregisterAllListeners();
    }

    public Boomerang getSolver() {
        return solver;
    }

    private void triggerNewBackwardQueries(
            Table<Edge, Val, NoWeight> backwardResults, Query lastQuery, QueryDirection direction, Set<Statement> result) {
        for (Cell<Edge, Val, NoWeight> cell : backwardResults.cellSet()) {
            Edge triggeringEdge = cell.getRowKey();
            Val fact = cell.getColumnKey();
            Collection<Query> queries;
            if (direction == QueryDirection.FORWARD) {
                queries =
                        spec.onForwardFlow((ForwardQuery) lastQuery, cell.getRowKey(), cell.getColumnKey(), result);
            } else {
                queries =
                        spec.onBackwardFlow((BackwardQuery) lastQuery, cell.getRowKey(), cell.getColumnKey());
            }
            for (Query q : queries) {
                addToQueue(new DemandDrivenGuidedAnalysis_n.QueryWithContext(q, new Node<>(triggeringEdge, fact), lastQuery));
            }
        }
    }

    private void addToQueue(DemandDrivenGuidedAnalysis_n.QueryWithContext nextQuery) {
        if (visited.add(nextQuery.query)) {
            queryQueue.add(nextQuery);
        }
    }
    private static class QueryWithContext {
        private QueryWithContext(Query query) {
            this.query = query;
        }

        private QueryWithContext(Query query, Node<Edge, Val> triggeringNode, Query parentQuery) {
            this.query = query;
            this.parentQuery = parentQuery;
            this.triggeringNode = triggeringNode;
        }

        Query query;
        Query parentQuery;
        Node<Edge, Val> triggeringNode;
    }

}
