package cc.eamon.open.annotation.common.mapper;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eamon on 2018/10/3.
 */
public class MapperRenameDetail {

    private String fieldName;

    private Map<String, RenameDetail> target = new HashMap<>();

    public void addValue(RenameDetail value){
        if (value == null) return;
        target.put(value.getOriginMapName(), value);
    }

    public RenameDetail getValue(String value){
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

    public Map<String, RenameDetail> getTarget() {
        return target;
    }

    public void setTarget(Map<String, RenameDetail> target) {
        this.target = target;
    }

    public static class RenameDetail {

        private String originMapName;

        private String renameMapName;

        public String getOriginMapName() {
            return originMapName;
        }

        public void setOriginMapName(String originMapName) {
            this.originMapName = originMapName;
        }

        public String getRenameMapName() {
            return renameMapName;
        }

        public void setRenameMapName(String renameMapName) {
            this.renameMapName = renameMapName;
        }
    }

}
