package com.hornetmall.framework.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.hornetmall.framework.annotation.SecurityModule;
import com.hornetmall.framework.annotation.SecurityOperation;
import com.hornetmall.framework.annotation.SecurityResource;
import com.hornetmall.framework.common.model.MetaColumn;
import com.hornetmall.framework.common.model.MetaOperation;
import com.hornetmall.framework.common.model.MetaResource;
import com.hornetmall.framework.common.model.MetaTable;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class HornetProcessor extends AbstractProcessor {

    private final List<MetaResource> resources = new ArrayList<>();
    private final List<MetaOperation> operations = new ArrayList<>();
    private final Set<String> resourceNames = new HashSet<>();
    private final Set<String> operationNames = new HashSet<>();
    //private final ObjectMapper objectMapper=new ObjectMapper();
    private String moduleName;
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z][0-9a-zA-Z]*");
    private static final String CODE_REGEX = "^[a-zA-Z][0-9a-zA-Z\\.]*[0-9a-zA-Z]";
    private ProcessingEnvironment environment;

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public HornetProcessor() {
        System.out.println(" --------------------------HornetProcessor-----------------------------------");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.environment = processingEnv;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(SecurityResource.class.getName(), SecurityOperation.class.getName(), SecurityModule.class.getName());
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(SecurityModule.class);
        elements.stream().findFirst().ifPresent(element -> {
            final SecurityModule module = element.getAnnotation(SecurityModule.class);
            if (StringUtils.isEmpty(moduleName) && Objects.nonNull(module)) {
                setModuleName(module.value());
            }

        });
        processResource(roundEnv);
        processOperations(roundEnv);
        if (roundEnv.processingOver()) {
            try {
                writeMetadata();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    private void processOperations(RoundEnvironment roundEnv) {

        Set<? extends Element> securityResources = roundEnv.getElementsAnnotatedWith(SecurityOperation.class);
        securityResources.forEach(element -> {
            final SecurityOperation operation = element.getAnnotation(SecurityOperation.class);
            if (Objects.isNull(operation)) {
                return;
            }

            final String name = StringUtils.isEmpty(operation.name()) ? element.getSimpleName().toString() : operation.name();
            if (operationNames.contains(name)) {
                return;
            }
            MetaOperation metaOperation = new MetaOperation();
            final Element enclosingElement = element.getEnclosingElement();
            if (enclosingElement instanceof TypeElement) {
                final TypeElement typeElement = (TypeElement) enclosingElement;
                final String className = environment.getElementUtils().getBinaryName(typeElement).toString();
                metaOperation.setClassName(className);
            }
            metaOperation.setModule(moduleName);
            metaOperation.setName(name);
            metaOperation.setMethodName(element.getSimpleName().toString());
            metaOperation.setReferences(Arrays.asList(operation.references()));
            operations.add(metaOperation);
        });
    }

    private void processResource(RoundEnvironment roundEnv) {

        Set<? extends Element> securityResources = roundEnv.getElementsAnnotatedWith(SecurityResource.class);
        securityResources.forEach(element -> {
            final Table table = element.getAnnotation(Table.class);
            if (Objects.isNull(table)) {
                return;
            }
            final SecurityResource securityResource = element.getAnnotation(SecurityResource.class);
            final Entity entity = element.getAnnotation(Entity.class);

            if (resourceNames.contains(securityResource.name())) {
                return;
            }

            resourceNames.add(securityResource.name());
            MetaResource resource = new MetaResource();
            resource.setName(securityResource.name());
            resource.setDisplayName(StringUtils.isEmpty(entity.name()) ? securityResource.name() : entity.name());
            resource.setType(securityResource.type().name());
            MetaTable metaTable = new MetaTable();
            metaTable.setName(table.name());
            metaTable.setDisplayName(resource.getDisplayName());
            metaTable.setColumns(Arrays.stream(securityResource.columns()).map(item -> new MetaColumn(item, item)).collect(Collectors.toList()));
            resource.setTable(metaTable);
            resources.add(resource);
        });
    }

    private void writeMetadata() throws IOException {
        final FileObject fileObject = this.environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/security-metadata.json", new Element[0]);
        try (OutputStream outputStream = fileObject.openOutputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(outputStream, Map.of("resources", resources, "operations", operations));
        }

    }
}

