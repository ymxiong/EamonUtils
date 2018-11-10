package cc.eamon.open.annotation.common.mapper;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eamon on 2018/10/3.
 */
public class MapperModifyDetail {

    private String fieldName;

    private Map<String, ModifyDetail> target = new HashMap<>();

    public void addValue(ModifyDetail value){
        if (value == null) return;
        target.put(value.getTargetMapName(), value);
    }

    public ModifyDetail getValue(String value){
        if(target.get(value)!=null){
            return target.get(value);
        }
        return null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Map<String, ModifyDetail> getTarget() {
        return target;
    }

    public void setTarget(Map<String, ModifyDetail> target) {
        this.target = target;
    }

    public static class ModifyDetail {

        private String targetMapName;

        private String methodName;

        public String getTargetMapName() {
            return targetMapName;
        }

        public void setTargetMapName(String targetMapName) {
            this.targetMapName = targetMapName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
    }

}
