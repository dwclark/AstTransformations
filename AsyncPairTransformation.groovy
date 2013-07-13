import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.*;
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.ast.builder.AstBuilder;
import static org.objectweb.asm.Opcodes.*;
import java.util.concurrent.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AsyncPairTransformation implements ASTTransformation {

  private static String asyncMethodName(String origMethodName) {
    return "async" + origMethodName.substring(0,1).toUpperCase() +
      origMethodName.substring(1);
  }

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalMethodAnnotation(astNodes, AsyncPair)) {
      return;
    }

    MethodNode annotatedMethod = astNodes[1];
    ClassNode declaringClass = annotatedMethod.declaringClass;
    declaringClass.addMethod(runnableAsyncMethod(annotatedMethod.name, annotatedMethod.parameters));
  }

  public MethodNode runnableAsyncMethod(String methodName, Parameter[] parameters) {
    /* What we want to express is something like the following:
       public void asyncMethodName(parameters) { <-- stage #3
         Thread.start {  <-- stage #2
	   methodName(parameters);   <-- stage #1
	 }
       }
    */

    MethodCallExpression stage1Inner = new MethodCallExpression(
      new VariableExpression("this"), new ConstantExpression(methodName),
      new ArgumentListExpression(parameters));
    ExpressionStatement stage1Outer = new ExpressionStatement(stage1Inner);

    ClosureExpression stage2Inner = new ClosureExpression([] as Parameter[], stage1Outer);
    StaticMethodCallExpression stage2Outer = new StaticMethodCallExpression(
      ClassHelper.make(Thread.class), "start", threadCalledClosure);

    ExpressionStatement stage3Inner = new ExpressionStatement(stage2Outer);
    return new MethodNode(asyncMethodName(methodName), ACC_PUBLIC, ClassHelper.void_WRAPPER_TYPE,
				      parameters, [] as ClassNode[], stage3Inner);
  }
}