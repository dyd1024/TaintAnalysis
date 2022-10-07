import boomerang.scene.jimple.IntAndStringBoomerangOptions;

public class SimpleBoomerangOptions extends IntAndStringBoomerangOptions {

    public StaticFieldStrategy getStaticFieldStrategy() {
        return StaticFieldStrategy.FLOW_SENSITIVE;
    }

    public boolean onTheFlyCallGraph() {
        return false;
    }

    public boolean trackStaticFieldAtEntryPointToClinit() {
        return true;
    }

    public int maxCallDepth() {
        return 5;
    }

    public int maxUnbalancedCallDepth() {
        return 5;
    }

    public int maxFieldDepth() {
        return 5;
    }

    public boolean allowMultipleQueries() {
        return true;
    }

}
