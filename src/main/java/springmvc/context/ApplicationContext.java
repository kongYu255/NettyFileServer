package springmvc.context;

import springmvc.util.XMLUtil;
import springmvc.util.BeanFactory;

public abstract class ApplicationContext extends BeanFactory {

    protected String configuration;

    protected XMLUtil xmlUtil;

    public ApplicationContext(String configuration){
        this.configuration = configuration;
        this.xmlUtil = new XMLUtil();
    }

}