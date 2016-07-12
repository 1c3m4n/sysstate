package nl.unionsoft.sysstate.plugins.http;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import nl.unionsoft.sysstate.common.dto.InstanceDto;
import nl.unionsoft.sysstate.common.dto.StateDto;
import nl.unionsoft.sysstate.common.enums.StateType;
import nl.unionsoft.sysstate.common.extending.StateResolver;
import nl.unionsoft.sysstate.common.logic.ResourceLogic;
import nl.unionsoft.sysstate.common.util.StateUtil;

@Service("httpStateResolver")
public class HttpStateResolverImpl implements StateResolver {

    public static final String URL = "url";

    private static final Logger LOG = LoggerFactory.getLogger(HttpStateResolverImpl.class);

    @Inject
    @Named("resourceLogic")
    private ResourceLogic resourceLogic;

    public void setState(final InstanceDto instance, final StateDto state) {
        Map<String, String> properties = instance.getConfiguration();
        HttpClient httpClient = resourceLogic.getResourceInstance(HttpConstants.RESOURCE_MANAGER_NAME, StringUtils.defaultIfEmpty(properties.get("httpClientId"), HttpConstants.DEFAULT_RESOURCE));
        state.setState(StateType.STABLE);
        LOG.debug("Preparing httpRequest...");

        final String uri = processUri(properties.get(URL));
        if (StringUtils.isEmpty(uri)) {
            throw new IllegalStateException("URL is empty!");
        }
        final HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader("Connection", "close");

        String userAgent = properties.get("userAgent");
        if (StringUtils.isNotEmpty(userAgent)) {
            httpGet.addHeader("User-Agent", userAgent);
        }

        final Long startTime = System.currentTimeMillis();
        try {
            LOG.debug("Executing httpRequest...");
            final HttpResponse httpResponse = httpClient.execute(httpGet);
            final long responseTime = System.currentTimeMillis() - startTime;
            LOG.debug("HttpRequest complete, execution took {} ms", responseTime);
            state.setResponseTime(responseTime);
            // FIXME
            handleHttpResponse(state, properties, httpResponse, instance);

        } catch (ConnectTimeoutException e){
            state.setState(StateType.ERROR);
            state.appendMessage(StateUtil.exceptionAsMessage(e));
            state.setDescription("HTTP TIMEOUT");
        } catch (UnknownHostException e){
            state.setState(StateType.ERROR);
            state.appendMessage(StateUtil.exceptionAsMessage(e));
            state.setDescription("HTTP HOST");
        } catch (final Exception e) {
            LOG.warn("Caught Exception while performing request: {}", e.getMessage(), e);
            handleStateForException(state, e, startTime);
        }
    }

    public String processUri(final String uri) {
        return uri;
    }

    private void handleHttpResponse(final StateDto state, final Map<String, String> configuration, final HttpResponse httpResponse, final InstanceDto instance) throws IOException {
        HttpEntity httpEntity = null;
        try {
            final StatusLine statusLine = httpResponse.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            state.setDescription("Status " +statusCode);
            httpEntity = httpResponse.getEntity();

            if (statusCode >= 300) {
                if (statusCode >= 400) {
                    state.setState(StateType.ERROR);
                } else {
                    state.setState(StateType.UNSTABLE);
                }
                state.appendMessage(EntityUtils.toString(httpEntity));
            } else {
                handleEntity(httpEntity, configuration, state, instance);
            }
        } finally {
            EntityUtils.consume(httpEntity);
        }
    }

    private void handleStateForException(final StateDto state, final Exception exception, final Long startTime) {
        state.setState(StateType.ERROR);
        state.setDescription(exception.getMessage());
        state.setResponseTime(System.currentTimeMillis() - startTime);
        state.appendMessage(StateUtil.exceptionAsMessage(exception));
    }

    public void handleEntity(final HttpEntity httpEntity, final Map<String, String> configuration, final StateDto state, final InstanceDto instance) throws IOException {

    }

    public String generateHomePageUrl(final InstanceDto instance) {
        Map<String, String> properties = instance.getConfiguration();
        String homePageUrl = processUri(properties.get(URL));
        return StringUtils.substringBefore(homePageUrl, "//") + "//" + StringUtils.substringBetween(homePageUrl, "//", "/");
    }

    

}
