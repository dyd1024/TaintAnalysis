package GeneralPropagators;

import de.fraunhofer.iem.secucheck.InternalFluentTQL.dsl.MethodConfigurator;
import de.fraunhofer.iem.secucheck.InternalFluentTQL.dsl.annotations.FluentTQLRepositoryClass;
import de.fraunhofer.iem.secucheck.InternalFluentTQL.dsl.annotations.GeneralPropagator;
import de.fraunhofer.iem.secucheck.InternalFluentTQL.fluentInterface.MethodPackage.Method;

@FluentTQLRepositoryClass
public class UserdefinePropagators {
    @GeneralPropagator
    public static Method rp1 = new MethodConfigurator("org.owasp.esapi.Encoder: java.lang.String decodeForHTML(java.lang.String)")
            .in().param(0)
            .out().returnValue()
            .configure();
}
