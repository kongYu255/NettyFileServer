package springmvc;

import springmvc.context.AnnotationApplicationContext;
import springmvc.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Field;

public class Application {
    public static void main(String[] args) {
        AnnotationApplicationContext context = new AnnotationApplicationContext("applicationContext.xml");
    }
}
