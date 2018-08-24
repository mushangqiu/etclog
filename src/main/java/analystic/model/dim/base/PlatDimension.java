package analystic.model.dim.base;


import common.GlobalConstants;
import org.apache.commons.lang.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlatDimension extends BaseDimension{

    private int id = 0;
    private String platform_name; //'平台名称'

    public PlatDimension(String platform_name) {
        this.platform_name = platform_name;
    }

    public PlatDimension() {

    }

    public PlatDimension(int id, String platform_name) {
        this(platform_name);
        this.id = id;
    }

    @Override
    public int compareTo(BaseDimension o) {
        if (o==this){
            return 0;
        }
        PlatDimension other = (PlatDimension) o;
        int tmp = this.id - other.id;
        if (tmp!=0){
            return tmp;
        }
        return this.platform_name.compareTo(other.platform_name);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.id);
        out.writeUTF(this.platform_name);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = in.readInt();
        this.platform_name = in.readUTF();
    }

    /**
     * 构建平台维度的集合对象
     * @param platform_name
     * @return
     */
    public static List<PlatDimension> bulidList(String platform_name){
        if(StringUtils.isEmpty(platform_name)){
            platform_name = GlobalConstants.DEFAULF_VALUE;
        }
        List<PlatDimension> li = new ArrayList<PlatDimension>();
        li.add(new PlatDimension(platform_name));
        li.add(new PlatDimension(GlobalConstants.ALL_OF_VALIE));
        return li;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id,platform_name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlatform_name() {
        return platform_name;
    }

    public void setPlatform_name(String platform_name) {
        this.platform_name = platform_name;
    }
}
