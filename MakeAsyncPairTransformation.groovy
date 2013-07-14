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
import org.codehaus.groovy.syntax.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MakeAsyncPairTransformation implements ASTTransformation {

  private static String asyncMethodName(String origMethodName) {
    return "async" + origMethodName.substring(0,1).toUpperCase() +
      origMethodName.substring(1);
  }

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalMethodAnnotation(astNodes, MakeAsyncPair)) {
      return;
    }

    AnnotationNode annotation = astNodes[0];
    Map annotationInfo = AstTransformUtils.extractAnnotationInfo(annotation);
    String executorVarName = annotationInfo['value'];
    MethodNode theMethod = astNodes[1];
    ClassNode theClass = theMethod.declaringClass;
    if(!executorVarName) {
      theClass.addMethod(plainThreadAsyncMethod(theMethod));
    }
    else if(executorVarName) {
      theClass.addMethod(executorAsyncMethod(theMethod, executorVarName));
    }
  
    AstTransformUtils.fixupScopes(sourceUnit);
  }

  public Parameter[] newMethodParameters(MethodNode methodNode) {
    Parameter[] newMethodParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    newMethodParameters.each { it.modifiers = ACC_FINAL; };
    return newMethodParameters;
  }

  public MethodNode plainThreadAsyncMethod(MethodNode methodNode) {
    ClassNode threadType = ClassHelper.make(Thread);
    CastExpression cast = wrapAndCastMethodCall(methodNode);
    ConstructorCallExpression cce = new ConstructorCallExpression(threadType, cast);
    DeclarationExpression declaration = new DeclarationExpression(new VariableExpression("toCall", threadType),
								  Token.newSymbol(Types.ASSIGN, -1, -1), cce);
    MethodCallExpression methodCall = new MethodCallExpression(declaration.getVariableExpression(), "start", MethodCallExpression.NO_ARGUMENTS);
    BlockStatement block = new BlockStatement();
    block.addStatement(new ExpressionStatement(declaration));
    block.addStatement(new ExpressionStatement(methodCall));
    block.addStatement(new ReturnStatement(declaration.getVariableExpression()));

    return new MethodNode(asyncMethodName(methodNode.name), ACC_PUBLIC, ClassHelper.make(Thread),
			  newMethodParameters(methodNode), ClassNode.EMPTY_ARRAY, block);
  }

  public MethodNode executorAsyncMethod(MethodNode methodNode, String executorVarName) {
    CastExpression cast = wrapAndCastMethodCall(methodNode);
    MethodCallExpression methodCall = new MethodCallExpression(new VariableExpression(executorVarName),
							       "submit", cast);
    ClassNode futureType = AstTransformUtils.makeGenericClassNode(Future, [ ClassHelper.getWrapper(methodNode.returnType) ]);
    return new MethodNode(asyncMethodName(methodNode.name), ACC_PUBLIC, futureType,
			  newMethodParameters(methodNode), ClassNode.EMPTY_ARRAY,
			  new ReturnStatement(methodCall));
  }

  public CastExpression wrapAndCastMethodCall(MethodNode methodNode) {
    ClassNode classNode;
    if(methodNode.returnType == ClassHelper.VOID_TYPE) {
      classNode = ClassHelper.make(Runnable);
    }
    else {
      classNode = AstTransformUtils.makeGenericClassNode(Callable, [ ClassHelper.getWrapper(methodNode.returnType) ]);
    }

    Parameter[] callingParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    MethodCallExpression stage1Inner = new MethodCallExpression(
      new VariableExpression("this"), new ConstantExpression(methodNode.name),
      new ArgumentListExpression(callingParameters));
    ReturnStatement stage1Outer = new ReturnStatement(stage1Inner);
    ClosureExpression closure = new ClosureExpression([] as Parameter[], stage1Outer);
    return new CastExpression(classNode, closure);
  }

  public DeclarationExpression makeInitialStatement(CastExpression cast, ClassNode classNode) {

    VariableExpression variable = new VariableExpression("toCall", classNode); 
    return new DeclarationExpression(variable, Token.newSymbol(Types.ASSIGN, -1, -1), cce);
  }

  public DeclarationExpression makeCastStatement(CastExpression cast, ClassNode classNode) {
    VariableExpression variable = new VariableExpression("toCall", classNode); 
    return new DeclarationExpression(variable, Token.newSymbol(Types.ASSIGN, -1, -1), cast);
  }
}