package cc.eamon.open.annotation.common.group;


import cc.eamon.open.annotation.ProcessingException;
import cc.eamon.open.annotation.common.mapper.Mapper;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

/**
 * Author: eamon
 * Email: eamon@eamon.cc
 * Time: 2018-12-05 02:59:23
 */

public class GroupProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public GroupProcessor() {
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Group.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {

            Map<String, List<GroupDetail>> groupDetailMap = new HashMap<>();
            // 扫描类 过滤掉非类上注解
            for (Element elem : roundEnv.getElementsAnnotatedWith(Group.class)) {
                if (elem.getKind() != ElementKind.CLASS) {
                    throw new ProcessingException(elem, "Only classes can be annotated with @%s", Mapper.class.getSimpleName());
                }
                Group group = elem.getAnnotation(Group.class);
                if (group.value().equals("")) continue;
                List<GroupDetail> sameGroupList = groupDetailMap.computeIfAbsent(group.value(), value -> new ArrayList<>());
                GroupDetail groupDetail = new GroupDetail();
                groupDetail.setBase(group.base());
                groupDetail.setList(group.list());
                groupDetail.setName(group.name());
                groupDetail.setValue(group.value());
                groupDetail.setTypeName(TypeName.get(elem.asType()));
                sameGroupList.add(groupDetail);
            }
            for (Map.Entry<String, List<GroupDetail>> entry : groupDetailMap.entrySet()) {
                if (entry.getValue().size() == 0) continue;
                String groupValue = entry.getValue().get(0).getValue();
                String className = groupValue.replaceAll(".*\\.", "");
                String packageName = groupValue.replaceAll("\\..?$", "");
                // 新建类
                TypeSpec.Builder typeSpec = TypeSpec.classBuilder(className);
                typeSpec.addModifiers(Modifier.PUBLIC);
                entry.getValue().forEach((groupDetail) -> {
                    if (groupDetail.isBase()){
                        typeSpec.superclass(groupDetail.getTypeName());
                    }else{
                        TypeName typeName = groupDetail.getTypeName();
                        if (groupDetail.isList()){
                            ClassName list = ClassName.get("java.util", "List");
                            typeName = ParameterizedTypeName.get(list, typeName);
                        }
                        FieldSpec fieldSpec = FieldSpec.builder(
                                typeName,
                                groupDetail.getName(),
                                Modifier.PUBLIC)
                                .build();
                        typeSpec.addField(fieldSpec);
                    }
                });
                // 写出数据
                JavaFile.builder(packageName, typeSpec.build()).build().writeTo(filer);
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


}
