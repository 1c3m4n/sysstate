package nl.unionsoft.sysstate.logic;

import java.util.List;
import java.util.Properties;

import nl.unionsoft.sysstate.logic.impl.PluginLogicImpl.Plugin;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public interface PluginLogic {
    public ClassPathXmlApplicationContext getPluginApplicationContext();

    public <T> T getComponent(final String name);

    public String[] getComponentNames(Class<?> type);

    public List<Plugin> getPlugins();

    public Properties getComponentProperties(String name);

}
