package tabby.config;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import tabby.core.container.RulesContainer;
import tabby.util.ArgumentEnum;
import tabby.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wh1t3P1g
 * @since 2020/11/9
 */
@Slf4j
public class GlobalConfiguration {

    public static String LIBS_PATH = String.join(File.separator, System.getProperty("user.dir"), "libs");
    public static String RULES_PATH = String.join(File.separator, System.getProperty("user.dir"), "rules");
    public static String KNOWLEDGE_PATH = String.join(File.separator, RULES_PATH, "knowledges.json");
    public static String SINK_RULE_PATH = String.join(File.separator, RULES_PATH, "sinks.json");
    public static String SYSTEM_RULE_PATH = String.join(File.separator, RULES_PATH, "system.json");
    public static String IGNORE_PATH = String.join(File.separator, RULES_PATH, "ignores.json");
    public static String BASIC_CLASSES_PATH = String.join(File.separator, RULES_PATH, "basicClasses.json");
    public static String COMMON_JARS_PATH = String.join(File.separator, RULES_PATH, "commonJars.json");
    public static String CACHE_PATH = String.join(File.separator, System.getProperty("user.dir"), "cache");
    public static String CLASSES_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_CLASSES.csv");
    public static String METHODS_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_METHODS.csv");
    public static String CALL_RELATIONSHIP_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_CALL.csv");
    public static String ALIAS_RELATIONSHIP_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_ALIAS.csv");
    public static String EXTEND_RELATIONSHIP_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_EXTEND.csv");
    public static String HAS_RELATIONSHIP_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_HAS.csv");
    public static String INTERFACE_RELATIONSHIP_CACHE_PATH = String.join(File.separator,CACHE_PATH, "GRAPHDB_PUBLIC_INTERFACES.csv");

    public static Gson GSON = new Gson();
    public static boolean DEBUG = false;
    public static int TIMEOUT = 2;
    public static String MODE = "gadget";
    public static String TARGET = null;
    public static String REAL_CACHE_PATH = null;
    public static String NEO4J_USERNAME = null;
    public static String NEO4J_PASSWORD = null;
    public static String NEO4J_URL = null;
    public static int CACHE_COMPRESS_TIMES = 3;

    public static RulesContainer rulesContainer;

    public static Map<String, String> libraries = new HashMap<>();

    public static boolean IS_WEB_MODE = false;
    public static boolean IS_JDK_ONLY = false;
    public static boolean IS_JDK_PROCESS = false;
    public static boolean IS_BUILD_ENABLE = false;
    public static boolean IS_LOAD_ENABLE = false;
    public static boolean IS_EXCLUDE_JDK = false;
    public static boolean IS_WITH_ALL_JDK = false;
    public static boolean IS_CHECK_FAT_JAR = false;
    public static boolean IS_CACHE_AUTO_REMOVE = false;
    public static boolean IS_NEED_CACHE_COMPRESS = false;
    public static boolean IS_FULL_CALL_GRAPH_CONSTRUCT = false;
    public static boolean IS_NEED_TO_CREATE_IGNORE_LIST = true;

    public static boolean isInitialed = false;

    static {
        if(!FileUtils.fileExists(RULES_PATH)){
            FileUtils.createDirectory(RULES_PATH);
        }

        if(!FileUtils.fileExists(CACHE_PATH)){
            FileUtils.createDirectory(CACHE_PATH);
        }
    }

