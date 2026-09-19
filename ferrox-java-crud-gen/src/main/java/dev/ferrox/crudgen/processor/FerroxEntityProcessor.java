package dev.ferrox.crudgen.processor;

import dev.ferrox.crudgen.annotation.FerroxEntity;
import dev.ferrox.crudgen.annotation.FerroxField;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("dev.ferrox.crudgen.annotation.FerroxEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class FerroxEntityProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement typeElement) {
                    generateSchemaProvider(typeElement);
                }
            }
        }
        return true;
    }

    private void generateSchemaProvider(TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString();
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        FerroxEntity entity = typeElement.getAnnotation(FerroxEntity.class);

        String tableName = entity.table().isEmpty() ? className.toLowerCase() : entity.table();

        StringBuilder fieldsJson = new StringBuilder();
        fieldsJson.append("[\n");

        boolean first = true;
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed instanceof VariableElement field) {
                if (!first) fieldsJson.append(",\n");
                
                String fieldName = field.getSimpleName().toString();
                String fieldType = field.asType().toString();
                FerroxField fieldAnnotation = field.getAnnotation(FerroxField.class);
                
                boolean isPk = fieldAnnotation != null && fieldAnnotation.isPrimaryKey();
                boolean isSearchable = fieldAnnotation != null && fieldAnnotation.isSearchable();

                fieldsJson.append(String.format(
                    "            {\"name\": \"%s\", \"type\": \"%s\", \"is_primary_key\": %b, \"is_searchable\": %b}",
                    fieldName, fieldType, isPk, isSearchable
                ));
                first = false;
            }
        }
        fieldsJson.append("\n        ]");

        String schemaJson = String.format("""
            {
                "entity_name": "%s",
                "table_name": "%s",
                "roles": { "read": "%s", "write": "%s" },
                "admin_ui": { "grid": %b, "kanban": %b },
                "fields": %s
            }
            """,
            className, tableName, entity.roleRead(), entity.roleWrite(),
            entity.adminGrid(), entity.adminKanban(), fieldsJson.toString()
        );

        String generatedClassName = className + "Schema";
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + generatedClassName);
            try (Writer writer = file.openWriter()) {
                writer.write(String.format("""
                    package %s;
                    
                    public class %s {
                        public static String getFerroxSchema() {
                            return \"\"\"
%s
                            \"\"\";
                        }
                    }
                    """, packageName, generatedClassName, schemaJson));
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate schema class: " + e.getMessage());
        }
    }
}
