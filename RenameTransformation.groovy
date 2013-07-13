import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.objectweb.asm.Opcodes;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class RenameTransformation implements ASTTransformation {

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    // use guard clauses as a form of defensive programming.
    if(!astNodes) return;
    if(!astNodes[0] || !astNodes[1]) return;
    if(!(astNodes[0] instanceof AnnotationNode)) return;
    if(astNodes[0].classNode?.name != Rename.class.name) return;
    if(!(astNodes[1] instanceof MethodNode)) return;

    MethodNode annotatedMethod = astNodes[1];
    MethodCallExpression addedCall = new MethodCallExpression(
      new VariableExpression("this"),
      new ConstantExpression("println"),
      new ArgumentListExpression(new ConstantExpression("I'm injected in " + annotatedMethod.name)));
    BlockStatement block = new BlockStatement();
    block.addStatements([new ExpressionStatement(addedCall), annotatedMethod.code]);
    annotatedMethod.code = block;
  }
}
