import boomerang.BackwardQuery;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.scene.AllocVal;
import boomerang.scene.ControlFlowGraph;
import boomerang.scene.Statement;
import boomerang.scene.Val;
import de.fraunhofer.iem.secucheck.InternalFluentTQL.dsl.exception.DuplicateTaintFlowQueryIDException;
import de.fraunhofer.iem.secucheck.analysis.implementation.SingleFlowTaintAnalysis.BoomerangSolver.Utility;
import de.fraunhofer.iem.secucheck.analysis.query.InputParameter;
import de.fraunhofer.iem.secucheck.analysis.query.Method;
import de.fraunhofer.iem.secucheck.analysis.query.MethodImpl;
import de.fraunhofer.iem.secucheck.analysis.query.OutputParameter;

import java.util.*;

public class SimpleSpecificationGuide implements IDemandDrivenGuidedManager_n {

    private Collection<Query> getOutForPropogator(Method propogatorMethod, Statement statement, ControlFlowGraph.Edge dataFlowEdge, Val dataFlowVal) {
        List<Query> queryList = new ArrayList();
        if (propogatorMethod.getOutputParameters() != null) {
            Iterator var6 = propogatorMethod.getOutputParameters().iterator();

            while(var6.hasNext()) {
                OutputParameter outputParameter = (OutputParameter)var6.next();
                int parameterIndex = outputParameter.getParamID();
                if (statement.getInvokeExpr().getArgs().size() >= parameterIndex) {
                    queryList.add(new ForwardQuery(dataFlowEdge, new AllocVal(statement.getInvokeExpr().getArg(parameterIndex), statement, statement.getInvokeExpr().getArg(parameterIndex))));
                }
            }
        }

        if (propogatorMethod.isOutputThis() && statement.getInvokeExpr().isInstanceInvokeExpr()) {
            queryList.add(new ForwardQuery(dataFlowEdge, new AllocVal(statement.getInvokeExpr().getBase(), statement, statement.getInvokeExpr().getBase())));
        }

        if (propogatorMethod.getReturnValue() != null && statement.isAssign()) {
            queryList.add(new ForwardQuery(dataFlowEdge, new AllocVal(statement.getLeftOp(), statement, statement.getLeftOp())));
        }

        return queryList;
    }

    private Collection<Query> getQueriesBasedOnTheRules(Method requiredPropogatorMethod, Statement statement, ControlFlowGraph.Edge dataFlowEdge, Val dataFlowVal) {
        List<Query> queryList = new ArrayList();
        if (requiredPropogatorMethod.getInputParameters() != null) {
            Iterator var6 = requiredPropogatorMethod.getInputParameters().iterator();

            while(var6.hasNext()) {
                InputParameter input = (InputParameter)var6.next();
                int parameterIndex = input.getParamID();
                if (statement.getInvokeExpr().getArgs().size() >= parameterIndex && statement.getInvokeExpr().getArg(parameterIndex).toString().equals(dataFlowVal.toString())) {
                    queryList.addAll(this.getOutForPropogator(requiredPropogatorMethod, statement, dataFlowEdge, dataFlowVal));
                    return queryList;
                }
            }
        }

        if (requiredPropogatorMethod.isInputThis() && statement.getInvokeExpr().isInstanceInvokeExpr() && statement.getInvokeExpr().getBase().toString().equals(dataFlowVal.toString())) {
            queryList.addAll(this.getOutForPropogator(requiredPropogatorMethod, statement, dataFlowEdge, dataFlowVal));
            return queryList;
        } else {
            return queryList;
        }
    }

    private Collection<Query> isPropogator(List<MethodImpl> propogators, Statement statement, ControlFlowGraph.Edge dataFlowEdge, Val dataFlowVal) {
        List<Query> queryList = new ArrayList();
        Iterator var6 = propogators.iterator();

        while(true) {
            while(var6.hasNext()) {
                Method requiredPropogatorMethod = (Method)var6.next();
                String requiredPropogatorSootSignature = Utility.wrapInAngularBrackets(requiredPropogatorMethod.getSignature());
                if (statement.containsInvokeExpr() && Utility.toStringEquals(statement.getInvokeExpr().getMethod().getSignature(), requiredPropogatorSootSignature)) {
                    queryList.addAll(this.getQueriesBasedOnTheRules(requiredPropogatorMethod, statement, dataFlowEdge, dataFlowVal));
                } else if (statement.getInvokeExpr().getMethod().getSubSignature().equals(requiredPropogatorMethod.getSignature())) {
                    queryList.addAll(this.getQueriesBasedOnTheRules(requiredPropogatorMethod, statement, dataFlowEdge, dataFlowVal));
                }
            }
            return queryList;
        }
    }

    @Override
    public Collection<Query> onForwardFlow (ForwardQuery query, ControlFlowGraph.Edge dataFlowEdge, Val dataFlowVal, Set<Statement> result) {
        Statement stmt = dataFlowEdge.getStart();
        ArrayList<Query> out = new ArrayList();
//        System.out.println("stmt: " + stmt);
//        System.out.println("dataFlowVal: " + dataFlowVal);

        Statement target = dataFlowEdge.getTarget();
        if (target.toString().contains("return") && getReturnVal(target.toString()).equals(dataFlowVal.getVariableName()) ){
            result.add(target);
        }

        if (stmt.containsInvokeExpr()) {

            Collection<Query> generalProp = null;
            try {
                List<MethodImpl> pro = Util.set_gen_pro();
                generalProp = this.isPropogator(pro, stmt, dataFlowEdge, dataFlowVal);
            } catch (DuplicateTaintFlowQueryIDException e) {
                e.printStackTrace();
            }
            out.addAll(generalProp);

            if (!out.isEmpty()){
                result.add(stmt);
            }

            if (out.isEmpty() && getLeftOption_fun(stmt.toString()).equals(dataFlowVal.getVariableName())){
                result.add(stmt);
            }

        }
        else {
//          例如 $stack3 = n - 1 语句，val的值为 n， 若soot中间码是SSA形式，两个n为同一个n，则以下判断条件成立。

            if (stmt.isAssign() && stmt.getRightOp().getVariableName().contains(dataFlowVal.getVariableName())){
                result.add(stmt);
            }

            if (stmt.isIdentityStmt() && getLeftOption_id(stmt.toString()).equals(dataFlowVal.getVariableName()) ){
                result.add(stmt);
            }
        }
        return out;
    }

    @Override
    public Collection<Query> onBackwardFlow(BackwardQuery query, ControlFlowGraph.Edge dataFlowEdge, Val dataFlowVal) {
        return Collections.emptyList();
    }

    public String getLeftOption_id (String identityStmt){
        String[] splitArray = identityStmt.split(":");
        return splitArray[0].strip();
    }

    public String getLeftOption_fun (String fun_sign){
        String[] splitArray = fun_sign.split("=");
        return splitArray[0].strip();
    }

    public String getReturnVal (String return_stmt){
        return return_stmt.replace("return", "").strip();
    }
}
