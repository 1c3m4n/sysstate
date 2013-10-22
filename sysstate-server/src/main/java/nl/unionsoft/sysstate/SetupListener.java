package nl.unionsoft.sysstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import nl.unionsoft.sysstate.common.dto.EnvironmentDto;
import nl.unionsoft.sysstate.common.dto.InstanceDto;
import nl.unionsoft.sysstate.common.dto.ProjectDto;
import nl.unionsoft.sysstate.common.dto.ProjectEnvironmentDto;
import nl.unionsoft.sysstate.common.logic.EnvironmentLogic;
import nl.unionsoft.sysstate.common.logic.InstanceLogic;
import nl.unionsoft.sysstate.common.logic.ProjectEnvironmentLogic;
import nl.unionsoft.sysstate.common.logic.ProjectLogic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service("setupListener")
public class SetupListener implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(SetupListener.class);

    @Inject
    @Named("instanceLogic")
    private InstanceLogic instanceLogic;

    @Inject
    @Named("projectLogic")
    private ProjectLogic projectLogic;

    @Inject
    @Named("environmentLogic")
    private EnvironmentLogic environmentLogic;

    @Inject
    @Named("projectEnvironmentLogic")
    private ProjectEnvironmentLogic projectEnvironmentLogic;

    public void afterPropertiesSet() throws Exception {

        List<ProjectDto> projects = projectLogic.getProjects();

        if (projects == null || projects.size() == 0) {
            LOG.info("No projects found, creating some default projects...");
            // No projects defined..

            createProject("GOOG");
            createProject("YAHO");
            createProject("BING");
            createProject("ILSE");

        }

        List<EnvironmentDto> environments = environmentLogic.getEnvironments();
        if (environments == null || environments.size() == 0) {
            // No environments defined..
            LOG.info("No environments found, creating some default environments...");
            EnvironmentDto prd = new EnvironmentDto();
            prd.setName("PROD");
            prd.setOrder(10);
            environmentLogic.createOrUpdate(prd);

            EnvironmentDto mock = new EnvironmentDto();
            mock.setName("MOCK");
            mock.setOrder(0);
            environmentLogic.createOrUpdate(mock);
        }

        List<InstanceDto> instances = instanceLogic.getInstances();
        if (instances == null || instances.isEmpty()) {
            LOG.info("No instances found, creating some default instances...");
            addTestInstance("google", "GOOG", "PROD", createHttpConfiguration("http://www.google.nl"), "http://www.google.nl", "httpStateResolver");
            addTestInstance("google", "GOOG", "MOCK", null, "http://www.yahoo.com", "mockStateResolver");
            addTestInstance("yahoo", "YAHO", "PROD", createHttpConfiguration("http://www.yahoo.com"), "http://www.yahoo.com", "httpStateResolver");
            addTestInstance("yahoo", "YAHO", "MOCK", null, "http://www.yahoo.com", "mockStateResolver");
            addTestInstance("bing", "BING", "PROD", createHttpConfiguration("http://www.bing.com"), "http://www.bing.com", "httpStateResolver");
            addTestInstance("bing", "BING", "MOCK", null, "http://www.bing.com", "mockStateResolver");
            addTestInstance("ilse", "ILSE", "PROD", createHttpConfiguration("http://www.ilse.nl"), "http://www.ilse.nl", "httpStateResolver");
            addTestInstance("ilse", "ILSE", "MOCK", null, "http://www.ilse.nl", "mockStateResolver");

        }

    }

    private Map<String, String> createHttpConfiguration(String url) {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put("url", url);
        return configuration;
    }

    private void addTestInstance(final String name, final String projectName, final String environmentName, final Map<String, String> configuration, final String homepageUrl,
            final String plugin) {
        ProjectEnvironmentDto projectEnvironment = projectEnvironmentLogic.getProjectEnvironment(projectName, environmentName);
        if (projectEnvironment == null) {
            LOG.info("Skipping creating of instance ${}, no projectEnvironment could be found for projectName '{}' and environmentName '{}'", new Object[] { name, projectName,
                    environmentName });
        } else {
            InstanceDto instance = new InstanceDto();
            instance.setName(name);
            instance.setProjectEnvironment(projectEnvironment);
            instance.setEnabled(true);
            instance.setConfiguration(configuration);
            instance.setHomepageUrl(homepageUrl);
            instance.setPluginClass(plugin);
            instance.setRefreshTimeout(10000);
            instance.setTags("application");
            instanceLogic.createOrUpdateInstance(instance);
        }
    }

    private void createProject(final String name) {
        ProjectDto project = new ProjectDto();
        project.setName(name);
        projectLogic.createOrUpdateProject(project);
    }

}
