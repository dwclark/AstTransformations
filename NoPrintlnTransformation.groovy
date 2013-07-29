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
public class NoPrintlnTransformation implements ASTTransformation {
  
  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalClassAnnotation(astNodes, NoPrintln)) {
      return;
    }

    ClassNode classNode = astNodes[1];
    NoPrintlnTransformer transformer = new NoPrintlnTransformer(sourceUnit);
    transformer.visitClass(classNode);
    AstTransformUtils.fixupScopes(sourceUnit);
  }
}

class NoPrintlnTransformer extends ClassCodeVisitorSupport {
  public SourceUnit source;
  private BlockStatement currentBlockStatement;
  private ExpressionStatement currentExpressionStatement;

  public NoPrintlnTransformer(SourceUnit source) {
    this.source = source;
  }

  @Override
  protected SourceUnit getSourceUnit() {
    return source;
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
    if(isPrintlnCall(methodCall)) {
      List arguments = methodCall.arguments.expressions;
      int idx = currentBlockStatement.statements.findIndexOf { it == currentExpressionStatement; };
      currentBlockStatement.statements.set(idx, EmptyStatement.INSTANCE);
    }
    
    super.visitMethodCallExpression(methodCall);
  }
  
  private boolean isPrintlnCall(MethodCallExpression mcall) {
    return (mcall.objectExpression instanceof VariableExpression &&
	    mcall.objectExpression.variable == 'this' &&
	    mcall.method instanceof ConstantExpression &&
	    mcall.method.value == 'println')
  }
}
