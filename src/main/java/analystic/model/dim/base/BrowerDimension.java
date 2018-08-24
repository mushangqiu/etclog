package analystic.model.dim.base;

import common.GlobalConstants;
import org.apache.commons.lang.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BrowerDimension extends BaseDimension{
    private int id = 0;
    private String browser_name = "";   //'浏览器名称'
    private String browser_version = "";//'浏览器版本号'

    public BrowerDimension(){

    }

    public BrowerDimension(String browser_name, String browser_version) {
        this.browser_name = browser_name;
        this.browser_version = browser_version;
    }

    public BrowerDimension(int id, String browser_name, String browser_version) {
        this(browser_name,browser_version);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrowser_name() {
        return browser_name;
    }

    public void setBrowser_name(String browser_name) {
        this.browser_name = browser_name;
    }

    public String getBrowser_version() {
        return browser_version;
    }

    public void setBrowser_version(String browser_version) {
        this.browser_version = browser_version;
    }

    @Override
    public int compareTo(BaseDimension o) {
        if (o==this){
            return 0;
        }
        BrowerDimension other = (BrowerDimension) o;
        int tmp = this.id - other.id;
        if(tmp!=0){
            return tmp;
        }
        tmp = this.browser_name.compareTo(other.browser_name);
        if(tmp!=0){
            return tmp;
        }
        return this.browser_version.compareTo(other.browser_version);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.id);
        dataOutput.writeUTF(this.browser_name);
        dataOutput.writeUTF(this.browser_version);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.id = dataInput.readInt();
        this.browser_name = dataInput.readUTF();
        this.browser_version = dataInput.readUTF();
    }

    public static List<BrowerDimension> buildList(String browserName,String browserVersion){
        if(StringUtils.isEmpty(browserName)){
            browserName = browserVersion = GlobalConstants.DEFAULF_VALUE;
        }if(StringUtils.isEmpty(browserVersion)){
            browserVersion = GlobalConstants.DEFAULF_VALUE;
        }
        List<BrowerDimension> li = new ArrayList<BrowerDimension>();
        li.add(new BrowerDimension(browserName, browserVersion));
        li.add(new BrowerDimension(browserName, GlobalConstants.ALL_OF_VALIE));
        return li;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if( o == null || getClass() != o.getClass())
            return false;
        BrowerDimension that = (BrowerDimension) o;
        if(id != that.id)
            return false;
        if (browser_name != null && !browser_name.equals(that.browser_name)) return false;
        if (browser_version != null && !browser_version.equals(that.browser_version)) return false ;
        return true;
    }
}
