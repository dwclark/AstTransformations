import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.ast.builder.AstBuilder;
import static org.objectweb.asm.Opcodes.*;
import org.codehaus.groovy.syntax.*;
import groovy.transform.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EnableUnlessTransformation implements ASTTransformation {
  
  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalClassAnnotation(astNodes, EnableUnless)) {
      return;
    }

    ClassNode classNode = astNodes[1];
    UnlessTransformer transformer = new UnlessTransformer(sourceUnit);
    transformer.visitClass(classNode);
    AstTransformUtils.fixupScopes(sourceUnit);
  }
}

class UnlessTransformer extends ClassCodeVisitorSupport {
  public SourceUnit source;
  private MethodNode currentMethodNode;
  private BlockStatement currentBlockStatement;
  private ExpressionStatement currentExpressionStatement;
  private MethodCallExpression currentMethodCallExpression;

  public UnlessTransformer(SourceUnit source) {
    this.source = source;
  }

  @Override
  protected SourceUnit getSourceUnit() {
    return source;
  }

  @Override
  public void visitMethod(MethodNode methodNode) {
    currentMethodNode = methodNode;
    super.visitMethod(methodNode);
  }

  @Override
  public void visitBlockStatement(BlockStatement block) {
    currentBlockStatement = block;
    super.visitBlockStatement(block);
  }

  @Override
  public void visitExpressionStatement(ExpressionStatement expr) {
    currentExpressionStatement = expr;
    super.visitExpressionStatement(expr);
  }

  @Override
  public void visitMethodCallExpression(MethodCallExpression methodCall) {
    if(isUnlessStatement(methodCall)) {
      List arguments = methodCall.arguments.expressions;
      BooleanExpression bExpr = new NotExpression(new BooleanExpression(new CastExpression(ClassHelper.boolean_TYPE,
											   arguments.get(0))));
      Statement stmt = arguments.get(1).code;
      IfStatement ifStmt = new IfStatement(bExpr, stmt, EmptyStatement.INSTANCE);
      int idx = currentBlockStatement.statements.findIndexOf { it == currentExpressionStatement; };
      currentBlockStatement.statements.set(idx, ifStmt);
    }
    
    super.visitMethodCallExpression(methodCall);
  }
  
  private boolean isUnlessStatement(MethodCallExpression mcall) {
    return (mcall.objectExpression instanceof VariableExpression &&
	    mcall.objectExpression.variable == 'this' &&
	    mcall.method instanceof ConstantExpression &&
	    mcall.method.value == 'unless' &&
	    mcall.arguments.expressions.size() == 2 &&
	    mcall.arguments.expressions.get(1) instanceof ClosureExpression) 
  }
}
