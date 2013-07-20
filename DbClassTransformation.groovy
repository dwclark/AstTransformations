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
public class DbClassTransformation implements ASTTransformation {

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalClassAnnotation(astNodes, Integrate)) {
      return;
    }

    ClassNode classNode = astNodes[1];
    AstTransformUtils.fixupScopes(sourceUnit);
  }

}