    public static void initConfig(){
        if(isInitialed) return;
//        log.info("Try to apply settings.properties");
        Properties props = new Properties();
        // read from config/settings.properties
        try(Reader reader = new FileReader("config/settings.properties")){
            props.load(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Config ERROR: config/settings.properties file not found!");
        }
        // apply to GlobalConfiguration

        IS_JDK_ONLY = getBooleanProperty(ArgumentEnum.IS_JDK_ONLY.getValue(), "false", props);

        if(IS_JDK_ONLY){
            props.setProperty(ArgumentEnum.WITH_ALL_JDK.getValue(), "true");
        }

        MODE = getProperty(ArgumentEnum.SET_BUILD_MODE.getValue(), "gadget", props);
        TARGET = getProperty(ArgumentEnum.TARGET.getValue(), "", props);
        REAL_CACHE_PATH = getProperty(ArgumentEnum.CACHE_PATH.getValue(), "./cache/graph", props);
        NEO4J_USERNAME = getProperty("tabby.neo4j.username", "neo4j", props);
        NEO4J_PASSWORD = getProperty("tabby.neo4j.password", "neo4j", props);
        NEO4J_URL = getProperty("tabby.neo4j.url", "bolt://localhost:7687", props);


        DEBUG = getBooleanProperty(ArgumentEnum.SET_DEBUG_ENABLE.getValue(), "false", props);

        IS_WEB_MODE = "web".equals(MODE);
        IS_EXCLUDE_JDK = getBooleanProperty(ArgumentEnum.EXCLUDE_JDK.getValue(), "false", props);
        IS_JDK_PROCESS = getBooleanProperty(ArgumentEnum.IS_JDK_PROCESS.getValue(), "false", props);
        IS_LOAD_ENABLE = getBooleanProperty(ArgumentEnum.LOAD_ENABLE.getValue(), "false", props);
        IS_BUILD_ENABLE = getBooleanProperty(ArgumentEnum.BUILD_ENABLE.getValue(), "false", props);
        IS_WITH_ALL_JDK = getBooleanProperty(ArgumentEnum.WITH_ALL_JDK.getValue(), "false", props);
        IS_CHECK_FAT_JAR = getBooleanProperty(ArgumentEnum.CHECK_FAT_JAR.getValue(), "false", props);
        IS_CACHE_AUTO_REMOVE = getBooleanProperty(ArgumentEnum.CACHE_AUTO_REMOVE.getValue(), "false", props);
        IS_NEED_CACHE_COMPRESS = getBooleanProperty(ArgumentEnum.CACHE_COMPRESS.getValue(), "false", props);
        IS_FULL_CALL_GRAPH_CONSTRUCT = getBooleanProperty(ArgumentEnum.IS_FULL_CALL_GRAPH_CREATE.getValue(), "false", props);
        IS_NEED_TO_CREATE_IGNORE_LIST = getBooleanProperty(ArgumentEnum.IS_NEET_TO_CREATE_IGNORE_LIST.getValue(), "true", props);

        try{
            TIMEOUT = getIntProperty(ArgumentEnum.SET_THREADS_TIMEOUT.getValue(), "2", props);
        }catch (Exception ignore){
        }

        try{
            CACHE_COMPRESS_TIMES = getIntProperty(ArgumentEnum.CACHE_COMPRESS_TIMES.getValue(), "3", props);
        }catch (Exception ignore){
        }


        // 支持绝对路径 issue 7
        if(!IS_JDK_ONLY && TARGET != null && !FileUtils.fileExists(TARGET)){
            String target = String.join(File.separator, System.getProperty("user.dir"), TARGET);
            if(!FileUtils.fileExists(target)){
                throw new IllegalArgumentException("target not exists!");
            }
        }

        String libraries = props.getProperty(ArgumentEnum.LIBRARIES.getValue());
        if(libraries != null){
            if(FileUtils.fileExists(libraries)){
                LIBS_PATH = libraries;
            }else{
                libraries = String.join(File.separator, System.getProperty("user.dir"), libraries);
                if(FileUtils.fileExists(libraries)){
                    LIBS_PATH = libraries;
                }
            }
        }

        isInitialed = true;
    }

    public static String getProperty(String key, String defaultValue, Properties props){
        return props.getProperty(key, defaultValue).trim();
    }

    public static boolean getBooleanProperty(String key, String defaultValue, Properties props){
        return Boolean.parseBoolean(getProperty(key, defaultValue, props));
    }

    public static int getIntProperty(String key, String defaultValue, Properties props){
        return Integer.parseInt(getProperty(key, defaultValue, props));
    }

}
