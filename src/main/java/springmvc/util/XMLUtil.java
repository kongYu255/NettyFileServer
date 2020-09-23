package springmvc.util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

public class XMLUtil {

    public String handlerXMLForScanPackage(String configuration){
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(configuration);
        SAXReader reader = new SAXReader();
        try{
            Document document = reader.read(ins);
            Element root = document.getRootElement();
            Element ele = root.element("package");
            String res = ele.attributeValue("package-scan");
            return res;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
